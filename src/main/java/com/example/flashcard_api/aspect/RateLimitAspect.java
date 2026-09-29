package com.example.flashcard_api.aspect;

import com.example.flashcard_api.exception.RateLimitException;
import com.example.flashcard_api.service.RedisRateLimitService;
import com.example.flashcard_api.utils.annotations.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@Aspect
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisRateLimitService redisRateLimitService;
    private final HttpServletRequest httpServletRequest;

    @Around("@annotation(com.example.flashcard_api.utils.annotations.RateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        String redisKey = "ratelimit:" + resolveIdentifier() + ":" + method.getName();

        long retryAfter = redisRateLimitService.retryAfterSeconds(redisKey, rateLimit.limit(), rateLimit.timeWindowSeconds());

        if (retryAfter > 0) {
            throw new RateLimitException(retryAfter);
        }

        return joinPoint.proceed();
    }

    private String resolveIdentifier() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "user:" + auth.getName();
        }

        return "ip:" + httpServletRequest.getRemoteAddr();
    }
}
