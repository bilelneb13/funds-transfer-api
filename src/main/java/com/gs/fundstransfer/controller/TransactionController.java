package com.gs.fundstransfer.controller;

import com.gs.fundstransfer.dto.TransferDto;
import com.gs.fundstransfer.request.OrderRequest;
import com.gs.fundstransfer.request.TransferRequest;
import com.gs.fundstransfer.services.TransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "transactions", description = "The controller responsible for transactions operations")
@RequestMapping("${url}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Handles the transfer of funds between two accounts.
     *
     * @param request the transfer request containing debit account ID, credit account ID, amount, and currency
     * @return a TransferDto object containing the details of the transfer, including debited and credited amounts and currency
     */
    @PostMapping("/transfer")
    TransferDto transfer(@Valid @RequestBody TransferRequest request) {
        return transactionService.transfer(request);
    }

    /**
     * Processes a withdrawal request.
     *
     * @param request the request containing withdrawal details such as account ID, amount, and currency
     * @return a TransferDto object containing details of the completed withdrawal transaction
     */
    @PostMapping("/withdraw")
    TransferDto withdraw(@Valid @RequestBody OrderRequest request) {
        return transactionService.withdraw(request);
    }

    /**
     * Handles the deposit operation for a given account.
     *
     * @param request the order request containing account information and the deposit amount
     * @return a TransferDto object containing details of the deposit transaction
     */
    @PostMapping("/deposit")
    TransferDto deposit(@Valid @RequestBody OrderRequest request) {
        return transactionService.deposit(request);
    }
}
