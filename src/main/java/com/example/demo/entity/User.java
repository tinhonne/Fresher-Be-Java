package com.example.demo.entity;

import static com.example.demo.constant.ValidationConstants.*;

import com.example.demo.security.authorization.AppRole;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

  @Column(length = USERNAME_MAX_LENGTH, nullable = false, unique = true)
  String username;

  @Column(length = PASSWORD_MAX_LENGTH, nullable = false)
  String password;

  @Column(length = USER_NAME_MAX_LENGTH, nullable = false)
  String name;

  @Column(nullable = false)
  @Builder.Default
  boolean enabled = true;

  @Column(nullable = false)
  @Builder.Default
  boolean mustChangePassword = true;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "role_name", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  Set<AppRole> roles = new HashSet<>();
}
