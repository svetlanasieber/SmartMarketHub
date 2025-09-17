package com.smartmarkethub.smart;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.smartmarkethub.smart.integration.inventory")
public class SecurityFeignConfig {
}


