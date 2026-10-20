package com.poiesis.gateway.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

public class SessionCookieAuthenticationFilter extends OncePerRequestFilter {
    private static final String SESSION_COOKIE = "POIESIS_SESSION";
    private static final String CSRF_COOKIE = "POIESIS_CSRF";
    private static final String CSRF_HEADER = "X-POIESIS-CSRF";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String bearer = request.getHeader("Authorization");
        String sessionToken = cookieValue(request, SESSION_COOKIE);
        if ((bearer == null || bearer.isBlank()) && sessionToken != null) {
            if (requiresCsrf(request) && !validCsrf(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token CSRF ausente ou inválido.");
                return;
            }
            chain.doFilter(new AuthorizationHeaderRequest(request, "Bearer " + sessionToken), response);
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean requiresCsrf(HttpServletRequest request) {
        return !HttpMethod.GET.matches(request.getMethod())
                && !HttpMethod.HEAD.matches(request.getMethod())
                && !HttpMethod.OPTIONS.matches(request.getMethod())
                && !HttpMethod.TRACE.matches(request.getMethod());
    }

    private boolean validCsrf(HttpServletRequest request) {
        String csrfCookie = cookieValue(request, CSRF_COOKIE);
        String csrfHeader = request.getHeader(CSRF_HEADER);
        if (csrfCookie == null || csrfHeader == null) return false;
        return MessageDigest.isEqual(
                csrfCookie.getBytes(StandardCharsets.UTF_8),
                csrfHeader.getBytes(StandardCharsets.UTF_8));
    }

    private String cookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName()) && !cookie.getValue().isBlank()) return cookie.getValue();
        }
        return null;
    }

    private static final class AuthorizationHeaderRequest extends HttpServletRequestWrapper {
        private final String authorization;

        private AuthorizationHeaderRequest(HttpServletRequest request, String authorization) {
            super(request);
            this.authorization = authorization;
        }

        @Override
        public String getHeader(String name) {
            return "Authorization".equalsIgnoreCase(name) ? authorization : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return "Authorization".equalsIgnoreCase(name)
                    ? Collections.enumeration(List.of(authorization))
                    : super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            var names = Collections.list(super.getHeaderNames());
            if (names.stream().noneMatch("Authorization"::equalsIgnoreCase)) names.add("Authorization");
            return Collections.enumeration(names);
        }
    }
}
