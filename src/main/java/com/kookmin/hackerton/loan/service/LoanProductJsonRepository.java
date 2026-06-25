package com.kookmin.hackerton.loan.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kookmin.hackerton.loan.model.LoanProduct;
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class LoanProductJsonRepository {

    private final ObjectMapper objectMapper;

    public LoanProductJsonRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<LoanProduct> load(String path) {
        if (!StringUtils.hasText(path)) {
            return Collections.emptyList();
        }

        File file = new File(path);

        if (!file.exists() || !file.isFile()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(
                    file,
                    new TypeReference<List<LoanProduct>>() {}
            );
        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }
}