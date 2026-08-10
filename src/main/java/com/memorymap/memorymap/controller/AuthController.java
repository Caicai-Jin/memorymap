package com.memorymap.memorymap.controller;

import com.memorymap.memorymap.dto.ForgotPasswordRequest;
import com.memorymap.memorymap.dto.ResetPasswordRequest;
import com.memorymap.memorymap.model.User;
import com.memorymap.memorymap.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public User createUser(@Valid @RequestBody User user){
        return userService.register(user);
    }

    @PostMapping("/login")
    public String loginUser(@Valid @RequestBody User user){
        return userService.login(user.getEmail(), user.getPassword());
    }

    @GetMapping("/verify-email")
    public void verifyEmail(@RequestParam String token){
        userService.verifyEmail(token);
    }

    @PostMapping("/forgot-password")
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request){
        userService.requestPasswordReset(request.email());
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request){
        userService.resetPassword(request.token(), request.newPassword());
    }
    // Because AuthController is a @RestController,
    // whatever a method returns becomes the HTTP response body sent back over the network
    // Spring doesn't store it anywhere, it just writes that string straight into the response
    // Spring sends back to whoever made the POST /login request.

    //it travels  3 methods call, then out over the network as the response body
    // to the CLIENT  that called /login.
}

