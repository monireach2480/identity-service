package com.digitalecosystem.identityservice.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SMSService {

    @Value("${app.twilio.account-sid:}")
    private String accountSid;

    @Value("${app.twilio.auth-token:}")
    private String authToken;

    @Value("${app.twilio.phone-number:}")
    private String fromPhoneNumber;

    public void sendOTP(String phoneNumber, String otp) {
        try {
            if (accountSid.isEmpty() || authToken.isEmpty()) {
                log.warn("Twilio not configured. OTP: {} for phone: {}", otp, phoneNumber);
                return;
            }

            Twilio.init(accountSid, authToken);

            Message message = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    "Your OTP code is: " + otp
            ).create();

            log.info("SMS sent successfully. SID: {}", message.getSid());
        } catch (Exception e) {
            log.error("Failed to send SMS", e);
        }
    }
}