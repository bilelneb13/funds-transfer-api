package com.gs.fundstransfer.services.impl;

import com.gs.fundstransfer.dto.TransferDto;
import com.gs.fundstransfer.exceptions.AccountNotFoundException;
import com.gs.fundstransfer.exceptions.NotSupportedCurrencyException;
import com.gs.fundstransfer.exceptions.SameAccountTransferException;
import com.gs.fundstransfer.exceptions.UnsufficientFundsException;
import com.gs.fundstransfer.model.Account;
import com.gs.fundstransfer.repository.AccountRepository;
import com.gs.fundstransfer.request.FXRateRequest;
import com.gs.fundstransfer.request.OrderRequest;
import com.gs.fundstransfer.request.TransferRequest;
import com.gs.fundstransfer.response.FXRateResponse;
import com.gs.fundstransfer.services.ForexService;
import com.gs.fundstransfer.services.TransactionService;
import jakarta.persistence.PessimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final ForexService forexService;
    private final AccountRepository accountRepository;

    @Retryable(retryFor = {PessimisticLockException.class, CannotAcquireLockException.class}, backoff = @Backoff(delay = 200))
    @Transactional
    @Override
    public TransferDto deposit(OrderRequest request) {
        Account account = accountRepository.findByIdWithLock(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(
                        request.getAccountId()));
        // Check if the source and target currencies are the same
        if (request.getCurrency().equals(account.getMoney().getCurrency().getCurrencyCode())) {
            // No need for conversion if currencies are the same
            MonetaryAmount moneyToDeposit = Monetary.getDefaultAmountFactory()
                    .setCurrency(request.getCurrency())
                    .setNumber(
                            request.getAmount())
                    .create();
            account.setMoney(account.getMoney().add(moneyToDeposit));
            accountRepository.save(account);
            return TransferDto.builder()
                    .creditAccountId(request.getAccountId())
                    .creditedAmount(moneyToDeposit) // No conversion, use the request amount
                    .rate(BigDecimal.ONE) // No exchange rate, so use 1.0
                    .build();
        }

        // If currencies are different, proceed with conversion
        FXRateRequest fXRateRequest = createFXRequest(request, account);
        FXRateResponse fxRateResponse = forexService.exchange(fXRateRequest);

        account.setMoney(account.getMoney().add(fxRateResponse.getConvertedAmount()));
        accountRepository.save(account);
        BigDecimal rate = fxRateResponse.getExchangeRate().getFactor().numberValue(BigDecimal.class);
        return TransferDto.builder()
                .creditAccountId(request.getAccountId())
                .creditedAmount(fxRateResponse.getConvertedAmount())
                .rate(
                        rate)
                .build();
    }


    @Retryable(retryFor = {PessimisticLockException.class, CannotAcquireLockException.class}, backoff = @Backoff(delay = 200))
    @Transactional
    @Override
    public TransferDto withdraw(OrderRequest request) {

        Account account = accountRepository.findByIdWithLock(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(
                        request.getAccountId()));

        // Check if the source and target currencies are the same
        if (request.getCurrency().equals(account.getMoney().getCurrency().getCurrencyCode())) {
            // No need for conversion if currencies are the same
            MonetaryAmount moneyToWithdraw = Monetary.getDefaultAmountFactory()
                    .setCurrency(request.getCurrency())
                    .setNumber(
                            request.getAmount())
                    .create();
            if (account.getMoney().compareTo(moneyToWithdraw) < 0) {
                throw new UnsufficientFundsException("InsufficientFunds in " + account);

            }
            account.setMoney(account.getMoney().subtract(moneyToWithdraw));
            accountRepository.save(account);
            return TransferDto.builder()
                    .debitAccountId(request.getAccountId())
                    .debitedAmount(moneyToWithdraw) // No conversion, use the request amount
                    .rate(BigDecimal.ONE) // No exchange rate, so use 1.0
                    .build();
        }

        FXRateRequest fXRateRequest = createFXRequest(request, account);

        FXRateResponse fxRateResponse = forexService.exchange(fXRateRequest);
        if (account.getMoney().compareTo(fxRateResponse.getConvertedAmount()) < 0) {
            throw new UnsufficientFundsException("InsufficientFunds in " + account);

        }
        account.setMoney(account.getMoney().subtract(fxRateResponse.getConvertedAmount()));

        accountRepository.save(account);

        BigDecimal rate = fxRateResponse.getExchangeRate().getFactor().numberValue(BigDecimal.class);

        return TransferDto.builder()
                .debitAccountId(request.getAccountId())
                .debitedAmount(fxRateResponse.getConvertedAmount())
                .rate(
                        rate)
                .build();
    }

    @Retryable(retryFor = {PessimisticLockException.class, CannotAcquireLockException.class}, backoff = @Backoff(delay = 200))
    @Transactional
    @Override
    public TransferDto transfer(TransferRequest request) {

        Long debitAccountId = request.getDebitAccountId();
        Long creditAccountId = request.getCreditAccountId();

        if (Objects.equals(debitAccountId, creditAccountId)) {
            throw new SameAccountTransferException("Credit and Debit accounts are the same");
        }

        Account debitAccount = accountRepository.findByIdWithLock(debitAccountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        debitAccountId));

        Account creditAccount = accountRepository.findByIdWithLock(creditAccountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        creditAccountId));

        MonetaryAmount debitMoneyRequest = Monetary.getDefaultAmountFactory()
                .setCurrency(request.getCurrency())
                .setNumber(
                        request.getAmount())
                .create();

        if (!debitAccount.getMoney().getCurrency().equals(debitMoneyRequest.getCurrency())) {
            throw new NotSupportedCurrencyException("The Request Currency should be " + debitAccount.getMoney()
                    .getCurrency());
        }
        if (debitAccount.getMoney().compareTo(debitMoneyRequest) < 0) {
            throw new UnsufficientFundsException("InsufficientFunds in " + debitAccount);
        }

        debitAccount.setMoney(debitAccount.getMoney().subtract(debitMoneyRequest));

        // Check if debit and credit accounts have the same currency
        if (debitAccount.getMoney().getCurrency().equals(creditAccount.getMoney().getCurrency())) {
            // No conversion needed
            creditAccount.setMoney(creditAccount.getMoney().add(debitMoneyRequest));

            accountRepository.save(debitAccount);
            accountRepository.save(creditAccount);

            return TransferDto.builder()
                    .debitedAmount(debitMoneyRequest)
                    .creditedAmount(debitMoneyRequest) // No conversion, so the credited amount is the same as debited
                    .rate(BigDecimal.ONE) // Set rate to 1.0 since no conversion took place
                    .debitAccountId(debitAccountId)
                    .creditAccountId(creditAccountId)
                    .build();
        }
        FXRateRequest fXRateRequest = createFXRequest(request, creditAccount);

        FXRateResponse fxRateResponse = forexService.exchange(fXRateRequest);


        creditAccount.setMoney(creditAccount.getMoney().add(fxRateResponse.getConvertedAmount()));

        accountRepository.save(debitAccount);
        accountRepository.save(creditAccount);
        BigDecimal rate = fxRateResponse.getExchangeRate().getFactor().numberValue(BigDecimal.class);
        return TransferDto.builder()
                .rate(rate)
                .debitedAmount(debitMoneyRequest)
                .creditedAmount(fxRateResponse.getConvertedAmount())
                .debitAccountId(
                        debitAccountId)
                .creditAccountId(creditAccountId)
                .build();
    }

    private static FXRateRequest createFXRequest(OrderRequest request, Account account) {
        return FXRateRequest.builder().monetaryAmount(Monetary.getDefaultAmountFactory()
                                                              .setCurrency(request.getCurrency())
                                                              .setNumber(
                                                                      request.getAmount())
                                                              .create()).targetCurrency(account.getMoney()
                                                                                                .getCurrency()).build();
    }

    private static FXRateRequest createFXRequest(TransferRequest request, Account account) {
        return FXRateRequest.builder().monetaryAmount(Monetary.getDefaultAmountFactory()
                                                              .setCurrency(request.getCurrency())
                                                              .setNumber(
                                                                      request.getAmount())
                                                              .create()).targetCurrency(account.getMoney()
                                                                                                .getCurrency()).build();
    }

}
