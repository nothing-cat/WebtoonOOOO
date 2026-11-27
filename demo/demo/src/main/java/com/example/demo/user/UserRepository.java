package com.example.demo.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @Modifying(clearAutomatically = true)
    @Query("update User u set u.nickname = :nickname where u.id = :id and u.blocked = false")
    int updateNickname(@Param("id") UUID id, @Param("nickname") String nickname);
}
