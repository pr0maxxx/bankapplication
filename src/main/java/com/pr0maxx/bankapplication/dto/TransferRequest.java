package com.pr0maxx.bankapplication.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequest {
    private Long fromCardId;
    private Long toCardId;
    private Double amount;
}
