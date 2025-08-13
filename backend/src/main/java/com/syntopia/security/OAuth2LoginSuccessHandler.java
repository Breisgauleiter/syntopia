package com.syntopia.security;

import com.syntopia.config.JwtTokenUtil;
import com.syntopia.model.User;
import com.syntopia.repository.UserRepository;
import com.syntopia.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    public OAuth2LoginSuccessHandler(UserRepository userRepository, UserService userService, JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof OAuth2User oAuth2User)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unsupported principal type");
            return;
        }

        Map<String, Object> attrs = oAuth2User.getAttributes();
        String githubId = String.valueOf(attrs.get("id"));
        String username = (String) attrs.getOrDefault("login", "user" + githubId);
        String email = (String) attrs.getOrDefault("email", username + "@users.noreply.github.com");
        String displayName = (String) attrs.getOrDefault("name", username);
        String avatarUrl = (String) attrs.getOrDefault("avatar_url", null);

        // Find existing by githubId or username
        Optional<User> existingByGithub = userRepository.findByGithubId(githubId);
        User user;
        if (existingByGithub.isPresent()) {
            user = existingByGithub.get();
        } else {
            // Fallback: try username/email
            user = userRepository.findByUsername(username).orElseGet(() -> userService.createUser(username, email, displayName));
            user.setGithubId(githubId);
        }
        user.setGitHubIntegrated(true);
        if (avatarUrl != null && (user.getAvatarUrl() == null || user.getAvatarUrl().startsWith("http"))) {
            user.setAvatarUrl(avatarUrl);
        }
        if (displayName != null && (user.getDisplayName() == null || user.getDisplayName().isBlank())) {
            user.setDisplayName(displayName);
        }
        userRepository.save(user);

        String accessToken = jwtTokenUtil.generateToken(user.getUsername());
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getUsername());

        String redirectUrl = frontendBaseUrl + "/oauth/callback?token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8) +
                "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8) +
                "&githubIntegrated=true";
        response.sendRedirect(redirectUrl);
    }
}
