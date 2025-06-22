package com.pr0maxx.bankapplication.controller;


import com.pr0maxx.bankapplication.dto.CardResponseDTO;
import com.pr0maxx.bankapplication.dto.TransferRequest;
import com.pr0maxx.bankapplication.model.Card;
import com.pr0maxx.bankapplication.model.User;
import com.pr0maxx.bankapplication.model.enums.CardStatus;
import com.pr0maxx.bankapplication.service.CardService;
import com.pr0maxx.bankapplication.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;
    private final UserService userService;

    public CardController(CardService cardService, UserService userService) {
        this.cardService = cardService;
        this.userService = userService;
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<CardResponseDTO>> getMyCards(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {
        Page<CardResponseDTO> cards = cardService.getUserCards(authentication.getName(), page, size, status);
        return ResponseEntity.ok(cards);
    }

    @PatchMapping("/{id}/block")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> blockCard(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        cardService.requestBlockCard(id, user);
        return ResponseEntity.ok("Карта успешно заблокирована");
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        cardService.transferBetweenCards(request, user);
        return ResponseEntity.ok("Перевод выполнен успешно");
    }

    @GetMapping("/{id}/balance")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Double> getCardBalance(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        Card card = cardService.getById(id)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        if (!card.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Эта карта вам не принадлежит");
        }

        if (!CardStatus.ACTIVE.equals(card.getStatus())) {
            throw new RuntimeException("Карта не активна");
        }

        return ResponseEntity.ok(card.getBalance());
    }



}
