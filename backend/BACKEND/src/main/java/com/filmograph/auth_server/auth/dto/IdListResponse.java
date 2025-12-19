package com.filmograph.auth_server.auth.dto;

import java.util.List; 

public class IdListResponse {
    private final List<Long> items;
    public IdListResponse(List<Long> items) { this.items = items; }
    public List<Long> getItems() { return items; }
} 

