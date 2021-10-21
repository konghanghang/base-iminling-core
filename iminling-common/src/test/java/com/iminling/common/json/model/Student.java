package com.iminling.common.json.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JacksonXmlRootElement(localName = "stu_inner")
public class Student {

    @JacksonXmlProperty(isAttribute = true, localName = "stu_id")
    private String id;  //学号---属性

    @JacksonXmlCData
    private String name;

    private String gender;

    private String age;
}
