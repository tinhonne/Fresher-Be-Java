package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AppConfig {

    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository){
        return args ->{
            if(userRepository.findByUsername("admin").isEmpty()){
                User user= User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .name("Vua")
//                        .role(RoleName.ADMIN)
                        .build();

                userRepository.save(user);
                log.warn("create Admin success, please change password");
            }
        };
    }
}
