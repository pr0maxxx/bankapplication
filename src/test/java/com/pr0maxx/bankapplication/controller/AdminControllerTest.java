package com.pr0maxx.bankapplication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pr0maxx.bankapplication.model.Card;
import com.pr0maxx.bankapplication.model.User;
import com.pr0maxx.bankapplication.model.enums.CardStatus;
import com.pr0maxx.bankapplication.security.JwtService;
import com.pr0maxx.bankapplication.service.CardService;
import com.pr0maxx.bankapplication.service.UserDetailsServiceImpl;
import com.pr0maxx.bankapplication.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = {"ADMIN"})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private Card card;
    private User user;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setEmail("admin@example.com");

        card = new Card();
        card.setId(1L);
        card.setNumber("encrypted");
        card.setBalance(1000.0);
        card.setExpirationDate("12/26");
        card.setStatus(CardStatus.ACTIVE);
        card.setOwner(user);
    }

    @Test
    void getAllCards_shouldReturnListOfCards() throws Exception {
        when(cardService.getAllCards()).thenReturn(List.of(card));

        mockMvc.perform(get("/api/admin/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void createCard_shouldReturnCreatedCard() throws Exception {
        when(cardService.createCard(any(Card.class), eq(1L))).thenReturn(card);

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("ownerId", "1")
                        .content(objectMapper.writeValueAsString(card)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(card.getId()));
    }

    @Test
    void changeCardStatus_shouldReturnUpdatedCard() throws Exception {
        card.setStatus(CardStatus.BLOCKED);
        when(cardService.changeStatus(eq(1L), eq(CardStatus.BLOCKED))).thenReturn(card);

        mockMvc.perform(put("/api/admin/cards/1/status")
                        .param("status", "BLOCKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void deleteCard_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/admin/cards/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        when(userService.getById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        user.setEmail("updated@example.com");

        when(userService.getById(1L)).thenReturn(Optional.of(user));
        when(userService.save(any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isNoContent());
    }
}
