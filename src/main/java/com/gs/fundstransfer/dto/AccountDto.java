package com.gs.fundstransfer.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Currency;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountDto {
     Long ownerId;
     String currency;
     BigDecimal balance;
}
