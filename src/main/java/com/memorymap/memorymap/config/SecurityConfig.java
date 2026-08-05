package com.memorymap.memorymap.config;

import com.memorymap.memorymap.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//  Before writing the register endpoint, we need a way to hash passwords
//  (never store plaintext passwords in the DB).
//  Spring Security gives me PasswordEncoder for this
//  but I need to register it as a Spring "bean"

// @Configuration tells Spring "this class defines beans
// @Bean tells Spring "call this method once at startup,
// and hand out the result to anything that asks for a PasswordEncoder."
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder(){
        //passwordEncoder = BCrypt = slow-by-design + auto-salted + one-way

        //BCrypt is the specific hashing algorithm
        // it's the industry standard for password hashing
        return new BCryptPasswordEncoder();
    }

    // - SecurityFilterChain is Spring Security's core concept:
    // an ordered list of filters that every incoming HTTP request passes through
    // before it reaches your controller (checking
    //  CSRF, checking auth, checking permissions, etc.).


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register", "/login", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
                // Its only job is to identify who's asking, not to approve/reject anything itself

        return http.build();
    }
}
//- CSRF (Cross-Site Request Forgery) protection defends against a browser with
// an active login session being tricked into submitting a form to your site from another page.
// It's relevant for server-rendered apps using cookies/sessions for auth.

// - requestMatchers("/register", "/login").permitAll() — these two paths are open to anyone, no login required.
// Makes sense: you can't require a login token to get a login token.
// - anyRequest().authenticated() — every other path (including your /moments endpoints)
// requires the caller to be authenticated. Right now nothing issues valid credentials for
//  that yet, so those endpoints are effectively unusable until you build the JWT filter