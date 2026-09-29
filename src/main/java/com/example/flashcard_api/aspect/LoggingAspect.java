package com.example.flashcard_api.aspect;

import com.example.flashcard_api.model.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(public * com.example.flashcard_api.controller..*(..)) && !within(com.example.flashcard_api.controller.AuthController)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        Object[] maskedArgs = Arrays.stream(args)
                .map(this::maskSensitive)
                .toArray();

        log.debug("[{}] - args: {}", methodName, Arrays.toString(maskedArgs));

        return joinPoint.proceed();
    }

    private Object maskSensitive(Object obj) {
        if (obj instanceof UserDto user) {
            return UserDto.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .password("******")
                    .email(user.getEmail() != null ? "******" : null)
                    .build();
        }

        return obj;
    }

}
