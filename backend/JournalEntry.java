package org.example;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String referenceNumber; // PR number

    private LocalDateTime dateCreated = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private JournalStatus status = JournalStatus.PENDING;

    @ManyToOne
    private User createdBy;

    @ManyToOne
    private User approvedBy;

    private String rejectionReason;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JournalDetail> details = new ArrayList<>();

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    public BigDecimal getTotalDebits() {
        return details.stream()
                .filter(d -> d.getType() == EntryType.DEBIT)
                .map(JournalDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalCredits() {
        return details.stream()
                .filter(d -> d.getType() == EntryType.CREDIT)
                .map(JournalDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
