package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class JournalService {

    @Autowired
    private JournalEntryRepository journalRepo;

    @Autowired
    private LedgerRepository ledgerRepo;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ErrorMessageRepository errorRepo;

    @Transactional
    public JournalEntry createJournalEntry(Long userId, JournalEntry entry) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (user.getRole() != Role.MANAGER && user.getRole() != Role.ACCOUNTANT) {
            throw new SecurityException("Only Manager or Accountant can create journal entries.");
        }

        // Validate debits and credits
        BigDecimal totalDebits = entry.getTotalDebits();
        BigDecimal totalCredits = entry.getTotalCredits();

        if (totalDebits.compareTo(totalCredits) != 0) {
            errorRepo.save(new ErrorMessage(null, "E001", "Debits must equal credits."));
            throw new IllegalArgumentException("Debits must equal credits.");
        }

        entry.setCreatedBy(user);
        entry.setReferenceNumber("PR-" + System.currentTimeMillis());
        return journalRepo.save(entry);
    }

    @Transactional
    public JournalEntry approveJournal(Long entryId, Long managerId) {
        JournalEntry entry = journalRepo.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found."));

        User manager = userRepo.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (manager.getRole() != Role.MANAGER) {
            throw new SecurityException("Only managers can approve journal entries.");
        }

        entry.setApprovedBy(manager);
        entry.setStatus(JournalStatus.APPROVED);

        // Post to ledger
        for (JournalDetail d : entry.getDetails()) {
            LedgerEntry ledger = new LedgerEntry();
            ledger.setAccount(d.getAccount());
            ledger.setDate(LocalDateTime.now());
            ledger.setDescription("Journal " + entry.getReferenceNumber());
            if (d.getType() == EntryType.DEBIT) {
                ledger.setDebit(d.getAmount());
                ledger.setCredit(BigDecimal.ZERO);
                d.getAccount().setBalance(d.getAccount().getBalance().add(d.getAmount()));
            } else {
                ledger.setCredit(d.getAmount());
                ledger.setDebit(BigDecimal.ZERO);
                d.getAccount().setBalance(d.getAccount().getBalance().subtract(d.getAmount()));
            }
            ledger.setBalance(d.getAccount().getBalance());
            ledger.setJournalEntry(entry);
            ledgerRepo.save(ledger);
            accountRepo.save(d.getAccount());
        }

        return journalRepo.save(entry);
    }

    @Transactional
    public JournalEntry rejectJournal(Long entryId, Long managerId, String reason) {
        JournalEntry entry = journalRepo.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found."));
        User manager = userRepo.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found."));
        entry.setApprovedBy(manager);
        entry.setStatus(JournalStatus.REJECTED);
        entry.setRejectionReason(reason);
        return journalRepo.save(entry);
    }

    public List<JournalEntry> getEntriesByStatus(JournalStatus status) {
        return journalRepo.findAll()
                .stream()
                .filter(e -> e.getStatus() == status)
                .toList();
    }
}
