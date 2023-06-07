package com.quarkus.chat.dto;

import lombok.Value;

@Value
public class AuthDto {
    String username;
    String password;
}
