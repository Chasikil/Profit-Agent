package com.restaurant.pos.model;

/**
 * Simple user roles for application-level access control.
 * Separate from domain Role enum to keep UI/app concerns isolated.
 */
public enum UserRole {
    ADMIN,
    WAITER
}
