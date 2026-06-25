package com.kookmin.hackerton.loan.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kookmin.hackerton.loan.model.LoanProduct;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

class LoanProductJsonRepositoryTest {

    @TempDir
    File tempDir;

    private final LoanProductJsonRepository repository =
            new LoanProductJsonRepository(new ObjectMapper());

    @Test
    void load_returnsProductsFromJsonFile() throws Exception {
        File jsonFile = new File(tempDir, "loan-products.json");

        String json =
            "[{" +
            "\"id\":\"test-loan\"," +
            "\"name\":\"테스트 대출\"," +
            "\"institution\":\"테스트 기관\"," +
            "\"limitText\":\"최대 1,000만원\"," +
            "\"limitAmount\":10000000," +
            "\"rateText\":\"연 5.0%\"," +
            "\"purpose\":\"생계\"," +
            "\"region\":\"전국\"" +
            "}]";

        Files.write(
                jsonFile.toPath(),
                json.getBytes(StandardCharsets.UTF_8)
        );

        List<LoanProduct> products = repository.load(jsonFile.getAbsolutePath());

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getId()).isEqualTo("test-loan");
        assertThat(products.get(0).getName()).isEqualTo("테스트 대출");
        assertThat(products.get(0).getInstitution()).isEqualTo("테스트 기관");
        assertThat(products.get(0).getLimitAmount()).isEqualTo(10_000_000L);
    }

    @Test
    void load_returnsEmptyListWhenPathIsBlank() {
        List<LoanProduct> products = repository.load("");

        assertThat(products).isEmpty();
    }

    @Test
    void load_returnsEmptyListWhenFileDoesNotExist() {
        File missingFile = new File(tempDir, "missing.json");

        List<LoanProduct> products = repository.load(missingFile.getAbsolutePath());

        assertThat(products).isEmpty();
    }

    @Test
    void load_returnsEmptyListWhenJsonIsInvalid() throws Exception {
        File jsonFile = new File(tempDir, "invalid.json");

        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write("{ invalid json");
        }

        List<LoanProduct> products = repository.load(jsonFile.getAbsolutePath());

        assertThat(products).isEmpty();
    }
}