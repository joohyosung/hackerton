package com.kookmin.hackerton.loan.controller;

import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kookmin.hackerton.loan.model.LoanSearchRequest;
import com.kookmin.hackerton.loan.service.LoanProductService;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanProductService loanProductService;

    @Test
    void search_returnsOkForValidRequest() throws Exception {
        when(loanProductService.search(any(LoanSearchRequest.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/loans/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isOk());
    }

    @Test
    void search_returnsBadRequestForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/loans/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("입력값 검증 실패"))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasKey("age")))
                .andExpect(jsonPath("$.errors", hasKey("annualIncome")))
                .andExpect(jsonPath("$.errors", hasKey("creditGrade")))
                .andExpect(jsonPath("$.errors", hasKey("loanAmount")))
                .andExpect(jsonPath("$.errors", hasKey("region")))
                .andExpect(jsonPath("$.errors", hasKey("purpose")));
    }

    private String validRequestJson() {
        return "{"
                + "\"age\":32,"
                + "\"annualIncome\":32000000,"
                + "\"creditGrade\":5,"
                + "\"loanAmount\":10000000,"
                + "\"region\":\"전국\","
                + "\"purpose\":\"생계\","
                + "\"existingMonthlyDebtPayment\":0,"
                + "\"existingAnnualDebtInterest\":0,"
                + "\"desiredLoanTermYears\":5,"
                + "\"expectedInterestRate\":5.0,"
                + "\"mortgageLoan\":false,"
                + "\"collateralValue\":0,"
                + "\"existingMortgageBalance\":0,"
                + "\"seniorDeposit\":0,"
                + "\"houseCount\":0,"
                + "\"houseArea\":0.0,"
                + "\"firstHomeBuyer\":false"
                + "}";
    }

    private String invalidRequestJson() {
        return "{"
                + "\"age\":-10,"
                + "\"annualIncome\":-1000000,"
                + "\"creditGrade\":20,"
                + "\"loanAmount\":-10000000,"
                + "\"region\":\"\","
                + "\"purpose\":\"\","
                + "\"desiredLoanTermYears\":0,"
                + "\"expectedInterestRate\":50.0,"
                + "\"houseCount\":5,"
                + "\"houseArea\":-1.0"
                + "}";
    }
}