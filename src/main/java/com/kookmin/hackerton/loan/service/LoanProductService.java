package com.kookmin.hackerton.loan.service;

import com.kookmin.hackerton.loan.config.LoanApiProperties;
import com.kookmin.hackerton.loan.model.LoanProduct;
import com.kookmin.hackerton.loan.model.LoanRecommendation;
import com.kookmin.hackerton.loan.model.LoanSearchRequest;
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
    private final RestTemplate restTemplate;

    public LoanProductService(LoanApiProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    public List<LoanRecommendation> search(LoanSearchRequest request) {
        LoanSearchRequest safeRequest = request == null ? new LoanSearchRequest() : request;
        List<LoanProduct> products = loadProducts();
        List<LoanRecommendation> recommendations = new ArrayList<LoanRecommendation>();

        for (LoanProduct product : products) {
            LoanRecommendation recommendation = score(product, safeRequest);
            if (recommendation.getScore() >= 35) {
                recommendations.add(recommendation);
            }
        }

        Collections.sort(recommendations, new Comparator<LoanRecommendation>() {
            @Override
            public int compare(LoanRecommendation left, LoanRecommendation right) {
                int scoreCompare = Integer.compare(right.getScore(), left.getScore());
                if (scoreCompare != 0) {
                    return scoreCompare;
                }
                return left.getProduct().getName().compareTo(right.getProduct().getName());
            }
        });

        return recommendations;
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
        int score = 45;
        List<String> reasons = new ArrayList<String>();

        if (request.getLoanAmount() != null && request.getLoanAmount() > 0) {
            Long limitAmount = product.getLimitAmount();
            if (limitAmount == null || request.getLoanAmount() <= limitAmount) {
                score += 20;
                reasons.add("희망 금액이 상품 한도 안에 있습니다.");
            } else {
                score -= 30;
            }
        }

        if (request.getAnnualIncome() != null && request.getAnnualIncome() > 0) {
            Long maxAnnualIncome = product.getMaxAnnualIncome();
            if (maxAnnualIncome == null || request.getAnnualIncome() <= maxAnnualIncome) {
                score += 15;
                reasons.add("입력한 연소득 조건과 맞습니다.");
            } else {
                score -= 20;
            }
        }

        if (request.getCreditGrade() != null && request.getCreditGrade() > 0) {
            Integer maxCreditGrade = product.getMaxCreditGrade();
            if (maxCreditGrade == null || request.getCreditGrade() <= maxCreditGrade) {
                score += 15;
                reasons.add("신용등급 조건을 충족합니다.");
            } else {
                score -= 20;
            }
        }

        if (request.getAge() != null && request.getAge() > 0) {
            Integer minAge = product.getMinAge();
            Integer maxAge = product.getMaxAge();
            boolean minOk = minAge == null || request.getAge() >= minAge;
            boolean maxOk = maxAge == null || request.getAge() <= maxAge;
            if (minOk && maxOk) {
                score += 10;
                reasons.add("연령 조건에 부합합니다.");
            } else {
                score -= 15;
            }
        }

        if (StringUtils.hasText(request.getRegion())) {
            if (matchesText(product.getRegion(), request.getRegion()) || matchesText(product.getRegion(), "전국")) {
                score += 10;
                reasons.add("거주 지역에서 신청 가능한 상품입니다.");
            } else {
                score -= 10;
            }
        }

        if (StringUtils.hasText(request.getPurpose())) {
            if (matchesText(product.getPurpose(), request.getPurpose()) || matchesText(product.getSummary(), request.getPurpose())) {
                score += 10;
                reasons.add("대출 용도와 관련성이 높습니다.");
            }
        }

        if (reasons.isEmpty()) {
            reasons.add("기본 조건으로 검토 가능한 상품입니다.");
        }

        return new LoanRecommendation(product, Math.max(0, Math.min(score, 100)), reasons);
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
