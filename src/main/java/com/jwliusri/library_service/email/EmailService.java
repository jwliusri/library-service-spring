package com.jwliusri.library_service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired private JavaMailSender javaMailSender;

    @Value("${spring.mail.from}") private String fromAddress;

    public void sendSimpleMail(EmailDetail detail)
    {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setFrom(fromAddress);
        mailMessage.setTo(detail.getRecipient());
        mailMessage.setText(detail.getMsgBody());
        mailMessage.setSubject(detail.getSubject());

        javaMailSender.send(mailMessage);
    }
}
