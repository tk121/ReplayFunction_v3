package com.example.app.feature.auth.service;

import com.example.app.feature.auth.entity.User;
import com.example.app.feature.auth.model.LoginUser;
import com.example.app.feature.auth.repository.UserRepository;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginUser login(String userName, String password) throws Exception {
        if (userName == null || userName.trim().length() == 0) {
            throw new IllegalArgumentException("ユーザー名は必須です");
        }
        if (password == null || password.length() == 0) {
            throw new IllegalArgumentException("パスワードは必須です");
        }

        User user = userRepository.findByUserName(userName.trim());
        if (user == null) {
            throw new IllegalArgumentException("ユーザー名またはパスワードが違います");
        }

        if (!user.isEnabled()) {
            throw new IllegalStateException("このユーザーは無効です");
        }

        if (!matchesPassword(password, user.getPassword())) {
            throw new IllegalArgumentException("ユーザー名またはパスワードが違います");
        }

        return new LoginUser(
                String.valueOf(user.getUserId()),
                user.getUserName(),
                user.isCanControl());
    }
    
    public LoginUser findLoginUserByUserId(String userId) throws Exception {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            return null;
        }
        if (!user.isEnabled()) {
            return null;
        }

        return new LoginUser(
                String.valueOf(user.getUserId()),
                user.getUserName(),
                user.isCanControl());
    }

    /**
     * 今は平文比較。
     * 将来ハッシュ化するときはこのメソッドだけ差し替えればよいです。
     */
    private boolean matchesPassword(String rawPassword, String storedPassword) {
        return storedPassword != null && storedPassword.equals(rawPassword);
    }
}