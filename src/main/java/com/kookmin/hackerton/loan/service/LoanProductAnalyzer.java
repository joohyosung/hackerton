package com.kookmin.hackerton.loan.service;

import com.kookmin.hackerton.loan.model.LoanProduct;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class LoanProductAnalyzer {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)");

    public Long resolveLimitAmount(LoanProduct product) {
        if (product == null) {
            return null;
        }

        if (product.getLimitAmount() != null && product.getLimitAmount() > 0) {
            return product.getLimitAmount();
        }

        Long parsed = parseKoreanMoney(product.getLimitText());

        if (parsed != null && parsed > 0) {
            return parsed;
        }

        return null;
    }

    public boolean isMortgageLikeProduct(LoanProduct product) {
        if (product == null) {
            return false;
        }

        String text = join(
                product.getPurpose(),
                product.getProductCategory(),
                product.getLoanTargetHouse(),
                product.getHouseArea(),
                product.getHouseHoldCount(),
                product.getName(),
                product.getSummary()
        );

        return containsAny(text, "주거", "주택", "전세", "월세", "보증금", "담보", "버팀목");
    }

    public boolean matchesAgeBand(LoanProduct product, Integer age) {
        if (product == null || age == null || age <= 0) {
            return true;
        }

        if (age <= 39 && isYes(product.getAge39Below())) {
            return true;
        }

        if (age >= 40 && age < 60 && isYes(product.getAge40Above())) {
            return true;
        }

        if (age >= 60 && isYes(product.getAge60Above())) {
            return true;
        }

        Integer minAge = product.getMinAge();
        Integer maxAge = product.getMaxAge();

        boolean minOk = minAge == null || age >= minAge;
        boolean maxOk = maxAge == null || age <= maxAge;

        return minOk && maxOk;
    }

    public boolean matchesCreditGrade(LoanProduct product, Integer creditGrade) {
        if (product == null || creditGrade == null || creditGrade <= 0) {
            return true;
        }

        if (creditGrade >= 1 && creditGrade <= 9) {
            String exactFlag = product.getCreditGradeFlag(creditGrade);
            if (isYes(exactFlag)) {
                return true;
            }
        }

        if (creditGrade >= 1 && creditGrade <= 5 && isYes(product.getCreditGrade1To5())) {
            return true;
        }

        if (creditGrade >= 6 && isYes(product.getCreditGrade6ToNoGrade())) {
            return true;
        }

        Integer maxCreditGrade = product.getMaxCreditGrade();

        return maxCreditGrade == null || creditGrade <= maxCreditGrade;
    }

    public boolean matchesIncome(LoanProduct product, Long annualIncome) {
        if (product == null || annualIncome == null || annualIncome <= 0) {
            return true;
        }

        if (isYes(product.getIncomeConditionNone())) {
            return true;
        }

        Long maxIncome = product.getMaxAnnualIncome();

        if (maxIncome == null || maxIncome <= 0) {
            maxIncome = parseKoreanMoney(join(
                    product.getAnnualIncomeText(),
                    product.getIncomeText(),
                    product.getIncomeCondition()
            ));
        }

        return maxIncome == null || annualIncome <= maxIncome;
    }

    public Long parseKoreanMoney(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }

        String normalized = text.replace(",", "").replace(" ", "");
        if (normalized.matches("\\d+")) {
            return Long.parseLong(normalized) * 10000L;
        }

        Matcher matcher = NUMBER_PATTERN.matcher(normalized);
        Long max = null;

        while (matcher.find()) {
            double number = Double.parseDouble(matcher.group(1));
            long amount;

            int end = matcher.end();
            String tail = normalized.substring(end);

            if (tail.startsWith("억원") || tail.startsWith("억")) {
                amount = Math.round(number * 100000000L);
            } else if (tail.startsWith("천만원")) {
                amount = Math.round(number * 10000000L);
            } else if (tail.startsWith("만원") || tail.startsWith("만")) {
                amount = Math.round(number * 10000L);
            } else {
                amount = Math.round(number);
            }

            if (max == null || amount > max) {
                max = amount;
            }
        }

        return max;
    }

    public Double parseMaxRate(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }

        Matcher matcher = NUMBER_PATTERN.matcher(text);
        Double max = null;

        while (matcher.find()) {
            double value = Double.parseDouble(matcher.group(1));
            if (max == null || value > max) {
                max = value;
            }
        }

        return max;
    }

    private boolean containsAny(String text, String... keywords) {
        if (!StringUtils.hasText(text)) {
            return false;
        }

        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private boolean isYes(String value) {
        return "Y".equalsIgnoreCase(value)
                || "예".equals(value)
                || "true".equalsIgnoreCase(value)
                || "1".equals(value)
                || "가능".equals(value)
                || "대상".equals(value)
                || "해당".equals(value)
                || "○".equals(value)
                || "o".equalsIgnoreCase(value);
    }

    private String join(String... values) {
        StringBuilder builder = new StringBuilder();

        for (String value : values) {
            if (StringUtils.hasText(value)) {
                builder.append(value).append(" ");
            }
        }

        return builder.toString();
    }
}
