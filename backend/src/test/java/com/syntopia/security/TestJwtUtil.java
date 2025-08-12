package com.syntopia.security;

import com.syntopia.config.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Utility bean to generate JWTs for tests without loading full auth stack. */
@Component
public class TestJwtUtil {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    public String userToken(String username) {
        return jwtTokenUtil.generateToken(username);
    }
}
