package example.consumer;

import static org.junit.jupiter.api.Assertions.*;

import com.iminling.common.json.JsonUtil;
import com.iminling.core.annotation.EnableResolve;
import com.iminling.core.constant.ResolveStrategy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.*;

/** Consumes the installed JAR, without the library's optional Feign or MyBatis dependencies. */
@SpringBootTest(
        classes = ConsumerSmokeTest.Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConsumerSmokeTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(Controller.class)
    static class Application {}

    record Input(String name) {}

    record Output(String name, long id) {}

    @RestController
    static class Controller {
        @PostMapping("/smoke")
        @EnableResolve(ResolveStrategy.ALL)
        public Output smoke(Input input) {
            return new Output(input.name(), 9007199254740993L);
        }
    }

    @LocalServerPort int port;

    @Test
    void startsAndServesRequestsWithoutRemovedIntegrations() throws Exception {
        for (String optional :
                new String[] {
                    "org.apache.ibatis.session.SqlSessionFactory",
                    "jakarta.persistence.EntityManager",
                    "com.querydsl.jpa.impl.JPAQueryFactory",
                    "org.springdoc.core.configuration.SpringDocConfiguration",
                    "io.swagger.v3.oas.models.OpenAPI",
                    "feign.Feign"
                })
            assertFalse(ClassUtils.isPresent(optional, getClass().getClassLoader()), optional);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request =
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/smoke"))
                        .header("Content-Type", "application/json")
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        "{\"input\":{\"name\":\"consumer\"}}"))
                        .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());
        var body = JsonUtil.getInstant().readTree(response.body());
        assertEquals(200, body.path("code").asInt());
        assertEquals("consumer", body.path("data").path("name").asString());
        assertEquals("9007199254740993", body.path("data").path("id").asString());
    }
}
