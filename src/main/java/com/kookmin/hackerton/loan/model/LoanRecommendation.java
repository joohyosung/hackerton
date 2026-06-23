package com.kookmin.hackerton.loan.model;

import java.util.ArrayList;
import java.util.List;

public class LoanRecommendation {

    private LoanProduct product;
    private int score;
    private List<String> reasons = new ArrayList<String>();

    public LoanRecommendation() {
    }

    public LoanRecommendation(LoanProduct product, int score, List<String> reasons) {
        this.product = product;
        this.score = score;
        this.reasons = reasons;
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

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }
}
