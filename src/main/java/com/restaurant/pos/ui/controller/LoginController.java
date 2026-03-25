package com.restaurant.pos.ui.controller;

import com.restaurant.pos.auth.AuthenticationException;
import com.restaurant.pos.model.AppContext;
import com.restaurant.pos.model.User;
import com.restaurant.pos.service.AuthService;
import com.restaurant.pos.service.UserAuthService;

/**
 * Handles login: validates credentials, sets Session and AppContext role, runs onSuccess.
 */
public class LoginController {

    private final UserAuthService userAuthService;
    private final AuthService authService;
    private final AppContext appContext;
    private final Runnable onLoginSuccess;

    public LoginController(UserAuthService userAuthService, AuthService authService, AppContext appContext, Runnable onLoginSuccess) {
        this.userAuthService = userAuthService;
        this.authService = authService;
        this.appContext = appContext;
        this.onLoginSuccess = onLoginSuccess;
    }

    /**
     * Attempt login. On success sets Session.currentUser and appContext role, then runs onLoginSuccess.
     *
     * @param username username
     * @param password password
     * @throws AuthenticationException if credentials are invalid
     */
    public void handleLogin(String username, String password) throws AuthenticationException {
        User user = userAuthService.login(username, password);
        if (appContext != null) {
            appContext.setCurrentRole(user.getRole());
            if (authService != null) {
                com.restaurant.pos.domain.model.Session s = authService.getCurrentSession();
                if (s != null && s.getEmployee() != null) {
                    appContext.setCurrentEmployee(s.getEmployee());
                }
            }
        }
        if (onLoginSuccess != null) {
            onLoginSuccess.run();
        }
    }
}
