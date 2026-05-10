package utils;
import models.*;


import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import javax.crypto.Cipher;

public class SecurityUtils {

    // These keys are generated once when the program starts
    private static KeyPair rsaKeyPair;

    static {
        // We learned that RSA needs a KeyPairGenerator to create keys
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            rsaKeyPair = keyGen.generateKeyPair();
        } catch (Exception e) {
            System.err.println("Could not initialize RSA: " + e.getMessage());
        }
    }

    // Method to hash password using SHA-256 (standard for banking)
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Convert byte array to a readable hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return password; // Fallback to plain if something is wrong (not secure but for school)
        }
    }

    // Method to encrypt data using RSA Public Key
    public static String encryptRSA(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, rsaKeyPair.getPublic());
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // Method to decrypt data using RSA Private Key
    public static String decryptRSA(String encryptedBase64) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
        byte[] decoded = Base64.getDecoder().decode(encryptedBase64);
        return new String(cipher.doFinal(decoded));
    }

    // This masks account numbers like ****1234
    public static String maskAccountNumber(String accNum) {
        if (accNum != null && accNum.length() >= 4) {
            return "****" + accNum.substring(accNum.length() - 4);
        }
        return "****";
    }

    // Validation methods using Regex
    public static boolean validateEmail(String email) {
        if (email == null) return false;
        return email.contains("@") && email.contains(".");
    }

    public static boolean validatePhone(String phone) {
        return phone != null && phone.matches("^\\d{10,15}$");
    }

    public static boolean validateNationalId(String id) {
        return id != null && id.length() >= 5;
    }
}
