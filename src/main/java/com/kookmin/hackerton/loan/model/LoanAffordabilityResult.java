package com.kookmin.hackerton.loan.model;

import java.util.ArrayList;
import java.util.List;

public class LoanAffordabilityResult {

    private double dsr;
    private double dti;
    private double ltv;

    private double dsrLimit;
    private double dtiLimit;
    private double ltvLimit;

    private Long estimatedMonthlyPayment;
    private Long estimatedAnnualPayment;

    private Long maxLoanByDsr;
    private Long maxLoanByDti;
    private Long maxLoanByLtv;
    private Long finalPossibleLoanAmount;

    private boolean dsrPassed;
    private boolean dtiPassed;
    private boolean ltvPassed;
    private boolean mortgageEvaluationUsed;

    private Double calculationRate;
    private Integer calculationTermYears;
    private String calculationMethod;

    private List<String> warnings = new ArrayList<String>();

    public double getDsr() {
        return dsr;
    }

    public void setDsr(double dsr) {
        this.dsr = dsr;
    }

    public double getDti() {
        return dti;
    }

    public void setDti(double dti) {
        this.dti = dti;
    }

    public double getLtv() {
        return ltv;
    }

    public void setLtv(double ltv) {
        this.ltv = ltv;
    }

    public double getDsrLimit() {
        return dsrLimit;
    }

    public void setDsrLimit(double dsrLimit) {
        this.dsrLimit = dsrLimit;
    }

    public double getDtiLimit() {
        return dtiLimit;
    }

    public void setDtiLimit(double dtiLimit) {
        this.dtiLimit = dtiLimit;
    }

    public double getLtvLimit() {
        return ltvLimit;
    }

    public void setLtvLimit(double ltvLimit) {
        this.ltvLimit = ltvLimit;
    }

    public Long getEstimatedMonthlyPayment() {
        return estimatedMonthlyPayment;
    }

    public void setEstimatedMonthlyPayment(Long estimatedMonthlyPayment) {
        this.estimatedMonthlyPayment = estimatedMonthlyPayment;
    }

    public Long getEstimatedAnnualPayment() {
        return estimatedAnnualPayment;
    }

    public void setEstimatedAnnualPayment(Long estimatedAnnualPayment) {
        this.estimatedAnnualPayment = estimatedAnnualPayment;
    }

    public Long getMaxLoanByDsr() {
        return maxLoanByDsr;
    }

    public void setMaxLoanByDsr(Long maxLoanByDsr) {
        this.maxLoanByDsr = maxLoanByDsr;
    }

    public Long getMaxLoanByDti() {
        return maxLoanByDti;
    }

    public void setMaxLoanByDti(Long maxLoanByDti) {
        this.maxLoanByDti = maxLoanByDti;
    }

    public Long getMaxLoanByLtv() {
        return maxLoanByLtv;
    }

    public void setMaxLoanByLtv(Long maxLoanByLtv) {
        this.maxLoanByLtv = maxLoanByLtv;
    }

    public Long getFinalPossibleLoanAmount() {
        return finalPossibleLoanAmount;
    }

    public void setFinalPossibleLoanAmount(Long finalPossibleLoanAmount) {
        this.finalPossibleLoanAmount = finalPossibleLoanAmount;
    }

    public boolean isDsrPassed() {
        return dsrPassed;
    }

    public void setDsrPassed(boolean dsrPassed) {
        this.dsrPassed = dsrPassed;
    }

    public boolean isDtiPassed() {
        return dtiPassed;
    }

    public void setDtiPassed(boolean dtiPassed) {
        this.dtiPassed = dtiPassed;
    }

    public boolean isLtvPassed() {
        return ltvPassed;
    }

    public void setLtvPassed(boolean ltvPassed) {
        this.ltvPassed = ltvPassed;
    }

    public boolean isMortgageEvaluationUsed() {
        return mortgageEvaluationUsed;
    }

    public void setMortgageEvaluationUsed(boolean mortgageEvaluationUsed) {
        this.mortgageEvaluationUsed = mortgageEvaluationUsed;
    }

    public Double getCalculationRate() {
        return calculationRate;
    }

    public void setCalculationRate(Double calculationRate) {
        this.calculationRate = calculationRate;
    }

    public Integer getCalculationTermYears() {
        return calculationTermYears;
    }

    public void setCalculationTermYears(Integer calculationTermYears) {
        this.calculationTermYears = calculationTermYears;
    }

    public String getCalculationMethod() {
        return calculationMethod;
    }

    public void setCalculationMethod(String calculationMethod) {
        this.calculationMethod = calculationMethod;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}