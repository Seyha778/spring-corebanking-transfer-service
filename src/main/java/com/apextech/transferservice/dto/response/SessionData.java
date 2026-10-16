package com.apextech.transferservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class SessionData {

    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    @Builder.Default
    private Instant createdOn = Instant.now();

    public void put(String key, Object value) {
        attributes.put(key, value);
    }

    public Object get(String key) {
        return attributes.get(key);
    }
}