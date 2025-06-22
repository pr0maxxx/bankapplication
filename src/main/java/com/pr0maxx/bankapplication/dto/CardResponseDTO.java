package com.pr0maxx.bankapplication.dto;

import com.pr0maxx.bankapplication.model.enums.CardStatus;
import lombok.Data;

@Data
public class CardResponseDTO {
    private Long id;
    private String number;
    private String expirationDate;
    private Double balance;
    private CardStatus status;
}
