package com.iminling.core.config.mybatis;

import static org.junit.jupiter.api.Assertions.*;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

class MybatisMigrationTest {
    @TempDir Path directory;

    interface Mapper {
        Map<String, Object> select();
    }

    private String xml(int value) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE mapper PUBLIC"
                + " \"-//mybatis.org//DTD Mapper 3.0//EN\""
                + " \"https://mybatis.org/dtd/mybatis-3-mapper.dtd\"><mapper namespace=\""
                + Mapper.class.getName()
                + "\"><select id=\"select\" resultType=\"map\">select "
                + value
                + " as user_id</select></mapper>";
    }

    @Test
    void mapsUnderscoresAndReloadsXmlWithCurrentMybatisPlus() throws Exception {
        var configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setObjectWrapperFactory(new MapWrapperFactory());
        configuration.setEnvironment(
                new Environment(
                        "test",
                        new JdbcTransactionFactory(),
                        new DriverManagerDataSource(
                                "jdbc:h2:mem:reload;DB_CLOSE_DELAY=-1", "sa", "")));
        Path path = directory.resolve("Mapper.xml");
        Files.writeString(path, xml(1));
        Resource resource = new FileSystemResource(path);
        try (var input = resource.getInputStream()) {
            new XMLMapperBuilder(
                            input,
                            configuration,
                            resource.toString(),
                            configuration.getSqlFragments())
                    .parse();
        }
        SqlSessionFactory factory = new MybatisSqlSessionFactoryBuilder().build(configuration);
        try (var session = factory.openSession()) {
            assertEquals(1, session.getMapper(Mapper.class).select().get("userId"));
        }
        try (var refresh = new MybatisMapperRefresh(new Resource[] {resource}, factory, 3600)) {
            Files.writeString(path, xml(2));
            refresh.refresh(resource);
            try (var session = factory.openSession()) {
                assertEquals(2, session.getMapper(Mapper.class).select().get("userId"));
            }
        }
    }
}
