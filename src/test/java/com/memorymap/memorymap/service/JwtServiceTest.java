package com.memorymap.memorymap.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Pure unit test: JwtService takes its secret directly through the constructor,
// so it can be built standalone here with no Spring context and no mocks at all.
class JwtServiceTest {

    private final JwtService jwtService = new JwtService("VbpI4iLjN3CfpY78PokOoGk7Byo/CXQ5/ozBfFUbT1A=");

    @Test
    void generatedTokenRoundTripsBackToTheSameEmail() {
        String token = jwtService.generateToken("test@example.com");

        assertTrue(jwtService.isTokenValid(token));
        assertEquals("test@example.com", jwtService.extractEmail(token));
    }

    @Test
    void tamperedTokenIsInvalid() {
        String token = jwtService.generateToken("test@example.com");

        assertFalse(jwtService.isTokenValid(token + "tampered"));
    }
}
