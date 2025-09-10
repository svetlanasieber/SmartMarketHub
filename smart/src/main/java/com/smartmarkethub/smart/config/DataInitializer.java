package com.smartmarkethub.smart.config;

import com.smartmarkethub.smart.model.User;
import com.smartmarkethub.smart.model.UserRole;
import com.smartmarkethub.smart.repository.RoleRepository;
import com.smartmarkethub.smart.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            UserRole userRole = new UserRole("ROLE_USER");
            roleRepository.save(userRole);

            UserRole adminRole = new UserRole("ROLE_ADMIN");
            roleRepository.save(adminRole);
        }

        if (userRepository.count() == 0) {
            UserRole adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();

            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@smartmarkethub.com");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRoles(Set.of(adminRole));

            userRepository.save(admin);
        }
    }
}
