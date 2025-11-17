package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        boolean success = userService.login(username, password);
        return success ? "Login successful" : "Login failed";
    }

    @PostMapping("/update-password")
    public String updatePassword(@RequestParam Long userId,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword) {
        boolean updated = userService.updatePassword(userId, oldPassword, newPassword);
        return updated ? "Password updated" : "Failed to update password";
    }
}
