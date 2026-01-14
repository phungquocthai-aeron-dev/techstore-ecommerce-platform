package com.techstore.identity.configuration;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.techstore.identity.constant.PredefinedRole;
import com.techstore.identity.entity.Role;
import com.techstore.identity.entity.Staff;
import com.techstore.identity.repository.RoleRepository;
import com.techstore.identity.repository.StaffRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;
    AdminAccountProperties adminProps;

    @Bean
    @ConditionalOnProperty(
            prefix = "spring.datasource",
            name = "driver-class-name",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner(
            StaffRepository staffRepository,
            RoleRepository roleRepository
    ) {

        return args -> {

            if (staffRepository.findByEmail(adminProps.getEmail()).isEmpty()) {

                Role adminRole = roleRepository.findByName(PredefinedRole.ADMIN)
                        .orElseGet(() -> roleRepository.save(
                                Role.builder()
                                        .name(PredefinedRole.ADMIN)
                                        .description("Admin role")
                                        .build()
                        ));

                Staff admin = Staff.builder()
                        .fullName("System Admin")
                        .email(adminProps.getEmail())
                        .password(passwordEncoder.encode(adminProps.getPassword()))
                        .status("ACTIVE")
                        .roles(Set.of(adminRole))
                        .build();

                staffRepository.save(admin);

                log.warn("Admin account created, please change password immediately");
            }
        };
    }
}
