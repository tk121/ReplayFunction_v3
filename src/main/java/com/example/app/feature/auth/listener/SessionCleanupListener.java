package com.example.app.feature.auth.listener;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.example.app.common.runtime.AppRuntime;
import com.example.app.feature.auth.model.LoginUser;

public class SessionCleanupListener implements HttpSessionListener {

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {

        String userId = (String) se.getSession().getAttribute("replay.loginUser");

        AppRuntime.getAuthModule().getLoginRegistry().logoutBySessionId(se.getSession().getId());
        

        if (userId != null && !AppRuntime.getAuthModule().getLoginRegistry().isLoggedIn(userId)) {
        	AppRuntime.getAuthModule().getOnlineRegistry().remove(userId);
        }
    }
    
    private LoginUser getLoginUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (LoginUser) session.getAttribute("replay.loginUser");
    }
}