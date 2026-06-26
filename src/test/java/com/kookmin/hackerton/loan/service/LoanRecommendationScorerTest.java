package com.kookmin.hackerton.loan.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.kookmin.hackerton.loan.model.LoanProduct;
import com.kookmin.hackerton.loan.model.LoanRecommendation;
import com.kookmin.hackerton.loan.model.LoanSearchRequest;
import org.junit.jupiter.api.Test;

class LoanRecommendationScorerTest {

    private final LoanProductAnalyzer analyzer = new LoanProductAnalyzer();
    private final LoanRecommendationScorer scorer = new LoanRecommendationScorer(analyzer);

    @Test
    void score_excludesProductWhenLoanAmountExceedsLimit() {
        LoanProduct product = new LoanProduct();
        product.setName("테스트 상품");
        product.setLimitText("1000");

        LoanSearchRequest request = basicRequest();
        request.setLoanAmount(20_000_000L);

        LoanRecommendation result = scorer.score(product, request);

        assertThat(result.isEligible()).isFalse();
    }

    @Test
    void score_givesHighScoreWhenPurposeAndRegionMatch() {
        LoanProduct product = new LoanProduct();
        product.setName("생계 대출");
        product.setLimitText("3000");
        product.setPurpose("생계");
        product.setRegion("전국");
        product.setRateText("3");

        LoanSearchRequest request = basicRequest();

        LoanRecommendation result = scorer.score(product, request);

        assertThat(result.isEligible()).isTrue();
        assertThat(result.getScore()).isGreaterThanOrEqualTo(70);
    }

    @Test
    void score_excludesLocalGovernmentProductWhenRegionDoesNotMatch() {
        LoanProduct product = new LoanProduct();
        product.setName("지역 대출");
        product.setLimitText("3000");
        product.setPurpose("생계");
        product.setRegion("강원");
        product.setInstitutionCategory("지자체");

        LoanSearchRequest request = basicRequest();
        request.setRegion("서울");

        LoanRecommendation result = scorer.score(product, request);

        assertThat(result.isEligible()).isFalse();
    }

    private LoanSearchRequest basicRequest() {
        LoanSearchRequest request = new LoanSearchRequest();
        request.setAge(30);
        request.setAnnualIncome(30_000_000L);
        request.setLoanAmount(10_000_000L);
        request.setRegion("서울");
        request.setPurpose("생계");
        return request;
    }
}
