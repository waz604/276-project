package com.cmpt276.studbuds.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for StudBuds.
 *
 * The app uses manual session-based authentication (checking session attributes
 * directly in controllers), NOT Spring Security's auth system. This config:
 *
 *  1. Permits all HTTP requests — controllers handle auth themselves.
 *  2. Disables CSRF — required so XP.js fetch() calls (no CSRF token) work.
 *  3. Disables the default redirect-to-login for unauthenticated requests —
 *     controllers return 401 JSON instead, which XP.js handles gracefully.
 *
 * Without this, Spring Security (pulled in via spring-boot-starter-actuator)
 * redirects unauthenticated POST /xp/award to /login (302) before the
 * controller's @ExceptionHandler can return 401. That breaks both the test
 * and the front-end bar seed.
 */
@Configuration
@EnableWebSecurity
@ConditionalOnClass(name = "org.springframework.security.config.annotation.web.builders.HttpSecurity")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — all state-changing API calls come from our own
            // JS via fetch(); adding CSRF tokens to every XP call would add
            // complexity with no real benefit here.
            .csrf(csrf -> csrf.disable())

            // Allow every request through — controllers check the session
            // manually and throw NullUserException → 401 JSON when needed.
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

            // Do NOT redirect unauthenticated users; controllers own that logic.
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
