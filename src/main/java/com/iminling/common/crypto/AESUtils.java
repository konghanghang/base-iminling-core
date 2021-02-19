package com.iminling.common.crypto;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidParameterSpecException;

/**
 * AES/CBC/PKCS7Padding工具
 */
@Slf4j
public class AESUtils {

    // 导入支持AES/CBC/PKCS7Padding的Provider
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 默认ECB key
     */
    public static final String ECB_KEY = "Hi9MuIAhR49SiWCQViUjHg==";

    // 加密模式
    public static final String CBC_INSTANCE = "AES/CBC/PKCS7Padding";
    public static final String ECB_INSTANCE = "AES/ECB/PKCS5Padding";
    private static final String AES_NAME = "AES";

    /**
     * 加密 ECB模式 使用默认key
     * @param content
     * @return
     */
    public static String encryptECB(String content) {
        return encryptECB(content, ECB_KEY);
    }

    /**
     * 加密 ECB模式
     * @param content 需要加密的内容
     * @param key  加密key
     * @return
     */
    public static String encryptECB(String content, String key) {
        try {
            Cipher cipher = Cipher.getInstance(ECB_INSTANCE);
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.decodeBase64(key), AES_NAME);
            byte[] byteContent = content.getBytes();
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);// 初始化
            byte[] result = cipher.doFinal(byteContent);
            return Base64.encodeBase64String(result);
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException", e);
        } catch (InvalidKeyException e) {
            log.error("InvalidKeyException", e);
        } catch (NoSuchPaddingException e) {
            log.error("NoSuchPaddingException", e);
        } catch (BadPaddingException e) {
            log.error("BadPaddingException", e);
        } catch (IllegalBlockSizeException e) {
            log.error("IllegalBlockSizeException", e);
        }
        return null;
    }

    /**
     * 解密 ECB模式 使用默认key
     * @param content
     * @return
     */
    public static String decryptECB(String content) {
        return decryptECB(content, ECB_KEY);
    }

    /**
     * 解密 ECB模式
     * @param content 需要解密的内容
     * @param key  解密key
     * @return
     */
    public static String decryptECB(String content, String key) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.decodeBase64(key), AES_NAME);
            Cipher cipher = Cipher.getInstance(ECB_INSTANCE);
            byte[] byteContent = Base64.decodeBase64(content);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);// 初始化
            byte[] result = cipher.doFinal(byteContent);
            return new String(result);
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException", e);
        } catch (InvalidKeyException e) {
            log.error("InvalidKeyException", e);
        } catch (NoSuchPaddingException e) {
            log.error("NoSuchPaddingException", e);
        } catch (BadPaddingException e) {
            log.error("BadPaddingException", e);
        } catch (IllegalBlockSizeException e) {
            log.error("IllegalBlockSizeException", e);
        }
        return null;
    }

    /**
     * 生成AES ECB模式 加密key
     * @return
     * @throws Exception
     */
    public static String generateECBKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_NAME);
        // 128、192、256
        keyGenerator.init(128);
        Key key = keyGenerator.generateKey();
        byte[] keyBytes = key.getEncoded();
        return Base64.encodeBase64String(keyBytes);
    }

    /**
     * 加密 CBC模式
     * @param data
     * @param sessionKey
     * @param iv
     * @return
     */
    public static String encryptCBC(String data, String sessionKey, String iv){
        byte[] dataByte = Base64.decodeBase64(data);
        byte[] keyByte = Base64.decodeBase64(sessionKey);
        byte[] ivByte = Base64.decodeBase64(iv);

        String encryptedData = null;
        try {
            //指定算法，模式，填充方式，创建一个Cipher
            Cipher cipher = Cipher.getInstance(CBC_INSTANCE);

            //生成Key对象
            Key sKeySpec = new SecretKeySpec(keyByte, AES_NAME);

            //把向量初始化到算法参数
            AlgorithmParameters params = AlgorithmParameters.getInstance(AES_NAME);
            params.init(new IvParameterSpec(ivByte));

            //指定用途，密钥，参数 初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, params);

            //指定加密
            byte[] result = cipher.doFinal(dataByte);

            //对结果进行Base64编码，否则会得到一串乱码，不便于后续操作
            encryptedData = org.apache.commons.codec.binary.Base64.encodeBase64String(result);
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException", e);
        } catch (NoSuchPaddingException e) {
            log.error("NoSuchPaddingException", e);
        } catch (InvalidKeyException e) {
            log.error("InvalidKeyException", e);
        } catch (InvalidAlgorithmParameterException e) {
            log.error("InvalidAlgorithmParameterException", e);
        } catch (IllegalBlockSizeException e) {
            log.error("IllegalBlockSizeException", e);
        } catch (BadPaddingException e) {
            log.error("BadPaddingException", e);
        } catch (InvalidParameterSpecException e) {
            log.error("InvalidParameterSpecException", e);
        }
        return encryptedData;
    }

    /**
     * 解密 CBC模式
     * @param encryptedData
     * @param sessionKey
     * @param iv
     * @return
     */
    public static String decryptCBC(String encryptedData,String sessionKey,String iv){
        //解密之前先把Base64格式的数据转成原始格式
        byte[] dataByte = Base64.decodeBase64(encryptedData);
        byte[] keyByte = Base64.decodeBase64(sessionKey);
        byte[] ivByte = Base64.decodeBase64(iv);

        String data = null;
        try {
            //指定算法，模式，填充方法 创建一个Cipher实例
            Cipher cipher = Cipher.getInstance(CBC_INSTANCE);
            //生成Key对象
            Key sKeySpec = new SecretKeySpec(keyByte, AES_NAME);

            //把向量初始化到算法参数
            AlgorithmParameters params = AlgorithmParameters.getInstance(AES_NAME);
            params.init(new IvParameterSpec(ivByte));

            //指定用途，密钥，参数 初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, params);

            //执行解密
            byte[] result = cipher.doFinal(dataByte);

            //解密后转成字符串
            data = new String(result);
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException", e);
        } catch (NoSuchPaddingException e) {
            log.error("NoSuchPaddingException", e);
        } catch (InvalidKeyException e) {
            log.error("InvalidKeyException", e);
        } catch (InvalidAlgorithmParameterException e) {
            log.error("InvalidAlgorithmParameterException", e);
        } catch (IllegalBlockSizeException e) {
            log.error("IllegalBlockSizeException", e);
        } catch (BadPaddingException e) {
            log.error("BadPaddingException", e);
        } catch (InvalidParameterSpecException e) {
            log.error("InvalidParameterSpecException", e);
        }
        return data;
    }

}
