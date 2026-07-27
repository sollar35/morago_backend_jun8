package org.morago.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PaymentResponse {

    private Long id;

    private BigDecimal amount;

    private String type;

    private LocalDateTime createdAt;

}