package com.apextech.transferservice.service.impl;

import com.apextech.transferservice.dto.response.SessionData;
import com.apextech.transferservice.dto.response.SessionResponse;
import com.apextech.transferservice.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private static final String SESSION_PREFIX = "core-banking:session:";
    private static final Duration SESSION_TTL = Duration.ofSeconds(30);
    private final ObjectMapper objectMapper;

    private static final String CONSUME_SCRIPT = """
        return redis.call('DEL', KEYS[1])
        """;

    private final RedisTemplate<String, Object> redisTemplateMap;

    @Override
    public SessionResponse create(SessionData data) {

        String sessionId = UUID.randomUUID().toString();
        String key = SESSION_PREFIX + sessionId;

        Map<String, Object> sessionData = new HashMap<>(data.getAttributes());

        sessionData.put("sessionId", sessionId);
        sessionData.put("createdOn", data.getCreatedOn().toString());

        redisTemplateMap.opsForValue().set(key, sessionData, SESSION_TTL);

        return new SessionResponse(sessionId, SESSION_TTL.toSeconds());
    }

    @Override
    public boolean verify(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }

        return Boolean.TRUE.equals(redisTemplateMap.hasKey(SESSION_PREFIX + sessionId));
    }

    @Override
    public boolean consume(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(CONSUME_SCRIPT, Long.class);

        Long result = redisTemplateMap.execute(script, List.of(SESSION_PREFIX + sessionId));

        return Long.valueOf(1L).equals(result);
    }

    @Override
    public SessionData getSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }

        Object value = redisTemplateMap.opsForValue().get(SESSION_PREFIX + sessionId);

        if (value == null) {
            return null;
        }

        // Convert the Redis JSON/map representation to SessionData
        return objectMapper.convertValue(value, SessionData.class);
    }

}