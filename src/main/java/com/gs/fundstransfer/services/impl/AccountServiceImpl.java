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

/**
 * Service implementation that handles the business logic related to account operations.
 *
 * This class provides methods for creating new accounts and retrieving existing accounts.
 * It utilizes AccountRepository for persistence operations and AccountMapper for data transformation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    /**
     * Creates a new account based on the provided account request and saves it to the repository.
     *
     * @param accountRequest the details for creating the new account, including the currency type
     * @return the newly created account, represented as an AccountDto
     */
    @Override
    public AccountDto save(CreateAccountRequest accountRequest) {
        var account = Account.builder()
                .money(createInitialAccount(accountRequest.getCurrency()))
                .build();
        Account saved = accountRepository.save(account);
        return accountMapper.toDto(saved);
    }

    /**
     * Creates an initial account with a balance of zero for the provided currency unit.
     *
     * @param currencyUnit the currency code (e.g., "USD", "EUR") to be used for the initial account balance
     * @return a MonetaryAmount with the given currency and a zero balance
     * @throws NotSupportedCurrencyException if the provided currency unit is not recognized
     */
    static MonetaryAmount createInitialAccount(String currencyUnit) {
        try {
            return Monetary.getDefaultAmountFactory()
                    .setCurrency(currencyUnit)
                    .setNumber(BigDecimal.ZERO)
                    .create();
        } catch (UnknownCurrencyException e) {
            throw new NotSupportedCurrencyException(e.getMessage());
        }
    }

    /**
     * Retrieves an existing account by its owner ID.
     *
     * @param id the ID of the account owner to retrieve
     * @return the account information as an AccountDto
     * @throws AccountNotFoundException if no account is found for the given ID
     */
    @Override
    public AccountDto get(Long id) {
        Optional<Account> optionalAccount = accountRepository.findById(id);
        optionalAccount.orElseThrow(() -> new AccountNotFoundException(id));
        return accountMapper.toDto(optionalAccount.get());
    }

    /**
     * Retrieves all existing accounts from the repository and converts them to a list of AccountDto objects.
     *
     * @return a list of accounts represented as AccountDto objects
     */
    @Override
    public List<AccountDto> getAll() {
        List<Account> optionalAccount = accountRepository.findAll();
        return accountMapper.toDtos(optionalAccount);
    }
}
