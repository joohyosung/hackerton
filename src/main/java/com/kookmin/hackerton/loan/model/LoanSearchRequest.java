package com.kookmin.hackerton.loan.model;

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

    @NotNull(message = "희망 대출금액은 필수입니다.")
    @Min(value = 0, message = "희망 대출금액은 0 이상이어야 합니다.")
    private Long loanAmount;

    @NotBlank(message = "거주 지역은 필수입니다.")
    private String region;

    @NotBlank(message = "대출 용도는 필수입니다.")
    private String purpose;

    @Min(value = 1, message = "신용등급은 1등급 이상이어야 합니다.")
    @Max(value = 10, message = "신용등급은 10등급 이하여야 합니다.")
    private Integer creditGrade;

    private String userType;

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

    public Integer getCreditGrade() {
        return creditGrade;
    }

    public void setCreditGrade(Integer creditGrade) {
        this.creditGrade = creditGrade;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
