package com.loconet.backend.dto;

import lombok.Data;

/**
 * DTO for receiving shout creation requests from the client.
 */
@Data
public class ShoutRequest {
    private String content;
    private double latitude;
    private double longitude;
    private Integer radiusMeters;
}