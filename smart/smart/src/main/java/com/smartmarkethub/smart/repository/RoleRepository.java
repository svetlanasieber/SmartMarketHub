package com.smartmarkethub.smart.repository;

import com.smartmarkethub.smart.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<UserRole, Long> {
    
    Optional<UserRole> findByName(String name);
    
    boolean existsByName(String name);
}