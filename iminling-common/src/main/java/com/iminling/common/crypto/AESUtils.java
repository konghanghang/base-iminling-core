package com.iminling.common.crypto;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class AESUtils {
    public static final String ECB_KEY = "Hi9MuIAhR49SiWCQViUjHg==";
    public static final String CBC_PKCS7 = "AES/CBC/PKCS7Padding";
    public static final String CBC_NO_PADDING = "AES/CBC/NoPadding";
    public static final String ECB_PKCS5 = "AES/ECB/PKCS5Padding";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private AESUtils() {}

    public static String generateECBKey() throws GeneralSecurityException {
        return generateECBKey(128);
    }

    public static String generateECBKey(int length) throws GeneralSecurityException {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(length);
        return Base64.encodeBase64String(generator.generateKey().getEncoded());
    }

    public static String encryptECB(String data) throws GeneralSecurityException {
        return encryptECB(data, ECB_KEY);
    }

    public static String encryptECB(String data, String key) throws GeneralSecurityException {
        return encryptECB(data, key, ECB_PKCS5);
    }

    public static String encryptECB(String data, String key, String type)
            throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(type);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(Base64.decodeBase64(key), "AES"));
        return Base64.encodeBase64String(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decryptECB(String data) throws GeneralSecurityException {
        return decryptECB(data, ECB_KEY);
    }

    public static String decryptECB(String data, String key) throws GeneralSecurityException {
        return decryptECB(data, key, ECB_PKCS5);
    }

    public static String decryptECB(String data, String key, String type)
            throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(type);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(Base64.decodeBase64(key), "AES"));
        return new String(cipher.doFinal(Base64.decodeBase64(data)), StandardCharsets.UTF_8);
    }

    public static String encryptCBC(String data, String key, String iv)
            throws GeneralSecurityException {
        return encryptCBC(data, key, iv, CBC_PKCS7);
    }

    public static String encryptCBC(String data, String key, String iv, String type)
            throws GeneralSecurityException {
        return encryptCBC(
                Base64.decodeBase64(data), Base64.decodeBase64(key), Base64.decodeBase64(iv), type);
    }

    public static String encryptCBC(byte[] data, byte[] key, byte[] iv)
            throws GeneralSecurityException {
        return encryptCBC(data, key, iv, CBC_PKCS7);
    }

    public static String encryptCBC(byte[] data, byte[] key, byte[] iv, String type)
            throws GeneralSecurityException {
        return Base64.encodeBase64String(crypt(Cipher.ENCRYPT_MODE, data, key, iv, type));
    }

    public static String decryptCBC(String data, String key, String iv)
            throws GeneralSecurityException {
        return decryptCBC(data, key, iv, CBC_PKCS7);
    }

    public static String decryptCBC(String data, String key, String iv, String type)
            throws GeneralSecurityException {
        return decryptCBC(
                Base64.decodeBase64(data), Base64.decodeBase64(key), Base64.decodeBase64(iv), type);
    }

    public static String decryptCBC(byte[] data, byte[] key, byte[] iv)
            throws GeneralSecurityException {
        return decryptCBC(data, key, iv, CBC_PKCS7);
    }

    public static String decryptCBC(byte[] data, byte[] key, byte[] iv, String type)
            throws GeneralSecurityException {
        return new String(crypt(Cipher.DECRYPT_MODE, data, key, iv, type), StandardCharsets.UTF_8);
    }

    private static byte[] crypt(int mode, byte[] data, byte[] key, byte[] iv, String type)
            throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(type);
        AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
        parameters.init(new IvParameterSpec(iv));
        cipher.init(mode, new SecretKeySpec(key, "AES"), parameters);
        return cipher.doFinal(data);
    }
}
