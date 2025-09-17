package com.smartmarkethub.smart.service;

import com.smartmarkethub.smart.model.User;
import com.smartmarkethub.smart.web.dto.UserCreateRequest;
import com.smartmarkethub.smart.web.dto.UserProfileUpdateRequest;
import com.smartmarkethub.smart.web.dto.UserStats;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {
    List<User> findAll();
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    User createUser(UserCreateRequest request, boolean isAdmin);
    User updateProfile(Long id, UserProfileUpdateRequest request);
    boolean changePassword(Long id, String currentPassword, String newPassword);
    User activateUser(Long id);
    User deactivateUser(Long id);
    boolean verifyPassword(Long id, String password);
    UserStats getUserStats(Long id);
    List<User> findActiveUsers();
    List<User> findUsersByRole(String roleName);
    User updateUserRoles(Long id, List<String> roleNames);
    boolean hasRole(Long id, String roleName);
    Map<String, Object> getUserActivityMetrics(Long id);
}