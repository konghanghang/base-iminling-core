package com.iminling.common.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.iminling.common.json.model.Group;
import com.iminling.common.json.model.Student;
import com.iminling.common.json.model.Teacher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class JsonUtilTest {

    private XmlMapper xmlMapper;

    @BeforeEach
    public void createXmlMapper() {
        xmlMapper = JsonUtil.createXmlMapper();
    }

    @Test
    void xml2Object() throws JsonProcessingException {
        Student student = new Student();
        student.setAge("20").setGender("男").setName("张>三").setId("100");
        String xml = xmlMapper.writeValueAsString(student);
        System.out.println(xml);

        Student newStu = xmlMapper.readValue(xml, Student.class);
        System.out.println("====");
    }

    @Test
    void  all() throws JsonProcessingException {
        Group group = new Group();
        group.setTeacher(new Teacher().setAge("55").setName("李四").setGender("男"));
        List<Student> list = new ArrayList<>();
        list.add(new Student().setAge("20").setGender("男").setName("张>三").setId("100"));
        list.add(new Student().setAge("21").setGender("男").setName("张>4").setId("101"));
        list.add(new Student().setAge("22").setGender("女").setName("张>5").setId("102"));
        group.setStudent(list);
        String xml = xmlMapper.writeValueAsString(group);
        System.out.println(xml);
    }

    @Test
    void testMap() throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        map.put("KONG", "hh<hh");
        map.put("HANG", "hhhh222");
        String xml = xmlMapper.writeValueAsString(map);
        System.out.println(xml);
        xml = "<HashMap><HANG>hhhh222</HANG><KONG>hh<![CDATA[>]]>hh</KONG></HashMap>";
        Map newMap = xmlMapper.readValue(xml, Map.class);
        System.out.println("==========");
    }

    @Test
    void testString() {
        String wo = JsonUtil.obj2Str("wo");
        System.out.println(wo);
        Map<String, Object> map = new HashMap<>();
        map.put("KONG", "hh<hh");
        map.put("HANG", "hhhh222");
        System.out.println(JsonUtil.obj2Str(map));
    }
}