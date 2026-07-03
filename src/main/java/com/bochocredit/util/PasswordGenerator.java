package com.bochocredit.util;

import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


@Service
public class PasswordGenerator {
    private static final String ALLOWED = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "abcdefghijklmnopqrstuvwxyz"
            + "0123456789"
            + "#$%&";
    private static final int LENGTH = 12;

    private static final SecureRandom SR = new SecureRandom();

    public String generarPassword(Long seedA) {
        try {
            long seedB = System.currentTimeMillis() % SR.nextLong();
            var temp = seedA;
            seedA *= seedB + SR.nextLong();
            seedB *= temp + SR.nextLong();

            long miliseconds = System.currentTimeMillis();

            temp = SR.nextLong() % SR.nextInt();
            temp = temp == 0 ? 1 : temp;
            seedA += miliseconds / temp;

            temp = SR.nextLong() % SR.nextInt();
            temp = temp == 0 ? 1 : temp;
            seedB += miliseconds / temp;

            // 1) Mix the two longs into a byte array
            ByteBuffer bb = ByteBuffer.allocate(Long.BYTES * 2);
            bb.putLong(seedA);
            bb.putLong(seedB);
            byte[] mixed = bb.array();

            // 2) Hash the mixed bytes to produce a uniform seed
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(mixed);

            // 3) Initialize SecureRandom with the digest as seed
            SecureRandom rnd = new SecureRandom(digest);

            // 4) Build the password
            StringBuilder pw = new StringBuilder(LENGTH);
            for (int i = 0; i < LENGTH; i++) {
                int idx = rnd.nextInt(ALLOWED.length());
                pw.append(ALLOWED.charAt(idx));
            }
            return pw.toString();

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 should always be available; wrap in runtime exception if not
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}