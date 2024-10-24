package com.gs.fundstransfer.mapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.MonetaryException;
import java.math.BigDecimal;

@Converter(autoApply = true)
public class MonetaryAmountConverter implements AttributeConverter<MonetaryAmount, String> {

    // Convert MonetaryAmount to a String format: "amount:currency"
    @Override
    public String convertToDatabaseColumn(MonetaryAmount attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getNumber()
                .toString() + "|" + attribute.getCurrency()
                .getCurrencyCode();
    }

    // Convert the String format from the database back into a MonetaryAmount
    @Override
    public MonetaryAmount convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }

        try {
            String[] parts = dbData.split("\\|");
            BigDecimal amount = new BigDecimal(parts[0]);
            String currencyCode = parts[1];

            return Monetary.getDefaultAmountFactory()
                    .setCurrency(currencyCode)
                    .setNumber(amount)
                    .create();
        } catch (Exception e) {
            throw new MonetaryException("Failed to convert the string to a MonetaryAmount", e);
        }
    }
}
