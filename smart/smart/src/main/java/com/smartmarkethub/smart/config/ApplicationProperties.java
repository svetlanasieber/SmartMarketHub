package com.smartmarkethub.smart.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {
    
    private final String name;
    private final String version;
    private final Features features;
    private final Analytics analytics;

    public ApplicationProperties(
            String name,
            String version,
            Features features,
            Analytics analytics) {
        this.name = name;
        this.version = version;
        this.features = features;
        this.analytics = analytics;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Features getFeatures() {
        return features;
    }

    public Analytics getAnalytics() {
        return analytics;
    }

    public static class Features {
        private final boolean advancedAnalytics;
        private final boolean userReviews;
        private final boolean recommendations;

        public Features(
                boolean advancedAnalytics,
                boolean userReviews,
                boolean recommendations) {
            this.advancedAnalytics = advancedAnalytics;
            this.userReviews = userReviews;
            this.recommendations = recommendations;
        }

        public boolean isAdvancedAnalytics() {
            return advancedAnalytics;
        }

        public boolean isUserReviews() {
            return userReviews;
        }

        public boolean isRecommendations() {
            return recommendations;
        }
    }

    public static class Analytics {
        private final String apiKey;
        private final String endpoint;
        private final boolean enabled;

        public Analytics(String apiKey, String endpoint, boolean enabled) {
            this.apiKey = apiKey;
            this.endpoint = endpoint;
            this.enabled = enabled;
        }

        public String getApiKey() {
            return apiKey;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }
}
