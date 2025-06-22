package com.pr0maxx.bankapplication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pr0maxx.bankapplication.dto.CardResponseDTO;
import com.pr0maxx.bankapplication.dto.TransferRequest;
import com.pr0maxx.bankapplication.model.Card;
import com.pr0maxx.bankapplication.model.User;
import com.pr0maxx.bankapplication.model.enums.CardStatus;
import com.pr0maxx.bankapplication.service.CardService;
import com.pr0maxx.bankapplication.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "user", roles = {"USER"})
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Card card;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("user");

        card = new Card();
        card.setId(1L);
        card.setOwner(user);
        card.setBalance(1000.0);
        card.setStatus(CardStatus.ACTIVE);
    }

    @Test
    void getMyCards_shouldReturnPageOfCards() throws Exception {
        CardResponseDTO dto = new CardResponseDTO();
        dto.setId(card.getId());
        dto.setBalance(card.getBalance());
        Page<CardResponseDTO> page = new PageImpl<>(List.of(dto));

        Mockito.when(cardService.getUserCards(eq("user"), eq(0), eq(10), any())).thenReturn(page);

        mockMvc.perform(get("/api/cards/my")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void blockCard_shouldReturnSuccessMessage() throws Exception {
        Mockito.when(userService.findByUsername("user")).thenReturn(user);
        Mockito.doNothing().when(cardService).requestBlockCard(1L, user);

        mockMvc.perform(patch("/api/cards/1/block"))
                .andExpect(status().isOk())
                .andExpect(content().string("Карта успешно заблокирована"));
    }

    @Test
    void transfer_shouldReturnSuccessMessage() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(100.0);

        Mockito.when(userService.findByUsername("user")).thenReturn(user);
        Mockito.doNothing().when(cardService).transferBetweenCards(any(TransferRequest.class), eq(user));

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Перевод выполнен успешно"));
    }

    @Test
    void getCardBalance_shouldReturnBalance() throws Exception {
        card.setStatus(CardStatus.ACTIVE); // нужно обязательно, иначе выбросится RuntimeException
        card.setOwner(user); // убедитесь, что карта принадлежит пользователю

        Mockito.when(userService.findByUsername("user")).thenReturn(user);
        Mockito.when(cardService.getById(1L)).thenReturn(Optional.of(card));

        mockMvc.perform(get("/api/cards/1/balance"))
                .andExpect(status().isOk())
                .andExpect(content().string("1000.0"));
    }

    @Test
    void getCardBalance_shouldFailIfNotOwner() throws Exception {
        User otherUser = new User();
        otherUser.setId(2L);
        card.setOwner(otherUser);

        Mockito.when(userService.findByUsername("user")).thenReturn(user);
        Mockito.when(cardService.getById(1L)).thenReturn(Optional.of(card));

        mockMvc.perform(get("/api/cards/1/balance"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getCardBalance_shouldFailIfNotActive() throws Exception {
        card.setStatus(CardStatus.BLOCKED);

        Mockito.when(userService.findByUsername("user")).thenReturn(user);
        Mockito.when(cardService.getById(1L)).thenReturn(Optional.of(card));

        mockMvc.perform(get("/api/cards/1/balance"))
                .andExpect(status().is4xxClientError());
    }
}
