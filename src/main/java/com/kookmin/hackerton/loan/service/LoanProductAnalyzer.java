package com.kookmin.hackerton.loan.service;

import com.kookmin.hackerton.loan.model.LoanProduct;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public Double resolveCalculationRate(LoanProduct product, Double fallbackRate) {
        Double productRate = parseMaxRate(product == null ? null : product.getRateText());

        if (productRate != null && productRate > 0) {
            return productRate;
        }

        if (fallbackRate != null && fallbackRate > 0) {
            return fallbackRate;
        }

        return 5.0;
    }

    public Integer resolveCalculationTermYears(LoanProduct product, Integer fallbackYears) {
        if (product != null) {
            Integer repaymentTerm = parseYears(product.getRepaymentTermYears());
            if (repaymentTerm != null && repaymentTerm > 0) {
                return repaymentTerm;
            }

            Integer totalTerm = parseYears(product.getTotalLoanTermYears());
            if (totalTerm != null && totalTerm > 0) {
                return totalTerm;
            }

            Integer periodTextYears = parseYears(product.getPeriodText());
            if (periodTextYears != null && periodTextYears > 0) {
                return periodTextYears;
            }
        }

        if (fallbackYears != null && fallbackYears > 0) {
            return fallbackYears;
        }

        return 5;
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
            maxIncome = parseKoreanMoney(join(product.getAnnualIncomeText(), product.getIncomeText(), product.getIncomeCondition()));
        }

        return maxIncome == null || annualIncome <= maxIncome;
    }

    public boolean matchesHouseCount(LoanProduct product, Integer houseCount) {
        if (product == null || houseCount == null) {
            return true;
        }

        String condition = product.getHouseHoldCount();

        if (!StringUtils.hasText(condition)) {
            return true;
        }

        if (condition.contains("2주택")) {
            return houseCount <= 2;
        }

        if (condition.contains("1주택")) {
            return houseCount <= 1;
        }

        if (condition.contains("무주택")) {
            return houseCount == 0;
        }

        return true;
    }

    public boolean matchesHouseArea(LoanProduct product, Double houseArea) {
        if (product == null || houseArea == null || houseArea <= 0) {
            return true;
        }

        String condition = product.getHouseArea();

        if (!StringUtils.hasText(condition)) {
            return true;
        }

        Double maxArea = parseMaxNumber(condition);

        return maxArea == null || houseArea <= maxArea;
    }

    public Long parseKoreanMoney(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }

        String normalized = text.replace(",", "").replace(" ", "");

        Matcher matcher = NUMBER_PATTERN.matcher(normalized);
        Long max = null;

        while (matcher.find()) {
            double number = Double.parseDouble(matcher.group(1));
            long amount;

            int end = matcher.end();
            String tail = normalized.substring(end);

            if (tail.startsWith("억원") || tail.startsWith("억")) {
                amount = Math.round(number * 100000000L);
            } else if (tail.startsWith("만원") || tail.startsWith("만")) {
                amount = Math.round(number * 10000L);
            } else if (tail.startsWith("천만원")) {
                amount = Math.round(number * 10000000L);
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

    public Integer parseYears(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }

        Matcher matcher = NUMBER_PATTERN.matcher(text);
        Integer max = null;

        while (matcher.find()) {
            int value = (int) Math.round(Double.parseDouble(matcher.group(1)));
            if (max == null || value > max) {
                max = value;
            }
        }

        return max;
    }

    private Double parseMaxNumber(String text) {
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
        return "Y".equalsIgnoreCase(value) || "예".equals(value) || "true".equalsIgnoreCase(value);
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
