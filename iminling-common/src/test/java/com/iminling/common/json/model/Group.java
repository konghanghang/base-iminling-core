package com.iminling.common.json.model;

import lombok.Data;
import lombok.experimental.Accessors;

import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@Data
@Accessors(chain = true)
@JacksonXmlRootElement(localName = "Class")
public class Group {

    Teacher teacher; // 教师

    @JacksonXmlElementWrapper(localName = "Students")
    @JacksonXmlProperty(localName = "Stu")
    List<Student> student; // 学生列表
}
