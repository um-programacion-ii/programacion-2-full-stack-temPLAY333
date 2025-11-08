package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

public class RedisTestContainer implements InitializingBean, DisposableBean {

    private GenericContainer redisContainer;
    private static final Logger LOG = LoggerFactory.getLogger(RedisTestContainer.class);

    @Override
    public void destroy() {
        if (null != redisContainer && redisContainer.isRunning()) {
            redisContainer.close();
        }
    }

    @Override
    public void afterPropertiesSet() {
        if (null == redisContainer) {
            redisContainer = new GenericContainer("redis:8.0.0")
                .withExposedPorts(6379)
                .withLogConsumer(new Slf4jLogConsumer(LOG))
                .withReuse(true);
        }
        if (!redisContainer.isRunning()) {
            redisContainer.start();
        }
    }

    public GenericContainer getRedisContainer() {
        return redisContainer;
    }
}
