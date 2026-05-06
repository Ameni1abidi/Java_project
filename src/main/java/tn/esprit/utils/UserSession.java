package tn.esprit.utils;

import tn.esprit.entities.User;

public final class UserSession {
    private static User currentUser;
    private static String sessionToken;

    private UserSession() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setSessionToken(String token) {
        sessionToken = token;
    }

    public static String getSessionToken() {
        return sessionToken;
    }

    public static void clear() {
        currentUser = null;
        sessionToken = null;
    }
}
