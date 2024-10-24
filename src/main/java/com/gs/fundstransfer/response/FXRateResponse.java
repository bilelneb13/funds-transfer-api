package com.gs.fundstransfer.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.money.MonetaryAmount;
import javax.money.convert.ExchangeRate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FXRateResponse {
    MonetaryAmount convertedAmount;
    ExchangeRate exchangeRate;
}
