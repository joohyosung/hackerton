package com.kookmin.hackerton.loan.service;

import com.kookmin.hackerton.loan.config.LoanApiProperties;
import com.kookmin.hackerton.loan.model.LoanProduct;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class LoanApiClient {

    private final LoanApiProperties properties;
    private final LoanProductXmlParser xmlParser;
    private final RestTemplate restTemplate;

    public LoanApiClient(
            LoanApiProperties properties,
            LoanProductXmlParser xmlParser
    ) {
        this.properties = properties;
        this.xmlParser = xmlParser;
        this.restTemplate = new RestTemplate();
    }

    public List<LoanProduct> fetchProducts() throws Exception {
        String xml = restTemplate.getForObject(
                UriComponentsBuilder.fromHttpUrl(properties.getEndpoint())
                        .path(properties.getPath())
                        .queryParam("serviceKey", properties.getServiceKey())
                        .queryParam("pageNo", 1)
                        .queryParam("numOfRows", properties.getPageSize())
                        .queryParam("type", "xml")
                        .build(false)
                        .toUri(),
                String.class
        );

        if (!StringUtils.hasText(xml)) {
            return Collections.emptyList();
        }

        return xmlParser.parseProducts(xml);
    }
}