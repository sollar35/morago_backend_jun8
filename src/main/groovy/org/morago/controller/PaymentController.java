package org.morago.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.morago.dto.payment.PaymentRequest;
import org.morago.dto.payment.PaymentResponse;
import org.morago.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/my")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                paymentService.getMyPayments(authentication.getName())
        );
    }

    @PostMapping("/withdraw")
    public ResponseEntity<PaymentResponse> withdraw(
            Authentication authentication,
            @Valid @RequestBody PaymentRequest request
    ) {
        return ResponseEntity.ok(
                paymentService.withdraw(authentication.getName(), request)
        );
    }
}
