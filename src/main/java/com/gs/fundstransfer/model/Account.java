package com.gs.fundstransfer.model;

import com.gs.fundstransfer.mapper.CurrencyUnitConverter;
import com.gs.fundstransfer.mapper.MonetaryAmountConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.Currency;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-account-id-gen")
    @GenericGenerator(name = "custom-account-id-gen", type = AccountIdGenerator.class, parameters = {})
    Long ownerId;
/*    @Column(nullable = false, length = 3)
    @Convert(converter = CurrencyUnitConverter.class)
    CurrencyUnit currency;*/

    @Column(nullable = false)
    @Convert(converter = MonetaryAmountConverter.class)
    MonetaryAmount money;


}
