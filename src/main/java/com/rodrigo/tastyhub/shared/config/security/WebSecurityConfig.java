package com.rodrigo.tastyhub.shared.config.security;

import com.rodrigo.tastyhub.modules.auth.infrastructure.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    private JwtAuthEntryPoint authEntryPoint;
    private CustomUserDetailsService userDetailsService;

    @Autowired
    public WebSecurityConfig(
        CustomUserDetailsService userDetailsService,
        JwtAuthEntryPoint authEntryPoint
    ) {
        this.userDetailsService = userDetailsService;
        this.authEntryPoint = authEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception.authenticationEntryPoint(authEntryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api-docs/**",
                    "/swagger-ui.html",
                    "/hello-world",
                    "/api/auth/login",
                    "/api/auth/signup",
                    "/api/auth/refresh-token",
                    "/api/auth/verify-email"
                ).permitAll()
                .requestMatchers("/api/auth/user/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/auth/admin/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
            ).httpBasic(Customizer.withDefaults());

        httpSecurity.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
}
