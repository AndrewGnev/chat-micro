package com.quarkus.chat.service;

import com.quarkus.chat.dto.AuthDto;

public interface AuthService {
    String login(AuthDto dto);
}
