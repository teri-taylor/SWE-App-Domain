package org.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ledger")
public class LedgerController {

    private final LedgerRepository ledgerRepo;
    private final AccountRepository accountRepo;

    public LedgerController(LedgerRepository ledgerRepo, AccountRepository accountRepo) {
        this.ledgerRepo = ledgerRepo;
        this.accountRepo = accountRepo;
    }

    // View the ledger for a specific account
    // GET /ledger/{accountId}
    @GetMapping("/{accountId}")
    public ResponseEntity<?> listByAccount(@PathVariable Long accountId) {
        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
        List<LedgerEntry> entries = ledgerRepo.findByAccountIdOrderByDateAscIdAsc(accountId);
        return ResponseEntity.ok(entries);
    }
}
