package com.smartmarkethub.smart.security.jwt;

import com.smartmarkethub.smart.security.SecurityProperties;
import com.smartmarkethub.smart.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthorizationFilter.class);
    
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final SecurityProperties securityProperties;

    public JwtAuthorizationFilter(
            JwtTokenProvider tokenProvider,
            CustomUserDetailsService userDetailsService,
            SecurityProperties securityProperties) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.securityProperties = securityProperties;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(securityProperties.getJwt().getHeaderName());
        if (StringUtils.hasText(bearerToken) && 
                bearerToken.startsWith(securityProperties.getJwt().getTokenPrefix())) {
            return bearerToken.substring(securityProperties.getJwt().getTokenPrefix().length());
        }
        return null;
    }
}
