package com.iminling.common.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;

/**
 * @author yslao@outlook.com
 * @since 2020/10/28
 */
public class MD5Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MD5Utils.class);

    private MD5Utils(){}

    /***
     * MD5加码 生成32位md5码
     * @param inStr 待散列字符串
     * @return  散列值
     */
    public static String string2MD5(String inStr) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            LOGGER.info(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();

    }

    /**
     * 加密解密算法 执行一次加密，两次解密
     * @param inStr 待散列字符串
     * @return 散列值
     */
    public static String convertMD5(String inStr) {

        char[] a = inStr.toCharArray();
        for (int i = 0; i < a.length; i++) {
            a[i] = (char) (a[i] ^ 't');
        }
        String s = new String(a);
        return s;

    }

    /**
     * 判断输入的密码和数据库中保存的MD5密码是否一致
     * @param inputPassword 输入的密码
     * @param md5DB 数据库保存的密码
     * @return 是否匹配
     */
    public static boolean passwordIsTrue(String inputPassword,String md5DB) {
        String md5 = string2MD5(inputPassword);
        return md5DB.equals(md5);
    }


    // 测试主函数
    public static void main(String args[]) {
        String s = "123456";
        LOGGER.info("原始：" + s);
        String toMd5 = string2MD5(s);
        LOGGER.info("MD5后：" + toMd5);
        LOGGER.info("base64:{}", Base64Utils.encode(toMd5));
        LOGGER.info("密码是否一致：{}", passwordIsTrue(s,toMd5));

    }


}
