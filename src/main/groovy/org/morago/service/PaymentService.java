package org.morago.service;

import lombok.RequiredArgsConstructor;
import org.morago.dto.payment.PaymentRequest;
import org.morago.dto.payment.PaymentResponse;
import org.morago.exception.UserNotFoundException;
import org.morago.exception.InsufficientBalanceException;
import org.morago.model.Call;
import org.morago.model.Payment;
import org.morago.model.PaymentType;
import org.morago.model.User;
import org.morago.repository.PaymentRepository;
import org.morago.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));
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

    public List<PaymentResponse> getMyPayments(String email) {

        User user = getCurrentUser(email);

        return paymentRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(payment -> new PaymentResponse(
                        payment.getId(),
                        payment.getAmount(),
                        payment.getType().name(),
                        payment.getCreatedAt()
                ))
                .toList();

    }

    @Transactional
    public PaymentResponse withdraw(String email, PaymentRequest request) {

        User user = getCurrentUser(email);

        if (user.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        user.setBalance(user.getBalance().subtract(request.getAmount()));
        userRepository.save(user);

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmount(request.getAmount());
        payment.setType(PaymentType.WITHDRAW);
        payment.setCreatedAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        return new PaymentResponse(
                saved.getId(), saved.getAmount(),
                saved.getType().name(), saved.getCreatedAt()
        );
    }

//    @Transactional
//    public void chargeForCall(User user, BigDecimal amount) {
//        if (user.getBalance().compareTo(amount) < 0) {
//            throw new InsufficientBalanceException("Insufficient Balance for call");
//        }
//        user.setBalance(user.getBalance().subtract(amount));
//        userRepository.save(user);
//
//        Payment payment = new Payment();
//        payment.setUser(user);
//        payment.setAmount(amount);
//        payment.setType(PaymentType.CALL_PAYMENT);
//        payment.setCreatedAt(LocalDateTime.now());
//        paymentRepository.save(payment);
//    }

    @Transactional
    public void refund(User user, BigDecimal amount) {
        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setType(PaymentType.REFUND);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    @Transactional
    public void payForCall(User client, User translator, BigDecimal amount, Call call) {

        if (client.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient Balance for call");
        }

        client.setBalance(client.getBalance().subtract(amount));
        translator.setBalance(translator.getBalance().add(amount));

        userRepository.save(client);
        userRepository.save(translator);

        LocalDateTime now = LocalDateTime.now();

        Payment clientPayment = new Payment();
        clientPayment.setUser(client);
        clientPayment.setCall(call);
        clientPayment.setAmount(amount.negate());
        clientPayment.setType(PaymentType.CALL_PAYMENT);
        clientPayment.setCreatedAt(now);
        paymentRepository.save(clientPayment);

        Payment translatorPayment = new Payment();
        translatorPayment.setUser(translator);
        translatorPayment.setCall(call);
        translatorPayment.setAmount(amount);
        translatorPayment.setType(PaymentType.CALL_PAYMENT);
        translatorPayment.setCreatedAt(now);
        paymentRepository.save(translatorPayment);
    }
}
