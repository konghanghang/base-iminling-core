package com.iminling.common.json.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Teacher {

    private String name;
    private String gender;
    private String age;

}
