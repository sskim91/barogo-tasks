package com.barogo.app.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author sskim
 */
@Slf4j
public class BCryptPasswordEncoderTest {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode("TestPassword123!");
        log.info("encodedPassword = {}", encodedPassword);
    }
}
