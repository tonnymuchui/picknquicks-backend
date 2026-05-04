package com.picknquicks.service.payment;

import com.picknquicks.dto.mpesa.MpesaCallbackRequest;
import com.picknquicks.dto.mpesa.MpesaStkPushResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface MpesaService {

    MpesaStkPushResponse initiateStkPush(UUID orderId, String phoneNumber, BigDecimal amount);

    void handleCallback(MpesaCallbackRequest callback);

    String queryTransaction(String checkoutRequestId);
}