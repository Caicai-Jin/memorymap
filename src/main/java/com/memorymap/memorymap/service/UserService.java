package com.memorymap.memorymap.service;

import com.memorymap.memorymap.exception.DuplicateEmailException;
import com.memorymap.memorymap.exception.EmailNotVerifiedException;
import com.memorymap.memorymap.exception.InvalidCredentialsException;
import com.memorymap.memorymap.exception.InvalidOrExpiredTokenException;
import com.memorymap.memorymap.model.User;
import com.memorymap.memorymap.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    // Off in the "test" profile — no existing test can retrieve a real token from a
    // real inbox, so gating login on verification there would break every e2e test
    // that registers then logs in.
    @Value("${app.email-verification.enabled:true}")
    private boolean emailVerificationEnabled;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, EmailService emailService) {
        this.userRepository= userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    public User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email= authentication.getName();
        User user=userRepository.findByEmail(email).orElseThrow(() ->new RuntimeException("User not found"));

        return user;
    }

    public User register(User user){
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new DuplicateEmailException("Email already in use");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

        if (emailVerificationEnabled) {
            user.setEmailVerified(false);
            user.setVerificationToken(UUID.randomUUID().toString());
            user.setVerificationTokenExpiry(LocalDateTime.now(ZoneOffset.UTC).plusHours(24));
        } else {
            user.setEmailVerified(true);
        }

        User saved = userRepository.save(user);

        if (emailVerificationEnabled) {
            emailService.sendVerificationEmail(saved);
        }

        return saved;
    }

    public void verifyEmail(String token){
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Invalid or expired verification link"));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new InvalidOrExpiredTokenException("Invalid or expired verification link");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    public String login(String email, String password){
        User user=userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

            if(!passwordEncoder.matches(password, user.getPassword())){
                throw new InvalidCredentialsException("Invalid email or password");
            }
            if (emailVerificationEnabled && !user.isEmailVerified()) {
                throw new EmailNotVerifiedException("Please verify your email before logging in");
            }
            return jwtService.generateToken(user.getEmail());


    }

    // Always completes normally whether or not the email is registered, so this
    // endpoint can't be used to check which emails have an account.
    public void requestPasswordReset(String email){
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setResetToken(UUID.randomUUID().toString());
            user.setResetTokenExpiry(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(30));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user);
        });
    }

    public void resetPassword(String token, String newPassword){
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Invalid or expired reset link"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new InvalidOrExpiredTokenException("Invalid or expired reset link");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}
