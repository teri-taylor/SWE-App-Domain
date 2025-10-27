package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journals")
public class JournalController {

    @Autowired
    private JournalService journalService;

    @PostMapping("/create")
    public JournalEntry createJournal(@RequestParam Long userId, @RequestBody JournalEntry entry) {
        return journalService.createJournalEntry(userId, entry);
    }

    @PostMapping("/approve/{id}")
    public JournalEntry approveJournal(@PathVariable Long id, @RequestParam Long managerId) {
        return journalService.approveJournal(id, managerId);
    }

    @PostMapping("/reject/{id}")
    public JournalEntry rejectJournal(@PathVariable Long id, @RequestParam Long managerId, @RequestParam String reason) {
        return journalService.rejectJournal(id, managerId, reason);
    }

    @GetMapping("/status/{status}")
    public List<JournalEntry> getByStatus(@PathVariable JournalStatus status) {
        return journalService.getEntriesByStatus(status);
    }
}
