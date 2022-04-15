package com.iminling.common.crypto;


import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jcajce.provider.asymmetric.RSA;
import org.junit.jupiter.api.Assertions;
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
    void signTest() {
        System.out.println(RSAUtils.sign("aaaa", RSAUtils.DEFAULT_PRIVATE_KEY));
    }

    @Test
    void checkTest() {
        String data = "aaaa";
        String sign = "ZevWSLCVLrTAn9znptUbXSckbDJ2lQ8W0npJ0zb5otZ7/n1/wVokLEfaedpL2m/wVxn50FFzsY6qmuGgQrrIM/u8FuuA/0DH7xnGlPsQuI6BPhZM+wxNpvqYLC+6KI/+kSVSZ7QehQqE+R8rowRLrqjv1QY3RX6F8BGF6tu4gxg=";
        boolean check = RSAUtils.check(data, sign, RSAUtils.DEFAULT_PUBLIC_KEY);
        Assertions.assertEquals(true, check);
        data = "aaaaa";
        check = RSAUtils.check(data, sign, RSAUtils.DEFAULT_PUBLIC_KEY);
        Assertions.assertEquals(false, check);
    }

    @Test
    void en() {
        String qazwsx = RSAUtils.encrypt("qazwsx");
        System.out.println(qazwsx);
        System.out.println(RSAUtils.decrypt(qazwsx));
    }

}