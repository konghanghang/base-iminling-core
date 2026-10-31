package com.iminling.common.crypto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Base64;

/**
 * @author yslao@outlook.com
 * @since 2021/2/19
 */
public class AESUtilsTest {

    @Test
    void generateECBKeyTest() throws Exception {
        String s = AESUtils.generateECBKey();
        Assertions.assertEquals(24, s.length());
        s = AESUtils.generateECBKey(192);
        Assertions.assertEquals(32, s.length());
        s = AESUtils.generateECBKey(256);
        Assertions.assertEquals(44, s.length());
    }

    @Test
    void encryptECB() throws Exception {
        String en = AESUtils.encryptECB("我是aaa");
        System.out.println(en);
        String s = AESUtils.decryptECB(en);
        Assertions.assertEquals("我是aaa", s);
    }

    @Test
    void encryptCBC() throws Exception {
        // 原始数据
        String data = "i am data";
        // 密钥
        String sessionKey = "i am strong key ";
        // 向量
        String iv = "i am iv i am iv ";

        // 用Base64编码
        Base64.Encoder encoder = Base64.getEncoder();
        String baseData = encoder.encodeToString(data.getBytes());
        String baseSessionKey = encoder.encodeToString(sessionKey.getBytes());
        String baseIv = encoder.encodeToString(iv.getBytes());

        // 获取加密数据
        String encryptedData = AESUtils.encryptCBC(baseData, baseSessionKey, baseIv);
        // 通过加密数据获得原始数据
        String dataReborn = AESUtils.decryptCBC(encryptedData, baseSessionKey, baseIv);

        // 打印解密出来的原始数据
        Assertions.assertEquals(data, dataReborn);
    }
}
