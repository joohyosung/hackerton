package com.kookmin.hackerton.loan.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.kookmin.hackerton.loan.model.LoanProduct;
import java.util.List;
import org.junit.jupiter.api.Test;

class LoanProductParsingTest {

    private final LoanProductXmlParser parser = new LoanProductXmlParser();

    @Test
    void parseProducts_parsesPublicApiXmlItems() throws Exception {
        List<LoanProduct> products = parser.parseProducts(publicApiXml());

        assertThat(products).hasSize(2);
    }

    @Test
    void parseProducts_mapsFirstProductFields() throws Exception {
        List<LoanProduct> products = parser.parseProducts(publicApiXml());
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
        List<LoanProduct> products = parser.parseProducts(publicApiXml());
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
        List<LoanProduct> products = parser.parseProducts(publicApiXml());

        LoanProduct first = products.get(0);
        LoanProduct second = products.get(1);

        assertThat(first.getLimitAmount()).isEqualTo(20_000_000L);
        assertThat(first.getMaxAnnualIncome()).isEqualTo(45_000_000L);

        assertThat(second.getLimitAmount()).isEqualTo(200_000_000L);
        assertThat(second.getMaxAnnualIncome()).isEqualTo(50_000_000L);
    }

    @Test
    void parseProducts_generatesProductIds() throws Exception {
        List<LoanProduct> products = parser.parseProducts(publicApiXml());

        assertThat(products.get(0).getId()).isNotBlank();
        assertThat(products.get(1).getId()).isNotBlank();
        assertThat(products.get(0).getId()).isNotEqualTo(products.get(1).getId());
    }

    private String publicApiXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<response>"
                + "<header><resultCode>00</resultCode><resultMsg>NORMAL SERVICE</resultMsg></header>"
                + "<body><items>"
                + "<item>"
                + "<seq>1</seq>"
                + "<finPrdNm>근로자 햇살론</finPrdNm>"
                + "<hdlInst>상호금융, 저축은행</hdlInst>"
                + "<lnLmt>최대 2,000만원</lnLmt>"
                + "<irt>연 11.5% 이하</irt>"
                + "<irtCtg>고정금리</irtCtg>"
                + "<usge>생계</usge>"
                + "<maxTotLnTrm>5년</maxTotLnTrm>"
                + "<trgt>저소득 근로자</trgt>"
                + "<suprTgtDtlCond>저소득 근로자</suprTgtDtlCond>"
                + "<rsdArea>전국</rsdArea>"
                + "<anin>4,500만원</anin>"
                + "<crdtSc_6_0>Y</crdtSc_6_0>"
                + "</item>"
                + "<item>"
                + "<seq>2</seq>"
                + "<finPrdNm>청년전용 버팀목 전세자금</finPrdNm>"
                + "<hdlInst>주택도시기금 수탁은행</hdlInst>"
                + "<lnLmt>최대 2억원</lnLmt>"
                + "<irt>연 1.8%~2.7%</irt>"
                + "<irtCtg>변동금리</irtCtg>"
                + "<usge>주거</usge>"
                + "<maxTotLnTrm>10년</maxTotLnTrm>"
                + "<trgt>무주택 청년</trgt>"
                + "<suprTgtDtlCond>무주택 청년</suprTgtDtlCond>"
                + "<rsdArea>전국</rsdArea>"
                + "<anin>5,000만원</anin>"
                + "<age_39Blw>Y</age_39Blw>"
                + "<housHoldCnt>무주택</housHoldCnt>"
                + "</item>"
                + "</items><totalCount>2</totalCount></body>"
                + "</response>";
    }
}
