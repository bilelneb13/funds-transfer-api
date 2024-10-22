package com.gs.fundstransfer.services.impl;

import com.gs.fundstransfer.dto.AccountDto;
import com.gs.fundstransfer.exceptions.AccountNotFoundException;
import com.gs.fundstransfer.exceptions.NotSupportedCurrencyException;
import com.gs.fundstransfer.mapper.AccountMapper;
import com.gs.fundstransfer.model.Account;
import com.gs.fundstransfer.repository.AccountRepository;
import com.gs.fundstransfer.request.CreateAccountRequest;
import com.gs.fundstransfer.services.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.UnknownCurrencyException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    public AccountDto save(CreateAccountRequest accountRequest) {
        var account = Account.builder().money(createInitialAccount(accountRequest.getCurrency())).build();
        Account saved = accountRepository.save(account);
        return accountMapper.toDto(saved);
    }

    private static MonetaryAmount createInitialAccount(String currencyUnit) {
        try {
            return Monetary.getDefaultAmountFactory()
                    .setCurrency(currencyUnit).setNumber(BigDecimal.ZERO).create();
        } catch (UnknownCurrencyException e) {
            throw new NotSupportedCurrencyException(e.getMessage());
        }
    }

    @Override
    public AccountDto get(Long id) {
        Optional<Account> optionalAccount = accountRepository.findById(id);
        optionalAccount.orElseThrow(() -> new AccountNotFoundException(id));
        return accountMapper.toDto(optionalAccount.get());
    }

    @Override
    public List<AccountDto> getAll() {
        List<Account> optionalAccount = accountRepository.findAll();
        return accountMapper.toDtos(optionalAccount);
    }
}
