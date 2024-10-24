package com.gs.fundstransfer.services.impl;

import com.gs.fundstransfer.exceptions.UnavailableFXRatesException;
import com.gs.fundstransfer.request.FXRateRequest;
import com.gs.fundstransfer.response.FXRateResponse;
import com.gs.fundstransfer.services.ForexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.money.MonetaryAmount;
import javax.money.MonetaryException;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.ExchangeRate;
import javax.money.convert.MonetaryConversions;

@Service
@RequiredArgsConstructor
public class ForexServiceImpl implements ForexService {
    @Override
    public FXRateResponse exchange(FXRateRequest fXRateRequest) {
        CurrencyConversion conversion = MonetaryConversions.getConversion(fXRateRequest.getTargetCurrency());
        MonetaryAmount convertedAmount = null;
        ExchangeRate exchangeRate;
        try {
            convertedAmount = fXRateRequest.getMonetaryAmount()
                    .with(conversion);
            // Retrieve the exchange rate used
            exchangeRate = conversion.getExchangeRate(fXRateRequest.getMonetaryAmount());

        } catch (MonetaryException e) {
            throw new UnavailableFXRatesException("Unable to retrieve exchange rate");
        }
        return FXRateResponse.builder()
                .convertedAmount(convertedAmount)
                .exchangeRate(exchangeRate)
                .build();
    }
}
