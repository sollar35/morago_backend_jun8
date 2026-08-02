package org.morago.service;

import lombok.RequiredArgsConstructor;
import org.morago.dto.call.CallRequest;
import org.morago.dto.call.CallResponse;
import org.morago.model.*;
import org.morago.repository.CallRepository;
import org.morago.repository.PaymentRepository;
import org.morago.repository.TranslatorProfileRepository;
import org.morago.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CallService {

    private final CallRepository callRepository;

    private final UserRepository userRepository;

    private final TranslatorProfileRepository translatorProfileRepository;

    private static final BigDecimal PRICE_PER_MINUTE = BigDecimal.valueOf(100);
    private final PaymentRepository paymentRepository;


    public CallResponse create(
            String email,
            CallRequest request
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        TranslatorProfile translator = translatorProfileRepository.findById(request.getTranslatorId())
                .orElseThrow(() ->
                        new RuntimeException("Translator not found"));

        Call call = new Call();

        LocalDateTime now = LocalDateTime.now();

        call.setClient(user);

        call.setTranslator(translator);

        call.setStatus(CallStatus.CREATED);

        call.setStartTime(now);

        call.setCost(BigDecimal.ZERO);

        call.setCreatedAt(now);

        Call savedCall = callRepository.save(call);

        return new CallResponse(savedCall.getId(),
                savedCall.getClient().getEmail(),
                savedCall.getTranslator().getUser().getEmail(),
                savedCall.getStatus(),
                savedCall.getCost()
        );

    }

    public List<CallResponse> getAll() {

        return callRepository.findAll()
                .stream()
                .map(call ->
                        new CallResponse(
                                call.getId(),
                                call.getClient().getEmail(),
                                call.getTranslator().getUser().getEmail(),
                                call.getStatus(),
                                call.getCost()
                        )
                )
                .toList();
    }

    public void delete(Long id) {

        callRepository.deleteById(id);

    }

    @Transactional
    public CallResponse finish(Long id) {

        Call call = callRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Call not found"));

        if (call.getStatus() == CallStatus.FINISHED) {
            throw new RuntimeException("Call already finished");
        }

        if (call.getStatus() == CallStatus.CANCELLED) {
            throw new RuntimeException("Call already cancelled");
        }

    if (call.getStatus() == CallStatus.CREATED) {
        throw new RuntimeException("Call is not started");
    }

        LocalDateTime now = LocalDateTime.now();

        call.setStatus(CallStatus.FINISHED);

        call.setEndTime(now);

        call.setUpdatedAt(now);

        long minutes = Duration.between(call.getStartTime(), now).toMinutes();

        if (minutes < 0) {
            throw new RuntimeException("Invalid call duration");
        }

        BigDecimal cost = BigDecimal.valueOf(minutes).multiply(PRICE_PER_MINUTE);

        call.setCost(cost);

        User client = call.getClient();
        User translator = call.getTranslator().getUser();

        if (client.getBalance().compareTo(cost) < 0) {
            throw new RuntimeException("Not enough balance");
        }

        client.setBalance(
                client.getBalance().subtract(cost)
        );

        translator.setBalance(
                translator.getBalance().add(cost)
        );

        userRepository.save(client);
        userRepository.save(translator);

        Payment clientPayment = new Payment();
        clientPayment.setUser(client);
        clientPayment.setCall(call);
        clientPayment.setAmount(cost.negate());
        clientPayment.setType(PaymentType.CALL_PAYMENT);
        clientPayment.setCreatedAt(now);

        paymentRepository.save(clientPayment);

        Payment translatorPayment = new Payment();
        translatorPayment.setUser(translator);
        translatorPayment.setCall(call);
        translatorPayment.setAmount(cost);
        translatorPayment.setType(PaymentType.CALL_PAYMENT);
        translatorPayment.setCreatedAt(now);

        paymentRepository.save(translatorPayment);


        Call savedCall = callRepository.save(call);

        return new CallResponse(
                savedCall.getId(),

                savedCall.getClient().getEmail(),

                savedCall.getTranslator().getUser().getEmail(),

                savedCall.getStatus(),

                savedCall.getCost()
        );

    }

    public CallResponse cancel(Long id) {

        Call call = callRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Call not found"));

        if (call.getStatus() == CallStatus.FINISHED) {
            throw new RuntimeException("Call already finished");
        }

        if (call.getStatus() == CallStatus.CANCELLED) {
            throw new RuntimeException("Call already cancelled");
        }

        LocalDateTime now = LocalDateTime.now();

        call.setStatus(CallStatus.CANCELLED);

        call.setEndTime(now);

        call.setUpdatedAt(now);

        Call savedCall = callRepository.save(call);

        return new CallResponse(
                savedCall.getId(),

                savedCall.getClient().getEmail(),

                savedCall.getTranslator().getUser().getEmail(),

                savedCall.getStatus(),

                savedCall.getCost()
        );

    }

    public CallResponse start(Long id) {

        Call call = callRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        if (call.getStatus() == CallStatus.IN_PROGRESS) {
            throw new RuntimeException("Call already started");
        }

        if (call.getStatus() == CallStatus.FINISHED) {
            throw new RuntimeException("Call already finished");
        }

        if (call.getStatus() == CallStatus.CANCELLED) {
            throw new RuntimeException("Call already cancelled");
        }

        LocalDateTime now = LocalDateTime.now();

        call.setStatus(CallStatus.IN_PROGRESS);

        call.setUpdatedAt(now);

        Call savedCall = callRepository.save(call);

        return new CallResponse(
                savedCall.getId(),

                savedCall.getClient().getEmail(),

                savedCall.getTranslator().getUser().getEmail(),

                savedCall.getStatus(),

                savedCall.getCost()
        );

    }

}
