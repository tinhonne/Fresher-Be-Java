package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

import static com.example.demo.constant.ValidationConstants.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    Long id;

    @Column(length = USERNAME_MAX_LENGTH,nullable = false,unique = true)
    String username;

    @Column(length = PASSWORD_MAX_LENGTH,nullable = false)
    String password;

    @Column(length = USER_NAME_MAX_LENGTH,nullable = false)
    String name;

    @Column(nullable = false)
    @Builder.Default
    boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    boolean mustChangePassword = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    Set<Role> roles = new HashSet<>();

}
