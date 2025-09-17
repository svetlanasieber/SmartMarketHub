package com.smartmarkethub.smart.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartmarkethub.smart.security.jwt.JwtAuthenticationFilter;
import com.smartmarkethub.smart.security.jwt.JwtAuthorizationFilter;
import com.smartmarkethub.smart.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final SecurityProperties securityProperties;
    private final JwtTokenProvider tokenProvider;
    private final ObjectMapper objectMapper;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(
            JwtAuthorizationFilter jwtAuthorizationFilter,
            SecurityProperties securityProperties,
            JwtTokenProvider tokenProvider,
            ObjectMapper objectMapper,
            UserDetailsService userDetailsService) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
        this.securityProperties = securityProperties;
        this.tokenProvider = tokenProvider;
        this.objectMapper = objectMapper;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
            authenticationManager,
            tokenProvider,
            securityProperties,
            objectMapper
        );
        filter.setFilterProcessesUrl("/api/auth/login");
        return filter;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationConfiguration authConfig) throws Exception {
        AuthenticationManager authenticationManager = authConfig.getAuthenticationManager();
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())  
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/home", "/login", "/register",
                    "/api/auth/**", "/api/public/**",
                    "/swagger-ui/**", "/v3/api-docs/**",
                    "/css/**", "/js/**", "/img/**", "/webjars/**",
                    "/products/**", "/api/products/**", "/api/categories/**"
                ).permitAll()
                .requestMatchers("/api/admin/**", "/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()  
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/")
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll());

       
        if (securityProperties.isRequireSsl()) {
            http.securityMatcher("/**")
                .headers(headers -> headers
                    .httpStrictTransportSecurity()
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000));
        }

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(securityProperties.getAllowedOrigins()));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
