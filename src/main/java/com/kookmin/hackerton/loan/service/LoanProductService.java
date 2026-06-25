package com.kookmin.hackerton.loan.service;

import com.kookmin.hackerton.loan.config.LoanApiProperties;
import com.kookmin.hackerton.loan.model.LoanProduct;
import com.kookmin.hackerton.loan.model.LoanRecommendation;
import com.kookmin.hackerton.loan.model.LoanSearchRequest;
import com.kookmin.hackerton.loan.config.LoanPolicyProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LoanProductService {

    private static final Logger log = LoggerFactory.getLogger(LoanProductService.class);
    private final LoanApiProperties properties;
    private final LoanPolicyProperties policyProperties;
    private final LoanApiClient loanApiClient;
    private final LoanSampleDataProvider sampleDataProvider;
    private final LoanRecommendationScorer recommendationScorer;
    private final LoanProductJsonRepository jsonRepository;

    public LoanProductService(
        LoanApiProperties properties,
        LoanPolicyProperties policyProperties,
        LoanApiClient loanApiClient,
        LoanSampleDataProvider sampleDataProvider,
        LoanRecommendationScorer recommendationScorer,
        LoanProductJsonRepository jsonRepository
    ) {
        this.properties = properties;
        this.policyProperties = policyProperties;
        this.loanApiClient = loanApiClient;
        this.sampleDataProvider = sampleDataProvider;
        this.recommendationScorer = recommendationScorer;
        this.jsonRepository = jsonRepository;
    }
    
    public List<LoanRecommendation> search(LoanSearchRequest request) {
        List<LoanRecommendation> recommendations = new ArrayList<LoanRecommendation>();

        for (LoanProduct product : loadProducts()) {
            LoanRecommendation recommendation = recommendationScorer.score(product, request);

            if (isRecommendable(recommendation)) {
                recommendations.add(recommendation);
            }
        }

        recommendations.sort((left, right) -> Integer.compare(right.getScore(), left.getScore()));

        return recommendations;
    }

    private boolean isRecommendable(LoanRecommendation recommendation) {
        return recommendation.isEligible()
                && recommendation.getScore() >= policyProperties.getMinimumRecommendationScore();
    }

    public LoanProduct findById(String id) {
        if (!StringUtils.hasText(id)) {
            return null;
        }

        for (LoanProduct product : loadProducts()) {
            if (id.equals(product.getId())) {
                return product;
            }
        }

        return null;
    }

    private List<LoanProduct> loadProducts() {
        List<LoanProduct> jsonProducts = jsonRepository.load(properties.getJsonPath());

        if (!jsonProducts.isEmpty()) {
            return jsonProducts;
        }

        if (!StringUtils.hasText(properties.getServiceKey())) {
            return sampleDataProvider.sampleProducts();
        }

        try {
            List<LoanProduct> products = loanApiClient.fetchProducts();

            if (!products.isEmpty()) {
                return products;
            }

            log.warn("Public API returned no loan products.");
        } catch (Exception exception) {
            log.warn("Failed to load loan products from public API.", exception);
        }

        return properties.isUseSampleWhenUnavailable()
                ? sampleDataProvider.sampleProducts()
                : Collections.emptyList();
    }
}
