package com.iminling.common.crypto;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author yslao@outlook.com
 * @since 2021/3/2
 */
@Slf4j
class MD5UtilsTest {

    @Test
    void encode() {
        String key = "xxx";
        System.out.println(MD5Utils.encode(key.concat("123")));
    }

    @Test
    void validDigest() {
        String key = "xxx";
        System.out.println(MD5Utils.validDigest(key.concat("123"), "85B6B930FED5DAD99A7B5671FDB6B1674C7702EE1A330A00C3D3E5C4"));
    }

    @Test
    void encodeWithoutSalt() {
        String key = "xxx";
        System.out.println(MD5Utils.encodeWithoutSalt(key.concat("123")));
    }
}