package com.techstore.identity.configuration;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import com.techstore.identity.client.UserServiceClient;
import com.techstore.identity.client.dto.CustomerAuthDTO;
import com.techstore.identity.client.dto.GoogleAuthDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserServiceClient userServiceClient;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        GoogleAuthDTO dto = GoogleAuthDTO.builder()
                .email(oauth2User.getAttribute("email"))
                .fullName(oauth2User.getAttribute("name"))
                .avatarUrl(oauth2User.getAttribute("picture"))
                .providerId(oauth2User.getAttribute("sub"))
                .build();

        CustomerAuthDTO customer = userServiceClient.handleGoogle(dto);

        return new CustomOAuth2User(customer, oauth2User.getAttributes());
    }
}
