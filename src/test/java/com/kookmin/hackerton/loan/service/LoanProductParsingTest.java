package com.kookmin.hackerton.loan.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.kookmin.hackerton.loan.model.LoanProduct;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;

class LoanProductParsingTest {

    private final LoanProductXmlParser parser = new LoanProductXmlParser();

    @Test
    void parseProducts_parsesSampleXmlItems() throws Exception {
        
        String xml = readSampleXml();

        List<LoanProduct> products = parser.parseProducts(xml);

        assertThat(products).hasSize(2);
    }

    @Test
    void parseProducts_mapsFirstProductFields() throws Exception {
        
        String xml = readSampleXml();

        List<LoanProduct> products = parser.parseProducts(xml);
        LoanProduct product = products.get(0);

        assertThat(product.getName()).isEqualTo("근로자 햇살론");
        assertThat(product.getInstitution()).isEqualTo("상호금융, 저축은행");
        assertThat(product.getLimitText()).isEqualTo("최대 2,000만원");
        assertThat(product.getRateText()).isEqualTo("연 11.5% 이하");
        assertThat(product.getPurpose()).isEqualTo("생계");
        assertThat(product.getRegion()).isEqualTo("전국");
        assertThat(product.getTarget()).isEqualTo("저소득 근로자");
    }

    @Test
    void parseProducts_mapsSecondProductFields() throws Exception {
        
        String xml = readSampleXml();

        List<LoanProduct> products = parser.parseProducts(xml);
        LoanProduct product = products.get(1);

        assertThat(product.getName()).isEqualTo("청년전용 버팀목 전세자금");
        assertThat(product.getInstitution()).isEqualTo("주택도시기금 수탁은행");
        assertThat(product.getLimitText()).isEqualTo("최대 2억원");
        assertThat(product.getRateText()).isEqualTo("연 1.8%~2.7%");
        assertThat(product.getPurpose()).isEqualTo("주거");
        assertThat(product.getRegion()).isEqualTo("전국");
        assertThat(product.getTarget()).isEqualTo("무주택 청년");
    }

    @Test
    void parseProducts_calculatesParsedValuesFromXml() throws Exception {
        
        String xml = readSampleXml();

        List<LoanProduct> products = parser.parseProducts(xml);

        LoanProduct first = products.get(0);
        LoanProduct second = products.get(1);

        assertThat(first.getLimitAmount()).isEqualTo(20_000_000L);
        assertThat(first.getMaxAnnualIncome()).isEqualTo(45_000_000L);

        assertThat(second.getLimitAmount()).isEqualTo(200_000_000L);
        assertThat(second.getMaxAnnualIncome()).isEqualTo(50_000_000L);
    }

    @Test
    void parseProducts_generatesProductIds() throws Exception {
       
        String xml = readSampleXml();

        List<LoanProduct> products = parser.parseProducts(xml);

        assertThat(products.get(0).getId()).isNotBlank();
        assertThat(products.get(1).getId()).isNotBlank();
        assertThat(products.get(0).getId()).isNotEqualTo(products.get(1).getId());
    }

    private String readSampleXml() throws Exception {
        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("sample-loan-products.xml");

        assertThat(inputStream).isNotNull();

        return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
    }
}
