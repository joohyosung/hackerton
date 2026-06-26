package com.kookmin.hackerton.loan.service;

import com.kookmin.hackerton.loan.model.LoanAffordabilityResult;
import com.kookmin.hackerton.loan.model.LoanProduct;
import com.kookmin.hackerton.loan.model.LoanRecommendation;
import com.kookmin.hackerton.loan.model.LoanSearchRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class LoanRecommendationScorer {

    private final LoanProductAnalyzer productAnalyzer;

    public LoanRecommendationScorer(LoanProductAnalyzer productAnalyzer) {
        this.productAnalyzer = productAnalyzer;
    }

    public LoanRecommendation score(LoanProduct product, LoanSearchRequest request) {
        LoanSearchRequest safeRequest = request == null ? new LoanSearchRequest() : request;
        List<String> reasons = new ArrayList<String>();
        List<String> warnings = new ArrayList<String>();

        ScoreResult limit = calculateLimitScore(product, safeRequest, reasons, warnings);
        ScoreResult purpose = calculatePurposeScore(product, safeRequest, reasons, warnings);
        ScoreResult target = calculateTargetScore(product, safeRequest, reasons, warnings);
        ScoreResult region = calculateRegionScore(product, safeRequest, reasons, warnings);
        ScoreResult condition = calculateConditionScore(product, safeRequest, reasons, warnings);
        int rateScore = calculateRateScore(product, reasons);

        int totalScore = limit.score + purpose.score + target.score
                + region.score + condition.score + rateScore;
        totalScore = Math.max(0, Math.min(totalScore, 100));

        boolean eligible = limit.eligible
                && purpose.eligible
                && target.eligible
                && region.eligible
                && condition.eligible;

        LoanAffordabilityResult affordability = new LoanAffordabilityResult();
        affordability.setWarnings(warnings);

        if (reasons.isEmpty()) {
            reasons.add("기본 조건으로 검토 가능한 상품입니다.");
        }

        return new LoanRecommendation(product, totalScore, eligible, reasons, warnings, affordability);
    }

    private ScoreResult calculateLimitScore(
            LoanProduct product,
            LoanSearchRequest request,
            List<String> reasons,
            List<String> warnings
    ) {
        Long productLimit = productAnalyzer.resolveLimitAmount(product);
        Long requestAmount = request.getLoanAmount();

        if (productLimit == null || productLimit <= 0 || requestAmount == null) {
            warnings.add("상품 한도 정보 확인이 필요합니다.");
            return ScoreResult.pass(10);
        }

        if (requestAmount > productLimit) {
            warnings.add("희망 대출금액이 상품 한도를 초과합니다.");
            return ScoreResult.fail(0);
        }

        double ratio = (double) requestAmount / productLimit;

        if (ratio <= 0.7) {
            reasons.add("희망 대출금액이 상품 한도에 충분히 여유 있게 들어갑니다.");
            return ScoreResult.pass(25);
        }

        reasons.add("희망 대출금액이 상품 한도 이내입니다.");
        return ScoreResult.pass(20);
    }

    private ScoreResult calculatePurposeScore(
            LoanProduct product,
            LoanSearchRequest request,
            List<String> reasons,
            List<String> warnings
    ) {
        String productPurpose = safe(product == null ? null : product.getPurpose());
        String requestPurpose = safe(request.getPurpose());

        if (!StringUtils.hasText(productPurpose) || !StringUtils.hasText(requestPurpose)) {
            warnings.add("상품 용도 정보 확인이 필요합니다.");
            return ScoreResult.pass(8);
        }

        if (equalsNormalized(productPurpose, requestPurpose)) {
            reasons.add("대출 용도가 상품 목적과 정확히 일치합니다.");
            return ScoreResult.pass(25);
        }

        if (containsEither(productPurpose, requestPurpose)) {
            reasons.add("대출 용도가 상품 목적과 일부 일치합니다.");
            return ScoreResult.pass(15);
        }

        warnings.add("대출 용도가 상품 목적과 다를 수 있습니다.");
        return ScoreResult.pass(0);
    }

    private ScoreResult calculateTargetScore(
            LoanProduct product,
            LoanSearchRequest request,
            List<String> reasons,
            List<String> warnings
    ) {
        String userType = safe(request.getUserType());

        if (!StringUtils.hasText(userType)) {
            return ScoreResult.pass(10);
        }

        String targetText = joinText(
                product == null ? null : product.getTarget(),
                product == null ? null : product.getProductCategory(),
                product == null ? null : product.getSummary()
        );

        if (!StringUtils.hasText(targetText)) {
            warnings.add("상품 대상 정보 확인이 필요합니다.");
            return ScoreResult.pass(8);
        }

        if (containsEither(targetText, userType)) {
            reasons.add("사용자 유형이 상품 대상과 일치합니다.");
            return ScoreResult.pass(20);
        }

        warnings.add("사용자 유형이 상품 대상과 다를 수 있습니다.");
        return ScoreResult.pass(0);
    }

    private ScoreResult calculateRegionScore(
            LoanProduct product,
            LoanSearchRequest request,
            List<String> reasons,
            List<String> warnings
    ) {
        String productRegion = safe(product == null ? null : product.getRegion());
        String requestRegion = safe(request.getRegion());

        if (!StringUtils.hasText(productRegion)) {
            warnings.add("상품 지역 정보 확인이 필요합니다.");
            return ScoreResult.pass(8);
        }

        if (containsEither(productRegion, "전국")) {
            reasons.add("전국에서 신청 가능한 상품입니다.");
            return ScoreResult.pass(15);
        }

        if (containsEither(productRegion, requestRegion)) {
            reasons.add("거주 지역과 상품 지역이 일치합니다.");
            return ScoreResult.pass(15);
        }

        if (isLocalGovernmentProduct(product)) {
            warnings.add("지자체 상품이나 거주 지역이 일치하지 않습니다.");
            return ScoreResult.fail(0);
        }

        warnings.add("거주 지역과 상품 지역이 다를 수 있습니다.");
        return ScoreResult.pass(0);
    }

    private ScoreResult calculateConditionScore(
            LoanProduct product,
            LoanSearchRequest request,
            List<String> reasons,
            List<String> warnings
    ) {
        int score = 0;
        boolean eligible = true;

        if (productAnalyzer.matchesAgeBand(product, request.getAge())) {
            score += 4;
            reasons.add("연령 조건에 부합합니다.");
        } else {
            warnings.add("연령 조건이 맞지 않습니다.");
            eligible = false;
        }

        if (productAnalyzer.matchesIncome(product, request.getAnnualIncome())) {
            score += 4;
            reasons.add("소득 조건에 부합합니다.");
        } else {
            warnings.add("소득 조건이 맞지 않을 수 있습니다.");
        }

        if (request.getCreditGrade() == null) {
            score += 2;
        } else if (productAnalyzer.matchesCreditGrade(product, request.getCreditGrade())) {
            score += 2;
            reasons.add("신용 조건에 부합합니다.");
        } else {
            warnings.add("신용 조건이 맞지 않습니다.");
            eligible = false;
        }

        return new ScoreResult(score, eligible);
    }

    private int calculateRateScore(LoanProduct product, List<String> reasons) {
        Double rate = productAnalyzer.parseMaxRate(product == null ? null : product.getRateText());

        if (rate == null) {
            return 2;
        }

        if (rate <= 3.0) {
            reasons.add("금리 조건이 매우 낮은 편입니다.");
            return 5;
        }

        if (rate <= 5.0) {
            return 4;
        }
        if (rate <= 10.0) {
            return 3;
        }
        if (rate <= 15.0) {
            return 2;
        }
        if (rate <= 20.0) {
            return 1;
        }

        return 0;
    }

    private boolean isLocalGovernmentProduct(LoanProduct product) {
        if (product == null) {
            return false;
        }

        return containsEither(product.getInstitutionCategory(), "지자체")
                || containsEither(product.getInstitution(), "시청")
                || containsEither(product.getInstitution(), "군청")
                || containsEither(product.getInstitution(), "구청");
    }

    private boolean equalsNormalized(String left, String right) {
        return normalize(left).equals(normalize(right));
    }

    private boolean containsEither(String left, String right) {
        if (!StringUtils.hasText(left) || !StringUtils.hasText(right)) {
            return false;
        }

        String normalizedLeft = normalize(left);
        String normalizedRight = normalize(right);

        return normalizedLeft.contains(normalizedRight)
                || normalizedRight.contains(normalizedLeft);
    }

    private String joinText(String... values) {
        StringBuilder builder = new StringBuilder();

        for (String value : values) {
            if (StringUtils.hasText(value)) {
                if (builder.length() > 0) {
                    builder.append(" ");
                }
                builder.append(value);
            }
        }

        return builder.toString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String normalize(String value) {
        return safe(value)
                .replace(" ", "")
                .replace("·", "")
                .replace(",", "")
                .trim()
                .toLowerCase(Locale.KOREAN);
    }

    private static class ScoreResult {
        private final int score;
        private final boolean eligible;

        private ScoreResult(int score, boolean eligible) {
            this.score = score;
            this.eligible = eligible;
        }

        private static ScoreResult pass(int score) {
            return new ScoreResult(score, true);
        }

        private static ScoreResult fail(int score) {
            return new ScoreResult(score, false);
        }
    }
}
