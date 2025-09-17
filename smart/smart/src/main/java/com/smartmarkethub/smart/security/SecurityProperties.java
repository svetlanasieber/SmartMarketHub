package com.smartmarkethub.smart.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
    private String[] allowedOrigins = {"http://localhost:8080"};
    private Jwt jwt = new Jwt();
    private Session session = new Session();
    private boolean requireSsl = false;

    @Data
    public static class Jwt {
        private String secret;
        private long expirationMs = 86400000; // 24 hours
        private String issuer = "Smart MarketHub";
        private String tokenPrefix = "Bearer ";
        private String headerName = "Authorization";
    }

    @Data
    public static class Session {
        private int maxConcurrentSessions = 1;
        private boolean invalidateOnLogout = true;
        private int rememberMeSeconds = 2592000; // 30 days
    }
}
