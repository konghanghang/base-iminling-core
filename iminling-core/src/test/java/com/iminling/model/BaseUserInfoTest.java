package com.iminling.model;

import com.iminling.model.core.BaseUserInfo;
import org.junit.jupiter.api.Test;

/**
 * @author yslao@outlook.com
 * @since 2021/6/12
 */
class BaseUserInfoTest {

    @Test
    void testParam() {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(100L);
        param(userInfo);
    }

    void param(BaseUserInfo baseUserInfo) {
        System.out.println(baseUserInfo.getId());
    }

}