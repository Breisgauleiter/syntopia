package com.syntopia.config;

import com.syntopia.security.AuthenticationUserDetailsService;
import com.syntopia.service.UserService;
import com.syntopia.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import java.io.IOException;

/**
 * JWT Request Filter for Syntopia Platform
 * 
 * Validates JWT tokens on each request to protected endpoints.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private AuthenticationUserDetailsService userDetailsService;

    @Autowired
    private UserService userService; // for domain attributes only

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain chain) throws ServletException, IOException {

        // Skip JWT processing for public endpoints
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/api/auth/login") || 
            requestPath.startsWith("/api/auth/register") ||
            requestPath.startsWith("/api/health")) {
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                logger.error("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                logger.error("JWT Token has expired");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Authorization header missing or not Bearer; path=" + requestPath);
            }
        }

        // Once we get the token validate it.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // if token is valid configure Spring Security to manually set authentication
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // After setting the Authentication in the context, we specify
                // that the current user is authenticated. So it passes the
                // Spring Security Configurations successfully.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

                // Also expose useful user attributes for controllers expecting them
                try {
                    request.setAttribute("username", username);
                    // Lookup user to provide email and id if available
                    java.util.Optional<User> userOpt = userService.findByUsername(username);
                    if (userOpt.isPresent()) {
                        User u = userOpt.get();
                        if (u.getEmail() != null) {
                            request.setAttribute("email", u.getEmail());
                        }
                        if (u.getId() != null) {
                            request.setAttribute("userId", u.getId());
                        }
                    }
                } catch (Exception ignored) {
                    // Do not block the request if attribute population fails
                }
            }
        }
        chain.doFilter(request, response);
    }
}
