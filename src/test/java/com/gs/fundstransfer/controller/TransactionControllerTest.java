package com.gs.fundstransfer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.fundstransfer.dto.TransferDto;
import com.gs.fundstransfer.exceptions.NotSupportedCurrencyException;
import com.gs.fundstransfer.request.OrderRequest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
                .andExpect(jsonPath("$.debitedAmount.currency").value(expectedTransferDto.getDebitedAmount()
                                                                              .getCurrency()
                                                                              .getCurrencyCode()))
                .andExpect(jsonPath("$.creditedAmount.currency").value(expectedTransferDto.getCreditedAmount()
                                                                               .getCurrency()
                                                                               .getCurrencyCode()))
                .andExpect(jsonPath("$.creditedAmount.amount").value(expectedTransferDto.getCreditedAmount()
                                                                             .getNumber()
                                                                             .doubleValue()))
                .andExpect(jsonPath("$.debitedAmount.amount").value(expectedTransferDto.getDebitedAmount()
                                                                            .getNumber()
                                                                            .doubleValue()))
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

        when(transactionService.transfer(any(TransferRequest.class))).thenThrow(new NotSupportedCurrencyException(
                "Not supported currency"));

        // Act & Assert
        mockMvc.perform(post(baseUrl + "/transactions/transfer").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testWithdraw_Successful() throws Exception {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setAccountId(1000000001L);
        orderRequest.setAmount(BigDecimal.valueOf(50));
        orderRequest.setCurrency("EUR");

        TransferDto transferDto = new TransferDto();
        transferDto.setDebitAccountId(1000000001L);
        transferDto.setDebitedAmount(Money.of(BigDecimal.valueOf(50), "EUR"));
        transferDto.setRate(BigDecimal.ONE);

        when(transactionService.withdraw(any(OrderRequest.class))).thenReturn(transferDto);

        mockMvc.perform(post(baseUrl + "/transactions/withdraw").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debitAccountId").value(transferDto.getDebitAccountId()))
                .andExpect(jsonPath("$.debitedAmount.currency").value(transferDto.getDebitedAmount()
                                                                              .getCurrency()
                                                                              .getCurrencyCode()))
                .andExpect(jsonPath("$.debitedAmount.amount").value(transferDto.getDebitedAmount()
                                                                            .getNumber()
                                                                            .doubleValue()))
                .andExpect(jsonPath("$.rate").value(transferDto.getRate()));
    }

    @Test
    void testDeposit_Successful() throws Exception {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setAccountId(1000000001L);
        orderRequest.setAmount(BigDecimal.valueOf(200));
        orderRequest.setCurrency("EUR");

        TransferDto transferDto = new TransferDto();
        transferDto.setCreditAccountId(1000000001L);
        transferDto.setCreditedAmount(Money.of(BigDecimal.valueOf(200), "EUR"));
        transferDto.setRate(BigDecimal.ONE);

        when(transactionService.deposit(any(OrderRequest.class))).thenReturn(transferDto);

        mockMvc.perform(post(baseUrl + "/transactions/deposit").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creditAccountId").value(transferDto.getCreditAccountId()))
                .andExpect(jsonPath("$.creditedAmount.currency").value(transferDto.getCreditedAmount()
                                                                               .getCurrency()
                                                                               .getCurrencyCode()))
                .andExpect(jsonPath("$.creditedAmount.amount").value(transferDto.getCreditedAmount()
                                                                             .getNumber()
                                                                             .doubleValue()))
                .andExpect(jsonPath("$.rate").value(transferDto.getRate()));
    }
}
