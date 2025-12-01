package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EventLogRepository eventLogRepository;

    @Autowired
    private UserRepository userRepository;

    // Add new account (Admins only)
    @Transactional
    public Account addAccount(Account account, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (user.getRole() != Role.ADMIN) {
            throw new SecurityException("Only Admins can add accounts.");
        }

        validateAccount(account);

        if (accountRepository.existsByAccountName(account.getAccountName()) ||
                accountRepository.existsByAccountNumber(account.getAccountNumber())) {
            throw new IllegalArgumentException("Duplicate account name or number.");
        }

        account.setCreatedBy(user);
        Account saved = accountRepository.save(account);
        logEvent("ADD_ACCOUNT", user, null, saved);
        return saved;
    }

    // Edit account (Admins only)
    @Transactional
    public Account editAccount(Long id, Account updatedAccount, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (user.getRole() != Role.ADMIN) {
            throw new SecurityException("Only Admins can edit accounts.");
        }

        Account existing = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        Account before = cloneAccount(existing);

        // If changing number/name ensure no duplicates (except with itself)
        if (!existing.getAccountNumber().equals(updatedAccount.getAccountNumber())
                && accountRepository.existsByAccountNumber(updatedAccount.getAccountNumber())) {
            throw new IllegalArgumentException("Duplicate account number.");
        }

        if (!existing.getAccountName().equalsIgnoreCase(updatedAccount.getAccountName())
                && accountRepository.existsByAccountName(updatedAccount.getAccountName())) {
            throw new IllegalArgumentException("Duplicate account name.");
        }

        existing.setAccountNumber(updatedAccount.getAccountNumber());
        existing.setAccountName(updatedAccount.getAccountName());
        existing.setCategory(updatedAccount.getCategory());
        existing.setSubcategory(updatedAccount.getSubcategory());
        existing.setBalance(updatedAccount.getBalance());

        validateAccount(existing);

        Account saved = accountRepository.save(existing);
        logEvent("EDIT_ACCOUNT", user, before, saved);
        return saved;
    }

    // Deactivate account (Admins only)
    @Transactional
    public void deactivateAccount(Long id, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (user.getRole() != Role.ADMIN) {
            throw new SecurityException("Only Admins can deactivate accounts.");
        }

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Accounts with a balance > 0 cannot be deactivated.");
        }

        Account before = cloneAccount(account);
        account.setActive(false);
        accountRepository.save(account);
        logEvent("DEACTIVATE_ACCOUNT", user, before, account);
    }

    // Search / filter
    public List<Account> searchAccounts(String token) {
        if (token == null || token.trim().isEmpty()) {
            return accountRepository.findAll();
        }
        String q = token.trim();
        return accountRepository
                .findByAccountNameContainingIgnoreCaseOrAccountNumberContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrSubcategoryContainingIgnoreCase(q, q, q, q);
    }

    // Basic validation helpers
    private void validateAccount(Account account) {
        if (!ValidationUtils.isValidAccountNumber(account.getAccountNumber())) {
            throw new IllegalArgumentException("Account number must be numeric only.");
        }

        if (!ValidationUtils.isValidAccountName(account.getAccountName())) {
            throw new IllegalArgumentException("Account name is required.");
        }

        if (account.getBalance() == null) account.setBalance(BigDecimal.ZERO);

        if (!ValidationUtils.hasTwoDecimalPlaces(account.getBalance())) {
            throw new IllegalArgumentException("Monetary values can have at most two decimal places.");
        }
    }

    // Event logging
    private void logEvent(String action, User user, Account before, Account after) {
        EventLog log = new EventLog();
        log.setAction(action);
        log.setUser(user);
        log.setTimestamp(LocalDateTime.now());
        log.setBeforeImage(before != null ? before.toString() : "N/A");
        log.setAfterImage(after != null ? after.toString() : "N/A");
        eventLogRepository.save(log);
    }

    private Account cloneAccount(Account a) {
        if (a == null) return null;
        Account copy = new Account();
        copy.setId(a.getId());
        copy.setAccountNumber(a.getAccountNumber());
        copy.setAccountName(a.getAccountName());
        copy.setCategory(a.getCategory());
        copy.setSubcategory(a.getSubcategory());
        copy.setBalance(a.getBalance());
        copy.setActive(a.isActive());
        return copy;
    }
}
