package com.kookmin.hackerton.loan.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.kookmin.hackerton.loan.config.LoanApiProperties;
import com.kookmin.hackerton.loan.config.LoanPolicyProperties;
import com.kookmin.hackerton.loan.model.LoanRecommendation;
import com.kookmin.hackerton.loan.model.LoanSearchRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

class LoanProductServiceTest {

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
    void minimumRecommendationScore_canBeConfigured() {
        LoanPolicyProperties policy = new LoanPolicyProperties();

        policy.setMinimumRecommendationScore(50);

        assertThat(policy.getMinimumRecommendationScore()).isEqualTo(50);
    }

    private LoanProductService createServiceWithoutApiKey() {
        LoanApiProperties apiProperties = new LoanApiProperties();
        apiProperties.setServiceKey("");

        LoanPolicyProperties policyProperties = new LoanPolicyProperties();
        LoanProductAnalyzer productAnalyzer = new LoanProductAnalyzer();
        LoanProductXmlParser xmlParser = new LoanProductXmlParser();
        LoanApiClient loanApiClient = new LoanApiClient(apiProperties, xmlParser);
        LoanRecommendationScorer recommendationScorer = new LoanRecommendationScorer(productAnalyzer);

        return new LoanProductService(
                apiProperties,
                policyProperties,
                loanApiClient,
                recommendationScorer
        );
    }

    private LoanSearchRequest basicRequest() {
        LoanSearchRequest request = new LoanSearchRequest();
        request.setAge(32);
        request.setAnnualIncome(32_000_000L);
        request.setLoanAmount(10_000_000L);
        request.setRegion("전국");
        request.setPurpose("생계");
        return request;
    }
}
