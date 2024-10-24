package com.gs.fundstransfer.services;

import com.gs.fundstransfer.dto.TransferDto;
import com.gs.fundstransfer.request.OrderRequest;
import com.gs.fundstransfer.request.TransferRequest;

public interface TransactionService {

    TransferDto deposit(OrderRequest request);

    TransferDto withdraw(OrderRequest request);

    TransferDto transfer(TransferRequest request);
}
