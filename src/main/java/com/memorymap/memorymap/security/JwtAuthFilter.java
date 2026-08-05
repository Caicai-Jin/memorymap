package com.memorymap.memorymap.security;

import com.memorymap.memorymap.service.JwtService;
import java.util.Collections;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization= request.getHeader("Authorization");
        if(authorization == null || !authorization.startsWith("Bearer ")){
            //passes the request along to the next filter in the chain (and eventually your controller)
            filterChain.doFilter(request,response);
            return;
        }
        String token= authorization.substring(7);
        if(jwtService.isTokenValid(token)){
            //this request is from filtertest@example.com, already verified, no special roles
            //                      principal           ,  credentials      , authorities
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                    =new UsernamePasswordAuthenticationToken(jwtService.extractEmail(token), null, Collections.emptyList());
            //if valid, the filter actively records "this request belongs to this email" somewhere the rest
            //  of the app can see.
            // SecurityContextHolder is Spring Security's storage location for "who is making the current request
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        }
        filterChain.doFilter(request,response);

    }
}

// SecurityContextHolder notes:
//It is Spring Security's storage location for "who is making the current request

// - It holds a SecurityContext, which holds the
//   Authentication object (the one this filter
//   builds via setAuthentication(...) above).
//
// - Backed by a ThreadLocal by default, so the
//   value is scoped to the current thread. Each
//   HTTP request normally runs on its own thread,
//   so in practice this is per-request: what this
//   filter sets for request A is invisible to
//   request B, and Spring clears it automatically
//   once request A finishes.
//
// - It's a static, globally-accessible holder.
//   Any class, anywhere in the call stack (filter,
//   controller, service three layers deep) can call
//   SecurityContextHolder.getContext().getAuthentication()
//   and get the same answer — no need to manually
//   pass "the current user" through every method.
//
// Flow: this filter runs first and WRITES the
// authenticated identity into it. Later,
// UserService.getCurrentUser() READS it back out,
// downstream, with no direct link to this filter
// other than this shared holder.