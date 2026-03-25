package com.restaurant.pos.model;

/**
 * Application session holding the currently logged-in user.
 * No dashboard access without Session.currentUser set.
 */
public final class Session {

    private static User currentUser;

    private Session() {
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /** Clear session (logout). */
    public static void clear() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
