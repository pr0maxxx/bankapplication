package com.pr0maxx.bankapplication.dto;


import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}
