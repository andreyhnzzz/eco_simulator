package com.ecosimulator.auth;

/**
 * Session singleton to store the currently authenticated user
 * Provides global access to the logged-in user throughout the application
 */
public class Session {
    private static User currentUser = null;

    private Session() {
        // Private constructor to prevent instantiation
    }

    /**
     * Set the current logged-in user
     * @param user the authenticated user
     */
    public static void setUser(User user) {
        currentUser = user;
    }

    /**
     * Get the current logged-in user
     * @return the current user, or null if not logged in
     */
    public static User getUser() {
        return currentUser;
    }

    /**
     * Check if a user is currently logged in
     * @return true if a user is logged in
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Clear the current session (logout)
     */
    public static void logout() {
        currentUser = null;
    }
}
