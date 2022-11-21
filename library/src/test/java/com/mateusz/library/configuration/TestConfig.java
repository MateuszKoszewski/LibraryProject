package com.mateusz.library.configuration;

import com.mateusz.library.exception.httpExceptionHandling.JwtAuthenticationEntryPoint;
import com.mateusz.library.security.JWTTokenProvider;
import com.mateusz.library.security.JwtAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.nio.file.AccessDeniedException;

@TestConfiguration
public class TestConfig {

    @Bean
    public JWTTokenProvider createJwtTokenProvider() {
        return new JWTTokenProvider();
    }

    @Bean
    public JwtAccessDeniedHandler createAccessDeniedHandler() {
        return new JwtAccessDeniedHandler();
    }

    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }

}
