package org.example;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class JournalDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private JournalEntry journalEntry;

    @ManyToOne
    private Account account;

    @Enumerated(EnumType.STRING)
    private EntryType type; // DEBIT or CREDIT

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    private String description;
}
