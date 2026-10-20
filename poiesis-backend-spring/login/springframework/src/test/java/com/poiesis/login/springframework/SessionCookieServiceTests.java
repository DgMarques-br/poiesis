package com.poiesis.login.springframework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.poiesis.login.springframework.security.SessionCookieService;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class SessionCookieServiceTests {
    @Test
    void issuesHttpOnlySessionAndReadableCsrfCookiesWithConfiguredLifetime() {
        var environment = new MockEnvironment()
                .withProperty("application.jwt.expiration", "900000")
                .withProperty("application.security.cookie.secure", "true")
                .withProperty("application.security.cookie.same-site", "Strict");
        var cookies = new SessionCookieService(environment);

        var session = cookies.sessionCookie("signed-token");
        var csrf = cookies.csrfCookie();

        assertEquals(SessionCookieService.SESSION_COOKIE, session.getName());
        assertTrue(session.isHttpOnly());
        assertFalse(csrf.isHttpOnly());
        assertTrue(session.isSecure());
        assertEquals("Strict", session.getSameSite());
        assertEquals(Duration.ofMinutes(15), session.getMaxAge());
        assertEquals(session.getMaxAge(), csrf.getMaxAge());
        assertNotEquals("", csrf.getValue());
        assertEquals(Duration.ZERO, cookies.clearSessionCookie().getMaxAge());
        assertEquals(Duration.ZERO, cookies.clearCsrfCookie().getMaxAge());
    }
}
