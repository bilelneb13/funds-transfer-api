package com.gs.fundstransfer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.fundstransfer.dto.AccountDto;
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

import java.util.Collections;
import java.util.List;

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

    @Value("${url}") // Inject the base URL from properties
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
        createAccountRequest.setCurrency("EUR"); // Set other required fields as necessary

        AccountDto accountDto = new AccountDto();
        accountDto.setCurrency("EUR"); // Set other fields as necessary

        when(accountService.save(any(CreateAccountRequest.class))).thenReturn(accountDto);

        // Act & Assert
        mockMvc.perform(post(baseUrl + "/accounts") // Adjust this based on your ${url} property
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountDto.getOwnerId()))
                .andExpect(jsonPath("$.currency").value(accountDto.getCurrency()));
    }

    @Test
    void testCreateAccount_unsuccessfulCreation() throws Exception {
        // Arrange
        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setCurrency("EUR"); // Set other required fields as necessary

        when(accountService.save(any(CreateAccountRequest.class))).thenThrow(new RuntimeException());

        // Act & Assert
        mockMvc.perform(post(baseUrl + "/accounts") // Adjust this based on your ${url} property
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetAccount_success() throws Exception {
        // Arrange
        Long accountId = 1L;
        AccountDto accountDto = new AccountDto();
        accountDto.setCurrency("EUR"); // Set other fields as necessary

        when(accountService.get(accountId)).thenReturn(accountDto);

        // Act & Assert
        mockMvc.perform(get(baseUrl + "/accounts/{id}", accountId) // Adjust this based on your ${url} property
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountDto.getOwnerId()))
                .andExpect(jsonPath("$.currency").value(accountDto.getCurrency()));
    }

    @Test
    void testGetAccount_notFound() throws Exception {
        // Arrange
        Long accountId = 1L;
        when(accountService.get(accountId)).thenThrow(new RuntimeException("Account not found")); // Adjust exception handling

        // Act & Assert
        mockMvc.perform(get(baseUrl + "/accounts/{id}", accountId) // Adjust this based on your ${url} property
                                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }

    @Test
    void testGetAllAccounts_success() throws Exception {
        // Arrange
        AccountDto accountDto1 = new AccountDto();
        accountDto1.setCurrency("EUR"); // Set other fields as necessary

        AccountDto accountDto2 = new AccountDto();
        accountDto2.setCurrency("USD"); // Set other fields as necessary

        when(accountService.getAll()).thenReturn(List.of(accountDto1, accountDto2));

        // Act & Assert
        mockMvc.perform(get("/api/accounts") // Adjust this based on your ${url} property
                                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath(
                "$").isArray()).andExpect(jsonPath("$[0].id").value(accountDto1.getOwnerId())).andExpect(jsonPath(
                "$[1].id").value(accountDto2.getOwnerId()));
    }

    @Test
    void testGetAllAccounts_emptyList() throws Exception {
        // Arrange
        when(accountService.getAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get(baseUrl + "/accounts") // Adjust this based on your ${url} property
                                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath(
                "$").isArray()).andExpect(jsonPath("$").isEmpty());
    }
}
