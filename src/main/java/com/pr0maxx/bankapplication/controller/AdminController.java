package com.pr0maxx.bankapplication.controller;

import com.pr0maxx.bankapplication.model.Card;
import com.pr0maxx.bankapplication.model.User;
import com.pr0maxx.bankapplication.model.enums.CardStatus;
import com.pr0maxx.bankapplication.service.CardService;
import com.pr0maxx.bankapplication.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final CardService cardService;
    private final UserService userService;

    public AdminController(CardService cardService, UserService userService) {
        this.cardService = cardService;
        this.userService = userService;
    }

    // --- КАРТЫ ---

    // Получить все карты
    @GetMapping("/cards")
    public ResponseEntity<List<Card>> getAllCards() {
        return ResponseEntity.ok(cardService.getAllCards());
    }

    // Создать карту для пользователя
    @PostMapping("/cards")
    public ResponseEntity<Card> createCard(@RequestBody Card card, @RequestParam Long ownerId) {
        return ResponseEntity.ok(cardService.createCard(card, ownerId));
    }

    // Изменить статус карты (активировать/блокировать)
    @PutMapping("/cards/{id}/status")
    public ResponseEntity<Card> changeCardStatus(@PathVariable Long id, @RequestParam CardStatus status) {
        return ResponseEntity.ok(cardService.changeStatus(id, status));
    }

    // Удалить карту
    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // --- ПОЛЬЗОВАТЕЛИ ---

    // Получить всех пользователей
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Получить пользователя по id
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Обновить пользователя (например, роль)
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return userService.getById(id)
                .map(user -> {
                    user.setEmail(updatedUser.getEmail());
                    user.setRole(updatedUser.getRole());
                    // Добавь другие поля, которые можно менять
                    return ResponseEntity.ok(userService.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Удалить пользователя
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
