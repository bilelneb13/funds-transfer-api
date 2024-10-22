package com.gs.fundstransfer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.fundstransfer.dto.AccountDto;
import com.gs.fundstransfer.request.CreateAccountRequest;
import com.gs.fundstransfer.services.AccountService;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Currency;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {
    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;

    @Test
    void testCreateAccount_successfulCreation() throws Exception {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();

        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        // Set required fields for CreateAccountRequest object

        AccountDto accountDto = new AccountDto();
        // Set required fields for AccountDto object

        when(accountService.save(any(CreateAccountRequest.class))).thenReturn(accountDto);

        mockMvc.perform(post("/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(createAccountRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateAccount_unsuccessfulCreation() throws Exception {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();

        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        // Set required fields for CreateAccountRequest object

        when(accountService.save(any(CreateAccountRequest.class))).thenThrow(new RuntimeException());

        mockMvc.perform(post("/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(createAccountRequest)))
                .andExpect(status().isInternalServerError());
    }
}
