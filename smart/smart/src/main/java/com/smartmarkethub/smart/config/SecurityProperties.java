package com.smartmarkethub.smart.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.security")
public record SecurityProperties(
    Jwt jwt,
    Session session,
    boolean requireSsl
) {
    public record Jwt(
        String secret,
        Duration expiration,
        String issuer
    ) {}

    public record Session(
        Duration timeout,
        boolean persistent,
        String domain
    ) {}
}