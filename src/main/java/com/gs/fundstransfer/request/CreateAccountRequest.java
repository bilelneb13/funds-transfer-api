package com.gs.fundstransfer.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import javax.money.CurrencyUnit;
import java.util.Currency;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAccountRequest {
    @NotNull(message = "Currency is required.")
    String currency;
}
