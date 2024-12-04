package org.example.journal_app.bootstrap;

import lombok.RequiredArgsConstructor;
import org.example.journal_app.entities.User;
import org.example.journal_app.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class InitData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            initDefaultUsers();
        }
    }

    private void initDefaultUsers() {
        User user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("password"))
                .email("user@example.com")
                .build();

        userRepository.save(user);
    }
}
