package com.kookmin.hackerton.loan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loan.api")
public class LoanApiProperties {

    private String endpoint = "https://apis.data.go.kr/B553701/LoanProductSearchingInfo";
    private String serviceKey = "";
    private int pageSize = 100;
    private boolean useSampleWhenUnavailable = true;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isUseSampleWhenUnavailable() {
        return useSampleWhenUnavailable;
    }

    public void setUseSampleWhenUnavailable(boolean useSampleWhenUnavailable) {
        this.useSampleWhenUnavailable = useSampleWhenUnavailable;
    }
}
