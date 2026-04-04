package com.online.MiniUdemy.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Verify your Radical account");
        message.setText("Welcome to Radical!\n\nYour One-Time Password (OTP) for registration is: "
                + otp + "\n\nPlease enter this code to complete your account creation.");

        mailSender.send(message);
    }
}