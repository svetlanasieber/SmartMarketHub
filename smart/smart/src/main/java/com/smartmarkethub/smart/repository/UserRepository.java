package com.smartmarkethub.smart.repository;

import com.smartmarkethub.smart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByIsActive(boolean isActive);
    
    @Query("SELECT u FROM User u WHERE u.lastLoginDate < :date")
    List<User> findInactiveUsers(@Param("date") LocalDateTime date);
    
    @Modifying
    @Query("UPDATE User u SET u.lastLoginDate = :date WHERE u.id = :id")
    void updateLastLoginDate(@Param("id") Long id, @Param("date") LocalDateTime date);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRole(@Param("roleName") String roleName);
}