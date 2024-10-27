package com.gs.fundstransfer.services.impl;

import com.gs.fundstransfer.dto.TransferDto;
import com.gs.fundstransfer.exceptions.AccountNotFoundException;
import com.gs.fundstransfer.exceptions.NotSupportedCurrencyException;
import com.gs.fundstransfer.exceptions.SameAccountTransferException;
import com.gs.fundstransfer.exceptions.UnsufficientFundsException;
import com.gs.fundstransfer.model.Account;
import com.gs.fundstransfer.repository.AccountRepository;
import com.gs.fundstransfer.request.FXRateRequest;
import com.gs.fundstransfer.request.TransferRequest;
import com.gs.fundstransfer.response.FXRateResponse;
import com.gs.fundstransfer.services.ForexService;
import org.javamoney.moneta.spi.DefaultNumberValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.money.Monetary;
import javax.money.convert.ExchangeRate;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    public static final long ACCOUNT_ID_1 = 1000000000L;
    public static final long ACCOUNT_ID_2 = 1000000001L;

    @Mock
    private ForexService forexService;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        transactionService = new TransactionServiceImpl(forexService, accountRepository);
    }

    @Test
    void transfer_same_account_exception() {
        TransferRequest request = new TransferRequest(ACCOUNT_ID_1, ACCOUNT_ID_1, new BigDecimal("100.00"), "USD");
        assertThrows(SameAccountTransferException.class, () ->
                transactionService.transfer(request));
    }

    @Test
    void transfer_account_not_found_exception() {
        TransferRequest request = new TransferRequest(ACCOUNT_ID_1, ACCOUNT_ID_2, new BigDecimal("100.00"), "USD");
        when(accountRepository.findByIdWithLock(ACCOUNT_ID_1)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () ->
                transactionService.transfer(request));
    }

    @Test
    void transfer_not_supported_currency_exception() {
        TransferRequest request = new TransferRequest(ACCOUNT_ID_1, ACCOUNT_ID_2, new BigDecimal("100.00"), "GBP");
        Account account1 = new Account(ACCOUNT_ID_1, Monetary.getDefaultAmountFactory()
                .setCurrency("USD")
                .setNumber(BigDecimal.TEN)
                .create());
        Account account2 = new Account(ACCOUNT_ID_2, Monetary.getDefaultAmountFactory()
                .setCurrency("USD")
                .setNumber(BigDecimal.TEN)
                .create());
        when(accountRepository.findByIdWithLock(ACCOUNT_ID_1)).thenReturn(Optional.of(account1));
        when(accountRepository.findByIdWithLock(ACCOUNT_ID_2)).thenReturn(Optional.of(account2));
        assertThrows(NotSupportedCurrencyException.class, () ->
                transactionService.transfer(request));
    }

    @Test
    void transfer_unsufficient_funds_exception() {
        TransferRequest request = new TransferRequest(ACCOUNT_ID_1, ACCOUNT_ID_2, new BigDecimal("100.00"), "USD");
        Account account1 = new Account(1L, Monetary.getDefaultAmountFactory()
                .setCurrency("USD")
                .setNumber(BigDecimal.TEN)
                .create());
        Account account2 = new Account(ACCOUNT_ID_2, Monetary.getDefaultAmountFactory()
                .setCurrency("USD")
                .setNumber(BigDecimal.TEN)
                .create());
        when(accountRepository.findByIdWithLock(ACCOUNT_ID_1)).thenReturn(Optional.of(account1));
        when(accountRepository.findByIdWithLock(ACCOUNT_ID_2)).thenReturn(Optional.of(account2));
        assertThrows(UnsufficientFundsException.class, () ->
                transactionService.transfer(request));
    }

    @Test
    void transfer_same_currency_success() {
        TransferRequest request = new TransferRequest(ACCOUNT_ID_1, ACCOUNT_ID_2, new BigDecimal("5.00"), "USD");
        Account account1 = new Account(ACCOUNT_ID_1, Monetary.getDefaultAmountFactory()
                .setCurrency("USD")
                .setNumber(BigDecimal.TEN)
                .create());
        Account account2 = new Account(ACCOUNT_ID_2, Monetary.getDefaultAmountFactory()
                .setCurrency("USD")
                .setNumber(BigDecimal.ZERO)
                .create());
        when(accountRepository.findByIdWithLock(ACCOUNT_ID_1)).thenReturn(Optional.of(account1));
        when(accountRepository.findByIdWithLock(ACCOUNT_ID_2)).thenReturn(Optional.of(account2));
        TransferDto result = transactionService.transfer(request);
        assertNotNull(result);
        assertEquals(new BigDecimal("5"),
                     result.getDebitedAmount()
                             .getNumber()
                             .numberValue(BigDecimal.class));
        assertEquals(new BigDecimal("5"),
                     result.getCreditedAmount()
                             .getNumber()
                             .numberValue(BigDecimal.class));
        assertEquals(BigDecimal.ONE, result.getRate());
    }

    @Test
    void transfer_different_currency_success() {
        // Arrange
        Long debitAccountId = 1000000001L;
        Long creditAccountId = 1000000002L;
        BigDecimal amount = BigDecimal.valueOf(500);
        String debitCurrency = "USD";
        String creditCurrency = "EUR";
        // Create a mock for ExchangeRate
        ExchangeRate mockExchangeRate = mock(ExchangeRate.class);
        when(mockExchangeRate.getFactor()).thenReturn(new DefaultNumberValue(BigDecimal.valueOf(0.85)));

        TransferRequest request = new TransferRequest(debitAccountId, creditAccountId, amount, debitCurrency);

        Account debitAccount = new Account();
        debitAccount.setMoney(Monetary.getDefaultAmountFactory()
                                      .setCurrency(debitCurrency)
                                      .setNumber(amount.add(BigDecimal.valueOf(1000)))
                                      .create());

        Account creditAccount = new Account();
        creditAccount.setMoney(Monetary.getDefaultAmountFactory()
                                       .setCurrency(creditCurrency)
                                       .setNumber(BigDecimal.ZERO)
                                       .create());

        when(accountRepository.findByIdWithLock(debitAccountId)).thenReturn(Optional.of(debitAccount));
        when(accountRepository.findByIdWithLock(creditAccountId)).thenReturn(Optional.of(creditAccount));

        FXRateRequest fxRateRequest = FXRateRequest.builder()
                .monetaryAmount(Monetary.getDefaultAmountFactory()
                                        .setCurrency(request.getCurrency())
                                        .setNumber(request.getAmount())
                                        .create())
                .targetCurrency(creditAccount.getMoney()
                                        .getCurrency())
                .build();
        FXRateResponse fxRateResponse = new FXRateResponse();
        fxRateResponse.setConvertedAmount(Monetary.getDefaultAmountFactory()
                                                  .setCurrency(creditCurrency)
                                                  .setNumber(amount.multiply(BigDecimal.valueOf(0.85)))
                                                  .create());
        fxRateResponse.setExchangeRate(mockExchangeRate);

        when(forexService.exchange(any(FXRateRequest.class))).thenReturn(fxRateResponse);

        // Act
        TransferDto result = transactionService.transfer(request);

        // Assert
        assertNotNull(result);
        assertEquals(debitAccountId, result.getDebitAccountId());
        assertEquals(creditAccountId, result.getCreditAccountId());
        assertEquals(amount,
                     result.getDebitedAmount()
                             .getNumber().numberValue(BigDecimal.class));
        assertEquals(fxRateResponse.getExchangeRate()
                             .getFactor()
                             .numberValue(BigDecimal.class), result.getRate());
    }
}