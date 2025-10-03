package com.example.personalmemory.service;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";

    // Generates a new AES key and returns it as a Base64 encoded string
    public String generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(256); // 256-bit AES
        SecretKey secretKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    // Encrypts a byte array using a Base64 encoded key
    public byte[] encrypt(byte[] data, String base64Key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(base64Key), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    // Decrypts a byte array using a Base64 encoded key
    public byte[] decrypt(byte[] encryptedData, String base64Key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(base64Key), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedData);
    }
}