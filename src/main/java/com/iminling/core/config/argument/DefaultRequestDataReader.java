package com.iminling.core.config.argument;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.HandlerMethod;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Configuration
public class DefaultRequestDataReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestDataReader.class);
    protected static final Set<HttpMethod> SUPPORTED_METHODS = EnumSet.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH);

    private final ObjectMapper objectMapper;

    public DefaultRequestDataReader() {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        this.objectMapper = mappingJackson2HttpMessageConverter.getObjectMapper();
        this.objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    public boolean canRead(@NotNull HttpInputMessage message) {
        MediaType mediaType = message.getHeaders().getContentType();
        if (!canRead(mediaType)) {
            return false;
        }
        HttpMethod httpMethod = (message instanceof HttpRequest ? ((HttpRequest) message).getMethod() : null);
        return canRead(httpMethod);
    }

    protected boolean canRead(@NotNull MediaType mediaType) {
        if (mediaType == null){
            return true;
        }
        for (MediaType supportedMediaType : getSupportedMediaTypes()) {
            if (supportedMediaType.includes(mediaType)) {
                return true;
            }
        }
        LOGGER.info("Not support mediaType: {}", mediaType);
        return false;
    }

    protected boolean canRead(@NotNull HttpMethod httpMethod) {
        if (httpMethod == null){
            return true;
        }
        if (SUPPORTED_METHODS.contains(httpMethod)) {
            return true;
        }
        LOGGER.debug("Not support request method: {}", httpMethod);
        return false;
    }

    public JsonNode read(HttpInputMessage message, HandlerMethod handlerMethod) throws IOException {
        InputStream inputStream = message.getBody();
        InputStream body;
        if (inputStream.markSupported()){
            inputStream.mark(1);
            body = (inputStream.read() != -1) ? inputStream : null;
        } else {
            PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream);
            int read = pushbackInputStream.read();
            if (read == -1){
                body = null;
            } else {
                body = pushbackInputStream;
                pushbackInputStream.unread(read);
            }
        }
        if (body != null) {
            return objectMapper.readTree(body);
        }
        return null;
    }

    public List<MediaType> getSupportedMediaTypes() {
        return Arrays.asList(MediaType.APPLICATION_JSON_UTF8, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN);
    }
}
