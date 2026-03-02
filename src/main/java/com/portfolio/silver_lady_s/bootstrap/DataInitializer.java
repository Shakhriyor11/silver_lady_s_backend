package com.portfolio.silver_lady_s.bootstrap;

import com.portfolio.silver_lady_s.entity.AboutUs;
import com.portfolio.silver_lady_s.entity.User;
import com.portfolio.silver_lady_s.entity.UserRole;
import com.portfolio.silver_lady_s.repository.AboutUsRepository;
import com.portfolio.silver_lady_s.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AboutUsRepository aboutUsRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.enabled:false}")
    private boolean adminEnabled;

    @Value("${app.bootstrap.admin.email:admin@silverladys.uz}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.password:Admin123!}")
    private String adminPassword;

    @Value("${app.bootstrap.admin.full-name:Admin}")
    private String adminFullName;

    @Override
    @Transactional
    public void run(String... args) {
        if (aboutUsRepository.count() == 0) {
            AboutUs a = new AboutUs();
            a.setShopName("Silver Lady's Jewelry");
            a.setAddress("Toshkent, ...");
            a.setPhone("+998 ...");
            a.setEmail("info@silverladys.uz");
            a.setWorkingHours("09:00 - 21:00");
            a.setLocationLink("");
            a.setDescription("Do'kon haqida qisqacha ma'lumot.");
            aboutUsRepository.save(a);
        }

        if (adminEnabled && !userRepository.existsByEmailIgnoreCase(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail.trim().toLowerCase());
            admin.setFullName(adminFullName);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole(UserRole.ADMIN);
            userRepository.save(admin);
        }
    }
}
