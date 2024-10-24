package com.gs.fundstransfer.services;

import com.gs.fundstransfer.request.FXRateRequest;
import com.gs.fundstransfer.response.FXRateResponse;

public interface ForexService {

    FXRateResponse exchange(FXRateRequest fXRateRequest);
}
