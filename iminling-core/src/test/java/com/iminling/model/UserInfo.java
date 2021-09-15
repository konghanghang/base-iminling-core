package com.iminling.model;

import com.iminling.model.core.BaseUserInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yslao@outlook.com
 * @since 2021/6/12
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class UserInfo extends BaseUserInfo {

    private Long id;

}
