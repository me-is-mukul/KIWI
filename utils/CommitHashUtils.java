package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CommitHashUtils {
    public static String generateCommitHash(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        byte[] hashBytes = digest.digest(data.getBytes());

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
