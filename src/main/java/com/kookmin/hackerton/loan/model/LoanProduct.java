package com.kookmin.hackerton.loan.model;

public class LoanProduct {

    private String id;
    private String name;
    private String institution;
    private String limitText;
    private Long limitAmount;
    private String rateType;
    private String rateText;
    private String purpose;
    private String periodText;
    private String target;
    private String region;
    private String summary;
    private String sourceUrl;
    private Integer minAge;
    private Integer maxAge;
    private Long maxAnnualIncome;
    private Integer maxCreditGrade;

    private String annualIncomeText;
    private String incomeText;
    private String incomeCondition;
    private String incomeConditionYes;
    private String incomeConditionNone;

    private String ageText;
    private String age39Below;
    private String age40Above;
    private String age60Above;

    private String creditGradeText;
    private String creditGrade1;
    private String creditGrade2;
    private String creditGrade3;
    private String creditGrade4;
    private String creditGrade5;
    private String creditGrade6;
    private String creditGrade7;
    private String creditGrade8;
    private String creditGrade9;
    private String creditGradeNoGrade;
    private String creditGrade1To5;
    private String creditGrade6ToNoGrade;

    private String loanTargetHouse;
    private String houseArea;
    private String houseHoldCount;

    private String productCategory;
    private String institutionCategory;
    private String applicationMethod;
    private String contact;
    private String relatedSite;
    private String guaranteeInstitution;
    private String extraNotes;
    private String preferentialRateCondition;
    private String overdueInterestRate;
    private String earlyRepaymentFee;
    private String loanAdditionalCost;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getLimitText() {
        return limitText;
    }

    public void setLimitText(String limitText) {
        this.limitText = limitText;
    }

    public Long getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(Long limitAmount) {
        this.limitAmount = limitAmount;
    }

    public String getRateType() {
        return rateType;
    }

    public void setRateType(String rateType) {
        this.rateType = rateType;
    }

    public String getRateText() {
        return rateText;
    }

    public void setRateText(String rateText) {
        this.rateText = rateText;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getPeriodText() {
        return periodText;
    }

    public void setPeriodText(String periodText) {
        this.periodText = periodText;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public Long getMaxAnnualIncome() {
        return maxAnnualIncome;
    }

    public void setMaxAnnualIncome(Long maxAnnualIncome) {
        this.maxAnnualIncome = maxAnnualIncome;
    }

    public Integer getMaxCreditGrade() {
        return maxCreditGrade;
    }

    public void setMaxCreditGrade(Integer maxCreditGrade) {
        this.maxCreditGrade = maxCreditGrade;
    }

    public String getCreditGradeFlag(int grade) {
        switch (grade) {
            case 1:
                return creditGrade1;
            case 2:
                return creditGrade2;
            case 3:
                return creditGrade3;
            case 4:
                return creditGrade4;
            case 5:
                return creditGrade5;
            case 6:
                return creditGrade6;
            case 7:
                return creditGrade7;
            case 8:
                return creditGrade8;
            case 9:
                return creditGrade9;
            default:
                return null;
        }
    }
}
