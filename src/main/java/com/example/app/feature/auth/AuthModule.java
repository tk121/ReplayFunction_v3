package com.example.app.feature.auth;

import javax.sql.DataSource;

import com.example.app.feature.auth.repository.UserRepository;
import com.example.app.feature.auth.service.AuthService;
import com.example.app.feature.auth.session.LoginPolicy;
import com.example.app.feature.auth.session.LoginUserRegistry;
import com.example.app.feature.auth.session.OnlineUserRegistry;

public class AuthModule {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final LoginUserRegistry loginRegistry;
    private final OnlineUserRegistry onlineRegistry;
    

    public AuthModule(DataSource dataSource, LoginPolicy loginPolicy) {
        this.userRepository = new UserRepository(dataSource);
        this.authService = new AuthService(userRepository);
        this.loginRegistry = new LoginUserRegistry();
        this.onlineRegistry = new OnlineUserRegistry(loginPolicy.getOnlineTimeoutMillis());
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public AuthService getAuthService() {
        return authService;
    }
    
    public LoginUserRegistry getLoginRegistry() {
		return loginRegistry;
	}
    
    public OnlineUserRegistry getOnlineRegistry() {
		return onlineRegistry;
	}
}