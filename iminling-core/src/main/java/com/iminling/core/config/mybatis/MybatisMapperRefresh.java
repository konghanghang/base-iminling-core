package com.iminling.core.config.mybatis;

import com.baomidou.mybatisplus.core.MybatisConfiguration;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;

/** Opt-in development-time XML mapper refresh. Closing the bean stops its background task. */
public class MybatisMapperRefresh implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(MybatisMapperRefresh.class);
    private final Configuration configuration;
    private final Map<Resource, Long> timestamps = new LinkedHashMap<>();
    private final ScheduledExecutorService executor;

    public MybatisMapperRefresh(Resource[] resources, SqlSessionFactory factory) {
        this(resources, factory, 20);
    }

    public MybatisMapperRefresh(Resource[] resources, SqlSessionFactory factory, int interval) {
        if (interval < 1)
            throw new IllegalArgumentException("refresh-mapper-interval must be positive");
        configuration = factory.getConfiguration();
        for (Resource resource : resources) {
            try {
                timestamps.put(resource, resource.lastModified());
            } catch (Exception error) {
                LOG.warn("Cannot watch mapper {}", resource, error);
            }
        }
        executor =
                Executors.newSingleThreadScheduledExecutor(
                        task -> {
                            Thread thread = new Thread(task, "mybatis refreshMapper");
                            thread.setDaemon(true);
                            return thread;
                        });
        executor.scheduleWithFixedDelay(
                this::checkForChanges, interval, interval, TimeUnit.SECONDS);
    }

    private void checkForChanges() {
        timestamps.replaceAll(
                (resource, previous) -> {
                    try {
                        long current = resource.lastModified();
                        if (current > previous) {
                            refresh(resource);
                            return current;
                        }
                    } catch (Exception error) {
                        LOG.error("Cannot refresh mapper {}", resource, error);
                    }
                    return previous;
                });
    }

    @SuppressWarnings("unchecked")
    synchronized void refresh(Resource resource) throws Exception {
        try (InputStream source = resource.getInputStream()) {
            var parser =
                    new XPathParser(
                            source,
                            true,
                            configuration.getVariables(),
                            new XMLMapperEntityResolver());
            String namespace = parser.evalNode("/mapper").getStringAttribute("namespace");
            Class<?> mapper = Resources.classForName(namespace);
            if (configuration instanceof MybatisConfiguration plus) plus.removeMapper(mapper);
            else {
                Map<Class<?>, ?> known =
                        (Map<Class<?>, ?>) field(configuration.getMapperRegistry(), "knownMappers");
                known.remove(mapper);
            }
            Set<String> loaded = (Set<String>) field(configuration, "loadedResources");
            loaded.remove(resource.toString());
            loaded.remove("namespace:" + namespace);
            for (String name :
                    List.of(
                            "mappedStatements",
                            "resultMaps",
                            "parameterMaps",
                            "keyGenerators",
                            "sqlFragments",
                            "caches")) {
                Map<String, ?> entries = (Map<String, ?>) field(configuration, name);
                Set<String> shortNames = new HashSet<>();
                entries.keySet().stream()
                        .filter(key -> key.startsWith(namespace + "."))
                        .forEach(key -> shortNames.add(key.substring(key.lastIndexOf('.') + 1)));
                entries.keySet()
                        .removeIf(
                                key ->
                                        key.equals(namespace)
                                                || key.startsWith(namespace + ".")
                                                || shortNames.contains(key));
            }
            try (InputStream input = resource.getInputStream()) {
                new XMLMapperBuilder(
                                input,
                                configuration,
                                resource.toString(),
                                configuration.getSqlFragments())
                        .parse();
            }
            LOG.info("Refreshed mapper {}", resource);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    private static Object field(Object target, String name) throws ReflectiveOperationException {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException ignored) {
                /* Search the superclass used by MyBatis-Plus. */
            }
        }
        throw new NoSuchFieldException(name);
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
