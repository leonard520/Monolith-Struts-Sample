package com.skishop.service.tax;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class TaxService {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    public BigDecimal calculateTax(BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        return amount.multiply(TAX_RATE).setScale(0, RoundingMode.DOWN);
    }
}
