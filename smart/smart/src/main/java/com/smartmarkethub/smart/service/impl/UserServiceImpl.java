package com.smartmarkethub.smart.service.impl;

import com.smartmarkethub.smart.model.User;
import com.smartmarkethub.smart.model.UserRole;
import com.smartmarkethub.smart.repository.RoleRepository;
import com.smartmarkethub.smart.repository.UserRepository;
import com.smartmarkethub.smart.service.UserService;
import com.smartmarkethub.smart.web.dto.UserCreateRequest;
import com.smartmarkethub.smart.web.dto.UserProfileUpdateRequest;
import com.smartmarkethub.smart.web.dto.UserStats;
import com.smartmarkethub.smart.web.exception.DuplicateResourceException;
import com.smartmarkethub.smart.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import com.smartmarkethub.smart.model.Order;
import com.smartmarkethub.smart.model.Review;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User createUser(UserCreateRequest request, boolean isAdmin) {
        logger.debug("Creating new user: {}", request.getUsername());
        
     
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setIsActive(true);

    
        Set<UserRole> roles = new HashSet<>();
        roles.add(roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Default role not found")));

        if (isAdmin) {
            roles.add(roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new IllegalStateException("Admin role not found")));
        }

        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        logger.info("Created new user: {}", savedUser.getUsername());
        
        return savedUser;
    }

    @Override
    public User updateProfile(Long id, UserProfileUpdateRequest request) {
        logger.debug("Updating profile for user id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

     
        userRepository.findByEmail(request.getEmail())
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(id)) {
                        throw new DuplicateResourceException("User", "email", request.getEmail());
                    }
                });

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }

        User updatedUser = userRepository.save(user);
        logger.info("Updated profile for user: {}", updatedUser.getUsername());
        
        return updatedUser;
    }

    @Override
    public boolean changePassword(Long id, String currentPassword, String newPassword) {
        logger.debug("Changing password for user id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            logger.warn("Invalid current password for user: {}", user.getUsername());
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Changed password for user: {}", user.getUsername());
        
        return true;
    }

    @Override
    public User activateUser(Long id) {
        logger.debug("Activating user id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setIsActive(true);
        User activatedUser = userRepository.save(user);
        logger.info("Activated user: {}", activatedUser.getUsername());
        
        return activatedUser;
    }

    @Override
    public User deactivateUser(Long id) {
        logger.debug("Deactivating user id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setIsActive(false);
        User deactivatedUser = userRepository.save(user);
        logger.info("Deactivated user: {}", deactivatedUser.getUsername());
        
        return deactivatedUser;
    }

    @Override
    public boolean verifyPassword(Long id, String password) {
        return userRepository.findById(id)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public UserStats getUserStats(Long id) {
        logger.debug("Getting stats for user id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

     
        List<Order> orders = new ArrayList<>(user.getOrders());
        BigDecimal totalSpent = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgOrderValue = orders.isEmpty() ? BigDecimal.ZERO :
                totalSpent.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);

      
        Map<String, Long> categoryCount = orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                    item -> item.getProduct().getCategory().getName(),
                    Collectors.counting()
                ));

        List<String> favoriteCategories = categoryCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

      
        Map<String, Integer> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(
                    Order::getStatus,
                    Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

       
        Map<String, BigDecimal> spendingByCategory = orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                    item -> item.getProduct().getCategory().getName(),
                    Collectors.mapping(
                        item -> item.getPriceAtTime().multiply(BigDecimal.valueOf(item.getQuantity())),
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                    )
                ));

        return UserStats.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .memberSince(user.getCreatedAt())
                .totalOrders(orders.size())
                .totalSpent(totalSpent)
                .averageOrderValue(avgOrderValue)
                .favoriteCategories(favoriteCategories)
                .ordersByStatus(ordersByStatus)
                .spendingByCategory(spendingByCategory)
                .lastOrderDate(orders.isEmpty() ? null : 
                        orders.stream()
                            .max(Comparator.comparing(Order::getCreatedAt))
                            .map(Order::getCreatedAt)
                            .orElse(null))
                .lastLoginDate(user.getLastLoginDate())
                .reviewsWritten(user.getReviews().size())
                .averageRating(calculateAverageRating(new ArrayList<>(user.getReviews())))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findActiveUsers() {
        return userRepository.findByIsActive(true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findUsersByRole(String roleName) {
        return userRepository.findByRole(roleName);
    }

    @Override
    public User updateUserRoles(Long id, List<String> roleNames) {
        logger.debug("Updating roles for user id: {} to {}", id, roleNames);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        Set<UserRole> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + name)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        User updatedUser = userRepository.save(user);
        logger.info("Updated roles for user: {}", updatedUser.getUsername());
        
        return updatedUser;
    }

    @Override
    public boolean hasRole(Long id, String roleName) {
        return userRepository.findById(id)
                .map(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals(roleName)))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserActivityMetrics(Long id) {
        logger.debug("Getting activity metrics for user id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        Map<String, Object> metrics = new HashMap<>();
        
       
        List<Order> orders = new ArrayList<>(user.getOrders());
        metrics.put("totalOrders", orders.size());
        metrics.put("totalSpent", orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
                
       
        List<Review> reviews = new ArrayList<>(user.getReviews());
        metrics.put("totalReviews", reviews.size());
        metrics.put("averageRating", calculateAverageRating(reviews));
        
      
        metrics.put("activityTimeline", generateActivityTimeline(user));
        
        return metrics;
    }

    private double calculateAverageRating(List<Review> reviews) {
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    private Map<String, List<String>> generateActivityTimeline(User user) {
        Map<String, List<String>> timeline = new TreeMap<>(Collections.reverseOrder());
        
    
        user.getOrders().forEach(order -> {
            String date = order.getCreatedAt().toLocalDate().toString();
            timeline.computeIfAbsent(date, k -> new ArrayList<>())
                    .add("Placed order #" + order.getId());
        });
        
       
        user.getReviews().forEach(review -> {
            String date = review.getCreatedAt().toLocalDate().toString();
            timeline.computeIfAbsent(date, k -> new ArrayList<>())
                    .add("Reviewed " + review.getProduct().getName());
        });
        
        return timeline;
    }
}
