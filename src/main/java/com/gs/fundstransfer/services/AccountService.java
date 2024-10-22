package com.gs.fundstransfer.services;

import com.gs.fundstransfer.dto.AccountDto;
import com.gs.fundstransfer.request.CreateAccountRequest;

import java.util.List;

public interface AccountService {

    AccountDto save(CreateAccountRequest account);

    AccountDto get(Long id);

    List<AccountDto> getAll();
}
