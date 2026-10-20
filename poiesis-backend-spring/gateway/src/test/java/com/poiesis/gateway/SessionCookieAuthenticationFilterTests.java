package com.poiesis.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.poiesis.gateway.config.SessionCookieAuthenticationFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class SessionCookieAuthenticationFilterTests {
    private final SessionCookieAuthenticationFilter filter = new SessionCookieAuthenticationFilter();

    @Test
    void requiresMatchingCsrfValueForUnsafeCookieRequests() throws Exception {
        var request = new MockHttpServletRequest("POST", "/v1/pedidos");
        request.setCookies(new Cookie("POIESIS_SESSION", "session-token"));
        var response = new MockHttpServletResponse();
        var reachedChain = new AtomicReference<Boolean>(false);

        filter.doFilter(request, response, (incoming, outgoing) -> reachedChain.set(true));

        assertEquals(403, response.getStatus());
        assertFalse(reachedChain.get());
    }

    @Test
    void forwardsCookieAsBearerWhenCsrfValuesMatch() throws Exception {
        var request = new MockHttpServletRequest("PATCH", "/v1/producao/1/status");
        request.setCookies(new Cookie("POIESIS_SESSION", "session-token"),
                new Cookie("POIESIS_CSRF", "csrf-token"));
        request.addHeader("X-POIESIS-CSRF", "csrf-token");
        var response = new MockHttpServletResponse();
        var forwarded = new AtomicReference<HttpServletRequest>();

        filter.doFilter(request, response, (incoming, outgoing) ->
                forwarded.set((HttpServletRequest) incoming));

        assertNotNull(forwarded.get());
        assertEquals("Bearer session-token", forwarded.get().getHeader("Authorization"));
    }

    @Test
    void preservesBearerClientsWithoutCookieCsrfRequirement() throws Exception {
        var request = new MockHttpServletRequest("POST", "/v1/pedidos");
        request.addHeader("Authorization", "Bearer native-token");
        request.setCookies(new Cookie("POIESIS_SESSION", "browser-token"));
        var response = new MockHttpServletResponse();
        var forwarded = new AtomicReference<HttpServletRequest>();

        filter.doFilter(request, response, (incoming, outgoing) ->
                forwarded.set((HttpServletRequest) incoming));

        assertNotNull(forwarded.get());
        assertEquals("Bearer native-token", forwarded.get().getHeader("Authorization"));
        assertTrue(response.getStatus() < 400);
    }
}
