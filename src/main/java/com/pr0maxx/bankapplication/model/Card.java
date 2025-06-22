package com.pr0maxx.bankapplication.model;

import com.pr0maxx.bankapplication.model.enums.CardStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cards")
@Getter
@Setter
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String number; // хранится в зашифрованном виде
    @Column(name = "expiration_date", nullable = false)
    private String expirationDate;
    private Double balance;

    @Enumerated(EnumType.STRING)
    private CardStatus status; // ACTIVE, BLOCKED, EXPIRED

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
