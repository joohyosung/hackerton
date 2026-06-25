package com.kookmin.hackerton.loan.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.kookmin.hackerton.loan.model.LoanProduct;
import org.junit.jupiter.api.Test;

class LoanProductAnalyzerTest {

    private final LoanProductAnalyzer analyzer = new LoanProductAnalyzer();

    @Test
    void parseKoreanMoney_parsesEok() {
        Long result = analyzer.parseKoreanMoney("최대 3억원");

        assertThat(result).isEqualTo(300_000_000L);
    }

    @Test
    void parseKoreanMoney_parsesManwon() {
        Long result = analyzer.parseKoreanMoney("최대 5000만원");

        assertThat(result).isEqualTo(50_000_000L);
    }

    @Test
    void parseKoreanMoney_returnsLargestAmount() {
        Long result = analyzer.parseKoreanMoney("최소 100만원 최대 3000만원");

        assertThat(result).isEqualTo(30_000_000L);
    }

    @Test
    void parseMaxRate_returnsLargestRate() {
        Double result = analyzer.parseMaxRate("연 3.5% ~ 7.2%");

        assertThat(result).isEqualTo(7.2);
    }

    @Test
    void resolveCalculationRate_usesProductRateFirst() {
        LoanProduct product = new LoanProduct();
        product.setRateText("연 4.5%");

        Double result = analyzer.resolveCalculationRate(product, 9.0);

        assertThat(result).isEqualTo(4.5);
    }

    @Test
    void resolveCalculationRate_usesFallbackRateWhenProductRateMissing() {
        LoanProduct product = new LoanProduct();

        Double result = analyzer.resolveCalculationRate(product, 6.0);

        assertThat(result).isEqualTo(6.0);
    }

    @Test
    void resolveCalculationRate_returnsDefaultRateWhenNoRate() {
        LoanProduct product = new LoanProduct();

        Double result = analyzer.resolveCalculationRate(product, null);

        assertThat(result).isEqualTo(5.0);
    }

    @Test
    void resolveLimitAmount_usesLimitAmountFirst() {
        LoanProduct product = new LoanProduct();
        product.setLimitAmount(20_000_000L);
        product.setLimitText("최대 5000만원");

        Long result = analyzer.resolveLimitAmount(product);

        assertThat(result).isEqualTo(20_000_000L);
    }

    @Test
    void resolveLimitAmount_parsesLimitText() {
        LoanProduct product = new LoanProduct();
        product.setLimitText("최대 7000만원");

        Long result = analyzer.resolveLimitAmount(product);

        assertThat(result).isEqualTo(70_000_000L);
    }

    @Test
    void matchesAgeBand_returnsTrueWhenAgeIsInRange() {
        LoanProduct product = new LoanProduct();
        product.setMinAge(19);
        product.setMaxAge(34);

        boolean result = analyzer.matchesAgeBand(product, 30);

        assertThat(result).isTrue();
    }

    @Test
    void matchesAgeBand_returnsFalseWhenAgeIsOutOfRange() {
        LoanProduct product = new LoanProduct();
        product.setMinAge(19);
        product.setMaxAge(34);

        boolean result = analyzer.matchesAgeBand(product, 40);

        assertThat(result).isFalse();
    }

    @Test
    void matchesIncome_returnsTrueWhenIncomeIsBelowLimit() {
        LoanProduct product = new LoanProduct();
        product.setMaxAnnualIncome(40_000_000L);

        boolean result = analyzer.matchesIncome(product, 30_000_000L);

        assertThat(result).isTrue();
    }

    @Test
    void matchesIncome_returnsFalseWhenIncomeExceedsLimit() {
        LoanProduct product = new LoanProduct();
        product.setMaxAnnualIncome(40_000_000L);

        boolean result = analyzer.matchesIncome(product, 50_000_000L);

        assertThat(result).isFalse();
    }

    @Test
    void matchesCreditGrade_returnsTrueWhenGradeIsAllowed() {
        LoanProduct product = new LoanProduct();
        product.setMaxCreditGrade(6);

        boolean result = analyzer.matchesCreditGrade(product, 5);

        assertThat(result).isTrue();
    }

    @Test
    void matchesCreditGrade_returnsFalseWhenGradeIsHigherThanLimit() {
        LoanProduct product = new LoanProduct();
        product.setMaxCreditGrade(6);

        boolean result = analyzer.matchesCreditGrade(product, 7);

        assertThat(result).isFalse();
    }

    @Test
    void matchesHouseCount_returnsTrueForNoHouseCondition() {
        LoanProduct product = new LoanProduct();
        product.setHouseHoldCount("무주택");

        boolean result = analyzer.matchesHouseCount(product, 0);

        assertThat(result).isTrue();
    }

    @Test
    void matchesHouseCount_returnsFalseForNoHouseCondition() {
        LoanProduct product = new LoanProduct();
        product.setHouseHoldCount("무주택");

        boolean result = analyzer.matchesHouseCount(product, 1);

        assertThat(result).isFalse();
    }

    @Test
    void matchesHouseArea_returnsTrueWhenAreaIsBelowLimit() {
        LoanProduct product = new LoanProduct();
        product.setHouseArea("85㎡ 이하");

        boolean result = analyzer.matchesHouseArea(product, 70.0);

        assertThat(result).isTrue();
    }

    @Test
    void matchesHouseArea_returnsFalseWhenAreaExceedsLimit() {
        LoanProduct product = new LoanProduct();
        product.setHouseArea("85㎡ 이하");

        boolean result = analyzer.matchesHouseArea(product, 100.0);

        assertThat(result).isFalse();
    }

    @Test
    void isMortgageLikeProduct_returnsTrueForHousingLoan() {
        LoanProduct product = new LoanProduct();
        product.setPurpose("주거");
        product.setName("전세자금대출");

        boolean result = analyzer.isMortgageLikeProduct(product);

        assertThat(result).isTrue();
    }

    @Test
    void isMortgageLikeProduct_returnsFalseForLivingExpenseLoan() {
        LoanProduct product = new LoanProduct();
        product.setPurpose("생계");
        product.setName("긴급생활자금");

        boolean result = analyzer.isMortgageLikeProduct(product);

        assertThat(result).isFalse();
    }
}