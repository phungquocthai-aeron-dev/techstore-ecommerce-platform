package com.techstore.identity.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
import com.techstore.identity.client.UserServiceClient;
import com.techstore.identity.client.dto.CustomerAuthDTO;
import com.techstore.identity.client.dto.GoogleAuthDTO;
import com.techstore.identity.client.dto.StaffAuthDTO;
import com.techstore.identity.configuration.CustomOAuth2User;
import com.techstore.identity.constant.UserType;
import com.techstore.identity.dto.request.AuthenticationRequest;
import com.techstore.identity.dto.request.IntrospectRequest;
import com.techstore.identity.dto.request.LogoutRequest;
import com.techstore.identity.dto.request.RefreshRequest;
import com.techstore.identity.dto.response.AuthenticationResponse;
import com.techstore.identity.dto.response.IntrospectResponse;
import com.techstore.identity.entity.InvalidatedToken;
import com.techstore.identity.exception.AppException;
import com.techstore.identity.exception.ErrorCode;
import com.techstore.identity.repository.InvalidatedTokenRepository;

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

    private final UserServiceClient userServiceClient;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final PasswordEncoder passwordEncoder;

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
            throw new IllegalStateException("JWT signerKey must be at least 64 bytes for HS512");
        }
    }

    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException | JOSEException | ParseException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse authenticateStaff(AuthenticationRequest request) {

        StaffAuthDTO staff = userServiceClient.getStaffByEmail(request.getUsername());

        if (staff == null || !passwordEncoder.matches(request.getPassword(), staff.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (!"ACTIVE".equals(staff.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        return AuthenticationResponse.builder()
                .token(generateToken(staff.getId(), staff.getScope(), UserType.STAFF.name()))
                .build();
    }

    public AuthenticationResponse authenticateCustomer(AuthenticationRequest request) {

        CustomerAuthDTO customer = userServiceClient.getCustomerByEmail(request.getUsername());

        if (customer == null || !passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (!"ACTIVE".equals(customer.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        return AuthenticationResponse.builder()
                .token(generateToken(customer.getId(), "ROLE_CUSTOMER", UserType.CUSTOMER.name()))
                .build();
    }

    //    public AuthenticationResponse handleGoogleUser(OAuth2User oauth2User) {
    //
    //        String email = oauth2User.getAttribute("email");
    //        if (email == null) {
    //            throw new AppException(ErrorCode.UNAUTHENTICATED);
    //        }
    //
    //        String fullName = oauth2User.getAttribute("name");
    //        String avatarUrl = oauth2User.getAttribute("picture");
    //        String providerId =
    //                oauth2User.getAttribute("sub") != null ? oauth2User.getAttribute("sub") : oauth2User.getName();
    //
    //        Customer customer = customerRepository.findByEmail(email).orElse(null);
    //
    //        if (customer == null) {
    //            customer = Customer.builder()
    //                    .email(email)
    //                    .fullName(fullName)
    //                    .avatarUrl(avatarUrl)
    //                    .provider("GOOGLE")
    //                    .providerId(providerId)
    //                    .status("ACTIVE")
    //                    .build();
    //            customerRepository.save(customer);
    //        } else {
    //            if (customer.getProvider() != null && !"GOOGLE".equals(customer.getProvider())) {
    //                throw new AppException(ErrorCode.ACCOUNT_ALREADY_LINKED);
    //            }
    //
    //            customer.setProvider("GOOGLE");
    //
    //            if (customer.getProviderId() == null) {
    //                customer.setProviderId(providerId);
    //            }
    //            if (customer.getAvatarUrl() == null) {
    //                customer.setAvatarUrl(avatarUrl);
    //            }
    //            if (customer.getFullName() == null) {
    //                customer.setFullName(fullName);
    //            }
    //
    //            if (!"ACTIVE".equals(customer.getStatus())) {
    //                throw new AppException(ErrorCode.ACCOUNT_DISABLED);
    //            }
    //
    //            customerRepository.save(customer);
    //        }
    //
    //        log.info("User logged in via Google: {}", email);
    //
    //        return AuthenticationResponse.builder()
    //                .token(generateToken(customer.getId(), "ROLE_CUSTOMER", UserType.CUSTOMER.name()))
    //                .build();
    //    }

    public AuthenticationResponse authenticateGoogle(OAuth2User oauth2User) {

        GoogleAuthDTO request = GoogleAuthDTO.builder()
                .email(oauth2User.getAttribute("email"))
                .fullName(oauth2User.getAttribute("name"))
                .avatarUrl(oauth2User.getAttribute("picture"))
                .providerId(
                        oauth2User.getAttribute("sub") != null ? oauth2User.getAttribute("sub") : oauth2User.getName())
                .build();

        CustomerAuthDTO customer = userServiceClient.handleGoogle(request);

        if (!"ACTIVE".equals(customer.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        return AuthenticationResponse.builder()
                .token(generateToken(customer.getId(), "ROLE_CUSTOMER", UserType.CUSTOMER.name()))
                .build();
    }

    public AuthenticationResponse refreshStaffToken(RefreshRequest request) throws ParseException, JOSEException {

        SignedJWT signedJWT = verifyToken(request.getToken(), true);
        var claims = signedJWT.getJWTClaimsSet();

        if (!UserType.STAFF.name().equals(claims.getStringClaim("user_type"))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        saveInvalidatedToken(claims);

        Long staffId = Long.valueOf(claims.getSubject());
        StaffAuthDTO staff = userServiceClient.getStaffById(staffId);

        if (staff == null || !"ACTIVE".equals(staff.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        return AuthenticationResponse.builder()
                .token(generateToken(staffId, staff.getScope(), UserType.STAFF.name()))
                .build();
    }

    public AuthenticationResponse refreshCustomerToken(RefreshRequest request) throws ParseException, JOSEException {

        SignedJWT signedJWT = verifyToken(request.getToken(), true);
        var claims = signedJWT.getJWTClaimsSet();

        if (!UserType.CUSTOMER.name().equals(claims.getStringClaim("user_type"))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        saveInvalidatedToken(claims);

        Long customerId = Long.valueOf(claims.getSubject());
        CustomerAuthDTO customer = userServiceClient.getCustomerById(customerId);

        if (customer == null || !"ACTIVE".equals(customer.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        return AuthenticationResponse.builder()
                .token(generateToken(customerId, "ROLE_CUSTOMER", UserType.CUSTOMER.name()))
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            LocalDateTime expiredAt =
                    expiryTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().token(jit).expiredAt(expiredAt).build();

            invalidatedTokenRepository.save(invalidatedToken);

        } catch (AppException exception) {
            log.info("Token already expired");
        }
    }

    private String generateToken(Long id, String roles, String userType) {

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(id))
                .claim("scope", roles)
                .claim("user_type", userType)
                .issuer(ISSUER)
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS512), new Payload(claimsSet.toJSONObject()));

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Failed to sign JWT", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public AuthenticationResponse generateToken(OAuth2User principal) {

        if (!(principal instanceof CustomOAuth2User user)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String token = generateToken(user.getId(), user.getRoles(), user.getUserType());

        return AuthenticationResponse.builder().token(token).build();
    }

    public SignedJWT verifyToken(String token, boolean isRefresh) throws ParseException, JOSEException {

        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        Date expiryTime = isRefresh
                ? new Date(signedJWT
                        .getJWTClaimsSet()
                        .getIssueTime()
                        .toInstant()
                        .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                        .toEpochMilli())
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

        String jti = signedJWT.getJWTClaimsSet().getJWTID();

        if (invalidatedTokenRepository.existsByToken(jti)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    public void saveInvalidatedToken(JWTClaimsSet claims) {
        String jti = claims.getJWTID();

        if (!invalidatedTokenRepository.existsById(jti)) {
            Date expiryTime = claims.getExpirationTime();

            LocalDateTime expiredAt =
                    expiryTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            InvalidatedToken token =
                    InvalidatedToken.builder().token(jti).expiredAt(expiredAt).build();

            invalidatedTokenRepository.save(token);
        }
    }
}
