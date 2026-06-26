package com.kookmin.hackerton.loan.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.kookmin.hackerton.loan.model.LoanProduct;
import org.junit.jupiter.api.Test;

class LoanProductAnalyzerTest {

    private final LoanProductAnalyzer analyzer = new LoanProductAnalyzer();

    @Test
    void parseKoreanMoney_parsesNumericPublicDataLimitAsManwon() {
        Long result = analyzer.parseKoreanMoney("5000");

        assertThat(result).isEqualTo(50_000_000L);
    }

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
    void parseMaxRate_returnsLargestRate() {
        Double result = analyzer.parseMaxRate("연 3.5% ~ 7.2%");

        assertThat(result).isEqualTo(7.2);
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
    void matchesAgeBand_returnsFalseWhenAgeIsOutOfRange() {
        LoanProduct product = new LoanProduct();
        product.setMinAge(19);
        product.setMaxAge(34);

        boolean result = analyzer.matchesAgeBand(product, 40);

        assertThat(result).isFalse();
    }

    @Test
    void matchesIncome_returnsFalseWhenIncomeExceedsLimit() {
        LoanProduct product = new LoanProduct();
        product.setMaxAnnualIncome(40_000_000L);

        boolean result = analyzer.matchesIncome(product, 50_000_000L);

        assertThat(result).isFalse();
    }

    @Test
    void matchesCreditGrade_returnsFalseWhenGradeIsHigherThanLimit() {
        LoanProduct product = new LoanProduct();
        product.setMaxCreditGrade(6);

        boolean result = analyzer.matchesCreditGrade(product, 7);

        assertThat(result).isFalse();
    }
}
