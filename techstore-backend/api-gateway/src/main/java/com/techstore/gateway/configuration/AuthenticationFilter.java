package com.techstore.gateway.configuration;

import com.techstore.gateway.dto.response.ApiResponse;
import com.techstore.gateway.service.IdentityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {
    IdentityService identityService;
    ObjectMapper objectMapper;

//    @NonFinal
//    private String[] publicEndpoints = {
//            "/identity/auth/.*",
//            "/identity/users/registration",
//            
//            "/identity/oauth2/.*",
//            "/identity/login/.*",
//            
//            "/file/media/download/.*",
//            
//            "/user/customers/register",
//            
//            "/order/payment/vnpay/ipn"
//
//    };
    
    private static final Map<HttpMethod, List<Pattern>> PUBLIC_ENDPOINTS =
            Map.of(
                    HttpMethod.GET, List.of(
                            Pattern.compile("/review/reviews/.*"),
                            Pattern.compile("/file/media/download/.*"),
                            Pattern.compile("/order/payment/vnpay/ipn"),
                            Pattern.compile("/product/products/.*"),
                            Pattern.compile("/product/brands/.*"),
                            Pattern.compile("/product/categories(/.*)?"),
                            Pattern.compile("/order/coupons/.*"),
                            Pattern.compile("/order/payment-methods/.*")
                    ),
                    HttpMethod.POST, List.of(
                            Pattern.compile("/identity/auth/.*"),
                            Pattern.compile("/identity/users/registration"),
                            Pattern.compile("/identity/oauth2/.*"),
                            Pattern.compile("/identity/login/.*"),
                            Pattern.compile("/user/customers/register")
                    )
            );

    @Value("${app.api-prefix}")
    @NonFinal
    private String apiPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Enter authentication filter....");

        if (isPublicEndpoint(exchange.getRequest()))
            return chain.filter(exchange);

        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeader))
            return unauthenticated(exchange.getResponse());

        String token = authHeader.getFirst().replace("Bearer ", "");
        log.info("Token: {}", token);

        return identityService.introspect(token).flatMap(introspectResponse -> {
            if (introspectResponse.getResult().isValid())
                return chain.filter(exchange);
            else
                return unauthenticated(exchange.getResponse());
        }).onErrorResume(throwable -> unauthenticated(exchange.getResponse()));
    }

    @Override
    public int getOrder() {
        return -1;
    }

//    private boolean isPublicEndpoint(ServerHttpRequest request){
//        return Arrays.stream(publicEndpoints)
//                .anyMatch(s -> request.getURI().getPath().matches(apiPrefix + s));
//    }
    
    private boolean isPublicEndpoint(ServerHttpRequest request) {

        String path = request.getURI().getPath().replaceFirst(apiPrefix, "");
        HttpMethod method = request.getMethod();

        if (method == null) return false;

        List<Pattern> patterns = PUBLIC_ENDPOINTS.get(method);

        if (patterns == null) return false;

        return patterns.stream()
                .anyMatch(pattern -> pattern.matcher(path).matches());
    }

    Mono<Void> unauthenticated(ServerHttpResponse response){
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(1401)
                .message("Unauthenticated")
                .build();

        String body = null;
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}