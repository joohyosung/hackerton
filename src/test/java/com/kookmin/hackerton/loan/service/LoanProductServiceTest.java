package com.kookmin.hackerton.loan.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.kookmin.hackerton.loan.config.LoanApiProperties;
import com.kookmin.hackerton.loan.config.LoanPolicyProperties;
import com.kookmin.hackerton.loan.model.LoanRecommendation;
import com.kookmin.hackerton.loan.model.LoanSearchRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

class LoanProductServiceTest {

    private LoanProductService createService() {
        LoanApiProperties apiProperties = new LoanApiProperties();
        apiProperties.setServiceKey("");
        apiProperties.setUseSampleWhenUnavailable(true);

        LoanPolicyProperties policyProperties = new LoanPolicyProperties();
        policyProperties.setDsrLimit(40.0);
        policyProperties.setDtiLimit(40.0);
        policyProperties.setLtvLimit(70.0);
        policyProperties.setFirstHomeBuyerLtvLimit(80.0);
        policyProperties.setStressRateAddition(0.0);
        policyProperties.setMinimumRecommendationScore(35);

        LoanProductAnalyzer productAnalyzer = new LoanProductAnalyzer();
        LoanRatioCalculator ratioCalculator = new LoanRatioCalculator();

        LoanProductXmlParser xmlParser = new LoanProductXmlParser();
        LoanApiClient loanApiClient = new LoanApiClient(apiProperties, xmlParser);

        LoanRecommendationScorer recommendationScorer = new LoanRecommendationScorer(
                policyProperties,
                ratioCalculator,
                productAnalyzer
        );

        return new LoanProductService(
                apiProperties,
                policyProperties,
                loanApiClient,
                new LoanSampleDataProvider(),
                recommendationScorer
        );
    }

    private LoanSearchRequest basicRequest() {
        LoanSearchRequest request = new LoanSearchRequest();
        request.setAge(32);
        request.setAnnualIncome(32_000_000L);
        request.setCreditGrade(5);
        request.setLoanAmount(10_000_000L);
        request.setRegion("전국");
        request.setPurpose("생계");
        request.setDesiredLoanTermYears(5);
        request.setExpectedInterestRate(5.0);
        request.setExistingMonthlyDebtPayment(0L);
        request.setExistingAnnualDebtInterest(0L);
        request.setMortgageLoan(false);
        request.setCollateralValue(0L);
        request.setExistingMortgageBalance(0L);
        request.setSeniorDeposit(0L);
        request.setHouseCount(0);
        request.setHouseArea(0.0);
        request.setFirstHomeBuyer(false);
        return request;
    }

    @Test
    void search_returnsRecommendationsWhenApiKeyMissing() {
        LoanProductService service = createService();

        List<LoanRecommendation> result = service.search(basicRequest());

        assertThat(result).isNotEmpty();
    }

    @Test
    void search_returnsResultsSortedByScoreDescending() {
        LoanProductService service = createService();

        List<LoanRecommendation> result = service.search(basicRequest());

        assertThat(result).isNotEmpty();

        for (int i = 1; i < result.size(); i++) {
            assertThat(result.get(i - 1).getScore())
                    .isGreaterThanOrEqualTo(result.get(i).getScore());
        }
    }

    @Test
    void search_excludesRecommendationWhenDsrExceeded() {
        LoanProductService service = createService();

        LoanSearchRequest request = basicRequest();
        request.setAnnualIncome(10_000_000L);
        request.setLoanAmount(100_000_000L);
        request.setExistingMonthlyDebtPayment(2_000_000L);

        List<LoanRecommendation> result = service.search(request);

        assertThat(result).isEmpty();
    }

    @Test
    void search_returnsDetailByIdFromRecommendation() {
        LoanProductService service = createService();

        List<LoanRecommendation> recommendations = service.search(basicRequest());

        assertThat(recommendations).isNotEmpty();

        String id = recommendations.get(0).getProduct().getId();

        assertThat(service.findById(id)).isNotNull();
    }

    @Test
    void findById_returnsNullWhenIdIsBlank() {
        LoanProductService service = createService();

        assertThat(service.findById("")).isNull();
        assertThat(service.findById(null)).isNull();
    }

    @Test
    void search_returnsNoRecommendation_whenDsrLimitIsVeryLow() {

        LoanApiProperties apiProperties = new LoanApiProperties();
        apiProperties.setServiceKey("");
        apiProperties.setUseSampleWhenUnavailable(true);

        LoanPolicyProperties policy = new LoanPolicyProperties();
        policy.setDsrLimit(1.0);
        policy.setDtiLimit(40.0);
        policy.setLtvLimit(70.0);
        policy.setFirstHomeBuyerLtvLimit(80.0);
        policy.setMinimumRecommendationScore(35);

        LoanProductAnalyzer analyzer = new LoanProductAnalyzer();
        LoanRatioCalculator calculator = new LoanRatioCalculator();

        LoanRecommendationScorer scorer =
                new LoanRecommendationScorer(policy, calculator, analyzer);

        LoanApiClient apiClient =
                new LoanApiClient(apiProperties, new LoanProductXmlParser());

        LoanProductService service =
                new LoanProductService(
                        apiProperties,
                        policy,
                        apiClient,
                        new LoanSampleDataProvider(),
                        scorer
                );

        List<LoanRecommendation> result =
                service.search(basicRequest());

        assertThat(result).isEmpty();
    }

    @Test
    void search_returnsNoRecommendation_whenMinimumScoreIsTooHigh() {

        LoanApiProperties apiProperties = new LoanApiProperties();
        apiProperties.setServiceKey("");
        apiProperties.setUseSampleWhenUnavailable(true);

        LoanPolicyProperties policy = new LoanPolicyProperties();
        policy.setDsrLimit(40.0);
        policy.setDtiLimit(40.0);
        policy.setLtvLimit(70.0);
        policy.setFirstHomeBuyerLtvLimit(80.0);
        policy.setMinimumRecommendationScore(90);

        LoanProductAnalyzer analyzer = new LoanProductAnalyzer();
        LoanRatioCalculator calculator = new LoanRatioCalculator();

        LoanRecommendationScorer scorer =
                new LoanRecommendationScorer(policy, calculator, analyzer);

        LoanApiClient apiClient =
                new LoanApiClient(apiProperties, new LoanProductXmlParser());

        LoanProductService service =
                new LoanProductService(
                        apiProperties,
                        policy,
                        apiClient,
                        new LoanSampleDataProvider(),
                        scorer
                );

        List<LoanRecommendation> result =
                service.search(basicRequest());

        assertThat(result).isEmpty();
    }

    @Test
    void firstHomeBuyerPolicy_canBeChanged() {

        LoanPolicyProperties policy = new LoanPolicyProperties();

        policy.setFirstHomeBuyerLtvLimit(85.0);

        assertThat(policy.getFirstHomeBuyerLtvLimit())
                .isEqualTo(85.0);
    }

    @Test
    void stressRate_canBeConfigured() {

        LoanPolicyProperties policy = new LoanPolicyProperties();

        policy.setStressRateAddition(1.5);

        assertThat(policy.getStressRateAddition())
                .isEqualTo(1.5);
    }
}