package com.digitalecosystem.identityservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void sendOTP(String email, String otp) {
        // TODO: Implement actual email sending (e.g., using JavaMailSender)
        // For now, just log
        log.info("Sending OTP {} to email: {}", otp, email);

        // Example implementation:
        // SimpleMailMessage message = new SimpleMailMessage();
        // message.setTo(email);
        // message.setSubject("Your OTP Code");
        // message.setText("Your OTP code is: " + otp);
        // mailSender.send(message);
    }
}