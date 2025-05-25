package com.jwliusri.library_service.security.mfa;

import java.util.NoSuchElementException;
import java.util.Random;
import java.util.UUID;

import com.jwliusri.library_service.email.EmailDetail;
import com.jwliusri.library_service.email.EmailService;
import com.jwliusri.library_service.user.User;
import com.jwliusri.library_service.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class MfaOtpService {

    private final UserRepository userRepository;

    private final EmailService emailService;

    private final MfaOtpRepository mfaOtpRepository;

    MfaOtpService(MfaOtpRepository mfaOtpRepository, EmailService emailService, UserRepository userRepository) {
        this.mfaOtpRepository = mfaOtpRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    public String generateOtp(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow();

        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        String requestId = UUID.randomUUID().toString();

        MfaOtp mfaOtp = new MfaOtp(requestId, username, otp);
        mfaOtpRepository.save(mfaOtp);
        
        // send email
        emailService.sendSimpleMail(new EmailDetail(user.getEmail(), "OTP: "+otp, "Library Service OTP"));

        return requestId;
    }

    public boolean validateOtp(String requestId, int otp) throws NoSuchElementException {
        MfaOtp mfaOtp = mfaOtpRepository.findById(requestId)
            .orElseThrow();

        return mfaOtp.getOtp() == otp;
    }

    public String getUsernameFromOtp(String requestId) {
        MfaOtp mfaOtp = mfaOtpRepository.findById(requestId)
            .orElseThrow();

        return mfaOtp.getUsername();
    }
}
