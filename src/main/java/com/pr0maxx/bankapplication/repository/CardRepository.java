package com.pr0maxx.bankapplication.repository;

import com.pr0maxx.bankapplication.model.Card;
import com.pr0maxx.bankapplication.model.User;
import com.pr0maxx.bankapplication.model.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findAllByOwner(User owner);
    Page<Card> findByOwner(User owner, Pageable pageable);

    Page<Card> findByOwnerAndStatus(User owner, CardStatus status, Pageable pageable);
}
