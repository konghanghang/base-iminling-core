package com.iminling.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.iminling.core.config.feign.CustomizeFeignAutoConfiguration;
import com.iminling.core.config.mybatis.CustomizeMybatisConfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

class OptionalIntegrationsTest {
    @Test
    void startsWithoutMybatisOrFeignOnTheClasspath() {
        new WebApplicationContextRunner()
                .withClassLoader(
                        new FilteredClassLoader(
                                "com.baomidou",
                                "org.apache.ibatis",
                                "feign",
                                "org.springframework.cloud"))
                .withConfiguration(
                        AutoConfigurations.of(
                                CoreBeanAutoConfiguration.class,
                                CustomizeMvcAutoConfiguration.class,
                                CustomizeMybatisConfig.class,
                                CustomizeFeignAutoConfiguration.class))
                .run(
                        context -> {
                            assertThat(context)
                                    .hasNotFailed()
                                    .hasSingleBean(GlobalInterceptor.class);
                            assertThat(context)
                                    .doesNotHaveBean("mybatisPlusInterceptor")
                                    .doesNotHaveBean("feignClient");
                        });
    }
}
