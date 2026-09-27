package dev.logMonitor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ErrorSpikeService {
    private static final Logger logger = LoggerFactory.getLogger(ErrorSpikeService.class);
    private final StringRedisTemplate redisTemplate;

    // Threshold: Max 5 error logs per service within a 60-second window
    private static final int MAX_ERRORS_PER_MINUTE = 5;
    private static final long WINDOW_SECONDS = 60;

    public ErrorSpikeService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Increments error count for a service in Redis and checks if threshold is exceeded.
     * @param serviceName Name of the microservice reporting the error
     * @return true if error rate is within limits; false if rate limit is exceeded (spike detected)
     */
    public boolean isWithinRateLimit(String serviceName) {
        String key = "error_count:" + serviceName;

        Long currentCount = redisTemplate.opsForValue().increment(key);

        // If key was just created, set expiration window
        if (currentCount != null && currentCount == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
        }

        if (currentCount != null && currentCount > MAX_ERRORS_PER_MINUTE) {
            logger.warn("Error spike detected for service [{}]! Count: {} exceeds limit: {}. Suppressing duplicate AI calls.",
                    serviceName, currentCount, MAX_ERRORS_PER_MINUTE);
            return false;
        }

        return true;
    }
}
