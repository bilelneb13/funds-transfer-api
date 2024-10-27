package com.gs.fundstransfer.services.impl;

import com.gs.fundstransfer.dto.AccountDto;
import com.gs.fundstransfer.exceptions.AccountNotFoundException;
import com.gs.fundstransfer.exceptions.NotSupportedCurrencyException;
import com.gs.fundstransfer.mapper.AccountMapper;
import com.gs.fundstransfer.model.Account;
import com.gs.fundstransfer.repository.AccountRepository;
import com.gs.fundstransfer.request.CreateAccountRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.gs.fundstransfer.services.impl.AccountServiceImpl.createInitialAccount;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {

    public static final long ACCOUNT_ID_1 = 1000000000L;
    public static final long ACCOUNT_ID_2 = 1000000001L;
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        accountService = new AccountServiceImpl(accountRepository, accountMapper);
    }


    @Test
    public void testSaveAccount() {
        CreateAccountRequest request = new CreateAccountRequest("USD");

        Account account = Account.builder()
                .money(createInitialAccount("USD"))
                .build();

        AccountDto accountDto = AccountDto.builder()
                .ownerId(ACCOUNT_ID_1)
                .currency("USD")
                .balance(BigDecimal.ZERO)
                .build();

        // Mock repository save behavior and mapper conversion
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        // Execute the save method
        AccountDto result = accountService.save(request);

        // Assert
        assertEquals(accountDto, result);
    }

    @Test
    void testSaveAccount_Successful() {
        CreateAccountRequest accountRequest = new CreateAccountRequest();
        accountRequest.setCurrency("USD");

        Account account = Account.builder()
                .money(createInitialAccount("USD"))
                .build();

        Account savedAccount = Account.builder()
                .money(createInitialAccount("USD"))
                .build();

        AccountDto accountDto = new AccountDto();
        accountDto.setOwnerId(ACCOUNT_ID_1);
        accountDto.setBalance(BigDecimal.ZERO);
        accountDto.setCurrency("USD");

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        when(accountMapper.toDto(savedAccount)).thenReturn(accountDto);

        AccountDto result = accountService.save(accountRequest);

        assertNotNull(result);
        assertEquals(ACCOUNT_ID_1, result.getOwnerId());
        assertEquals("USD", result.getCurrency());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        verify(accountRepository, times(1)).save(account);
        verify(accountMapper, times(1)).toDto(savedAccount);
    }

    @Test
    void testSaveAccount_NotSupportedCurrencyException() {
        CreateAccountRequest accountRequest = new CreateAccountRequest();
        accountRequest.setCurrency("INVALID");

        assertThrows(NotSupportedCurrencyException.class, () -> accountService.save(accountRequest));
        verify(accountRepository, never()).save(any(Account.class));
    }


    @Test
    void testGetAccountById_Successful() {
        Long accountId = ACCOUNT_ID_1;

        Account account = Account.builder()
                .ownerId(accountId)
                .money(createInitialAccount("USD"))
                .build();

        AccountDto accountDto = new AccountDto();
        accountDto.setOwnerId(accountId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        AccountDto result = accountService.get(accountId);

        assertNotNull(result);
        assertEquals(accountId, result.getOwnerId());
        verify(accountRepository, times(1)).findById(accountId);
        verify(accountMapper, times(1)).toDto(account);
    }

    @Test
    void testGetAccountById_AccountNotFoundException() {
        Long accountId = ACCOUNT_ID_1;

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.get(accountId));
        verify(accountRepository, times(1)).findById(accountId);
    }


    @Test
    void testGetAllAccounts_Successful() {
        Account account1 = Account.builder()
                .ownerId(ACCOUNT_ID_1)
                .money(createInitialAccount("USD"))
                .build();

        Account account2 = Account.builder()
                .ownerId(ACCOUNT_ID_2)
                .money(createInitialAccount("EUR"))
                .build();

        AccountDto accountDto1 = new AccountDto();
        accountDto1.setOwnerId(ACCOUNT_ID_1);

        AccountDto accountDto2 = new AccountDto();
        accountDto2.setOwnerId(ACCOUNT_ID_2);

        when(accountRepository.findAll()).thenReturn(List.of(account1, account2));
        when(accountMapper.toDtos(List.of(account1, account2))).thenReturn(List.of(accountDto1, accountDto2));

        List<AccountDto> result = accountService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(ACCOUNT_ID_1, result.getFirst().getOwnerId());
        assertEquals(ACCOUNT_ID_2, result.get(1).getOwnerId());
        verify(accountRepository, times(1)).findAll();
        verify(accountMapper, times(1)).toDtos(List.of(account1, account2));
    }
}