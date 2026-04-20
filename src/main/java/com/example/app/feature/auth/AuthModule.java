package com.example.app.feature.auth;

import javax.sql.DataSource;

import com.example.app.feature.auth.repository.UserRepository;
import com.example.app.feature.auth.service.AuthService;

public class AuthModule {

    private final UserRepository userRepository;
    private final AuthService authService;

    public AuthModule(DataSource dataSource) {
        this.userRepository = new UserRepository(dataSource);
        this.authService = new AuthService(userRepository);
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public AuthService getAuthService() {
        return authService;
    }
}