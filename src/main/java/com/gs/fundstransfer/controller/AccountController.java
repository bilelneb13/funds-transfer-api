package com.gs.fundstransfer.controller;

import com.gs.fundstransfer.dto.AccountDto;
import com.gs.fundstransfer.request.CreateAccountRequest;
import com.gs.fundstransfer.services.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(
        name = "account",
        description = "The controller responsible for accounts operations"
)
@RequestMapping(path = "${base_url}/accounts")
@RequiredArgsConstructor
public class AccountController {

    /**
     * Service responsible for handling account-related operations.
     * It provides methods to save and retrieve account information.
     */
    private final AccountService accountService;

    /**
     * Creates a new account.
     *
     * @param account the CreateAccountRequest object containing the details of the account to be created
     * @return a ResponseEntity containing the created account
     */
    @Operation(summary = "Create a new account", description = "Creates a new account with the given details.")
    @ApiResponse(responseCode = "200", description = "Account created successfully",
            content = @Content(schema = @Schema(implementation = AccountDto.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping
    public ResponseEntity<AccountDto> create(@Valid @RequestBody CreateAccountRequest account) {
        return ResponseEntity.ok(accountService.save(account));
    }

    /**
     * Retrieves an account by its unique identifier.
     *
     * @param id the unique identifier of the account to retrieve
     * @return a {@code ResponseEntity} containing the {@code AccountDto} of the requested account
     */
    @Operation(summary = "Get account by ID", description = "Retrieves account details by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "Account retrieved successfully",
            content = @Content(schema = @Schema(implementation = AccountDto.class)))
    @ApiResponse(responseCode = "404", description = "Account not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @Parameter(name = "id", description = "Unique identifier of the account", required = true)
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.get(id));
    }

    /**
     * Retrieves a list of all accounts.
     *
     * @return a ResponseEntity containing a list of AccountDto objects representing all accounts
     */
    @Operation(summary = "Get all accounts", description = "Returns a list of all account details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of accounts"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<AccountDto>> getAll() {
        return ResponseEntity.ok(accountService.getAll());
    }
}
