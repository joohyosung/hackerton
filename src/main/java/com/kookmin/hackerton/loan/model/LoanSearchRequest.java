package com.kookmin.hackerton.loan.model;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class LoanSearchRequest {

    @NotNull(message = "연령은 필수입니다.")
    @Min(value = 19, message = "연령은 19세 이상이어야 합니다.")
    @Max(value = 100, message = "연령은 100세 이하여야 합니다.")
    private Integer age;

    @NotNull(message = "연소득은 필수입니다.")
    @Min(value = 0, message = "연소득은 0 이상이어야 합니다.")
    private Long annualIncome;

    @NotNull(message = "신용등급은 필수입니다.")
    @Min(value = 1, message = "신용등급은 1등급 이상이어야 합니다.")
    @Max(value = 10, message = "신용등급은 10등급 이하여야 합니다.")
    private Integer creditGrade;

    @NotNull(message = "희망 대출금액은 필수입니다.")
    @Min(value = 0, message = "희망 대출금액은 0 이상이어야 합니다.")
    private Long loanAmount;

    @NotBlank(message = "거주 지역은 필수입니다.")
    private String region;

    @NotBlank(message = "대출 용도는 필수입니다.")
    private String purpose;

    @Min(value = 0, message = "기존 대출 월 상환액은 0 이상이어야 합니다.")
    private Long existingMonthlyDebtPayment;

    @Min(value = 0, message = "기존 기타대출 연간 이자액은 0 이상이어야 합니다.")
    private Long existingAnnualDebtInterest;

    @Min(value = 1, message = "상환기간은 1년 이상이어야 합니다.")
    @Max(value = 40, message = "상환기간은 40년 이하여야 합니다.")
    private Integer desiredLoanTermYears;

    @DecimalMin(value = "0.0", message = "예상 금리는 0 이상이어야 합니다.")
    @DecimalMax(value = "30.0", message = "예상 금리는 30 이하이어야 합니다.")
    private Double expectedInterestRate;

    private Boolean mortgageLoan;

    @Min(value = 0, message = "담보가치는 0 이상이어야 합니다.")
    private Long collateralValue;

    @Min(value = 0, message = "기존 주담대 잔액은 0 이상이어야 합니다.")
    private Long existingMortgageBalance;

    @Min(value = 0, message = "선순위 보증금은 0 이상이어야 합니다.")
    private Long seniorDeposit;

    @Min(value = 0, message = "주택 보유 수는 0 이상이어야 합니다.")
    @Max(value = 2, message = "주택 보유 수는 2 이하이어야 합니다.")
    private Integer houseCount;

    @DecimalMin(value = "0.0", message = "주택 면적은 0 이상이어야 합니다.")
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