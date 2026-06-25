package com.kookmin.hackerton.loan.service;

import com.kookmin.hackerton.loan.model.LoanProduct;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Component
public class LoanProductXmlParser {

    public List<LoanProduct> parseProducts(String xml) throws Exception {
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
}