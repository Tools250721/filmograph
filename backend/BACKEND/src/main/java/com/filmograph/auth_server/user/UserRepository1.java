package com.filmograph.auth_server.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Deprecated
interface UserRepository1 extends JpaRepository<User, Long> {

    @Deprecated
    boolean existsByEmailIgnoreCase(String email);

    @Deprecated
    Optional<User> findByEmailIgnoreCase(String email);

    @Deprecated
    default boolean existsByEmailNormalized(String email) {
        return existsByEmailIgnoreCase(email == null ? null : email.trim());
    }

    @Deprecated
    default Optional<User> findByEmailNormalized(String email) {
        return findByEmailIgnoreCase(email == null ? null : email.trim());
    }
}
