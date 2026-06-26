package com.kookmin.hackerton.loan.model;

import java.util.ArrayList;
import java.util.List;

public class LoanAffordabilityResult {

    private List<String> warnings = new ArrayList<String>();

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
