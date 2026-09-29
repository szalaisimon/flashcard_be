package com.example.flashcard_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisRateLimitService {

    private static final DefaultRedisScript<Long> RATE_LIMIT = new DefaultRedisScript<>("""
            local count = redis.call('INCR', KEYS[1])
            local ttl = redis.call('TTL', KEYS[1])
            if ttl < 0 then
                redis.call('EXPIRE', KEYS[1], ARGV[2])
                ttl = tonumber(ARGV[2])
            end
            if count > tonumber(ARGV[1]) then
                return math.max(ttl, 1)
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public long retryAfterSeconds(String key, int limit, int timeWindowSeconds) {
        final Long retryAfter = redisTemplate.execute(RATE_LIMIT, List.of(key),
                Integer.toString(limit), Integer.toString(timeWindowSeconds));
        if (retryAfter == null) {
            throw new IllegalStateException("Rate limit check failed.");
        }
        return retryAfter;
    }
}
