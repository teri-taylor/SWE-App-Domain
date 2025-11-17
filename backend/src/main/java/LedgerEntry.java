package org.example;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Account account;

    private LocalDateTime date;

    private String description;

    @Column(precision = 19, scale = 2)
    private BigDecimal debit;

    @Column(precision = 19, scale = 2)
    private BigDecimal credit;

    @Column(precision = 19, scale = 2)
    private BigDecimal balance;

    @ManyToOne
    private JournalEntry journalEntry;
}
