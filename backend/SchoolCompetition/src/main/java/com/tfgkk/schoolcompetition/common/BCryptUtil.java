package com.tfgkk.schoolcompetition.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 极简版密码加密工具类 (为了简化，这里使用 SHA-256 + Base64)
 * 如果项目引入了 Spring Security，建议使用 BCryptPasswordEncoder
 */
public class BCryptUtil {

    public static String encode(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Encryption algorithm not found", e);
        }
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }
}
