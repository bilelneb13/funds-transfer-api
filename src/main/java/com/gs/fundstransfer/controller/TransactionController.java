package com.gs.fundstransfer.controller;

import com.gs.fundstransfer.dto.TransferDto;
import com.gs.fundstransfer.request.TransferRequest;
import com.gs.fundstransfer.request.OrderRequest;
import com.gs.fundstransfer.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Transaction Controller", description = "Controller for handling transactions including transfers, withdrawals, and deposits")
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
    @Operation(summary = "Transfer funds between accounts", description = "Transfers a specified amount from one account to another.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer completed successfully",
                    content = @Content(schema = @Schema(implementation = TransferDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid transfer request"),
            @ApiResponse(responseCode = "500", description = "Internal server error during transfer operation")
    })
    @PostMapping("/transfer")
    public TransferDto transfer(@Valid @RequestBody TransferRequest request) {
        return transactionService.transfer(request);
    }

    /**
     * Processes a withdrawal request.
     *
     * @param request the request containing withdrawal details such as account ID, amount, and currency
     * @return a TransferDto object containing details of the completed withdrawal transaction
     */
    @Operation(summary = "Withdraw funds", description = "Processes a withdrawal request from a specified account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Withdrawal successful",
                    content = @Content(schema = @Schema(implementation = TransferDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid withdrawal request"),
            @ApiResponse(responseCode = "500", description = "Internal server error during withdrawal operation")
    })
    @PostMapping("/withdraw")
    public TransferDto withdraw(@Valid @RequestBody OrderRequest request) {
        return transactionService.withdraw(request);
    }

    /**
     * Handles the deposit operation for a given account.
     *
     * @param request the order request containing account information and the deposit amount
     * @return a TransferDto object containing details of the deposit transaction
     */
    @Operation(summary = "Deposit funds", description = "Processes a deposit into a specified account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deposit successful",
                    content = @Content(schema = @Schema(implementation = TransferDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid deposit request"),
            @ApiResponse(responseCode = "500", description = "Internal server error during deposit operation")
    })
    @PostMapping("/deposit")
    public TransferDto deposit(@Valid @RequestBody OrderRequest request) {
        return transactionService.deposit(request);
    }
}
