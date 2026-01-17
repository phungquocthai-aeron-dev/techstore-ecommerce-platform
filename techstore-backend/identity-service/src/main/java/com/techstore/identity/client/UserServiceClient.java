package com.techstore.identity.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.techstore.identity.client.dto.CustomerAuthDTO;
import com.techstore.identity.client.dto.GoogleAuthDTO;
import com.techstore.identity.client.dto.StaffAuthDTO;
import com.techstore.identity.exception.AppException;
import com.techstore.identity.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    @Qualifier("userWebClient")
    private final WebClient userServiceClient;

    public StaffAuthDTO getStaffByEmail(String email) {
        return userServiceClient
                .get()
                .uri(uri -> uri.path("/internal/auth/staff")
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .bodyToMono(StaffAuthDTO.class)
                .block();
    }

    public StaffAuthDTO getStaffById(Long id) {
        return userServiceClient
                .get()
                .uri("/internal/auth/staff/{id}", id)
                .retrieve()
                .bodyToMono(StaffAuthDTO.class)
                .block();
    }

    public CustomerAuthDTO getCustomerByEmail(String email) {
        return userServiceClient
                .get()
                .uri(uri -> uri.path("/internal/auth/customer")
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .bodyToMono(CustomerAuthDTO.class)
                .block();
    }

    public CustomerAuthDTO getCustomerById(Long id) {
        return userServiceClient
                .get()
                .uri("/internal/auth/customer/{id}", id)
                .retrieve()
                .bodyToMono(CustomerAuthDTO.class)
                .block();
    }

    public CustomerAuthDTO handleGoogle(GoogleAuthDTO request) {
        return userServiceClient
                .post()
                .uri("/internal/auth/customer/google")
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        status -> status.value() == 403,
                        response -> Mono.error(new AppException(ErrorCode.ACCOUNT_DISABLED)))
                .onStatus(
                        status -> status.value() == 409,
                        response -> Mono.error(new AppException(ErrorCode.ACCOUNT_ALREADY_LINKED)))
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new AppException(ErrorCode.USER_SERVICE_UNAVAILABLE)))
                .bodyToMono(CustomerAuthDTO.class)
                .block();
    }
}
