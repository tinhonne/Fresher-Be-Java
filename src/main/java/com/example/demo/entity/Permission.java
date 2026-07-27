package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    Long id;

    @Column(length = 50, nullable = false, unique = true)
    String code;

    @Column(length = 255)
    String description;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    Set<Role> roles = new HashSet<>();
}


