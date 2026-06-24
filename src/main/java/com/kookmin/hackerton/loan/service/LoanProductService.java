package com.kookmin.hackerton.loan.service;

import com.kookmin.hackerton.loan.config.LoanApiProperties;
import com.kookmin.hackerton.loan.model.LoanProduct;
import com.kookmin.hackerton.loan.model.LoanRecommendation;
import com.kookmin.hackerton.loan.model.LoanSearchRequest;
import com.kookmin.hackerton.loan.config.LoanPolicyProperties;
import com.kookmin.hackerton.loan.model.LoanAffordabilityResult;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Service
public class LoanProductService {

    private static final Logger log = LoggerFactory.getLogger(LoanProductService.class);
    private final LoanApiProperties properties;
    private final LoanPolicyProperties policyProperties;
    private final LoanRatioCalculator ratioCalculator;
    private final LoanProductAnalyzer productAnalyzer;
    private final RestTemplate restTemplate;

    public LoanProductService(
        LoanApiProperties properties,
        LoanPolicyProperties policyProperties,
        LoanRatioCalculator ratioCalculator,
        LoanProductAnalyzer productAnalyzer
    ) {
        this.properties = properties;
        this.policyProperties = policyProperties;
        this.ratioCalculator = ratioCalculator;
        this.productAnalyzer = productAnalyzer;
        this.restTemplate = new RestTemplate();
    }

