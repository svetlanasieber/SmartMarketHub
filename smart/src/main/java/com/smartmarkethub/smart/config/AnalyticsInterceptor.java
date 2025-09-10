package com.smartmarkethub.smart.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AnalyticsInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsInterceptor.class);
    
    private final String apiKey;
    private final String endpoint;

    public AnalyticsInterceptor(String apiKey, String endpoint) {
        this.apiKey = apiKey;
        this.endpoint = endpoint;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void postHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            @Nullable ModelAndView modelAndView) {
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long executeTime = endTime - startTime;

        String path = request.getRequestURI();
        String method = request.getMethod();
        int status = response.getStatus();

        logger.debug("Request: {} {} - Status: {} - Time: {}ms", 
                method, path, status, executeTime);

      
        sendAnalytics(method, path, status, executeTime);
    }

    private void sendAnalytics(String method, String path, int status, long executeTime) {
        if (apiKey != null && endpoint != null) {

            logger.debug("Would send analytics to {} with key {}: {} {} (status: {}, time: {}ms)",
                    endpoint, apiKey, method, path, status, executeTime);
        }
    }
}
