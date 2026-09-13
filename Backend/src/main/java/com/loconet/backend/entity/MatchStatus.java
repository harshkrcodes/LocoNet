package com.loconet.backend.entity;

/**
 * Mirrors the PostgreSQL ENUM type `match_status`.
 */
public enum MatchStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
