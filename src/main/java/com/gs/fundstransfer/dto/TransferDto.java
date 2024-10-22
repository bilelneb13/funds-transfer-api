package com.gs.fundstransfer.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gs.fundstransfer.mapper.MonetaryAmountSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.money.MonetaryAmount;
import javax.money.NumberValue;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferDto {
    Long debitAccountId;
    Long creditAccountId;
    @JsonSerialize(using = MonetaryAmountSerializer.class)
    MonetaryAmount debitedAmount;
    @JsonSerialize(using = MonetaryAmountSerializer.class)
    MonetaryAmount creditedAmount;
    NumberValue rate;
}
