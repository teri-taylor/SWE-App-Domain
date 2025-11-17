package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/journal")
public class JournalEntryController {

    @Autowired
    private JournalService journalService;

    // ✅ Returns all journal entries for a given status (PENDING, APPROVED, REJECTED)
    @GetMapping("/status/{status}")
    public ResponseEntity<List<JournalEntry>> findByStatus(@PathVariable JournalStatus status) {
        return ResponseEntity.ok(journalService.getEntriesByStatus(status));
    }

    // ✅ Optional shortcuts — so you can visit these directly
    @GetMapping("/approved")
    public ResponseEntity<List<JournalEntry>> getApproved() {
        return ResponseEntity.ok(journalService.getEntriesByStatus(JournalStatus.APPROVED));
    }

    @GetMapping("/rejected")
    public ResponseEntity<List<JournalEntry>> getRejected() {
        return ResponseEntity.ok(journalService.getEntriesByStatus(JournalStatus.REJECTED));
    }
}

