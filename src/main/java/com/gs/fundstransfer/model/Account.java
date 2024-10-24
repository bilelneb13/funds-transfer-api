package com.gs.fundstransfer.model;

import com.gs.fundstransfer.mapper.MonetaryAmountConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.money.MonetaryAmount;

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
    @Column(nullable = false)
    @Convert(converter = MonetaryAmountConverter.class)
    MonetaryAmount money;


}
