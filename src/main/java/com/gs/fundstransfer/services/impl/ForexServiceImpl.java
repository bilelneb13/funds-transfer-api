package com.gs.fundstransfer.services.impl;

import com.gs.fundstransfer.exceptions.UnavailableFXRatesException;
import com.gs.fundstransfer.request.FXRateRequest;
import com.gs.fundstransfer.response.FXRateResponse;
import com.gs.fundstransfer.services.ForexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.money.MonetaryAmount;
import javax.money.MonetaryException;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.ExchangeRate;
import javax.money.convert.MonetaryConversions;

@Service
@RequiredArgsConstructor
public class ForexServiceImpl implements ForexService {
/*    @Value("${exchange.api.url}")
    String apiUrl;

    @Value("${exchange.api.key}")
    String apiKey;

    @Autowired
    private final RestTemplate restTemplate;


    private Map<String, BigDecimal> fxRates;

    @PostConstruct
    public void loadFx() {
        fxRates = getExchangeRates();
    }

    @Override
    public Map<String, BigDecimal> getExchangeRates() {
        // 1. Set the Bearer token in the headers
        String exchangeRateApiUrl = apiUrl + "?access_key=" + apiKey;
        ResponseEntity<FXRateResponse> response = restTemplate.getForEntity(exchangeRateApiUrl, FXRateResponse.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new HttpServerErrorException(response.getStatusCode());
        }

        return response.getBody() != null ? response.getBody().getRates().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                // Copy the key as-is
                entry -> BigDecimal.valueOf(entry.getValue())
                // Convert Double to BigDecimal
        )) : null;
    }*/

    @Override
    public FXRateResponse exchange(FXRateRequest fXRateRequest) {
        CurrencyConversion conversion = MonetaryConversions.getConversion(fXRateRequest.getTargetCurrency());
        MonetaryAmount convertedAmount = null;
        ExchangeRate exchangeRate;
        try {
            convertedAmount = fXRateRequest.getMonetaryAmount().with(conversion);
            // Retrieve the exchange rate used
            exchangeRate = conversion.getExchangeRate(fXRateRequest.getMonetaryAmount());

        } catch (MonetaryException e) {
            throw new UnavailableFXRatesException("Unable to retrieve exchange rate");
        }
        return FXRateResponse.builder().convertedAmount(convertedAmount).exchangeRate(exchangeRate).build();
    }
}
