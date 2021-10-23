package com.iminling.common.crypto;

import cn.hutool.core.util.StrUtil;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;

/**
 * @author yslao@outlook.com
 * @since 2020-10-28
 */
public class Base64Utils {

    /**
     * url安全编码
     * base64传统编码中会出现+, /两个会被url直接转义的符号，因此如果希望通过url传输这些编码字符串，我们
     * 需要先做传统base64编码，随后将+和/分别替换为- _两个字符，在接收端则做相反的动作解码
     * 在base64解码的过程中, 要清除掉结尾处的等号,
     * 然后再反查”base64索引与字母对照表”,转换成原始的字节序列.
     * 那么,去掉尾部的等号,并没有丢失原始信息,但结构变得不规范.
     * @param data 加密字符串
     * @return 加密后的字符串
     */
    public static String safeUrlEncode(String data){
        byte[] result = data.getBytes(StandardCharsets.UTF_8);
        return safeUrlEncode(result);
    }

    /**
     * 安全加密
     * @param data 字节数组
     * @return 密文
     */
    public static String safeUrlEncode(byte[] data){
        String base64 = Base64.encodeBase64String(data);
        String urlSafeStr = base64.replace('+', '-');
        urlSafeStr = urlSafeStr.replace('/', '_');
        //urlSafeStr = urlSafeStr.replaceAll("=", "");
        return urlSafeStr;
    }

    /**
     * 安全解密
     * @param safeBase64Str 密文
     * @return 明文
     */
    public static String safeUrlDecode(final String safeBase64Str) {
        String base64Str = safeBase64Str.replace('-', '+');
        base64Str = base64Str.replace('_', '/');
        /*int mod4 = base64Str.length() % 4;
        if(mod4 > 0){
            base64Str = base64Str + "====".substring(mod4);
        }*/
        byte[] str = Base64.decodeBase64(base64Str);
        return new String(str, StandardCharsets.UTF_8);
    }

    /**
     * base64文件转字节数组
     * @param base64Data base64文件
     * @return 字节数组
     */
    public static byte[] base64FileToByte(String base64Data) {
        if (base64Data != null && (!base64Data.equals("undefined"))) {
            String base64Encode = base64Data.substring(base64Data.indexOf(',') + 1);
            if (base64Encode != null || base64Encode != "") {
                if (Base64.isBase64(base64Encode) == true) {
                    byte[] imageByte = Base64.decodeBase64(base64Encode);
                    return imageByte;
                }
            }
        }
        return null;
    }

    /**
     * base64解码
     * @param s 加密字符串
     * @return 加密后的字符串
     */
    public static String encode(String s) {
        if (StrUtil.isBlank(s)) {
            return null;
        }
        return Base64.encodeBase64String(s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * base64解码
     * @param s 待解密字符串
     * @return 解密后的字符串
     */
    public static String decode(String s) {
        if (StrUtil.isBlank(s)){
            return null;
        }
        return new String(Base64.decodeBase64(s), StandardCharsets.UTF_8);
    }

}