    private LoanRecommendation score(LoanProduct product, LoanSearchRequest request) {
        List<String> reasons = new ArrayList<String>();
        List<String> warnings = new ArrayList<String>();

        LoanAffordabilityResult affordability = calculateAffordability(product, request, warnings);

        int productFitScore = calculateProductFitScore(product, request, reasons, warnings);
        int affordabilityScore = calculateAffordabilityScore(affordability, reasons, warnings);
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

    public LoanProduct findById(String id) {
        if (!StringUtils.hasText(id)) {
            return null;
        }

        for (LoanProduct product : loadProducts()) {
            if (id.equals(product.getId())) {
                return product;
            }
        }

        return null;
    }

    private List<LoanProduct> loadProducts() {
        if (!StringUtils.hasText(properties.getServiceKey())) {
            return sampleProducts();
        }

        try {
            List<LoanProduct> products = fetchFromPublicApi();
            if (!products.isEmpty()) {
                return products;
            }
        } catch (Exception exception) {
            log.warn("Failed to load loan products from public API. Falling back to sample data.", exception);
        }

        if (properties.isUseSampleWhenUnavailable()) {
            return sampleProducts();
        }

        return Collections.emptyList();
    }

    private List<LoanProduct> fetchFromPublicApi() throws Exception {
        String xml = restTemplate.getForObject(
            UriComponentsBuilder.fromHttpUrl(properties.getEndpoint())
                .path(properties.getPath())
                .queryParam("serviceKey", properties.getServiceKey())
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", properties.getPageSize())
                .queryParam("type", "xml")
                .build(false)
                .toUri(),
            String.class
        );

        if (!StringUtils.hasText(xml)) {
            return Collections.emptyList();
        }

        return parseProducts(xml);
    }

    private List<LoanProduct> parseProducts(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        NodeList items = document.getElementsByTagName("item");
        List<LoanProduct> products = new ArrayList<LoanProduct>();

        for (int index = 0; index < items.getLength(); index++) {
            Node node = items.item(index);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            LoanProduct product = toProduct((Element) node, index);
            if (StringUtils.hasText(product.getName())) {
                products.add(product);
            }
        }

        return products;
    }

    private LoanProduct toProduct(Element item, int index) {
        LoanProduct product = new LoanProduct();
        product.setName(firstText(item, "finPrdNm", "loanProductName", "productName", "상품명"));
        product.setInstitution(firstText(item, "hdlInst", "ofrInstNm", "hdlInstDtlVw", "instNm", "institution", "취급기관"));
        product.setLimitText(firstText(item, "lnLmt", "loanLimit", "limit", "대출한도"));
        product.setRateType(firstText(item, "irtCtg", "rateType", "금리구분"));
        product.setRateText(firstText(item, "irt", "interestRate", "rate", "금리"));
        product.setPurpose(firstText(item, "usge", "loanUse", "purpose", "자금용도"));
        product.setPeriodText(firstText(item, "maxTotLnTrm", "loanPeriod", "period", "총대출기간"));
        product.setTarget(firstText(item, "trgt", "suprTgtDtlCond", "tgtFltr", "target", "지원대상"));
        product.setRegion(firstText(item, "rsdArea", "rsdAreaPamtEqltIstm", "rgn", "region", "area", "지역"));
        product.setSummary(summaryText(item));
        product.setSourceUrl(firstText(item, "rltSite", "url", "homepage", "link"));
        product.setLimitAmount(parseMoney(product.getLimitText()));
        product.setMinAge(inferMinAge(item));
        product.setMaxAge(inferMaxAge(item));
        product.setMaxAnnualIncome(parseMoney(firstText(item, "anin", "incm")));
        product.setMaxCreditGrade(inferMaxCreditGrade(item));

        product.setAgeText(firstText(item, "age"));
        product.setAge39Below(firstText(item, "age_39Blw"));
        product.setAge40Above(firstText(item, "age_40Abnml"));
        product.setAge60Above(firstText(item, "age_60Abnml"));

        product.setAnnualIncomeText(firstText(item, "anin"));
        product.setIncomeText(firstText(item, "incm"));
        product.setIncomeCondition(firstText(item, "incmCnd"));
        product.setIncomeConditionYes(firstText(item, "incmCndY"));
        product.setIncomeConditionNone(firstText(item, "incmCndN"));

        product.setCreditGradeText(firstText(item, "crdtSc"));
        product.setCreditGrade1(firstText(item, "crdtSc_1"));
        product.setCreditGrade2(firstText(item, "crdtSc_2"));
        product.setCreditGrade3(firstText(item, "crdtSc_3"));
        product.setCreditGrade4(firstText(item, "crdtSc_4"));
        product.setCreditGrade5(firstText(item, "crdtSc_5"));
        product.setCreditGrade6(firstText(item, "crdtSc_6"));
        product.setCreditGrade7(firstText(item, "crdtSc_7"));
        product.setCreditGrade8(firstText(item, "crdtSc_8"));
        product.setCreditGrade9(firstText(item, "crdtSc_9"));
        product.setCreditGradeNoGrade(firstText(item, "crdtSc_0"));
        product.setCreditGrade1To5(firstText(item, "crdtSc_1_5"));
        product.setCreditGrade6ToNoGrade(firstText(item, "crdtSc_6_0"));

        product.setLoanTargetHouse(firstText(item, "lnTgtHous"));
        product.setHouseArea(firstText(item, "housAr"));
        product.setHouseHoldCount(firstText(item, "housHoldCnt"));

        product.setPreferentialRateCondition(firstText(item, "prftAddIrtCond"));
        product.setOverdueInterestRate(firstText(item, "ovItrYr"));
        product.setEarlyRepaymentFee(firstText(item, "rpymdCfe"));
        product.setLoanAdditionalCost(firstText(item, "lnIcdcst"));

        if (!StringUtils.hasText(product.getName())) {
            product.setName("대출 상품 " + (index + 1));
        }
        if (!StringUtils.hasText(product.getInstitution())) {
            product.setInstitution("취급기관 확인 필요");
        }
        if (!StringUtils.hasText(product.getRegion())) {
            product.setRegion("전국");
        }

        product.setId(slug(product.getName() + "-" + product.getInstitution() + "-" + index));
        return product;
    }

    private String summaryText(Element item) {
        List<String> parts = new ArrayList<String>();
        addIfPresent(parts, firstText(item, "suprTgtDtlCond"));
        addIfPresent(parts, firstText(item, "etcRefSbjc"));
        addIfPresent(parts, firstText(item, "kinfaPrdEtc"));
        addIfPresent(parts, firstText(item, "jnMthd"));
        addIfPresent(parts, firstText(item, "rfrcCnpl"));

        if (parts.isEmpty()) {
            return firstText(item, "description", "summary", "상세내용");
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                builder.append(" ");
            }
            builder.append(parts.get(i));
        }
        return builder.toString();
    }

