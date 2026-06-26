package com.kookmin.hackerton.loan.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.kookmin.hackerton.loan.config.LoanApiProperties;
import com.kookmin.hackerton.loan.config.LoanPolicyProperties;
import com.kookmin.hackerton.loan.model.LoanRecommendation;
import com.kookmin.hackerton.loan.model.LoanSearchRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

class LoanProductServiceTest {

    private LoanProductService createServiceWithoutApiKey() {
        LoanApiProperties apiProperties = new LoanApiProperties();
        apiProperties.setServiceKey("");

        LoanPolicyProperties policyProperties = defaultPolicy();

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
                recommendationScorer
        );
    }

    private LoanPolicyProperties defaultPolicy() {
        LoanPolicyProperties policyProperties = new LoanPolicyProperties();
        policyProperties.setDsrLimit(40.0);
        policyProperties.setDtiLimit(40.0);
        policyProperties.setLtvLimit(70.0);
        policyProperties.setFirstHomeBuyerLtvLimit(80.0);
        policyProperties.setStressRateAddition(0.0);
        policyProperties.setMinimumRecommendationScore(35);
        return policyProperties;
    }

    private LoanSearchRequest basicRequest() {
        LoanSearchRequest request = new LoanSearchRequest();
        request.setAge(32);
        request.setAnnualIncome(32_000_000L);
        request.setCreditGrade(5);
        request.setLoanAmount(10_000_000L);
        request.setRegion("전국");
        request.setPurpose("생계");
        return request;
}

    @Test
    void search_returnsEmptyListWhenApiKeyMissing() {
        LoanProductService service = createServiceWithoutApiKey();

        List<LoanRecommendation> result = service.search(basicRequest());

        assertThat(result).isEmpty();
    }

    @Test
    void findById_returnsNullWhenIdIsBlank() {
        LoanProductService service = createServiceWithoutApiKey();

        assertThat(service.findById("")).isNull();
        assertThat(service.findById(null)).isNull();
    }

    @Test
    void firstHomeBuyerPolicy_canBeChanged() {
        LoanPolicyProperties policy = new LoanPolicyProperties();

        policy.setFirstHomeBuyerLtvLimit(85.0);

        assertThat(policy.getFirstHomeBuyerLtvLimit()).isEqualTo(85.0);
    }

    @Test
    void stressRate_canBeConfigured() {
        LoanPolicyProperties policy = new LoanPolicyProperties();

        policy.setStressRateAddition(1.5);

        assertThat(policy.getStressRateAddition()).isEqualTo(1.5);
    }
}