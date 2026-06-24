package com.kookmin.hackerton.loan;

import com.kookmin.hackerton.loan.config.LoanApiProperties;
import com.kookmin.hackerton.loan.config.LoanPolicyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    LoanApiProperties.class,
    LoanPolicyProperties.class
})
public class DaechulAlioApplication {

    public static void main(String[] args) {
        SpringApplication.run(DaechulAlioApplication.class, args);
    }
}
