package com.techstore.identity.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.techstore.identity.dto.request.AuthenticationRequest;
import com.techstore.identity.dto.response.AuthenticationResponse;
import com.techstore.identity.entity.Customer;
import com.techstore.identity.entity.Staff;
import com.techstore.identity.exception.AppException;
import com.techstore.identity.exception.ErrorCode;
import com.techstore.identity.repository.CustomerRepository;
import com.techstore.identity.repository.InvalidatedTokenRepository;
import com.techstore.identity.repository.StaffRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    StaffRepository staffRepository;
    CustomerRepository customerRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signerKey}")
    String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    long REFRESHABLE_DURATION;

    private static final String ISSUER = "techstore.com";

    @PostConstruct
    void validateSignerKey() {
        if (SIGNER_KEY.getBytes().length < 64) {
            throw new IllegalStateException(
                    "JWT signerKey must be at least 64 bytes for HS512");
        }
    }

    public AuthenticationResponse staffLogin(AuthenticationRequest request) {

        Staff staff = staffRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (!passwordEncoder.matches(request.getPassword(), staff.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (!"ACTIVE".equals(staff.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        return AuthenticationResponse.builder()
                .token(generateToken(
                        staff.getId(),
                        buildStaffScope(staff)
                ))
                .build();
    }

    public AuthenticationResponse customerLogin(AuthenticationRequest request) {

        Customer customer = customerRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (!"ACTIVE".equals(customer.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        return AuthenticationResponse.builder()
                .token(generateToken(
                        customer.getId(),
                        "ROLE_CUSTOMER"
                ))
                .build();
    }

    public AuthenticationResponse authenticateGoogle(Customer customer) {

        if (!"ACTIVE".equals(customer.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        return AuthenticationResponse.builder()
                .token(generateToken(
                        customer.getId(),
                        "ROLE_CUSTOMER"
                ))
                .build();
    }

    private String generateToken(Long id, String roles) {

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(id))
                .claim("scope", roles)
                .issuer(ISSUER)
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now()
                                .plus(VALID_DURATION, ChronoUnit.SECONDS)
                                .toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWSObject jwsObject = new JWSObject(
                new JWSHeader(JWSAlgorithm.HS512),
                new Payload(claimsSet.toJSONObject())
        );

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Failed to sign JWT", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public SignedJWT verifyToken(String token, boolean isRefresh)
            throws ParseException, JOSEException {

        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        Date expiryTime = isRefresh
                ? new Date(
                        signedJWT.getJWTClaimsSet()
                                .getIssueTime()
                                .toInstant()
                                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                                .toEpochMilli()
                )
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        if (!signedJWT.verify(verifier)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (!ISSUER.equals(signedJWT.getJWTClaimsSet().getIssuer())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (expiryTime.before(new Date())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (invalidatedTokenRepository.existsByToken(token)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    private String buildStaffScope(Staff staff) {
        StringJoiner joiner = new StringJoiner(" ");
        staff.getRoles().forEach(role ->
                joiner.add("ROLE_" + role.getName())
        );
        return joiner.toString();
    }
}
