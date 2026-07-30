package com.example.demo.config;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AppConfig {

    private static final String DEVELOPMENT_ADMIN_USERNAME = "admin";
    private static final String DEVELOPMENT_ADMIN_PASSWORD = "admin";
    private static final String DEVELOPMENT_ADMIN_NAME = "Vua";
    private static final String DEVELOPMENT_ADMIN_ROLE = "Admin";

    private final PasswordEncoder passwordEncoder;

    /**
     * Creates the development administrator account when it does not already exist.
     *
     * @param userRepository repository used to inspect and create users
     * @param roleRepository repository used to resolve the administrator role
     * @return the administrator bootstrap runner
     * @throws AppException when the runner executes and the development administrator role cannot be found ({@code ROLE_NOT_FOUND})
     */
    @Bean
    public ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository){
        return args ->{
            if(!userRepository.existsByUsername(DEVELOPMENT_ADMIN_USERNAME)){

                Role role = roleRepository.findByName(DEVELOPMENT_ADMIN_ROLE)
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

                HashSet<Role> roles = new HashSet<>();
                roles.add(role);
                User user= User.builder()
                        .username(DEVELOPMENT_ADMIN_USERNAME)
                        .password(passwordEncoder.encode(DEVELOPMENT_ADMIN_PASSWORD))
                        .name(DEVELOPMENT_ADMIN_NAME)
                        .roles(roles)
                        .build();

                userRepository.save(user);
                log.warn("create Admin success, please change password");
            }
        };
    }
}
