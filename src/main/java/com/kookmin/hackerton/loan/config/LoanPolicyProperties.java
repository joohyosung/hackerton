package com.kookmin.hackerton.loan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loan.policy")
public class LoanPolicyProperties {

    private double dsrLimit = 40.0;
    private double dtiLimit = 40.0;
    private double ltvLimit = 70.0;
    private double firstHomeBuyerLtvLimit = 80.0;
    private double stressRateAddition = 0.0;
    private int minimumRecommendationScore = 35;

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

    public double getFirstHomeBuyerLtvLimit() {
        return firstHomeBuyerLtvLimit;
    }

    public void setFirstHomeBuyerLtvLimit(double firstHomeBuyerLtvLimit) {
        this.firstHomeBuyerLtvLimit = firstHomeBuyerLtvLimit;
    }

    public double getStressRateAddition() {
        return stressRateAddition;
    }

    public void setStressRateAddition(double stressRateAddition) {
        this.stressRateAddition = stressRateAddition;
    }

    public int getMinimumRecommendationScore() {
        return minimumRecommendationScore;
    }

    public void setMinimumRecommendationScore(int minimumRecommendationScore) {
        this.minimumRecommendationScore = minimumRecommendationScore;
    }
}