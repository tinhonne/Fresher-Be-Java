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

    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository){
        return args ->{
            if(!userRepository.existsByUsername("admin")){

                Role role = roleRepository.findByName("Admin").orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

                HashSet<Role> roles = new HashSet<>();
                roles.add(role);
                User user= User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .name("Vua")
                        .roles(roles)
                        .build();

                userRepository.save(user);
                log.warn("create Admin success, please change password");
            }
        };
    }
}
