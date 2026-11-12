package com.zycus.pims.gateway.config;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class SpaWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Pass API calls, actuator health endpoints, and static files with extensions through normally
        if (path.startsWith("/api") || path.startsWith("/actuator") || path.contains(".")) {
            return chain.filter(exchange);
        }

        // For non-file client-side SPA routes (e.g. /, /login, /products, /orders), forward to /index.html
        return chain.filter(
            exchange.mutate()
                .request(exchange.getRequest().mutate().path("/index.html").build())
                .build()
        );
    }
}
