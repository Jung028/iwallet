package com.alipay.business.common.util.requesthash;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * @author adam
 * @date 10/3/2026 7:31 PM
 */
public class HashUtil {
    /**
     * generate idempotency request hash for prevent duplicate request (accidental) first stage,
     * second stage is check idempotency key
     *
     * @param amount
     * @param currency
     * @param payerAccountNo
     * @param payeeAccountNo
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String generateTransferRequestHash(BigDecimal amount, String currency, String payerAccountNo, String payeeAccountNo) throws NoSuchAlgorithmException {
            String input = amount.toString() + "." + currency + "." + payerAccountNo + "." + payeeAccountNo;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedHash); // Returns 64-char hex string
    }

    public static String generateTopUpRequestHash(BigDecimal amount, String currency, String cardType, String uniqueRequestId) throws NoSuchAlgorithmException {
        String input = amount.toString() + "." + currency + "." + cardType + "." + uniqueRequestId;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(encodedHash); // Returns 64-char hex string
    }



    public static String generatePublishTopUpRequestHash(Long amount, String currency, String customer) throws NoSuchAlgorithmException {
        String input = amount.toString() + "." + currency + "." + customer;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(encodedHash); // Returns 64-char hex string
    }
}