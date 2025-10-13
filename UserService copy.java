package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    // Register a new user and send approval email
    public User registerUser(User user) {
        // If username provided contains spaces, generate canonical username
        String baseUsername = user.getUsername() == null ? "user" : user.getUsername().trim();
        String initial = baseUsername.length() > 0 ? baseUsername.substring(0, 1).toLowerCase() : "u";
        String[] nameParts = baseUsername.split("\\s+");
        String lastName = nameParts[nameParts.length - 1].toLowerCase();
        String mmYY = String.format("%02d%02d", LocalDateTime.now().getMonthValue(),
                LocalDateTime.now().getYear() % 100);

        String candidate = initial + lastName + mmYY;
        // Ensure uniqueness by appending numeric suffix if needed
        int suffix = 0;
        String uniqueCandidate = candidate;
        while (userRepository.findByUsername(uniqueCandidate).isPresent()) {
            suffix++;
            uniqueCandidate = candidate + suffix;
        }

        user.setUsername(uniqueCandidate);

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        user.setPasswordLastChanged(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        // Send registration approval email (if mail is configured)
        sendEmail(user.getEmail(), "Registration Successful",
                "Hello " + user.getUsername() + ",\n\nYour registration request has been received and approved.\n\nRegards,\nAccounting Team");

        return savedUser;
    }

    // Login method
    public boolean login(String username, String password) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) return false;

        User user = optionalUser.get();
        if (user.isSuspended()) return false;

        boolean matches = passwordEncoder.matches(password, user.getPassword());
        if (!matches) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) user.setSuspended(true);
            userRepository.save(user);

            if (user.isSuspended()) {
                sendEmail(user.getEmail(), "Account Suspended",
                        "Hello " + user.getUsername() + ",\n\nYour account has been suspended due to multiple failed login attempts.\n\nRegards,\nAccounting Team");
            }

            return false;
        }

        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        // Password expiration warning
        if (user.getPasswordLastChanged() != null &&
                ChronoUnit.DAYS.between(user.getPasswordLastChanged(), LocalDateTime.now()) > 80) {
            sendEmail(user.getEmail(), "Password Expiration Warning",
                    "Hello " + user.getUsername() + ",\n\nYour password will expire soon. Please update it to avoid lockout.\n\nRegards,\nAccounting Team");
        }

        return true;
    }

    // Update password with validation
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) return false;

        User user = optionalUser.get();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) return false;
        if (!isValidPassword(newPassword)) return false;

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordLastChanged(LocalDateTime.now());
        userRepository.save(user);

        sendEmail(user.getEmail(), "Password Updated",
                "Hello " + user.getUsername() + ",\n\nYour password has been successfully updated.\n\nRegards,\nAccounting Team");

        return true;
    }

    // Helper to send email
    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("no-reply@accounting.com");
            mailSender.send(message);
        } catch (Exception e) {
            // For local testing, mail server might not be configured — swallow or log
            System.out.println("Mail send failed (local/dev): " + e.getMessage());
        }
    }

    // Password policy enforcement
    private boolean isValidPassword(String password) {
        if (password == null) return false;
        if (password.length() < 8) return false;
        if (!password.chars().anyMatch(Character::isUpperCase)) return false;
        if (!password.chars().anyMatch(Character::isLowerCase)) return false;
        if (!password.chars().anyMatch(Character::isDigit)) return false;
        return true;
    }
}
