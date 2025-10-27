package org.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/journal")
public class JournalEntryController {

    private final JournalService journalService;

    public JournalEntryController(JournalService journalService) {
        this.journalService = journalService;
    }

    // ✅ Create journal entry (already validated in JournalService)
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestParam Long userId, @RequestBody JournalEntry entry) {
        JournalEntry created = journalService.createJournalEntry(userId, entry);
        return ResponseEntity.ok(created);
    }

    // ✅ Approve journal entry
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id, @RequestParam Long managerId) {
        JournalEntry approved = journalService.approveJournal(id, managerId);
        return ResponseEntity.ok(approved);
    }

    // ✅ Reject journal entry (requires reason)
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(
            @PathVariable Long id,
            @RequestParam Long managerId,
            @RequestBody RejectRequest body) {

        if (body == null || body.getReason() == null || body.getReason().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Rejection reason is required.");
        }

        JournalEntry rejected = journalService.rejectJournal(id, managerId, body.getReason());
        return ResponseEntity.ok(rejected);
    }
}
