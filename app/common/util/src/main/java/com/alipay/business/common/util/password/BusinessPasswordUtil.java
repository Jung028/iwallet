package com.alipay.business.common.util.password;

import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * @author adam
 * @date 10/3/2026 1:43 AM
 */
public class BusinessPasswordUtil {

    /**
     * verify password
     *
     * @param password
     * @param hashedUserPassword
     * @return
     */
    public static boolean verifyPassword(String password, String hashedUserPassword) {
        if (password == null || hashedUserPassword == null) {
            System.out.println(password);
            System.out.println(hashedUserPassword);
            return false;
        }
        try {
            return BCrypt.checkpw(password, hashedUserPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}