package ru.practicum.yandex;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.practicum.yandex.user.model.User;
import ru.practicum.yandex.user.model.UserRole;
import ru.practicum.yandex.user.repository.UserRepository;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class EwmServiceApp {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(EwmServiceApp.class, args);
    }

    @PostConstruct
    public void init() {
        User admin = User.builder()
                .email("admin@email.com")
                .name("admin")
                .role(UserRole.ADMIN)
                .password(passwordEncoder.encode("password"))
                .build();
        userRepository.save(admin);
    }
}