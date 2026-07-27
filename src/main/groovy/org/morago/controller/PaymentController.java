package org.morago.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.morago.dto.payment.PaymentRequest;
import org.morago.dto.payment.PaymentResponse;
import org.morago.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/deposit")
    public ResponseEntity<PaymentResponse> deposit(
            Authentication authentication,
            @RequestBody @Valid PaymentRequest request
    ) {

        return ResponseEntity.ok(
                paymentService.deposit(
                        authentication.getName(),
                        request
                )
        );
    }

}
