package com.kookmin.hackerton.loan.controller;

import com.kookmin.hackerton.loan.model.LoanProduct;
import com.kookmin.hackerton.loan.model.LoanRecommendation;
import com.kookmin.hackerton.loan.model.LoanSearchRequest;
import com.kookmin.hackerton.loan.service.LoanProductService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanProductService loanProductService;

    public LoanController(LoanProductService loanProductService) {
        this.loanProductService = loanProductService;
    }

    @PostMapping("/search")
    public List<LoanRecommendation> search(@RequestBody LoanSearchRequest request) {
        return loanProductService.search(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanProduct> detail(@PathVariable String id) {
        LoanProduct product = loanProductService.findById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}
