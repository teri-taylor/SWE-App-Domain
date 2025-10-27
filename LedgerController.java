package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    @Autowired
    private LedgerRepository ledgerRepo;

    @GetMapping("/{accountId}")
    public List<LedgerEntry> getLedger(@PathVariable Long accountId) {
        return ledgerRepo.findAll()
                .stream()
                .filter(l -> l.getAccount().getId().equals(accountId))
                .toList();
    }
}
