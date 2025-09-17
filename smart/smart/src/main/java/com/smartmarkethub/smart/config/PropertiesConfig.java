package com.smartmarkethub.smart.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    ApplicationProperties.class,
    DatabaseProperties.class,
    SecurityProperties.class
})
public class PropertiesConfig {
}


