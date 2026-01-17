package com.techstore.identity.configuration;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.techstore.identity.client.dto.CustomerAuthDTO;

public class CustomOAuth2User implements OAuth2User {

    private final CustomerAuthDTO customer;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(CustomerAuthDTO customer, Map<String, Object> attributes) {
        this.customer = customer;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    @Override
    public String getName() {
        return customer.getEmail();
    }

    public Long getId() {
        return customer.getId();
    }

    public String getUserType() {
        return "CUSTOMER";
    }

    public String getRoles() {
        return "ROLE_CUSTOMER";
    }
}
