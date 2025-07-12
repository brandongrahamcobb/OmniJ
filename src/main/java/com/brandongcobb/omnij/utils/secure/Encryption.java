//
//  Encryption.java
//  
//
//  Created by Brandon Cobb on 6/25/25.
//
package com.brandongcobb.omnij.utils.secure;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

public class Encryption {

    private static final String ALGO = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 128;
    private static final int IV_SIZE = 16;
    private static final int ITERATIONS = 65536;
    private static final int SALT_SIZE = 16;

    public static void encryptToFile(byte[] data, File outFile, char[] password) throws Exception {
        byte[] salt = SecureRandom.getInstanceStrong().generateSeed(SALT_SIZE);
        SecretKeySpec key = deriveKey(password, salt);
        byte[] iv = SecureRandom.getInstanceStrong().generateSeed(IV_SIZE);
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(data);
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(salt);
            fos.write(iv);
            fos.write(encrypted);
        }
    }

    public static byte[] decryptFromFile(File inFile, char[] password) throws Exception {
        byte[] allBytes = Files.readAllBytes(inFile.toPath());
        byte[] salt = new byte[SALT_SIZE];
        byte[] iv = new byte[IV_SIZE];
        byte[] encrypted = new byte[allBytes.length - SALT_SIZE - IV_SIZE];
        System.arraycopy(allBytes, 0, salt, 0, SALT_SIZE);
        System.arraycopy(allBytes, SALT_SIZE, iv, 0, IV_SIZE);
        System.arraycopy(allBytes, SALT_SIZE + IV_SIZE, encrypted, 0, encrypted.length);
        SecretKeySpec key = deriveKey(password, salt);
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(encrypted);
    }

    private static SecretKeySpec deriveKey(char[] password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_SIZE);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }
}

