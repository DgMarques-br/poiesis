package com.poiesis.login.springframework.security;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class SessionCookieService {
    public static final String SESSION_COOKIE = "POIESIS_SESSION";
    public static final String CSRF_COOKIE = "POIESIS_CSRF";

    private final Duration tokenLifetime;
    private final boolean secure;
    private final String sameSite;
    private final SecureRandom secureRandom = new SecureRandom();

    public SessionCookieService(Environment environment) {
        this.tokenLifetime = Duration.ofMillis(
                environment.getRequiredProperty("application.jwt.expiration", Long.class));
        this.secure = environment.getProperty("application.security.cookie.secure", Boolean.class, false);
        this.sameSite = environment.getProperty("application.security.cookie.same-site", "Strict");
    }

    public ResponseCookie sessionCookie(String token) {
        return baseCookie(SESSION_COOKIE, token).httpOnly(true).maxAge(tokenLifetime).build();
    }

    public ResponseCookie csrfCookie() {
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        return baseCookie(CSRF_COOKIE, Base64.getUrlEncoder().withoutPadding().encodeToString(token))
                .httpOnly(false)
                .maxAge(tokenLifetime)
                .build();
    }

    public ResponseCookie clearSessionCookie() {
        return baseCookie(SESSION_COOKIE, "").httpOnly(true).maxAge(Duration.ZERO).build();
    }

    public ResponseCookie clearCsrfCookie() {
        return baseCookie(CSRF_COOKIE, "").httpOnly(false).maxAge(Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String name, String value) {
        return ResponseCookie.from(name, value)
                .path("/")
                .secure(secure)
                .sameSite(sameSite);
    }
}
