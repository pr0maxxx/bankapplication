package com.pr0maxx.bankapplication.service;

import com.pr0maxx.bankapplication.dto.CardResponseDTO;
import com.pr0maxx.bankapplication.dto.TransferRequest;
import com.pr0maxx.bankapplication.model.Card;
import com.pr0maxx.bankapplication.model.User;
import com.pr0maxx.bankapplication.model.enums.CardStatus;
import com.pr0maxx.bankapplication.repository.CardRepository;
import com.pr0maxx.bankapplication.repository.UserRepository;
import com.pr0maxx.bankapplication.security.EncryptionService;
import com.pr0maxx.bankapplication.util.CardUtils;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    public CardService(CardRepository cardRepository, UserRepository userRepository, EncryptionService encryptionService) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    public Optional<Card> getById(Long id) {
        return cardRepository.findById(id);
    }

    public void deleteById(Long id) {
        cardRepository.deleteById(id);
    }

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    public Card createCard(Card card, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        try {
            card.setNumber(encryptionService.encrypt(card.getNumber()));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при шифровании номера карты", e);
        }

        card.setOwner(owner);
        card.setStatus(CardStatus.ACTIVE);
        return cardRepository.save(card);
    }

    public Card changeStatus(Long cardId, CardStatus status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));
        card.setStatus(status);
        return cardRepository.save(card);
    }


    public Page<CardResponseDTO> getUserCards(String username, int page, int size, String statusFilter) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cards;

        if (statusFilter != null) {
            CardStatus status = CardStatus.valueOf(statusFilter.toUpperCase());
            cards = cardRepository.findByOwnerAndStatus(user, status, pageable);
        } else {
            cards = cardRepository.findByOwner(user, pageable);
        }

        return cards.map(card -> {
            CardResponseDTO dto = new CardResponseDTO();
            try {
                String decryptedNumber = encryptionService.decrypt(card.getNumber());
                dto.setNumber(CardUtils.maskCardNumber(decryptedNumber));
            } catch (Exception e) {
                dto.setNumber("**** **** **** ERROR");
            }
            dto.setId(card.getId());
            dto.setExpirationDate(card.getExpirationDate());
            dto.setBalance(card.getBalance());
            dto.setStatus(card.getStatus());
            return dto;
        });
    }

    public void requestBlockCard(Long cardId, User user) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        if (!card.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Нет доступа к этой карте");
        }

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new RuntimeException("Карта уже заблокирована");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Transactional
    public void transferBetweenCards(TransferRequest request, User user) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть положительной");
        }

        Card from = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new RuntimeException("Карта списания не найдена"));
        Card to = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new RuntimeException("Карта зачисления не найдена"));

        if (!from.getOwner().getId().equals(user.getId()) || !to.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Обе карты должны принадлежать вам");
        }

        if (!CardStatus.ACTIVE.equals(from.getStatus()) || !CardStatus.ACTIVE.equals(to.getStatus())) {
            throw new RuntimeException("Обе карты должны быть активными");
        }

        if (from.getBalance() < request.getAmount()) {
            throw new RuntimeException("Недостаточно средств на карте списания");
        }

        from.setBalance(from.getBalance() - request.getAmount());
        to.setBalance(to.getBalance() + request.getAmount());

        cardRepository.save(from);
        cardRepository.save(to);
    }

}
