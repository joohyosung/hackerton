package com.kookmin.hackerton.loan.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LoanRatioCalculatorTest {

    private final LoanRatioCalculator calculator = new LoanRatioCalculator();

    @Test
    void calculateMonthlyPayment_returnsMonthlyRepayment() {
        long result = calculator.calculateMonthlyPayment(
                10_000_000L,
                5.0,
                5
        );

        assertThat(result).isGreaterThan(0L);
    }

    @Test
    void calculateMonthlyPayment_returnsZeroWhenPrincipalIsZero() {
        long result = calculator.calculateMonthlyPayment(
                0L,
                5.0,
                5
        );

        assertThat(result).isEqualTo(0L);
    }

    @Test
    void calculateDsr_returnsDebtServiceRatio() {
        double result = calculator.calculateDsr(
                50_000_000L,
                6_000_000L,
                9_000_000L
        );

        assertThat(result).isEqualTo(30.0);
    }

    @Test
    void calculateDsr_returns999WhenIncomeIsZero() {
        double result = calculator.calculateDsr(
                0L,
                6_000_000L,
                9_000_000L
        );

        assertThat(result).isEqualTo(999.0);
    }

    @Test
    void calculateDti_returnsDebtToIncomeRatio() {
        double result = calculator.calculateDti(
                50_000_000L,
                8_000_000L,
                2_000_000L
        );

        assertThat(result).isEqualTo(20.0);
    }

    @Test
    void calculateLtv_returnsLoanToValueRatio() {
        double result = calculator.calculateLtv(
                70_000_000L,
                100_000_000L,
                0L,
                0L
        );

        assertThat(result).isEqualTo(70.0);
    }

    @Test
    void calculateLtv_includesExistingMortgageAndSeniorDeposit() {
        double result = calculator.calculateLtv(
                50_000_000L,
                100_000_000L,
                10_000_000L,
                10_000_000L
        );

        assertThat(result).isEqualTo(70.0);
    }

    @Test
    void calculateMaxLoanByLtv_returnsAvailableLoanAmount() {
        long result = calculator.calculateMaxLoanByLtv(
                100_000_000L,
                10_000_000L,
                5_000_000L,
                70.0
        );

        assertThat(result).isEqualTo(55_000_000L);
    }
}