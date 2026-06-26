package com.kookmin.hackerton.loan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loan.policy")
public class LoanPolicyProperties {

    private int minimumRecommendationScore = 45;

    public int getMinimumRecommendationScore() {
        return minimumRecommendationScore;
    }

    public void setMinimumRecommendationScore(int minimumRecommendationScore) {
        this.minimumRecommendationScore = minimumRecommendationScore;
    }
}
