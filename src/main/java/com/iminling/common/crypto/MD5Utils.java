package com.iminling.common.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * @author yslao@outlook.com
 * @since 2020/10/28
 */
public class MD5Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MD5Utils.class);

    private MD5Utils(){}

    private static final String HEX_NUMS_STR = "0123456789ABCDEF";

    private static final Integer SALT_LENGTH = Integer.valueOf(12);

    /**
     * 将16进制字符串转为字节数组
     * @param hex 16进制字符串
     * @return 字节数组
     */
    private static byte[] hexStringToByte(String hex) {
        int len = hex.length() / 2;
        byte[] result = new byte[len];
        char[] hexChars = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte)(HEX_NUMS_STR.indexOf(hexChars[pos]) << 4
                            | HEX_NUMS_STR.indexOf(hexChars[pos + 1]));
        }
        return result;
    }

    /**
     * 字节数组转16进制字符串
     * @param b 字节数组
     * @return 16进制字符串
     */
    private static String byteToHexString(byte[] b) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1)
                hex = '0' + hex;
            hexString.append(hex.toUpperCase());
        }
        return hexString.toString();
    }

    /**
     * 验证字符串是否和密文一致
     * @param str 字符串
     * @param digestStr 密文
     * @return {@link Boolean}
     */
    public static boolean validDigest(String str, String digestStr) {
        boolean result = false;
        try {
            // 将16进制字符串转成字节数组
            byte[] pwdInDb = hexStringToByte(digestStr);
            // 声明盐变量
            byte[] salt = new byte[SALT_LENGTH.intValue()];
            // 将盐从数据中提取出来
            System.arraycopy(pwdInDb, 0, salt, 0, SALT_LENGTH.intValue());
            // 创建摘要对象
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 将盐传入摘要对象
            md.update(salt);
            // 将口令数据传入摘要对象
            md.update(str.getBytes(StandardCharsets.UTF_8));
            // 生成输入口令的消息摘要
            byte[] digest = md.digest();
            // 声明一个保存口令消息摘要得变量
            byte[] digestInDb = new byte[pwdInDb.length - SALT_LENGTH.intValue()];
            // 获取口令消息摘要
            System.arraycopy(pwdInDb, SALT_LENGTH.intValue(), digestInDb, 0, digestInDb.length);
            // 比较两个消息摘要
            result = Arrays.equals(digest, digestInDb);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("NoSuchAlgorithmException", e);
        }
        return result;
    }

    /**
     * 带盐加密
     * @param str 待加密字符串
     * @return 加密字符串
     */
    public static String encode(String str) {
        String encodeStr = null;
        try {
            // 随机数生成器
            SecureRandom random = new SecureRandom();
            // 盐数组
            byte[] salt = new byte[SALT_LENGTH.intValue()];
            // 随机数放入盐数组
            random.nextBytes(salt);
            // 创建摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 传入盐对象
            md.update(salt);
            // 传入口令数据
            md.update(str.getBytes(StandardCharsets.UTF_8));
            // 获取消息摘要字节数组
            byte[] digest = md.digest();
            // 加密后的口令数组变量,要在里边存放盐,所以加上盐的字节长度
            byte[] pwd = new byte[digest.length + SALT_LENGTH.intValue()];
            // 将盐的字节拷贝到生成的加密口令字节数组的前12个字节,以便在验证口令的时候取出盐
            System.arraycopy(salt, 0, pwd, 0, SALT_LENGTH.intValue());
            // 将消息摘要拷贝到加密口令字节数组,从第13个字节开始
            System.arraycopy(digest, 0, pwd, SALT_LENGTH.intValue(), digest.length);
            // 将字节数组格式加密后的口令转化为16进制字符串格式的口令
            encodeStr = byteToHexString(pwd);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("NoSuchAlgorithmException", e);
        }
        return encodeStr;
    }

    /**
     * 不带盐加密
     * @param str 待加密字符串
     * @return 加密字符串
     */
    public static String encodeWithoutSalt(String str) {
        String encodeStr = null;
        try {
            // 创建消息摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 将口令的数据传入消息摘要对象
            md.update(str.getBytes(StandardCharsets.UTF_8));
            // 获得消息摘要的字节数组
            byte[] digest = md.digest();
            // 将字节数组转为16进制字符串
            encodeStr = byteToHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("NoSuchAlgorithmException", e);
        }
        return encodeStr;
    }

}
