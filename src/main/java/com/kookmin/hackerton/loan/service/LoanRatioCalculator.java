package com.kookmin.hackerton.loan.service;

import org.springframework.stereotype.Service;

@Service
public class LoanRatioCalculator {

    public long calculateMonthlyPayment(long principal, double annualRate, int years) {
        if (principal <= 0 || years <= 0) {
            return 0L;
        }

        int months = years * 12;
        double monthlyRate = annualRate / 100.0 / 12.0;

        if (monthlyRate <= 0) {
            return Math.round((double) principal / months);
        }

        double payment = principal * monthlyRate * Math.pow(1 + monthlyRate, months)
                / (Math.pow(1 + monthlyRate, months) - 1);

        return Math.round(payment);
    }

    public double calculateDsr(long annualIncome, long existingAnnualDebtPayment, long newAnnualDebtPayment) {
        if (annualIncome <= 0) {
            return 999.0;
        }

        return (existingAnnualDebtPayment + newAnnualDebtPayment) * 100.0 / annualIncome;
    }

    public double calculateDti(long annualIncome, long newAnnualMortgagePayment, long existingAnnualDebtInterest) {
        if (annualIncome <= 0) {
            return 999.0;
        }

        return (newAnnualMortgagePayment + existingAnnualDebtInterest) * 100.0 / annualIncome;
    }

    public double calculateLtv(
            long requestedLoanAmount,
            long collateralValue,
            long existingMortgageBalance,
            long seniorDeposit
    ) {
        if (collateralValue <= 0) {
            return 999.0;
        }

        long totalSecuredAmount = requestedLoanAmount + existingMortgageBalance + seniorDeposit;
        return totalSecuredAmount * 100.0 / collateralValue;
    }

    public long calculateMaxLoanByDsr(
            long annualIncome,
            long existingAnnualDebtPayment,
            double dsrLimit,
            double annualRate,
            int years
    ) {
        long allowedAnnualPayment = Math.round(annualIncome * dsrLimit / 100.0) - existingAnnualDebtPayment;

        if (allowedAnnualPayment <= 0) {
            return 0L;
        }

        return reversePrincipalFromAnnualPayment(allowedAnnualPayment, annualRate, years);
    }

    public long calculateMaxLoanByDti(
            long annualIncome,
            long existingAnnualDebtInterest,
            double dtiLimit,
            double annualRate,
            int years
    ) {
        long allowedAnnualPayment = Math.round(annualIncome * dtiLimit / 100.0) - existingAnnualDebtInterest;

        if (allowedAnnualPayment <= 0) {
            return 0L;
        }

        return reversePrincipalFromAnnualPayment(allowedAnnualPayment, annualRate, years);
    }

    public long calculateMaxLoanByLtv(
            long collateralValue,
            long existingMortgageBalance,
            long seniorDeposit,
            double ltvLimit
    ) {
        if (collateralValue <= 0) {
            return 0L;
        }

        long maxSecuredAmount = Math.round(collateralValue * ltvLimit / 100.0);
        long available = maxSecuredAmount - existingMortgageBalance - seniorDeposit;

        return Math.max(0L, available);
    }

    private long reversePrincipalFromAnnualPayment(long annualPayment, double annualRate, int years) {
        if (annualPayment <= 0 || years <= 0) {
            return 0L;
        }

        long monthlyPayment = annualPayment / 12;
        int months = years * 12;
        double monthlyRate = annualRate / 100.0 / 12.0;

        if (monthlyRate <= 0) {
            return monthlyPayment * months;
        }

        double principal = monthlyPayment
                * (Math.pow(1 + monthlyRate, months) - 1)
                / (monthlyRate * Math.pow(1 + monthlyRate, months));

        return Math.round(principal);
    }
}