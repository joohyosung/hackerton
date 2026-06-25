package com.kookmin.hackerton.loan.service;

import com.kookmin.hackerton.loan.model.LoanProduct;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class LoanSampleDataProvider {

    public List<LoanProduct> sampleProducts() {
        List<LoanProduct> products = new ArrayList<LoanProduct>();

        products.add(sample(
                "햇살론15",
                "서민금융진흥원",
                "최대 2,000만원",
                20000000L,
                "고정금리",
                "연 15.9%, 성실상환 시 인하 가능",
                "생계 대환",
                "3년 또는 5년",
                "저신용, 저소득 근로자 및 사업자",
                "전국",
                "대부업이나 불법사금융 이용이 불가피한 최저신용자를 위한 정책서민금융상품입니다.",
                19,
                null,
                45000000L,
                10
        ));

        products.add(sample(
                "근로자 햇살론",
                "상호금융, 저축은행",
                "최대 2,000만원",
                20000000L,
                "변동금리",
                "연 11.5% 이하",
                "생계",
                "3년 또는 5년",
                "재직 3개월 이상 근로자",
                "전국",
                "제도권 금융 이용이 어려운 근로자에게 생계자금을 지원하는 상품입니다.",
                19,
                null,
                45000000L,
                6
        ));

        products.add(sample(
                "새희망홀씨II",
                "시중은행",
                "최대 3,500만원",
                35000000L,
                "은행별 상이",
                "은행별 심사에 따라 결정",
                "생계 대환",
                "최대 5년",
                "저소득 또는 저신용 서민",
                "전국",
                "은행권에서 운영하는 대표적인 서민 맞춤형 신용대출 상품입니다.",
                19,
                null,
                50000000L,
                6
        ));

        products.add(sample(
                "미소금융 창업 운영자금",
                "서민금융진흥원",
                "최대 7,000만원",
                70000000L,
                "고정금리",
                "연 4.5% 내외",
                "창업 운영",
                "최대 6년",
                "창업 예정자 또는 영세 자영업자",
                "전국",
                "담보나 신용이 부족한 창업자와 영세 자영업자의 창업 및 운영자금을 지원합니다.",
                19,
                null,
                45000000L,
                7
        ));

        products.add(sample(
                "청년전용 버팀목 전세자금",
                "주택도시기금",
                "최대 2억원",
                200000000L,
                "고정 또는 변동",
                "연 1.8%~2.7% 수준",
                "주거",
                "2년, 최대 10년까지 연장 가능",
                "만 19세 이상 34세 이하 무주택 청년",
                "전국",
                "청년층의 주거 안정을 위한 전세자금 대출 상품입니다.",
                19,
                34,
                50000000L,
                null
        ));

        return products;
    }

    private LoanProduct sample(
            String name,
            String institution,
            String limitText,
            Long limitAmount,
            String rateType,
            String rateText,
            String purpose,
            String periodText,
            String target,
            String region,
            String summary,
            Integer minAge,
            Integer maxAge,
            Long maxAnnualIncome,
            Integer maxCreditGrade
    ) {
        LoanProduct product = new LoanProduct();
        product.setId(slug(name + "-" + institution));
        product.setName(name);
        product.setInstitution(institution);
        product.setLimitText(limitText);
        product.setLimitAmount(limitAmount);
        product.setRateType(rateType);
        product.setRateText(rateText);
        product.setPurpose(purpose);
        product.setPeriodText(periodText);
        product.setTarget(target);
        product.setRegion(region);
        product.setSummary(summary);
        product.setMinAge(minAge);
        product.setMaxAge(maxAge);
        product.setMaxAnnualIncome(maxAnnualIncome);
        product.setMaxCreditGrade(maxCreditGrade);
        return product;
    }

    private String slug(String value) {
        String normalized = value == null ? "loan-product" : value.trim().toLowerCase(Locale.KOREAN);
        normalized = normalized.replaceAll("[^0-9a-z가-힣]+", "-");
        normalized = normalized.replaceAll("(^-|-$)", "");
        return StringUtils.hasText(normalized) ? normalized : "loan-product";
    }
}