package com.memorymap.memorymap.service;

import com.memorymap.memorymap.exception.EmailNotVerifiedException;
import com.memorymap.memorymap.exception.InvalidOrExpiredTokenException;
import com.memorymap.memorymap.model.User;
import com.memorymap.memorymap.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Unit-level coverage for the email-verification/password-reset flow added on top
// of register/login. The "app.email-verification.enabled" flag is normally read
// from config via @Value; ReflectionTestUtils sets it directly here since there's
// no Spring context in a pure-Mockito unit test.
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private void enableEmailVerification(boolean enabled) {
        ReflectionTestUtils.setField(userService, "emailVerificationEnabled", enabled);
    }

    @Test
    void registerWithVerificationEnabledCreatesAnUnverifiedUserAndSendsAnEmail() {
        enableEmailVerification(true);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = new User();
        user.setEmail("new@test.com");
        user.setPassword("plaintext");

        User saved = userService.register(user);

        assertFalse(saved.isEmailVerified());
        assertNotNull(saved.getVerificationToken());
        assertNotNull(saved.getVerificationTokenExpiry());
        verify(emailService).sendVerificationEmail(saved);
    }

    @Test
    void verifyEmailMarksAUserVerifiedForAValidToken() {
        User user = new User();
        user.setEmailVerified(false);
        user.setVerificationToken("good-token");
        user.setVerificationTokenExpiry(LocalDateTime.now(ZoneOffset.UTC).plusHours(1));
        when(userRepository.findByVerificationToken("good-token")).thenReturn(Optional.of(user));

        userService.verifyEmail("good-token");

        assertTrue(user.isEmailVerified());
        assertNull(user.getVerificationToken());
    }

    @Test
    void verifyEmailThrowsForAnExpiredToken() {
        User user = new User();
        user.setVerificationToken("stale-token");
        user.setVerificationTokenExpiry(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(1));
        when(userRepository.findByVerificationToken("stale-token")).thenReturn(Optional.of(user));

        assertThrows(InvalidOrExpiredTokenException.class, () -> userService.verifyEmail("stale-token"));
    }

    @Test
    void verifyEmailThrowsForAnUnknownToken() {
        when(userRepository.findByVerificationToken("nope")).thenReturn(Optional.empty());

        assertThrows(InvalidOrExpiredTokenException.class, () -> userService.verifyEmail("nope"));
    }

    @Test
    void loginThrowsWhenVerificationIsEnabledAndTheUserIsNotVerified() {
        enableEmailVerification(true);
        User user = new User();
        user.setEmail("unverified@test.com");
        user.setPassword("hashed");
        user.setEmailVerified(false);
        when(userRepository.findByEmail("unverified@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        assertThrows(EmailNotVerifiedException.class,
                () -> userService.login("unverified@test.com", "password"));
    }

    @Test
    void requestPasswordResetDoesNothingForAnUnknownEmail() {
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        userService.requestPasswordReset("nobody@test.com");

        verify(emailService, never()).sendPasswordResetEmail(any());
    }

    @Test
    void resetPasswordUpdatesThePasswordForAValidToken() {
        User user = new User();
        user.setResetToken("reset-token");
        user.setResetTokenExpiry(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(10));
        when(userRepository.findByResetToken("reset-token")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("hashed-new");

        userService.resetPassword("reset-token", "newPassword123");

        assertEquals("hashed-new", user.getPassword());
        assertNull(user.getResetToken());
    }

    @Test
    void resetPasswordThrowsForAnExpiredToken() {
        User user = new User();
        user.setResetToken("stale-reset");
        user.setResetTokenExpiry(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(1));
        when(userRepository.findByResetToken("stale-reset")).thenReturn(Optional.of(user));

        assertThrows(InvalidOrExpiredTokenException.class,
                () -> userService.resetPassword("stale-reset", "newPassword123"));
    }
}
