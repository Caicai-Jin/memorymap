package com.memorymap.memorymap.service;

import com.memorymap.memorymap.exception.DuplicateEmailException;
import com.memorymap.memorymap.exception.InvalidCredentialsException;
import com.memorymap.memorymap.model.User;
import com.memorymap.memorymap.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository= userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
        else{
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setCreatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }
    }

    public String login(String email, String password){
        User user=userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

            if(!passwordEncoder.matches(password, user.getPassword())){
                throw new InvalidCredentialsException("Invalid email or password");
            }
            return jwtService.generateToken(user.getEmail());


    }
}
