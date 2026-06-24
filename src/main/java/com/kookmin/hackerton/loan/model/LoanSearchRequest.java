package com.kookmin.hackerton.loan.model;

public class LoanSearchRequest {

    private Integer age;
    private Long annualIncome;
    private Integer creditGrade;
    private Long loanAmount;
    private String region;
    private String purpose;

    private Long existingMonthlyDebtPayment;
    private Long existingAnnualDebtInterest;

    private Integer desiredLoanTermYears;
    private Double expectedInterestRate;

    private Boolean mortgageLoan;
    private Long collateralValue;
    private Long existingMortgageBalance;
    private Long seniorDeposit;

    private Integer houseCount;
    private Double houseArea;
    private Boolean firstHomeBuyer;

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Long getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(Long annualIncome) {
        this.annualIncome = annualIncome;
    }

    public Integer getCreditGrade() {
        return creditGrade;
    }

    public void setCreditGrade(Integer creditGrade) {
        this.creditGrade = creditGrade;
    }

    public Long getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(Long loanAmount) {
        this.loanAmount = loanAmount;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Long getExistingMonthlyDebtPayment() {
        return existingMonthlyDebtPayment;
    }

    public void setExistingMonthlyDebtPayment(Long existingMonthlyDebtPayment) {
        this.existingMonthlyDebtPayment = existingMonthlyDebtPayment;
    }

    public Long getExistingAnnualDebtInterest() {
        return existingAnnualDebtInterest;
    }

    public void setExistingAnnualDebtInterest(Long existingAnnualDebtInterest) {
        this.existingAnnualDebtInterest = existingAnnualDebtInterest;
    }

    public Integer getDesiredLoanTermYears() {
        return desiredLoanTermYears;
    }

    public void setDesiredLoanTermYears(Integer desiredLoanTermYears) {
        this.desiredLoanTermYears = desiredLoanTermYears;
    }

    public Double getExpectedInterestRate() {
        return expectedInterestRate;
    }

    public void setExpectedInterestRate(Double expectedInterestRate) {
        this.expectedInterestRate = expectedInterestRate;
    }

    public Boolean getMortgageLoan() {
        return mortgageLoan;
    }

    public void setMortgageLoan(Boolean mortgageLoan) {
        this.mortgageLoan = mortgageLoan;
    }

    public Long getCollateralValue() {
        return collateralValue;
    }

    public void setCollateralValue(Long collateralValue) {
        this.collateralValue = collateralValue;
    }

    public Long getExistingMortgageBalance() {
        return existingMortgageBalance;
    }

    public void setExistingMortgageBalance(Long existingMortgageBalance) {
        this.existingMortgageBalance = existingMortgageBalance;
    }

    public Long getSeniorDeposit() {
        return seniorDeposit;
    }

    public void setSeniorDeposit(Long seniorDeposit) {
        this.seniorDeposit = seniorDeposit;
    }

    public Integer getHouseCount() {
        return houseCount;
    }

    public void setHouseCount(Integer houseCount) {
        this.houseCount = houseCount;
    }

    public Double getHouseArea() {
        return houseArea;
    }

    public void setHouseArea(Double houseArea) {
        this.houseArea = houseArea;
    }

    public Boolean getFirstHomeBuyer() {
        return firstHomeBuyer;
    }

    public void setFirstHomeBuyer(Boolean firstHomeBuyer) {
        this.firstHomeBuyer = firstHomeBuyer;
    }
}