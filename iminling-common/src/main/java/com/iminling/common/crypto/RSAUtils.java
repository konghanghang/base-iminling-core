package com.iminling.common.crypto;

import com.iminling.common.exception.EncryptException;
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

    private static final String KEY_ALGORITHM = "RSA";
    private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

    public static final String DEFAULT_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCPUEK0j+ZqgfV8Ld+5g62PoXb/wUNiwoBJ/I1jwdhsNQXTNM4dDox7ShRvENURKJMfM1Y+FiwwfR1RM3yc+oDhjM0jUroQOQKtCA7qbNGqGmOha5CTtgM9fs0rJ/zkntLxjlgrppKMYZIDXqPE0IhM/LvJ6KE1HznC9tIZqIwpKwIDAQAB";
    public static final String DEFAULT_PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAI9QQrSP5mqB9Xwt37mDrY+hdv/BQ2LCgEn8jWPB2Gw1BdM0zh0OjHtKFG8Q1REokx8zVj4WLDB9HVEzfJz6gOGMzSNSuhA5Aq0IDups0aoaY6FrkJO2Az1+zSsn/OSe0vGOWCumkoxhkgNeo8TQiEz8u8nooTUfOcL20hmojCkrAgMBAAECgYEAjqGZkez5rJzk/g8F77Ne5GkVbxsgfsUHOxtweI0vYRowTvDqBb86D2Y+Cf/dnnMcYVwNhEM6/ZM3v14XsoAaDWHGWv8s3vh6LNZCoRaVx8T+HQulLh9Ce9P1kwDBDvRaOu+mEmBr2hfi/fuFBDU8rYUu8xQwaeYrtotpoNk680ECQQDSgcwv6cNz+lmynoXj+kiO1nayh0JtQyfNyQIcs3s43MbcosG/Y7IXYdS0oM4yM9TC49hHo7vI/s462j/Z4g/LAkEArkkDiblz3i67rcQ7CTZ2s3rKXDZTMaFkURc5PaKD78psQ3yAvW7nlprueoZGagQHv1amS1Tre1BlEE4iXfRgIQJAesU0nuUJFwT1d1U45UM9OgEebPSx6yN1fXE0CCUtQnaQjHFn6gVtAnIOKeJ3H5RXC+ryYqW9iXYCNRTAlIRhjQJBAKitOaNdvWpXyISQ2qI79/4U5S0B1tq93J0u4NNqKk81ljpqkR7F65WzNQOYWSXZ6LWiYyeddJrQYD+7nz9XeIECQD3CI0AbTymKcobc/+3nFQPE0Db/C6rl1fE/MgvXXBOtXBFlVrhtYrWMGlGmPLM+S0qGJyqpUHYmE8kVaLhdNII=";

    /**
     * 随机生成密钥对
     *
     * @param keyLength key长度
     * @return key对象
     */
    public static KeyPair genKeyPair(int keyLength) {
        // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        }
        // 初始化密钥对生成器，密钥大小为96-1024位
        keyPairGen.initialize(keyLength);
        // 生成一个密钥对，保存在keyPair中
        return keyPairGen.generateKeyPair();
    }

    /**
     * 签名
     * @param data          待签名数据
     * @param privateKey    私钥
     * @return
     */
    public static String sign(String data, String privateKey) {
        try {
            PrivateKey priKey = getPrivateKey(privateKey);
            Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
            signature.initSign(priKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signed = signature.sign();
            return Base64.encodeBase64String(signed);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidKeySpecException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 校验签名
     *
     * @param data   内容
     * @param sign      签名
     * @param publicKey 公钥
     */
    public static boolean check(String data, String sign, String publicKey) {
        try {
            PublicKey pubKey = getPublicKey(publicKey);
            Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
            signature.initVerify(pubKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.decodeBase64(sign));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidKeyException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * RSA公钥加密
     *
     * @param str 待加密字符串
     * @return 加密后字符串
     */
    public static String encrypt(String str) {
        return encrypt(str, DEFAULT_PUBLIC_KEY);
    }


    /**
     * RSA公钥加密
     *
     * @param str       加密字符串
     * @param publicKey 公钥
     * @return 密文
     */
    public static String encrypt(String str, String publicKey) {
        try {
            PublicKey pubKey = getPublicKey(publicKey);
            //RSA加密
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] result = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(result);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException |
                 InvalidKeyException | InvalidKeySpecException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * RSA私钥解密
     *
     * @param str 待解密字符串
     * @return 明文
     */
    public static String decrypt(String str) {
        return decrypt(str, DEFAULT_PRIVATE_KEY);
    }

    /**
     * RSA私钥解密
     *
     * @param str        加密字符串
     * @param privateKey 私钥
     * @return 铭文
     */
    public static String decrypt(String str, String privateKey) {
        try {
            PrivateKey priKey = getPrivateKey(privateKey);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            byte[] bytes = Base64.decodeBase64(str);
            return new String(cipher.doFinal(bytes));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException |
                 InvalidKeyException | InvalidKeySpecException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 返回公钥对象
     *
     * @param publicKey publickey
     * @return pulickey对象
     */
    private static PublicKey getPublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytes = Base64.decodeBase64(publicKey.getBytes(StandardCharsets.UTF_8));
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw e;
        }
    }

    /**
     * 返回私钥对象
     *
     * @param privateKey 私钥
     * @return 私钥对象
     */
    private static PrivateKey getPrivateKey(String privateKey)
        throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytes = Base64.decodeBase64(privateKey.getBytes(StandardCharsets.UTF_8));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw e;
        }
    }
}
