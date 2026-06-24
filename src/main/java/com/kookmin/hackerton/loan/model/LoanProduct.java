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

    private String repaymentMethod;
    private String repaymentTermYears;  
    private String totalLoanTermYears;  

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

    public String getAnnualIncomeText() {
    return annualIncomeText;
    }

    public void setAnnualIncomeText(String annualIncomeText) {
        this.annualIncomeText = annualIncomeText;
    }

    public String getIncomeText() {
        return incomeText;
    }

    public void setIncomeText(String incomeText) {
        this.incomeText = incomeText;
    }

    public String getIncomeCondition() {
        return incomeCondition;
    }

    public void setIncomeCondition(String incomeCondition) {
        this.incomeCondition = incomeCondition;
    }

    public String getIncomeConditionYes() {
        return incomeConditionYes;
    }

    public void setIncomeConditionYes(String incomeConditionYes) {
        this.incomeConditionYes = incomeConditionYes;
    }

    public String getIncomeConditionNone() {
        return incomeConditionNone;
    }

    public void setIncomeConditionNone(String incomeConditionNone) {
        this.incomeConditionNone = incomeConditionNone;
    }

    public String getAgeText() {
        return ageText;
    }

    public void setAgeText(String ageText) {
        this.ageText = ageText;
    }

    public String getAge39Below() {
        return age39Below;
    }

    public void setAge39Below(String age39Below) {
        this.age39Below = age39Below;
    }

    public String getAge40Above() {
        return age40Above;
    }

    public void setAge40Above(String age40Above) {
        this.age40Above = age40Above;
    }

    public String getAge60Above() {
        return age60Above;
    }

    public void setAge60Above(String age60Above) {
        this.age60Above = age60Above;
    }

    public String getCreditGradeText() {
        return creditGradeText;
    }

    public void setCreditGradeText(String creditGradeText) {
        this.creditGradeText = creditGradeText;
    }

    public String getCreditGrade1() {
        return creditGrade1;
    }

    public void setCreditGrade1(String creditGrade1) {
        this.creditGrade1 = creditGrade1;
    }

    public String getCreditGrade2() {
        return creditGrade2;
    }

    public void setCreditGrade2(String creditGrade2) {
        this.creditGrade2 = creditGrade2;
    }

    public String getCreditGrade3() {
        return creditGrade3;
    }

    public void setCreditGrade3(String creditGrade3) {
        this.creditGrade3 = creditGrade3;
    }

    public String getCreditGrade4() {
        return creditGrade4;
    }

    public void setCreditGrade4(String creditGrade4) {
        this.creditGrade4 = creditGrade4;
    }

    public String getCreditGrade5() {
        return creditGrade5;
    }

    public void setCreditGrade5(String creditGrade5) {
        this.creditGrade5 = creditGrade5;
    }

    public String getCreditGrade6() {
        return creditGrade6;
    }

    public void setCreditGrade6(String creditGrade6) {
        this.creditGrade6 = creditGrade6;
    }

    public String getCreditGrade7() {
        return creditGrade7;
    }

    public void setCreditGrade7(String creditGrade7) {
        this.creditGrade7 = creditGrade7;
    }

    public String getCreditGrade8() {
        return creditGrade8;
    }

    public void setCreditGrade8(String creditGrade8) {
        this.creditGrade8 = creditGrade8;
    }

    public String getCreditGrade9() {
        return creditGrade9;
    }

    public void setCreditGrade9(String creditGrade9) {
        this.creditGrade9 = creditGrade9;
    }

    public String getCreditGradeNoGrade() {
        return creditGradeNoGrade;
    }

    public void setCreditGradeNoGrade(String creditGradeNoGrade) {
        this.creditGradeNoGrade = creditGradeNoGrade;
    }

    public String getCreditGrade1To5() {
        return creditGrade1To5;
    }

    public void setCreditGrade1To5(String creditGrade1To5) {
        this.creditGrade1To5 = creditGrade1To5;
    }

    public String getCreditGrade6ToNoGrade() {
        return creditGrade6ToNoGrade;
    }

    public void setCreditGrade6ToNoGrade(String creditGrade6ToNoGrade) {
        this.creditGrade6ToNoGrade = creditGrade6ToNoGrade;
    }

    public String getLoanTargetHouse() {
        return loanTargetHouse;
    }

    public void setLoanTargetHouse(String loanTargetHouse) {
        this.loanTargetHouse = loanTargetHouse;
    }

    public String getHouseArea() {
        return houseArea;
    }

    public void setHouseArea(String houseArea) {
        this.houseArea = houseArea;
    }

    public String getHouseHoldCount() {
        return houseHoldCount;
    }

    public void setHouseHoldCount(String houseHoldCount) {
        this.houseHoldCount = houseHoldCount;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getInstitutionCategory() {
        return institutionCategory;
    }

    public void setInstitutionCategory(String institutionCategory) {
        this.institutionCategory = institutionCategory;
    }

    public String getApplicationMethod() {
        return applicationMethod;
    }

    public void setApplicationMethod(String applicationMethod) {
        this.applicationMethod = applicationMethod;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getRelatedSite() {
        return relatedSite;
    }

    public void setRelatedSite(String relatedSite) {
        this.relatedSite = relatedSite;
    }

    public String getGuaranteeInstitution() {
        return guaranteeInstitution;
    }

    public void setGuaranteeInstitution(String guaranteeInstitution) {
        this.guaranteeInstitution = guaranteeInstitution;
    }

    public String getExtraNotes() {
        return extraNotes;
    }

    public void setExtraNotes(String extraNotes) {
        this.extraNotes = extraNotes;
    }

    public String getPreferentialRateCondition() {
        return preferentialRateCondition;
    }

    public void setPreferentialRateCondition(String preferentialRateCondition) {
        this.preferentialRateCondition = preferentialRateCondition;
    }

    public String getOverdueInterestRate() {
        return overdueInterestRate;
    }

    public void setOverdueInterestRate(String overdueInterestRate) {
        this.overdueInterestRate = overdueInterestRate;
    }

    public String getEarlyRepaymentFee() {
        return earlyRepaymentFee;
    }

    public void setEarlyRepaymentFee(String earlyRepaymentFee) {
        this.earlyRepaymentFee = earlyRepaymentFee;
    }

    public String getLoanAdditionalCost() {
        return loanAdditionalCost;
    }

    public void setLoanAdditionalCost(String loanAdditionalCost) {
        this.loanAdditionalCost = loanAdditionalCost;
    }

    public String getRepaymentMethod() {
        return repaymentMethod;
    }

    public void setRepaymentMethod(String repaymentMethod) {
        this.repaymentMethod = repaymentMethod;
    }

    public String getRepaymentTermYears() {
        return repaymentTermYears;
    }

    public void setRepaymentTermYears(String repaymentTermYears) {
        this.repaymentTermYears = repaymentTermYears;
    }

    public String getTotalLoanTermYears() {
        return totalLoanTermYears;
    }

    public void setTotalLoanTermYears(String totalLoanTermYears) {
        this.totalLoanTermYears = totalLoanTermYears;
    }
}
