package com.quarkus.chat.dto;

import lombok.Value;

@Value
public class RegistrationDto {
    String username;
    String password;
    String repeatedPassword;
}
