package com.example.demo.adapter.web.service.impl;

import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class RestaurantTaxCalculator {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int SCALE = 2;

    public TaxBreakdown calculate(BigDecimal grossAmount, BigDecimal discountAmount, BigDecimal taxRate, boolean intraState) {

        BigDecimal gross = money(grossAmount);
        BigDecimal discount = money(discountAmount);

        if (discount.compareTo(gross) > 0) {
            discount = gross;
        }

        BigDecimal taxableAmount = money(gross.subtract(discount));
        BigDecimal rate = taxRate == null ? BigDecimal.ZERO : taxRate;
        BigDecimal totalTax = money(taxableAmount
                        .multiply(rate)
                        .divide(HUNDRED, 2, RoundingMode.HALF_UP));

        BigDecimal cgst = BigDecimal.ZERO;
        BigDecimal sgst = BigDecimal.ZERO;
        BigDecimal igst = BigDecimal.ZERO;
        if (intraState) {
            cgst = money(totalTax.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));
            sgst = money(totalTax.subtract(cgst));
        } else {
            igst = totalTax;
        }

        BigDecimal invoiceTotal = money(taxableAmount.add(totalTax));

        return TaxBreakdown.builder()
                .grossAmount(gross)
                .discountAmount(discount)
                .taxableAmount(taxableAmount)
                .taxRate(rate)
                .totalTax(totalTax)
                .cgst(cgst)
                .sgst(sgst)
                .igst(igst)
                .invoiceTotal(invoiceTotal)
                .build();
    }

    private BigDecimal money(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)
                : value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    @Getter
    @Builder
    public static class TaxBreakdown {

        private BigDecimal grossAmount;
        private BigDecimal discountAmount;
        private BigDecimal taxableAmount;

        private BigDecimal taxRate;
        private BigDecimal totalTax;

        private BigDecimal cgst;
        private BigDecimal sgst;
        private BigDecimal igst;

        private BigDecimal invoiceTotal;
    }

}