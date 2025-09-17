package com.smartmarkethub.smart.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartmarkethub.smart.security.SecurityProperties;
import com.smartmarkethub.smart.web.dto.LoginRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.io.IOException;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtTokenProvider tokenProvider;
    private final SecurityProperties securityProperties;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider,
            SecurityProperties securityProperties,
            ObjectMapper objectMapper) {
        super(authenticationManager); 
        this.tokenProvider = tokenProvider;
        this.securityProperties = securityProperties;
        this.objectMapper = objectMapper;
        setFilterProcessesUrl("/api/auth/login"); 
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            return this.getAuthenticationManager().authenticate(authenticationToken);
        } catch (IOException e) {
            logger.error("Failed to parse authentication request", e);
            throw new RuntimeException("Failed to parse authentication request", e);
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        
        String token = tokenProvider.generateToken(authResult);
        response.addHeader(
            securityProperties.getJwt().getHeaderName(),
            securityProperties.getJwt().getTokenPrefix() + token
        );
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {
        
        logger.warn("Authentication failed: {}", failed.getMessage());
        super.unsuccessfulAuthentication(request, response, failed);
    }
}
