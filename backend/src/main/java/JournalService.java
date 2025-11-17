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

    /**
     * Creates a journal entry using only accounts found in the Chart of Accounts.
     */
    @Transactional
    public JournalEntry createJournalEntry(Long userId, JournalEntry entry) {
        // 1) Authorize creator
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (user.getRole() != Role.MANAGER && user.getRole() != Role.ACCOUNTANT) {
            throw new SecurityException("Only Manager or Accountant can create journal entries.");
        }

        // 2) Validate that each journal detail uses an existing account
        if (entry.getDetails() == null || entry.getDetails().isEmpty()) {
            throw new IllegalArgumentException("Journal entry must contain at least one detail line.");
        }

        for (JournalDetail d : entry.getDetails()) {
            if (d.getAccount() == null || d.getAccount().getId() == null) {
                throw new IllegalArgumentException("Each journal detail must reference an account.");
            }
            Account acct = accountRepo.findById(d.getAccount().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Account not found in Chart of Accounts: id=" + d.getAccount().getId()));
            d.setAccount(acct);
            d.setJournalEntry(entry);
        }

        // 3) Validate debit/credit balance
        BigDecimal totalDebits = entry.getTotalDebits();
        BigDecimal totalCredits = entry.getTotalCredits();
        if (totalDebits.compareTo(totalCredits) != 0) {
            errorRepo.save(new ErrorMessage(null, "E001", "Debits must equal credits."));
            throw new IllegalArgumentException("Debits must equal credits.");
        }

        // 4) Set metadata
        entry.setCreatedBy(user);
        entry.setStatus(JournalStatus.PENDING);
        entry.setDateCreated(LocalDateTime.now());
        entry.setReferenceNumber("PR-" + System.currentTimeMillis());

        // 5) Save
        return journalRepo.save(entry);
    }

    /**
     * Manager approves a journal entry prepared by an accountant.
     */
    @Transactional
    public JournalEntry approveJournal(Long entryId, Long managerId) {
        JournalEntry entry = journalRepo.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found."));

        // Must be pending and created by accountant
        if (entry.getStatus() != JournalStatus.PENDING) {
            throw new IllegalStateException("Only PENDING entries can be approved.");
        }
        if (entry.getCreatedBy() == null || entry.getCreatedBy().getRole() != Role.ACCOUNTANT) {
            throw new IllegalStateException("Only entries prepared by an ACCOUNTANT can be approved.");
        }

        // Validate manager
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

    /**
     * Manager rejects a journal entry prepared by an accountant, with a required reason.
     */
    @Transactional
    public JournalEntry rejectJournal(Long entryId, Long managerId, String reason) {
        JournalEntry entry = journalRepo.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found."));

        // Must be pending and created by accountant
        if (entry.getStatus() != JournalStatus.PENDING) {
            throw new IllegalStateException("Only PENDING entries can be rejected.");
        }
        if (entry.getCreatedBy() == null || entry.getCreatedBy().getRole() != Role.ACCOUNTANT) {
            throw new IllegalStateException("Only entries prepared by an ACCOUNTANT can be rejected.");
        }

        // Validate manager
        User manager = userRepo.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found."));
        if (manager.getRole() != Role.MANAGER) {
            throw new SecurityException("Only managers can reject journal entries.");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required.");
        }

        entry.setApprovedBy(manager);
        entry.setStatus(JournalStatus.REJECTED);
        entry.setRejectionReason(reason.trim());

        return journalRepo.save(entry);
    }

    public List<JournalEntry> getEntriesByStatus(JournalStatus status) {
        return journalRepo.findByStatusOrderByDateCreatedDesc(status);
    }
}