    private void addIfPresent(List<String> values, String value) {
        if (StringUtils.hasText(value)) {
            values.add(value);
        }
    }

    private LoanRecommendation score(LoanProduct product, LoanSearchRequest request) {
        List<String> reasons = new ArrayList<String>();
        List<String> warnings = new ArrayList<String>();

        LoanAffordabilityResult affordability = calculateAffordability(request);

        boolean dsrPassed = affordability.isDsrPassed();
        boolean dtiPassed = affordability.isDtiPassed();
        boolean ltvPassed = affordability.isLtvPassed();

        if (!dsrPassed) {
            warnings.add("예상 DSR이 기준을 초과합니다. 예상 "
                + formatPercent(affordability.getDsr())
                + ", 기준 "
                + formatPercent(affordability.getDsrLimit())
            );
        }

        if (affordability.isMortgageEvaluationUsed() && !dtiPassed) {
            warnings.add("예상 DTI가 기준을 초과합니다. 예상 "
                + formatPercent(affordability.getDti())
                + ", 기준 "
                + formatPercent(affordability.getDtiLimit()));
        }

        if (affordability.isMortgageEvaluationUsed() && !ltvPassed) {
            warnings.add("예상 LTV가 기준을 초과합니다. 예상 "
                + formatPercent(affordability.getLtv())
                + ", 기준 "
                + formatPercent(affordability.getLtvLimit()));
        }

        boolean ratioPassed = dsrPassed
            && (!affordability.isMortgageEvaluationUsed() || dtiPassed)
            && (!affordability.isMortgageEvaluationUsed() || ltvPassed);

        int score = 0;

        if (ratioPassed) {
            score += 45;
            reasons.add("DSR, DTI, LTV 기준으로 대출 가능성을 통과했습니다.");
        } else {
            score += 10;
        }

        score += calculateStabilityScore(affordability);
        score += calculateProductFitScore(product, request, reasons, warnings);
        score += calculateRateAndLimitScore(product, request, reasons, warnings);

        if (affordability.getEstimatedMonthlyPayment() != null
            && affordability.getEstimatedMonthlyPayment() > 0) {
            reasons.add("예상 월 상환액은 약 "
                + formatWon(affordability.getEstimatedMonthlyPayment())
                + "입니다.");
        }

        if (affordability.getFinalPossibleLoanAmount() != null
            && affordability.getFinalPossibleLoanAmount() > 0) {
            reasons.add("계산상 가능 한도는 약 "
                + formatWon(affordability.getFinalPossibleLoanAmount())
                + "입니다.");
        }

        boolean eligible = ratioPassed && warnings.isEmpty();

        if (reasons.isEmpty()) {
            reasons.add("기본 조건으로 검토 가능한 상품입니다.");
        }

        affordability.setWarnings(warnings);

        return new LoanRecommendation(
            product,
            Math.max(0, Math.min(score, 100)),
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
    private int calculateStabilityScore(LoanAffordabilityResult affordability) {
        double dsrLimit = affordability.getDsrLimit();

        if (dsrLimit <= 0) {
            return 0;
        }

        double usage = affordability.getDsr() / dsrLimit;

        if (usage <= 0.70) {
            return 25;
        }
        if (usage <= 0.85) {
            return 18;
        }
        if (usage <= 1.00) {
            return 10;
        }

        return 0;
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

        if (productAnalyzer.matchesHouseCount(product, request.getHouseCount())) {
            score += 0;
        } else {
            warnings.add("주택 보유 수 조건이 맞지 않을 수 있습니다.");
        }

        if (productAnalyzer.matchesHouseArea(product, request.getHouseArea())) {
            score += 0;
        } else {
            warnings.add("주택 면적 조건이 맞지 않을 수 있습니다.");
        }

        return Math.min(score, 35);
    }

    private int calculateAffordabilityScore(
            LoanAffordabilityResult affordability,
            List<String> reasons,
            List<String> warnings
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
    private int calculateRateAndLimitScore(
        LoanProduct product,
        LoanSearchRequest request,
        List<String> reasons,
        List<String> warnings
    ) {
        int score = 0;

        if (request.getLoanAmount() != null && request.getLoanAmount() > 0) {
            Long limitAmount = product.getLimitAmount();

            if (limitAmount == null) {
                score += 2;
                reasons.add("상품 한도 확인이 필요합니다.");
            } else if (request.getLoanAmount() <= limitAmount) {
                score += 5;
                reasons.add("희망 금액이 상품 한도 안에 있습니다.");
            } else {
                warnings.add("희망 금액이 상품 최대한도를 초과합니다.");
            }
        }

        String rateText = product.getRateText();
        if (StringUtils.hasText(rateText)) {
            if (rateText.contains("1.") || rateText.contains("2.") || rateText.contains("3.") || rateText.contains("4.")) {
                score += 5;
                reasons.add("금리 조건이 상대적으로 낮은 편입니다.");
            } else {
                score += 2;
            }
        }

        return Math.min(score, 10);
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
    private String formatPercent(double value) {
        return roundOne(value) + "%";
    }

    private String formatWon(Long value) {
        if (value == null || value <= 0) {
            return "0원";
        }

        if (value >= 100000000L) {
            double eok = value / 100000000.0;
            return roundOne(eok) + "억원";
        }

        return Math.round(value / 10000.0) + "만원";
    }

    private boolean matchesText(String source, String keyword) {
        if (!StringUtils.hasText(source) || !StringUtils.hasText(keyword)) {
            return false;
        }

        return source.toLowerCase(Locale.KOREAN).contains(keyword.toLowerCase(Locale.KOREAN));
    }

    private Integer inferMinAge(Element item) {
        if (isPositive(firstText(item, "age_60Abnml"))) {
            return 60;
        }
        if (isPositive(firstText(item, "age_40Abnml"))) {
            return 40;
        }

        String ageText = firstText(item, "age");
        Matcher matcher = Pattern.compile("(\\d+)").matcher(ageText);
        if (matcher.find()) {
            return Integer.valueOf(matcher.group(1));
        }

        return null;
    }

    private Integer inferMaxAge(Element item) {
        if (isPositive(firstText(item, "age_39Blw"))) {
            return 39;
        }
        if (isPositive(firstText(item, "age_40Abnml"))) {
            return 59;
        }

        String ageText = firstText(item, "age");
        Matcher matcher = Pattern.compile("(\\d+)").matcher(ageText);
        Integer max = null;
        while (matcher.find()) {
            max = Integer.valueOf(matcher.group(1));
        }
        return max;
    }

    private Integer inferMaxCreditGrade(Element item) {
        if (isPositive(firstText(item, "crdtSc_6_0"))) {
            return 10;
        }
        if (isPositive(firstText(item, "crdtSc_1_5"))) {
            return 5;
        }

        int max = 0;
        for (int grade = 1; grade <= 9; grade++) {
            if (isPositive(firstText(item, "crdtSc_" + grade))) {
                max = grade;
            }
        }

        return max > 0 ? max : null;
    }

    private boolean isPositive(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        String normalized = value.trim().toLowerCase(Locale.KOREAN);
        return "y".equals(normalized)
            || "yes".equals(normalized)
            || "1".equals(normalized)
            || "true".equals(normalized)
            || "가능".equals(normalized)
            || "대상".equals(normalized)
            || "해당".equals(normalized)
            || "○".equals(normalized)
            || "o".equals(normalized);
    }

    private String firstText(Element item, String... names) {
        Map<String, String> normalized = new HashMap<String, String>();
        NodeList children = item.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                normalized.put(child.getNodeName().toLowerCase(Locale.ROOT), child.getTextContent().trim());
            }
        }

        for (String name : names) {
            String value = normalized.get(name.toLowerCase(Locale.ROOT));
            if (StringUtils.hasText(value)) {
                return value;
            }
        }

        return "";
    }

    private Long parseMoney(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }

        String normalized = text.replace(",", "").replace(" ", "");
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)(억|천만|백만|만원|원)?");
        Matcher matcher = pattern.matcher(normalized);
        Long largest = null;

        while (matcher.find()) {
            double number = Double.parseDouble(matcher.group(1));
            String unit = matcher.group(2);
            long multiplier = 1L;
            if ("억".equals(unit)) {
                multiplier = 100000000L;
            } else if ("천만".equals(unit)) {
                multiplier = 10000000L;
            } else if ("백만".equals(unit)) {
                multiplier = 1000000L;
            } else if ("만원".equals(unit)) {
                multiplier = 10000L;
            }

            long amount = Math.round(number * multiplier);
            if (largest == null || amount > largest) {
                largest = amount;
            }
        }

        return largest;
    }

