package com.example.flashcard_api.security.filter;

import com.example.flashcard_api.exception.FlashCardApiException;
import com.example.flashcard_api.security.jwt.JwtUtils;
import com.example.flashcard_api.security.service.UserDetailsServiceImpl;
import com.example.flashcard_api.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/v1/user/register", "/api/v1/user/login", "/api/v1/user/refresh", "/api/v1/user/logout", "/api/v1/health");

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final RefreshTokenService refreshTokenService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PUBLIC_PATHS.contains(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final Cookie[] cookies = request.getCookies();
        if (cookies != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            for (Cookie cookie : cookies) {
                if (!"accessToken".equals(cookie.getName())) {
                    continue;
                }
                try {
                    final Claims claims = jwtUtils.extractClaims(cookie.getValue());
                    final String username = claims.getSubject();
                    if (refreshTokenService.isActive(claims.get("sid", Long.class), username)) {
                        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        final UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (FlashCardApiException | UsernameNotFoundException | JwtException e) {
                    SecurityContextHolder.clearContext();
                } catch (RuntimeException e) {
                    log.error("Session validation failed", e);
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"error\":\"The service is unavailable. Please try again.\"}");
                    return;
                }
                break;
            }
        }
        filterChain.doFilter(request, response);
    }
}
