package org.example.journal_app.services;

import org.example.journal_app.entities.ResetToken;
import org.example.journal_app.repositories.ResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TokenService {

    @Autowired
    private ResetTokenRepository resetTokenRepository;

    public String generateResetToken(String email) {
        String token = UUID.randomUUID().toString();

        ResetToken resetToken = new ResetToken();
        resetToken.setEmail(email);
        resetToken.setToken(token);
        resetToken.setExpirationTime(System.currentTimeMillis() + 3600000); // 1 hour expiration

        resetTokenRepository.save(resetToken);
        return token;
    }

    public boolean validateResetToken(String token) {
        ResetToken resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (resetToken.getExpirationTime() < System.currentTimeMillis()) {
            return false;
        }
        return true;
    }
}

