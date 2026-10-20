package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "restaurant.tx")
public class RestaurantTaxProperties {

    // Default GST Rate
    private BigDecimal defaultRate = new BigDecimal("5.00");

    // true :- restaurant is operating under the applicable ITC-eligible 18% regime.
    // false :- default 5% restaurant regime without ITC
    private boolean inputTaxCreditAllowed = false;

    // GST registration details
    private String gstin;
    private String legalName;
    private String tradeName;

    // Supplier address/state
    private String address;
    private String city;
    private String state;
    private String stateCode;
    private String postalCode;

    // Restaurant service classification
    private String sac = "996331";

    // Used when determine CGST/SGST vs IGST
    private String supplierStateCode;

}