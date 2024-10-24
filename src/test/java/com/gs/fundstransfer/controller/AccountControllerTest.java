package com.gs.fundstransfer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.fundstransfer.dto.AccountDto;
import com.gs.fundstransfer.exceptions.AccountNotFoundException;
import com.gs.fundstransfer.request.CreateAccountRequest;
import com.gs.fundstransfer.services.AccountService;
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
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    private ObjectMapper objectMapper;

    @Value("${url}")
    private String baseUrl;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateAccount_successfulCreation() throws Exception {
        // Arrange
        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setCurrency("EUR");

        AccountDto accountDto = new AccountDto();
        accountDto.setCurrency("EUR");
        accountDto.setBalance(BigDecimal.ZERO);
        accountDto.setOwnerId(4336178608L);
        when(accountService.save(any(CreateAccountRequest.class))).thenReturn(accountDto);

        // Act & Assert
        mockMvc.perform(post(baseUrl + "/accounts").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId", greaterThanOrEqualTo(1000000000L)))
                .andExpect(jsonPath("$.ownerId", lessThanOrEqualTo(9999999999L)))
                .andExpect(jsonPath("$.balance").value(accountDto.getBalance()))
                .andExpect(jsonPath("$.currency").value(accountDto.getCurrency()));
    }

    @Test
    void testCreateAccount_unsuccessfulCreation() throws Exception {
        // Arrange
        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setCurrency(null);

            when(accountService.save(any(CreateAccountRequest.class))).thenThrow(new RuntimeException());

        // Act & Assert
        mockMvc.perform(post(baseUrl + "/accounts").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAccount_success() throws Exception {
        // Arrange
        Long accountId = 1000000000L;
        AccountDto accountDto = new AccountDto();
        accountDto.setCurrency("EUR");
        accountDto.setBalance(BigDecimal.ZERO);
        accountDto.setOwnerId(accountId);

        when(accountService.get(accountId)).thenReturn(accountDto);

        // Act & Assert
        mockMvc.perform(get(baseUrl + "/accounts/{id}", accountId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").value(accountDto.getOwnerId()))
                .andExpect(jsonPath("$.currency").value(accountDto.getCurrency()))
                .andExpect(jsonPath("$.balance").value(accountDto.getBalance()));
    }

    @Test
    void testGetAccount_notFound() throws Exception {
        // Arrange
        Long accountId = 1000000000L;
        when(accountService.get(accountId)).thenThrow(new AccountNotFoundException(1000000000L));

        // Act & Assert
        mockMvc.perform(get(baseUrl + "/accounts/{id}", accountId) // Adjust this based on your ${url} property
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllAccounts_success() throws Exception {
        // Arrange
        AccountDto accountDto1 = new AccountDto();
        accountDto1.setCurrency("EUR");
        accountDto1.setOwnerId(1000000000L);
        accountDto1.setBalance(BigDecimal.ZERO);

        AccountDto accountDto2 = new AccountDto();
        accountDto2.setCurrency("USD");
        accountDto2.setOwnerId(1000000001L);
        accountDto2.setBalance(BigDecimal.ZERO);

        when(accountService.getAll()).thenReturn(List.of(accountDto1, accountDto2));

        mockMvc.perform(get(baseUrl + "/accounts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ownerId").value(accountDto1.getOwnerId()))
                .andExpect(jsonPath("$[0].currency").value(accountDto1.getCurrency()))
                .andExpect(jsonPath("$[0].balance").value(accountDto1.getBalance()))
                .andExpect(jsonPath("$[1].ownerId").value(accountDto2.getOwnerId()))
                .andExpect(jsonPath("$[1].currency").value(accountDto2.getCurrency()))
                .andExpect(jsonPath("$[1].balance").value(accountDto2.getBalance()));

    }

    @Test
    void testGetAllAccounts_emptyList() throws Exception {
        when(accountService.getAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(baseUrl + "/accounts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
