package com.gs.fundstransfer.mapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import javax.money.MonetaryAmount;
import java.io.IOException;

public class MonetaryAmountSerializer extends JsonSerializer<MonetaryAmount> {
    @Override
    public void serialize(MonetaryAmount o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("amount",
                                       o.getNumber()
                                               .doubleValueExact());
        jsonGenerator.writeStringField("currency",
                                       o.getCurrency()
                                               .getCurrencyCode());
        jsonGenerator.writeEndObject();
    }
}
