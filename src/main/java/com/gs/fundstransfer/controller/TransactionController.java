package com.gs.fundstransfer.controller;

import com.gs.fundstransfer.dto.AccountDto;
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

    @PostMapping("/transfer")
    TransferDto transfer(@Valid @RequestBody TransferRequest request) {
        return transactionService.transfer(request);
    }

    @PostMapping("/withdraw")
    TransferDto withdraw(@Valid @RequestBody OrderRequest request) {
        return transactionService.withdraw(request);
    }
    @PostMapping("/deposit")
    TransferDto deposit(@Valid @RequestBody OrderRequest request) {
        return transactionService.deposit(request);
    }
}
