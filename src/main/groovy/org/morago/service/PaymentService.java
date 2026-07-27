package org.morago.service;

import lombok.RequiredArgsConstructor;
import org.morago.dto.payment.PaymentRequest;
import org.morago.dto.payment.PaymentResponse;
import org.morago.model.Payment;
import org.morago.model.PaymentType;
import org.morago.model.User;
import org.morago.repository.PaymentRepository;
import org.morago.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    @Transactional
    public PaymentResponse deposit(String email,
                                   PaymentRequest request) {

        User user = getCurrentUser(email);

        user.setBalance(
                user.getBalance().add(request.getAmount())
        );

        userRepository.save(user);

        Payment payment = new Payment();

        payment.setUser(user);

        payment.setAmount(request.getAmount());

        payment.setType(PaymentType.DEPOSIT);

        payment.setCreatedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        return new PaymentResponse(
                savedPayment.getId(),
                savedPayment.getAmount(),
                savedPayment.getType().name(),
                savedPayment.getCreatedAt()
        );
    }
}
