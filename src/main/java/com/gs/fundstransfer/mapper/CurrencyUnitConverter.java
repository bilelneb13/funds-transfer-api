package com.gs.fundstransfer.mapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

@Converter(autoApply = true)
public class CurrencyUnitConverter implements AttributeConverter<CurrencyUnit, String> {

    @Override
    public String convertToDatabaseColumn(CurrencyUnit currencyUnit) {
        if (currencyUnit != null) {
            return currencyUnit.getCurrencyCode();
        }
        return null;
    }

    @Override
    public CurrencyUnit convertToEntityAttribute(String currencyCode) {
        if (currencyCode != null) {
            return Monetary.getCurrency(currencyCode);
        }
        return null;
    }
}
