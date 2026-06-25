package com.kookmin.hackerton.loan.service;

import com.kookmin.hackerton.loan.config.LoanPolicyProperties;
import com.kookmin.hackerton.loan.model.LoanAffordabilityResult;
import com.kookmin.hackerton.loan.model.LoanProduct;
import com.kookmin.hackerton.loan.model.LoanRecommendation;
import com.kookmin.hackerton.loan.model.LoanSearchRequest;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class LoanRecommendationScorer {

    private final LoanPolicyProperties policyProperties;
    private final LoanRatioCalculator ratioCalculator;
    private final LoanProductAnalyzer productAnalyzer;

    public LoanRecommendationScorer(
            LoanPolicyProperties policyProperties,
            LoanRatioCalculator ratioCalculator,
            LoanProductAnalyzer productAnalyzer
    ) {
        this.policyProperties = policyProperties;
        this.ratioCalculator = ratioCalculator;
        this.productAnalyzer = productAnalyzer;
    }

    public LoanRecommendation score(LoanProduct product, LoanSearchRequest request) {
        List<String> reasons = new ArrayList<String>();
        List<String> warnings = new ArrayList<String>();

        LoanAffordabilityResult affordability = calculateAffordability(product, request, warnings);

        int productFitScore = calculateProductFitScore(product, request, reasons, warnings);
        int affordabilityScore = calculateAffordabilityScore(affordability, reasons);
        int rateLimitScore = calculateRateAndLimitScore(product, request, reasons, warnings);
        int dataQualityScore = calculateDataQualityScore(product, reasons);

        int totalScore = productFitScore + affordabilityScore + rateLimitScore + dataQualityScore;
        totalScore = Math.max(0, Math.min(totalScore, 100));

        boolean eligible = affordability.isDsrPassed();

        if (affordability.isMortgageEvaluationUsed()) {
            eligible = eligible && affordability.isDtiPassed() && affordability.isLtvPassed();
        }

        if (!productAnalyzer.matchesAgeBand(product, request.getAge())) {
            eligible = false;
        }

        if (!productAnalyzer.matchesIncome(product, request.getAnnualIncome())) {
            eligible = false;
        }

        if (!productAnalyzer.matchesCreditGrade(product, request.getCreditGrade())) {
            eligible = false;
        }

        if (!productAnalyzer.matchesHouseCount(product, request.getHouseCount())) {
            eligible = false;
        }

        if (!productAnalyzer.matchesHouseArea(product, request.getHouseArea())) {
            eligible = false;
        }

        affordability.setWarnings(warnings);

        if (reasons.isEmpty()) {
            reasons.add("기본 조건으로 검토 가능한 상품입니다.");
        }

        return new LoanRecommendation(
                product,
                totalScore,
                eligible,
                reasons,
                warnings,
                affordability
        );
    }

    private LoanAffordabilityResult calculateAffordability(
            LoanProduct product,
            LoanSearchRequest request,
            List<String> warnings
    ) {
        LoanAffordabilityResult result = new LoanAffordabilityResult();

        long annualIncome = safe(request.getAnnualIncome());
        long requestedLoanAmount = safe(request.getLoanAmount());
        long existingAnnualDebtPayment = safe(request.getExistingMonthlyDebtPayment()) * 12;
        long existingAnnualDebtInterest = safe(request.getExistingAnnualDebtInterest());

        double rate = productAnalyzer.resolveCalculationRate(product, request.getExpectedInterestRate());
        rate += policyProperties.getStressRateAddition();

        int termYears = productAnalyzer.resolveCalculationTermYears(product, request.getDesiredLoanTermYears());

        long monthlyPayment = ratioCalculator.calculateMonthlyPayment(
                requestedLoanAmount,
                rate,
                termYears
        );

        long annualPayment = monthlyPayment * 12;

        double dsr = ratioCalculator.calculateDsr(
                annualIncome,
                existingAnnualDebtPayment,
                annualPayment
        );

        boolean mortgageEvaluationUsed = isMortgageEvaluationUsed(product, request);

        double dti = 0.0;
        double ltv = 0.0;
        boolean dtiPassed = true;
        boolean ltvPassed = true;

        double ltvLimit = Boolean.TRUE.equals(request.getFirstHomeBuyer())
                ? policyProperties.getFirstHomeBuyerLtvLimit()
                : policyProperties.getLtvLimit();

        Long maxLoanByDsr = ratioCalculator.calculateMaxLoanByDsr(
                annualIncome,
                existingAnnualDebtPayment,
                policyProperties.getDsrLimit(),
                rate,
                termYears
        );

        Long maxLoanByDti = null;
        Long maxLoanByLtv = null;

        if (mortgageEvaluationUsed) {
            dti = ratioCalculator.calculateDti(
                    annualIncome,
                    annualPayment,
                    existingAnnualDebtInterest
            );

            ltv = ratioCalculator.calculateLtv(
                    requestedLoanAmount,
                    safe(request.getCollateralValue()),
                    safe(request.getExistingMortgageBalance()),
                    safe(request.getSeniorDeposit())
            );

            maxLoanByDti = ratioCalculator.calculateMaxLoanByDti(
                    annualIncome,
                    existingAnnualDebtInterest,
                    policyProperties.getDtiLimit(),
                    rate,
                    termYears
            );

            maxLoanByLtv = ratioCalculator.calculateMaxLoanByLtv(
                    safe(request.getCollateralValue()),
                    safe(request.getExistingMortgageBalance()),
                    safe(request.getSeniorDeposit()),
                    ltvLimit
            );

            dtiPassed = dti <= policyProperties.getDtiLimit();
            ltvPassed = ltv <= ltvLimit;

            if (!dtiPassed) {
                warnings.add("예상 DTI가 기준을 초과합니다.");
            }

            if (!ltvPassed) {
                warnings.add("예상 LTV가 기준을 초과합니다.");
            }
        }

        boolean dsrPassed = dsr <= policyProperties.getDsrLimit();

        if (!dsrPassed) {
            warnings.add("예상 DSR이 기준을 초과합니다.");
        }

        Long finalPossibleLoanAmount = maxLoanByDsr;

        if (mortgageEvaluationUsed) {
            finalPossibleLoanAmount = minPositive(finalPossibleLoanAmount, maxLoanByDti);
            finalPossibleLoanAmount = minPositive(finalPossibleLoanAmount, maxLoanByLtv);
        }

        result.setDsr(roundOne(dsr));
        result.setDti(roundOne(dti));
        result.setLtv(roundOne(ltv));
        result.setDsrLimit(policyProperties.getDsrLimit());
        result.setDtiLimit(policyProperties.getDtiLimit());
        result.setLtvLimit(ltvLimit);
        result.setEstimatedMonthlyPayment(monthlyPayment);
        result.setEstimatedAnnualPayment(annualPayment);
        result.setMaxLoanByDsr(maxLoanByDsr);
        result.setMaxLoanByDti(maxLoanByDti);
        result.setMaxLoanByLtv(maxLoanByLtv);
        result.setFinalPossibleLoanAmount(finalPossibleLoanAmount);
        result.setDsrPassed(dsrPassed);
        result.setDtiPassed(dtiPassed);
        result.setLtvPassed(ltvPassed);
        result.setMortgageEvaluationUsed(mortgageEvaluationUsed);
        result.setCalculationRate(roundOne(rate));
        result.setCalculationTermYears(termYears);
        result.setCalculationMethod(product.getRepaymentMethod());

        return result;
    }

    private int calculateProductFitScore(
            LoanProduct product,
            LoanSearchRequest request,
            List<String> reasons,
            List<String> warnings
    ) {
        int score = 0;

        if (productAnalyzer.matchesAgeBand(product, request.getAge())) {
            score += 7;
            reasons.add("연령 조건에 부합합니다.");
        } else {
            warnings.add("연령 조건이 맞지 않을 수 있습니다.");
        }

        if (productAnalyzer.matchesIncome(product, request.getAnnualIncome())) {
            score += 7;
            reasons.add("소득 조건에 부합합니다.");
        } else {
            warnings.add("소득 조건이 맞지 않을 수 있습니다.");
        }

        if (productAnalyzer.matchesCreditGrade(product, request.getCreditGrade())) {
            score += 7;
            reasons.add("신용등급 조건에 부합합니다.");
        } else {
            warnings.add("신용등급 조건이 맞지 않을 수 있습니다.");
        }

        if (matchesText(product.getPurpose(), request.getPurpose())
                || matchesText(product.getProductCategory(), request.getPurpose())) {
            score += 7;
            reasons.add("대출 용도가 상품 목적과 일치합니다.");
        }

        if (matchesText(product.getRegion(), request.getRegion())
                || matchesText(product.getRegion(), "전국")) {
            score += 7;
            reasons.add("거주 지역에서 신청 가능한 상품입니다.");
        }

        if (!productAnalyzer.matchesHouseCount(product, request.getHouseCount())) {
            warnings.add("주택 보유 수 조건이 맞지 않을 수 있습니다.");
        }

        if (!productAnalyzer.matchesHouseArea(product, request.getHouseArea())) {
            warnings.add("주택 면적 조건이 맞지 않을 수 있습니다.");
        }

        return Math.min(score, 35);
    }

    private int calculateAffordabilityScore(
            LoanAffordabilityResult affordability,
            List<String> reasons
    ) {
        int score = 0;

        if (affordability.isDsrPassed()) {
            score += 20;
            reasons.add("예상 DSR이 기준 이내입니다.");
        }

        if (affordability.isMortgageEvaluationUsed()) {
            if (affordability.isDtiPassed()) {
                score += 7;
                reasons.add("예상 DTI가 기준 이내입니다.");
            }

            if (affordability.isLtvPassed()) {
                score += 8;
                reasons.add("예상 LTV가 기준 이내입니다.");
            }
        } else {
            score += calculateDsrStabilityBonus(affordability);
        }

        return Math.min(score, 35);
    }

    private int calculateDsrStabilityBonus(LoanAffordabilityResult affordability) {
        if (affordability.getDsrLimit() <= 0) {
            return 0;
        }

        double usage = affordability.getDsr() / affordability.getDsrLimit();

        if (usage <= 0.70) {
            return 15;
        }

        if (usage <= 0.85) {
            return 10;
        }

        if (usage <= 1.00) {
            return 5;
        }

        return 0;
    }

    private int calculateRateAndLimitScore(
            LoanProduct product,
            LoanSearchRequest request,
            List<String> reasons,
            List<String> warnings
    ) {
        int score = 0;

        Long productLimit = productAnalyzer.resolveLimitAmount(product);

        if (productLimit != null && request.getLoanAmount() != null) {
            if (request.getLoanAmount() <= productLimit) {
                score += 10;
                reasons.add("희망 대출금액이 상품 한도 이내입니다.");
            } else {
                warnings.add("희망 대출금액이 상품 한도를 초과합니다.");
            }
        } else {
            score += 3;
            warnings.add("상품 한도 정보 확인이 필요합니다.");
        }

        Double rate = productAnalyzer.resolveCalculationRate(product, request.getExpectedInterestRate());

        if (rate <= 4.0) {
            score += 10;
            reasons.add("금리 조건이 낮은 편입니다.");
        } else if (rate <= 8.0) {
            score += 6;
        } else {
            score += 3;
        }

        return Math.min(score, 20);
    }

    private int calculateDataQualityScore(LoanProduct product, List<String> reasons) {
        int score = 0;

        if (StringUtils.hasText(product.getLimitText())
                && StringUtils.hasText(product.getRateText())
                && StringUtils.hasText(product.getRepaymentMethod())) {
            score += 5;
        }

        if (StringUtils.hasText(product.getApplicationMethod())
                || StringUtils.hasText(product.getContact())
                || StringUtils.hasText(product.getRelatedSite())) {
            score += 5;
        }

        if (score >= 5) {
            reasons.add("상품 정보가 비교적 충분히 제공됩니다.");
        }

        return score;
    }

    private boolean isMortgageEvaluationUsed(LoanProduct product, LoanSearchRequest request) {
        if (Boolean.TRUE.equals(request.getMortgageLoan())) {
            return true;
        }

        return productAnalyzer.isMortgageLikeProduct(product);
    }

    private boolean matchesText(String source, String keyword) {
        if (!StringUtils.hasText(source) || !StringUtils.hasText(keyword)) {
            return false;
        }

        return source.contains(keyword) || keyword.contains(source);
    }

    private long safe(Long value) {
        return value == null ? 0L : value;
    }

    private Long minPositive(Long left, Long right) {
        if (left == null || left <= 0) {
            return right;
        }

        if (right == null || right <= 0) {
            return left;
        }

        return Math.min(left, right);
    }

    private double roundOne(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}