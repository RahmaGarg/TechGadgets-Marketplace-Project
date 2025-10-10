package com.example.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.demo.entities.Role;
import com.example.demo.enums.RoleType;
import com.example.demo.repositories.RoleRepository;

@Configuration
public class DataInitializer {
    
    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepository) {
        return args -> {
            // Créer les 3 rôles s'ils n'existent pas
            for (RoleType roleType : RoleType.values()) {
                if (!roleRepository.existsByName(roleType)) {
                    Role role = Role.builder()
                            .name(roleType)
                            .build();
                    roleRepository.save(role);
                }
            }
            System.out.println("✓ Roles initialized: ADMIN, SELLER, CLIENT");
        };
    }
}