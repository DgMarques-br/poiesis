package com.poiesis.produto.security;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

final class SessionJwtDecoder implements JwtDecoder {
    private final JwtDecoder delegate;
    private final RestClient login;

    SessionJwtDecoder(JwtDecoder delegate, String loginUrl) {
        this.delegate = delegate;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.login = RestClient.builder().baseUrl(loginUrl).requestFactory(factory).build();
    }

    @Override
    public Jwt decode(String token) {
        Jwt jwt = delegate.decode(token);
        try {
            login.get().uri("/v1/auth/session").headers(headers -> headers.setBearerAuth(token))
                    .retrieve().toBodilessEntity();
        } catch (RestClientException exception) {
            throw new BadJwtException("Sessão inválida ou indisponível.", exception);
        }
        return jwt;
    }
}
