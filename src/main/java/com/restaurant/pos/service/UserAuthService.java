package com.restaurant.pos.service;

import com.restaurant.pos.auth.AuthenticationException;
import com.restaurant.pos.model.User;

/**
 * Authentication service for app login.
 * In-memory; hardcoded users. Returns User on success, throws on failure.
 */
public interface UserAuthService {

    /**
     * Authenticate by username and password.
     *
     * @param username username
     * @param password password
     * @return authenticated User (and sets Session.currentUser)
     * @throws AuthenticationException if credentials are invalid
     */
    User login(String username, String password) throws AuthenticationException;

    /** Clear current user (logout). */
    void logout();
}
