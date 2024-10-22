package com.gs.fundstransfer.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.Currency;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferRequest {
    Long debitAccountId;
    Long creditAccountId;
    MonetaryAmount monetaryToTransfer;
}
