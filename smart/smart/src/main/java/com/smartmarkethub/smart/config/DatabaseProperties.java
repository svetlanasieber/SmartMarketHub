package com.smartmarkethub.smart.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.database")
public class DatabaseProperties {
    
    private final String url;
    private final String username;
    private final String password;
    private final int maxConnections;
    private final Duration connectionTimeout;
    private final Pool pool;

    public DatabaseProperties(
            String url,
            String username,
            String password,
            int maxConnections,
            Duration connectionTimeout,
            Pool pool) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.maxConnections = maxConnections;
        this.connectionTimeout = connectionTimeout;
        this.pool = pool;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    public Pool getPool() {
        return pool;
    }

    public static class Pool {
        private final int minIdle;
        private final int maxIdle;
        private final int maxSize;

        public Pool(int minIdle, int maxIdle, int maxSize) {
            this.minIdle = minIdle;
            this.maxIdle = maxIdle;
            this.maxSize = maxSize;
        }

        public int getMinIdle() {
            return minIdle;
        }

        public int getMaxIdle() {
            return maxIdle;
        }

        public int getMaxSize() {
            return maxSize;
        }
    }
}
