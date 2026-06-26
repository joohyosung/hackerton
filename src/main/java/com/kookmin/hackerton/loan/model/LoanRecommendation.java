package com.kookmin.hackerton.loan.model;

import java.util.ArrayList;
import java.util.List;

public class LoanRecommendation {

    private LoanProduct product;
    private int score;
    private boolean eligible = true;
    private List<String> reasons = new ArrayList<String>();
    private List<String> warnings = new ArrayList<String>();
    private LoanAffordabilityResult affordability;

    public LoanRecommendation() {
    }

    public LoanRecommendation(LoanProduct product, int score, List<String> reasons) {
        this.product = product;
        this.score = score;
        this.reasons = reasons;
    }

    public LoanRecommendation(
            LoanProduct product,
            int score,
            boolean eligible,
            List<String> reasons,
            List<String> warnings,
            LoanAffordabilityResult affordability
    ) {
        this.product = product;
        this.score = score;
        this.eligible = eligible;
        this.reasons = reasons;
        this.warnings = warnings;
        this.affordability = affordability;
    }

    public LoanProduct getProduct() {
        return product;
    }

    public void setProduct(LoanProduct product) {
        this.product = product;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public LoanAffordabilityResult getAffordability() {
        return affordability;
    }

    public void setAffordability(LoanAffordabilityResult affordability) {
        this.affordability = affordability;
    }
}
