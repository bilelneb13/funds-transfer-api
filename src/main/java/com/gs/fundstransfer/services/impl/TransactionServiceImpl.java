package com.gs.fundstransfer.services.impl;

import com.gs.fundstransfer.dto.AccountDto;
import com.gs.fundstransfer.dto.TransferDto;
import com.gs.fundstransfer.exceptions.AccountNotFoundException;
import com.gs.fundstransfer.exceptions.NotSupportedCurrencyException;
import com.gs.fundstransfer.exceptions.SameAccountTransferException;
import com.gs.fundstransfer.exceptions.UnsufficientFundsException;
import com.gs.fundstransfer.mapper.AccountMapper;
import com.gs.fundstransfer.model.Account;
import com.gs.fundstransfer.repository.AccountRepository;
import com.gs.fundstransfer.request.FXRateRequest;
import com.gs.fundstransfer.request.OrderRequest;
import com.gs.fundstransfer.request.TransferRequest;
import com.gs.fundstransfer.response.FXRateResponse;
import com.gs.fundstransfer.services.AccountService;
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
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final AccountService accountService;
    private final ForexService forexService;
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Retryable(retryFor = {PessimisticLockException.class, CannotAcquireLockException.class}, maxAttempts = 3, backoff = @Backoff(delay = 200))
    @Transactional
    @Override
    public TransferDto deposit(OrderRequest request) {

        Account account = accountRepository.findByIdWithLock(request.getAccountId()).orElseThrow(() -> new AccountNotFoundException(
                request.getAccountId()));
        FXRateRequest fXRateRequest = createFXRequest(request, account);
        FXRateResponse fxRateResponse = forexService.exchange(fXRateRequest);

        account.setMoney(account.getMoney().add(fxRateResponse.getConvertedAmount()));

        Account saved = accountRepository.save(account);
        return TransferDto.builder().creditAccountId(request.getAccountId()).creditedAmount(fxRateResponse.getConvertedAmount()).rate(
                fxRateResponse.getExchangeRate().getFactor()).build();
    }


    @Retryable(retryFor = {PessimisticLockException.class, CannotAcquireLockException.class}, maxAttempts = 3, backoff = @Backoff(delay = 200))
    @Transactional
    @Override
    public TransferDto withdraw(OrderRequest request) {

        Account account = accountRepository.findByIdWithLock(request.getAccountId()).orElseThrow(() -> new AccountNotFoundException(
                request.getAccountId()));

        FXRateRequest fXRateRequest = createFXRequest(request, account);

        FXRateResponse fxRateResponse = forexService.exchange(fXRateRequest);
        if (account.getMoney().compareTo(fxRateResponse.getConvertedAmount()) < 0) {
            throw new UnsufficientFundsException("InsufficientFunds in " + account);

        }
        account.setMoney(account.getMoney().subtract(fxRateResponse.getConvertedAmount()));

        Account saved = accountRepository.save(account);
        return TransferDto.builder().debitAccountId(request.getAccountId()).debitedAmount(fxRateResponse.getConvertedAmount()).rate(
                fxRateResponse.getExchangeRate().getFactor()).build();
    }

    @Retryable(retryFor = {PessimisticLockException.class, CannotAcquireLockException.class}, maxAttempts = 3, backoff = @Backoff(delay = 200))
    @Transactional
    @Override
    public TransferDto transfer(TransferRequest request) {

        Long debitAccountId = request.getDebitAccountId();
        Long creditAccountId = request.getCreditAccountId();

        if (Objects.equals(debitAccountId, creditAccountId)) {
            throw new SameAccountTransferException("Credit and Debit accounts are the same");
        }

        Account debitAccount = accountRepository.findByIdWithLock(debitAccountId).orElseThrow(() -> new AccountNotFoundException(
                debitAccountId));

        Account creditAccount = accountRepository.findByIdWithLock(creditAccountId).orElseThrow(() -> new AccountNotFoundException(
                creditAccountId));

        if (!debitAccount.getMoney().getCurrency().equals(request.getMonetaryToTransfer().getCurrency())) {
            throw new NotSupportedCurrencyException("The Request Currency should be " + debitAccount.getMoney().getCurrency());
        }
        if (debitAccount.getMoney().compareTo(request.getMonetaryToTransfer()) < 0) {
            throw new UnsufficientFundsException("InsufficientFunds in " + debitAccount);
        }

        debitAccount.setMoney(debitAccount.getMoney().subtract(request.getMonetaryToTransfer()));


        FXRateRequest fXRateRequest = createFXRequest(request, debitAccount);

        FXRateResponse fxRateResponse = forexService.exchange(fXRateRequest);


        creditAccount.setMoney(creditAccount.getMoney().add(fxRateResponse.getConvertedAmount()));

        accountRepository.save(debitAccount);
        accountRepository.save(creditAccount);
        return TransferDto.builder().rate(fxRateResponse.getExchangeRate().getFactor()).debitedAmount(request.getMonetaryToTransfer()).creditedAmount(
                fxRateResponse.getConvertedAmount()).debitAccountId(debitAccountId).creditAccountId(creditAccountId).build();
    }

    private static FXRateRequest createFXRequest(OrderRequest request, Account account) {
        return FXRateRequest.builder().monetaryAmount(Monetary.getDefaultAmountFactory().setCurrency(request.getCurrency().getCurrencyCode()).setNumber(
                request.getAmount()).create()).targetCurrency(account.getMoney().getCurrency()).build();
    }

    private static FXRateRequest createFXRequest(TransferRequest request, Account account) {
        return FXRateRequest.builder().monetaryAmount(Monetary.getDefaultAmountFactory().setCurrency(request.getMonetaryToTransfer().getCurrency().getCurrencyCode()).setNumber(
                request.getMonetaryToTransfer().getNumber()).create()).targetCurrency(account.getMoney().getCurrency()).build();
    }

}
