package com.gs.fundstransfer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.fundstransfer.dto.AccountDto;
import com.gs.fundstransfer.dto.TransferDto;
import com.gs.fundstransfer.exceptions.AccountNotFoundException;
import com.gs.fundstransfer.exceptions.NotSupportedCurrencyException;
import com.gs.fundstransfer.request.CreateAccountRequest;
import com.gs.fundstransfer.request.TransferRequest;
import com.gs.fundstransfer.services.AccountService;
import com.gs.fundstransfer.services.TransactionService;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private TransactionService transactionService;

    private ObjectMapper objectMapper;

    @Value("${url}")
    private String baseUrl;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }


    @Test
    void testTransfer_success() throws Exception {
        // Arrange
        Long debitAccountId = 1000000000L;
        Long creditAccountId = 1000000001L;
        BigDecimal amount = BigDecimal.valueOf(100);
        String currency = "EUR";

        TransferRequest transferRequest = new TransferRequest(debitAccountId, creditAccountId, amount, currency);

        TransferDto expectedTransferDto = new TransferDto();
        expectedTransferDto.setDebitedAmount(Money.of(amount, currency));
        expectedTransferDto.setCreditedAmount(Money.of(amount, currency));
        expectedTransferDto.setDebitAccountId(debitAccountId);
        expectedTransferDto.setCreditAccountId(creditAccountId);
        expectedTransferDto.setRate(BigDecimal.ONE);

        when(transactionService.transfer(any(TransferRequest.class))).thenReturn(expectedTransferDto);

        // Act & Assert
        mockMvc.perform(post(baseUrl + "/transactions/transfer").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debitedAmount.currency").value(expectedTransferDto.getDebitedAmount().getCurrency().getCurrencyCode()))
                .andExpect(jsonPath("$.creditedAmount.currency").value(expectedTransferDto.getCreditedAmount().getCurrency().getCurrencyCode()))
                .andExpect(jsonPath("$.creditedAmount.amount").value(expectedTransferDto.getCreditedAmount().getNumber().doubleValue()))
                .andExpect(jsonPath("$.debitedAmount.amount").value(expectedTransferDto.getDebitedAmount().getNumber().doubleValue()))
                .andExpect(jsonPath("$.creditAccountId").value(expectedTransferDto.getCreditAccountId()))
                .andExpect(jsonPath("$.debitAccountId").value(expectedTransferDto.getDebitAccountId()))
                .andExpect(jsonPath("$.rate").value(expectedTransferDto.getRate()));

    }

    @Test
    void testTransfer_failure() throws Exception {
        // Arrange
        Long debitAccountId = 1000000000L;
        Long creditAccountId = 1000000001L;
        BigDecimal amount = BigDecimal.valueOf(100);
        String currency = "EUR";

        TransferRequest transferRequest = new TransferRequest(debitAccountId, creditAccountId, amount, currency);

        when(transactionService.transfer(any(TransferRequest.class))).thenThrow(new NotSupportedCurrencyException("Not supported currency"));

        // Act & Assert
        mockMvc.perform(post(baseUrl + "/transactions/transfer").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());
    }
}
