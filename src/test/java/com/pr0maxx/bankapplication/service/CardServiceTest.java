package com.pr0maxx.bankapplication.service;

import com.pr0maxx.bankapplication.dto.TransferRequest;
import com.pr0maxx.bankapplication.model.Card;
import com.pr0maxx.bankapplication.model.User;
import com.pr0maxx.bankapplication.model.enums.CardStatus;
import com.pr0maxx.bankapplication.repository.CardRepository;
import com.pr0maxx.bankapplication.repository.UserRepository;
import com.pr0maxx.bankapplication.security.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock private UserRepository userRepository;
    @Mock private EncryptionService encryptionService;

    @InjectMocks
    private CardService cardService;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
    }

    @Test
    void createCard_shouldEncryptAndSave() throws Exception {
        Card card = new Card();
        card.setNumber("1234-5678-9012-3456");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted");
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        Card saved = cardService.createCard(card, 1L);

        assertEquals(user, saved.getOwner());
        assertEquals(CardStatus.ACTIVE, saved.getStatus());
        assertEquals("encrypted", saved.getNumber());
    }

    @Test
    void createCard_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        Card card = new Card();

        assertThrows(RuntimeException.class, () -> cardService.createCard(card, 1L));
    }

    @Test
    void changeStatus_shouldUpdateStatus() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Card updated = cardService.changeStatus(1L, CardStatus.BLOCKED);

        assertEquals(CardStatus.BLOCKED, updated.getStatus());
    }

    @Test
    void requestBlockCard_shouldBlockSuccessfully() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.ACTIVE);
        card.setOwner(user);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.requestBlockCard(1L, user);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void transferBetweenCards_shouldTransferSuccessfully() {
        Card from = new Card();
        from.setId(1L);
        from.setOwner(user);
        from.setStatus(CardStatus.ACTIVE);
        from.setBalance(500.0);

        Card to = new Card();
        to.setId(2L);
        to.setOwner(user);
        to.setStatus(CardStatus.ACTIVE);
        to.setBalance(100.0);

        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(200.0);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(to));

        cardService.transferBetweenCards(request, user);

        assertEquals(300.0, from.getBalance());
        assertEquals(300.0, to.getBalance());
    }
}
