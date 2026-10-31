package com.iminling.core.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.iminling.common.json.JsonUtil;
import com.iminling.core.annotation.*;
import com.iminling.core.config.filter.LoginFilter;
import com.iminling.core.config.value.ResultModel;
import com.iminling.core.constant.ResolveStrategy;
import com.iminling.core.exception.AuthorizeException;
import com.iminling.core.util.ThreadContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Map;

class MigrationWebTest {
    private final WebApplicationContextRunner runner =
            new WebApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(
                                    CoreBeanAutoConfiguration.class,
                                    CustomizeMvcAutoConfiguration.class))
                    .withUserConfiguration(WebConfig.class);

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @EnableAspectJAutoProxy
    static class WebConfig {
        @Bean
        TestController testController() {
            return new TestController();
        }

        @Bean
        LoginFilter loginFilter() {
            return new LoginFilter() {
                @Override
                public int getOrder() {
                    return 0;
                }

                @Override
                public void doFilter(HandlerMethod handler, HttpServletRequest request) {
                    if (request.getRequestURI().equals("/denied")) throw new AuthorizeException();
                }
            };
        }
    }

    record Input(@NotBlank String name) {}

    @RestController
    static class TestController {
        @PostMapping("/nested/{id}")
        @EnableResolve(ResolveStrategy.ALL)
        public Map<String, Object> nested(
                Input input,
                @PathVariable String id,
                @RequestHeader("X-Test") String header,
                long count) {
            return Map.of("name", input.name(), "id", id, "header", header, "count", count);
        }

        @GetMapping("/text")
        @EnableResolve
        public String text() {
            return "你好";
        }

        @GetMapping("/already")
        @EnableResolve
        public ResultModel<?> already() {
            return ResultModel.ok();
        }

        @GetMapping("/denied")
        @EnableResolve
        public String denied() {
            return "unexpected";
        }

        @PostMapping("/validate")
        @EnableResolve(ResolveStrategy.ALL)
        @Validate
        public Input validate(Input input) {
            return input;
        }
    }

    @Test
    void bindsNestedJsonAndPreservesPathAndHeaderParameters() {
        runner.run(
                context -> {
                    assertNull(context.getStartupFailure());
                    MockMvc mvc =
                            MockMvcBuilders.webAppContextSetup(context)
                                    .addFilters(context.getBean(CustomizeGlobalFilter.class))
                                    .build();
                    var result =
                            mvc.perform(
                                            post("/nested/42")
                                                    .header("X-Test", "header")
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .content(
                                                            "{\"input\":{\"name\":\"test\"},\"count\":7}"))
                                    .andExpect(status().isOk())
                                    .andReturn();
                    var json =
                            JsonUtil.getInstant()
                                    .readTree(result.getResponse().getContentAsString());
                    assertEquals(200, json.path("code").asInt());
                    assertEquals("test", json.path("data").path("name").asString());
                    assertEquals("42", json.path("data").path("id").asString());
                    assertEquals("header", json.path("data").path("header").asString());
                    assertEquals("7", json.path("data").path("count").asString());
                    assertNull(ThreadContext.getClientInfo());
                    mvc.perform(get("/text"))
                            .andExpect(status().isOk())
                            .andExpect(
                                    content()
                                            .json(
                                                    "{\"code\":200,\"data\":\"你好\",\"message\":\"请求成功\"}"));
                    mvc.perform(get("/already"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.code").value(200))
                            .andExpect(jsonPath("$.data").doesNotExist());
                    mvc.perform(get("/denied"))
                            .andExpect(status().isForbidden())
                            .andExpect(jsonPath("$.code").value(403));
                    assertNull(ThreadContext.getClientInfo());
                });
    }

    @Test
    void retainsCustomValidationAndHandlesEmptyBody() {
        runner.run(
                context -> {
                    MockMvc mvc =
                            MockMvcBuilders.webAppContextSetup(context)
                                    .addFilters(context.getBean(CustomizeGlobalFilter.class))
                                    .build();
                    mvc.perform(
                                    post("/validate")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content("{\"input\":{\"name\":\"\"}}"))
                            .andExpect(status().isInternalServerError())
                            .andExpect(jsonPath("$.code").value(500));
                    var result =
                            mvc.perform(
                                            post("/nested/42")
                                                    .header("X-Test", "header")
                                                    .contentType(MediaType.APPLICATION_JSON))
                                    .andExpect(status().isInternalServerError())
                                    .andReturn();
                    assertFalse(result.getResponse().getContentAsString().isBlank());
                    assertNull(ThreadContext.getClientInfo());
                });
    }
}
