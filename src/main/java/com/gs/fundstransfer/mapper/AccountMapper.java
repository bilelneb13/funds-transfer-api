package com.gs.fundstransfer.mapper;

import com.gs.fundstransfer.dto.AccountDto;
import com.gs.fundstransfer.model.Account;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import javax.money.CurrencyUnit;
import javax.money.NumberValue;
import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.FIELD)

public interface AccountMapper {
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    Account toModel(AccountDto accountDto);

    @Mapping(expression = "java(asString(account.getMoney().getCurrency()))", target = "currency")
    @Mapping(expression = "java(asBigDecimal(account.getMoney().getNumber()))", target = "balance")
    AccountDto toDto(Account account);

    List<AccountDto> toDtos(List<Account> accounts);

    default String asString(CurrencyUnit currencyUnit) {
        return currencyUnit.getCurrencyCode();
    }

    default BigDecimal asBigDecimal(NumberValue money) {
        return BigDecimal.valueOf(money.doubleValueExact());
    }
}
