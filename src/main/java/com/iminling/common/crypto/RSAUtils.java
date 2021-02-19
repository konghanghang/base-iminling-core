package com.iminling.common.crypto;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author yslao@outlook.com
 * @since 2021/2/1
 */
@Slf4j
public class RSAUtils {

    private static final String DEFAULT_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCPUEK0j+ZqgfV8Ld+5g62PoXb/wUNiwoBJ/I1jwdhsNQXTNM4dDox7ShRvENURKJMfM1Y+FiwwfR1RM3yc+oDhjM0jUroQOQKtCA7qbNGqGmOha5CTtgM9fs0rJ/zkntLxjlgrppKMYZIDXqPE0IhM/LvJ6KE1HznC9tIZqIwpKwIDAQAB";
    private static final String DEFAULT_PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAI9QQrSP5mqB9Xwt37mDrY+hdv/BQ2LCgEn8jWPB2Gw1BdM0zh0OjHtKFG8Q1REokx8zVj4WLDB9HVEzfJz6gOGMzSNSuhA5Aq0IDups0aoaY6FrkJO2Az1+zSsn/OSe0vGOWCumkoxhkgNeo8TQiEz8u8nooTUfOcL20hmojCkrAgMBAAECgYEAjqGZkez5rJzk/g8F77Ne5GkVbxsgfsUHOxtweI0vYRowTvDqBb86D2Y+Cf/dnnMcYVwNhEM6/ZM3v14XsoAaDWHGWv8s3vh6LNZCoRaVx8T+HQulLh9Ce9P1kwDBDvRaOu+mEmBr2hfi/fuFBDU8rYUu8xQwaeYrtotpoNk680ECQQDSgcwv6cNz+lmynoXj+kiO1nayh0JtQyfNyQIcs3s43MbcosG/Y7IXYdS0oM4yM9TC49hHo7vI/s462j/Z4g/LAkEArkkDiblz3i67rcQ7CTZ2s3rKXDZTMaFkURc5PaKD78psQ3yAvW7nlprueoZGagQHv1amS1Tre1BlEE4iXfRgIQJAesU0nuUJFwT1d1U45UM9OgEebPSx6yN1fXE0CCUtQnaQjHFn6gVtAnIOKeJ3H5RXC+ryYqW9iXYCNRTAlIRhjQJBAKitOaNdvWpXyISQ2qI79/4U5S0B1tq93J0u4NNqKk81ljpqkR7F65WzNQOYWSXZ6LWiYyeddJrQYD+7nz9XeIECQD3CI0AbTymKcobc/+3nFQPE0Db/C6rl1fE/MgvXXBOtXBFlVrhtYrWMGlGmPLM+S0qGJyqpUHYmE8kVaLhdNII=";

    /**
     * 随机生成密钥对
     * @param keyLength
     * @return
     */
    public static KeyPair genKeyPair(int keyLength) {
        // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        }
        // 初始化密钥对生成器，密钥大小为96-1024位
        keyPairGen.initialize(keyLength);
        // 生成一个密钥对，保存在keyPair中
        return keyPairGen.generateKeyPair();
    }

    /**
     * RSA公钥加密
     * @param str
     * @return
     */
    public static String encrypt(String str) {
        return encrypt(str, DEFAULT_PUBLIC_KEY);
    }


    /**
     * RSA公钥加密
     * @param str 加密字符串
     * @param publicKey 公钥
     * @return 密文
     */
    public static String encrypt(String str, String publicKey) {
        PublicKey pubKey = getPublicKey(publicKey);
        //RSA加密
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] result = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(result);
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException", e);
        } catch (NoSuchPaddingException e) {
            log.error("NoSuchPaddingException", e);
        } catch (BadPaddingException e) {
            log.error("BadPaddingException", e);
        } catch (IllegalBlockSizeException e) {
            log.error("IllegalBlockSizeException", e);
        } catch (InvalidKeyException e) {
            log.error("InvalidKeyException", e);
        }
        return null;
    }

    /**
     * RSA私钥解密
     * @param str
     * @return
     */
    public static String decrypt(String str){
        return decrypt(str, DEFAULT_PRIVATE_KEY);
    }

    /**
     * RSA私钥解密
     * @param str 加密字符串
     * @param privateKey 私钥
     * @return 铭文
     */
    public static String decrypt(String str, String privateKey){
        PrivateKey priKey = getPrivateKey(privateKey);
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            byte[] bytes = Base64.decodeBase64(str);
            return new String(cipher.doFinal(bytes));
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException", e);
        } catch (NoSuchPaddingException e) {
            log.error("NoSuchPaddingException", e);
        } catch (BadPaddingException e) {
            log.error("BadPaddingException", e);
        } catch (IllegalBlockSizeException e) {
            log.error("IllegalBlockSizeException", e);
        } catch (InvalidKeyException e) {
            log.error("InvalidKeyException", e);
        }
        return null;
    }

    /**
     * 返回公钥对象
     * @param publicKey
     * @return
     */
    private static PublicKey getPublicKey(String publicKey) {
        byte[] bytes = Base64.decodeBase64(publicKey.getBytes(StandardCharsets.UTF_8));
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 返回私钥对象
     * @param privateKey
     * @return
     */
    private static PrivateKey getPrivateKey(String privateKey) {
        byte[] bytes = Base64.decodeBase64(privateKey.getBytes(StandardCharsets.UTF_8));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
