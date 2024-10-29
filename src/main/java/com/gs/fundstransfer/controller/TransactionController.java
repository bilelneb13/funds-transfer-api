package com.gs.fundstransfer.controller;

import com.gs.fundstransfer.dto.TransferDto;
import com.gs.fundstransfer.request.OrderRequest;
import com.gs.fundstransfer.request.TransferRequest;
import com.gs.fundstransfer.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Transfer funds", description = "Endpoint for transferring funds between accounts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer successful", content = @Content(schema = @Schema(implementation = TransferDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/transfer")
    TransferDto transfer(@Valid @RequestBody TransferRequest request) {
        return transactionService.transfer(request);
    }

    @Operation(summary = "Withdraw funds", description = "Endpoint for withdrawing funds from an account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Withdrawal successful", content = @Content(schema = @Schema(implementation = TransferDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/withdraw")
    TransferDto withdraw(@Valid @RequestBody OrderRequest request) {
        return transactionService.withdraw(request);
    }

    @Operation(summary = "Deposit funds", description = "Endpoint for depositing funds to an account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deposit successful", content = @Content(schema = @Schema(implementation = TransferDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/deposit")
    TransferDto deposit(@Valid @RequestBody OrderRequest request) {
        return transactionService.deposit(request);
    }
}
