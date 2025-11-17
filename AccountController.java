package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    // Add account — requires userId of a real user (must be ADMIN)
    @PostMapping("/add")
    public Account addAccount(@RequestBody Account account, @RequestParam Long userId) {
        return accountService.addAccount(account, userId);
    }

    // Edit account — requires userId of a real user (must be ADMIN)
    @PostMapping("/edit/{id}")
    public Account editAccount(@PathVariable Long id, @RequestBody Account account, @RequestParam Long userId) {
        return accountService.editAccount(id, account, userId);
    }

    // Deactivate account — requires real userId (must be ADMIN)
    @PostMapping("/deactivate/{id}")
    public String deactivateAccount(@PathVariable Long id, @RequestParam Long userId) {
        accountService.deactivateAccount(id, userId);
        return "Account deactivated.";
    }

    // Search / filter
    @GetMapping("/search")
    public List<Account> search(@RequestParam(required = false) String token) {
        return accountService.searchAccounts(token);
    }
}
