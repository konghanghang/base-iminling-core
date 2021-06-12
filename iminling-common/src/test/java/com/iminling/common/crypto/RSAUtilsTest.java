package com.iminling.common.crypto;


import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

/**
 * @author yslao@outlook.com
 * @since 2021/2/19
 */
public class RSAUtilsTest {

    @Test
    void genKeyPair() {
        KeyPair keyPair = RSAUtils.genKeyPair(1024);
        String publicKey = new String(Base64.encodeBase64(keyPair.getPublic().getEncoded()));
        String privateKey = new String(Base64.encodeBase64(keyPair.getPrivate().getEncoded()));
        System.out.println(publicKey);
        System.out.println(privateKey);
    }

    @Test
    void en() {
        String qazwsx = RSAUtils.encrypt("qazwsx");
        System.out.println(qazwsx);
        System.out.println(RSAUtils.decrypt(qazwsx));
    }

}