    private String slug(String value) {
        String normalized = value == null ? "loan-product" : value.trim().toLowerCase(Locale.KOREAN);
        normalized = normalized.replaceAll("[^0-9a-z가-힣]+", "-");
        normalized = normalized.replaceAll("(^-|-$)", "");
        return StringUtils.hasText(normalized) ? normalized : "loan-product";
    }

    private List<LoanProduct> sampleProducts() {
        List<LoanProduct> products = new ArrayList<LoanProduct>();
        products.add(sample(
            "햇살론15",
            "서민금융진흥원",
            "최대 2,000만원",
            20000000L,
            "고정금리",
            "연 15.9%, 성실상환 시 인하 가능",
            "생계 대환",
            "3년 또는 5년",
            "저신용, 저소득 근로자 및 사업자",
            "전국",
            "대부업이나 불법사금융 이용이 불가피한 최저신용자를 위한 정책서민금융상품입니다.",
            19,
            null,
            45000000L,
            10
        ));
        products.add(sample(
            "근로자 햇살론",
            "상호금융, 저축은행",
            "최대 2,000만원",
            20000000L,
            "변동금리",
            "연 11.5% 이하",
            "생계",
            "3년 또는 5년",
            "재직 3개월 이상 근로자",
            "전국",
            "제도권 금융 이용이 어려운 근로자에게 생계자금을 지원하는 상품입니다.",
            19,
            null,
            45000000L,
            6
        ));
        products.add(sample(
            "새희망홀씨II",
            "시중은행",
            "최대 3,500만원",
            35000000L,
            "은행별 상이",
            "은행별 심사에 따라 결정",
            "생계 대환",
            "최대 5년",
            "저소득 또는 저신용 서민",
            "전국",
            "은행권에서 운영하는 대표적인 서민 맞춤형 신용대출 상품입니다.",
            19,
            null,
            50000000L,
            6
        ));
        products.add(sample(
            "미소금융 창업 운영자금",
            "서민금융진흥원",
            "최대 7,000만원",
            70000000L,
            "고정금리",
            "연 4.5% 내외",
            "창업 운영",
            "최대 6년",
            "창업 예정자 또는 영세 자영업자",
            "전국",
            "담보나 신용이 부족한 창업자와 영세 자영업자의 창업 및 운영자금을 지원합니다.",
            19,
            null,
            45000000L,
            7
        ));
        products.add(sample(
            "청년전용 버팀목 전세자금",
            "주택도시기금",
            "최대 2억원",
            200000000L,
            "고정 또는 변동",
            "연 1.8%~2.7% 수준",
            "주거",
            "2년, 최대 10년까지 연장 가능",
            "만 19세 이상 34세 이하 무주택 청년",
            "전국",
            "청년층의 주거 안정을 위한 전세자금 대출 상품입니다.",
            19,
            34,
            50000000L,
            null
        ));

        return products;
    }

    private LoanProduct sample(
        String name,
        String institution,
        String limitText,
        Long limitAmount,
        String rateType,
        String rateText,
        String purpose,
        String periodText,
        String target,
        String region,
        String summary,
        Integer minAge,
        Integer maxAge,
        Long maxAnnualIncome,
        Integer maxCreditGrade
    ) {
        LoanProduct product = new LoanProduct();
        product.setId(slug(name + "-" + institution));
        product.setName(name);
        product.setInstitution(institution);
        product.setLimitText(limitText);
        product.setLimitAmount(limitAmount);
        product.setRateType(rateType);
        product.setRateText(rateText);
        product.setPurpose(purpose);
        product.setPeriodText(periodText);
        product.setTarget(target);
        product.setRegion(region);
        product.setSummary(summary);
        product.setMinAge(minAge);
        product.setMaxAge(maxAge);
        product.setMaxAnnualIncome(maxAnnualIncome);
        product.setMaxCreditGrade(maxCreditGrade);
        return product;
    }
}
