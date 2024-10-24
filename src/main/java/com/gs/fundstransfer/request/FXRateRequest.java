package com.gs.fundstransfer.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FXRateRequest {
    MonetaryAmount monetaryAmount;
    CurrencyUnit targetCurrency;

}
