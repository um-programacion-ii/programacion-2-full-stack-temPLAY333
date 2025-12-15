package com.example.demo.config;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import org.hibernate.cache.jcache.ConfigSettings;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
// RedissonConfiguration is loaded via reflection to avoid compile-time dependency
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tech.jhipster.config.JHipsterProperties;
import tech.jhipster.config.cache.PrefixedKeyGenerator;

@Configuration
@EnableCaching
public class CacheConfiguration {

    private final Logger log = LoggerFactory.getLogger(CacheConfiguration.class);

    private GitProperties gitProperties;
    private BuildProperties buildProperties;

    @Bean
    public javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration(JHipsterProperties jHipsterProperties) {
        MutableConfiguration<Object, Object> jcacheConfig = new MutableConfiguration<>();

        // Default expiry
        jcacheConfig.setStatisticsEnabled(true);
        jcacheConfig.setExpiryPolicyFactory(
            CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, jHipsterProperties.getCache().getRedis().getExpiration()))
        );

        // Try to create a Redisson-backed JCache configuration. If Redis/Redisson is not available,
        // fall back to an in-memory JCache configuration (MutableConfiguration) so the application can still start.
        try {
            URI redisUri = URI.create(jHipsterProperties.getCache().getRedis().getServer()[0]);

            Config config = new Config();
            // Fix Hibernate lazy initialization https://github.com/jhipster/generator-jhipster/issues/22889
            config.setCodec(new org.redisson.codec.SerializationCodec());
            if (jHipsterProperties.getCache().getRedis().isCluster()) {
                ClusterServersConfig clusterServersConfig = config
                    .useClusterServers()
                    .setMasterConnectionPoolSize(jHipsterProperties.getCache().getRedis().getConnectionPoolSize())
                    .setMasterConnectionMinimumIdleSize(jHipsterProperties.getCache().getRedis().getConnectionMinimumIdleSize())
                    .setSubscriptionConnectionPoolSize(jHipsterProperties.getCache().getRedis().getSubscriptionConnectionPoolSize())
                    .addNodeAddress(jHipsterProperties.getCache().getRedis().getServer());

                if (redisUri.getUserInfo() != null) {
                    clusterServersConfig.setPassword(redisUri.getUserInfo().substring(redisUri.getUserInfo().indexOf(':') + 1));
                }
            } else {
                SingleServerConfig singleServerConfig = config
                    .useSingleServer()
                    .setConnectionPoolSize(jHipsterProperties.getCache().getRedis().getConnectionPoolSize())
                    .setConnectionMinimumIdleSize(jHipsterProperties.getCache().getRedis().getConnectionMinimumIdleSize())
                    .setSubscriptionConnectionPoolSize(jHipsterProperties.getCache().getRedis().getSubscriptionConnectionPoolSize())
                    .setAddress(jHipsterProperties.getCache().getRedis().getServer()[0]);

                if (redisUri.getUserInfo() != null) {
                    singleServerConfig.setPassword(redisUri.getUserInfo().substring(redisUri.getUserInfo().indexOf(':') + 1));
                }
            }

            // Create Redisson client and return Redisson-backed jcache configuration
            RedissonClient redisson = Redisson.create(config);
            log.info("Redisson client created for Redis server '{}'", jHipsterProperties.getCache().getRedis().getServer()[0]);

            // Use reflection to create RedissonConfiguration to avoid compile-time dependency on redisson-jcache
            try {
                Class<?> redissonConfigClass = Class.forName("org.redisson.jcache.configuration.RedissonConfiguration");
                java.lang.reflect.Method fromInstanceMethod = redissonConfigClass.getMethod("fromInstance", RedissonClient.class, javax.cache.configuration.Configuration.class);
                @SuppressWarnings("unchecked")
                javax.cache.configuration.Configuration<Object, Object> redissonConfig = (javax.cache.configuration.Configuration<Object, Object>) fromInstanceMethod.invoke(null, redisson, jcacheConfig);
                return redissonConfig;
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                log.warn("RedissonConfiguration not available, falling back to in-memory JCache. Cause: {}", e.getMessage());
                return jcacheConfig;
            }
        } catch (Exception ex) {
            // If any error (e.g., Redis not reachable), fallback to in-memory JCache configuration
            log.warn("Redis/Redisson configuration failed or Redis not reachable; falling back to in-memory JCache. Cause: {}", ex.getMessage());
            // jcacheConfig already prepared and will be used with the default JCache provider (in-memory)
            return jcacheConfig;
        }
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cm, javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration) {
        return hibernateProperties -> {
            // CRITICAL: If CacheManager is null, create a fallback in-memory one
            javax.cache.CacheManager cacheManagerToUse = cm;
            if (cacheManagerToUse == null) {
                log.warn("CacheManager is null in hibernatePropertiesCustomizer, creating fallback in-memory CacheManager");
                try {
                    CachingProvider provider = Caching.getCachingProvider();
                    cacheManagerToUse = provider.getCacheManager();
                } catch (Exception e) {
                    log.error("Failed to create fallback CacheManager: {}", e.getMessage(), e);
                    // If we can't create a CacheManager, disable second-level cache
                    log.error("Disabling Hibernate second-level cache due to CacheManager creation failure");
                    hibernateProperties.put("hibernate.cache.use_second_level_cache", false);
                    return;
                }
            }

            // CRITICAL: If Redisson JCacheManager is used, ensure it has the default configuration BEFORE Hibernate uses it
            // This must happen before ConfigSettings.CACHE_MANAGER is set
            // NOTE: In Redisson 3.46.0, setDefaultConfiguration method may not exist, so we disable cache if it fails
            boolean defaultConfigSet = setDefaultConfigurationIfNeeded(cacheManagerToUse, jcacheConfiguration, "hibernatePropertiesCustomizer");
            if (!defaultConfigSet) {
                log.error("CRITICAL: Failed to set default configuration on JCacheManager before Hibernate initialization!");
                log.error("CacheManager class: {}", cacheManagerToUse.getClass().getName());
                log.error("This version of Redisson may not support setDefaultConfiguration method.");
                log.error("Disabling Hibernate second-level cache to prevent startup failure.");
                log.error("The application will run without second-level cache. This is safe but may impact performance.");
                // Disable second-level cache to prevent the error and allow application to start
                hibernateProperties.put("hibernate.cache.use_second_level_cache", false);
                return;
            }
            log.info("Default configuration set successfully, enabling Hibernate second-level cache");
            hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cacheManagerToUse);
            // If caches are missing in the provider, let Hibernate create them on-the-fly to avoid startup failures
            hibernateProperties.put("hibernate.javax.cache.missing_cache_strategy", "create");
        };
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer(javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration) {
        return cm -> {
            log.info("========================================");
            log.info("Inicializando caches en cacheManagerCustomizer");
            log.info("CacheManager class: {}", cm != null ? cm.getClass().getName() : "null");
            log.info("========================================");

            if (cm == null) {
                log.error("CacheManager es null en cacheManagerCustomizer, no se pueden crear caches");
                return;
            }

            // Set default configuration first before creating any caches
            boolean configSet = setDefaultConfigurationIfNeeded(cm, jcacheConfiguration, "cacheManagerCustomizer");
            if (!configSet) {
                log.warn("Failed to set default configuration in cacheManagerCustomizer - cache creation may fail");
            }

            log.info("Creando caches de usuario...");
            createCache(cm, com.example.demo.repository.UserRepository.USERS_BY_LOGIN_CACHE, jcacheConfiguration);
            createCache(cm, com.example.demo.repository.UserRepository.USERS_BY_EMAIL_CACHE, jcacheConfiguration);
            createCache(cm, com.example.demo.domain.User.class.getName(), jcacheConfiguration);
            createCache(cm, com.example.demo.domain.Authority.class.getName(), jcacheConfiguration);
            createCache(cm, com.example.demo.domain.User.class.getName() + ".authorities", jcacheConfiguration);

            log.info("Creando caches de entidades de negocio...");
            createCache(cm, com.example.demo.domain.Evento.class.getName(), jcacheConfiguration);
            createCache(cm, com.example.demo.domain.Evento.class.getName() + ".integrantes", jcacheConfiguration);
            createCache(cm, com.example.demo.domain.EventoTipo.class.getName(), jcacheConfiguration);
            createCache(cm, com.example.demo.domain.Integrante.class.getName(), jcacheConfiguration);
            createCache(cm, com.example.demo.domain.Integrante.class.getName() + ".eventos", jcacheConfiguration);
            createCache(cm, com.example.demo.domain.Venta.class.getName(), jcacheConfiguration);
            createCache(cm, com.example.demo.domain.Asiento.class.getName(), jcacheConfiguration);
            createCache(cm, com.example.demo.domain.AlumnoProfile.class.getName(), jcacheConfiguration);
            // jhipster-needle-redis-add-entry

            log.info("Verificando caches creados...");
            verifyCacheExists(cm, com.example.demo.repository.UserRepository.USERS_BY_LOGIN_CACHE);
            verifyCacheExists(cm, com.example.demo.repository.UserRepository.USERS_BY_EMAIL_CACHE);

            log.info("========================================");
            log.info("Inicializacion de caches completada");
            log.info("========================================");
        };
    }

    private void verifyCacheExists(javax.cache.CacheManager cm, String cacheName) {
        if (cm == null) {
            log.error("CacheManager es null, no se puede verificar cache '{}'", cacheName);
            return;
        }
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            log.info("Cache '{}' verificado: EXISTE", cacheName);
        } else {
            log.error("Cache '{}' verificado: NO EXISTE - esto causara errores en autenticacion", cacheName);
        }
    }

    private void createCache(
        javax.cache.CacheManager cm,
        String cacheName,
        javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration
    ) {
        if (cm == null) {
            log.error("CacheManager es null, no se puede crear cache '{}'", cacheName);
            return;
        }

        try {
            javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
            if (cache != null) {
                log.debug("Cache '{}' ya existe, limpiando", cacheName);
                cache.clear();
            } else {
                log.info("Creando cache '{}'", cacheName);
                cm.createCache(cacheName, jcacheConfiguration);
                // Verificar que se creó correctamente
                cache = cm.getCache(cacheName);
                if (cache != null) {
                    log.info("Cache '{}' creado exitosamente", cacheName);
                } else {
                    log.error("ERROR: Cache '{}' NO se creo aunque createCache() no lanzo excepcion", cacheName);
                }
            }
        } catch (Exception e) {
            log.error("ERROR al crear cache '{}': {}", cacheName, e.getMessage(), e);
            // No lanzamos la excepción para que otros caches puedan crearse
        }
    }

    @Autowired(required = false)
    public void setGitProperties(GitProperties gitProperties) {
        this.gitProperties = gitProperties;
    }

    @Autowired(required = false)
    public void setBuildProperties(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new PrefixedKeyGenerator(this.gitProperties, this.buildProperties);
    }

    /**
     * Helper method to set default configuration on Redisson JCacheManager using reflection
     * @param cm CacheManager instance
     * @param jcacheConfiguration Configuration to set as default
     * @param context Context string for logging
     * @return true if configuration was set successfully, false otherwise
     */
    private boolean setDefaultConfigurationIfNeeded(javax.cache.CacheManager cm, javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration, String context) {
        if (cm == null) {
            log.debug("CacheManager is null in {}, skipping default configuration setup", context);
            return false;
        }

        try {
            Class<?> cmClass = cm.getClass();
            String className = cmClass.getName();

            // Check if this is Redisson's JCacheManager
            if (className.equals("org.redisson.jcache.JCacheManager")) {
                try {
                    // Use getDeclaredMethod to avoid compile-time dependency
                    java.lang.reflect.Method setDefaultConfigMethod = cmClass.getDeclaredMethod("setDefaultConfiguration", javax.cache.configuration.Configuration.class);
                    setDefaultConfigMethod.setAccessible(true);
                    setDefaultConfigMethod.invoke(cm, jcacheConfiguration);
                    log.info("Successfully set default JCache configuration on Redisson JCacheManager in {}", context);
                    return true;
                } catch (NoSuchMethodException e) {
                    log.error("setDefaultConfiguration method not found in JCacheManager class {} - this will cause Hibernate initialization to fail!", className);
                    return false;
                } catch (SecurityException e) {
                    log.error("Security exception when trying to set default configuration in {}: {}", context, e.getMessage());
                    return false;
                } catch (java.lang.reflect.InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    log.error("Exception invoking setDefaultConfiguration in {}: {}", context, cause != null ? cause.getMessage() : e.getMessage());
                    if (cause != null) {
                        cause.printStackTrace();
                    }
                    return false;
                }
            } else {
                log.debug("CacheManager is not Redisson JCacheManager (class: {}) in {}, no default configuration needed", className, context);
                return true; // Not a Redisson JCacheManager, so no action needed
            }
        } catch (Throwable t) {
            log.error("Unexpected error setting default configuration in {}: {}", context, t.getMessage(), t);
            return false;
        }
    }

    @Bean
    @Primary
    public javax.cache.CacheManager cacheManagerBean(javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration) {
        javax.cache.CacheManager cm = null;
        try {
            // Try to get Redisson JCache provider explicitly
            CachingProvider provider = null;
            boolean isRedissonProvider = false;
            try {
                // First try to get Redisson's JCache provider
                Class<?> redissonProviderClass = Class.forName("org.redisson.jcache.JCachingProvider");
                provider = (CachingProvider) redissonProviderClass.getDeclaredConstructor().newInstance();
                isRedissonProvider = true;
                log.debug("Using Redisson JCache provider");
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                // Fallback to default provider
                log.debug("Redisson JCache provider not found, using default provider: {}", e.getMessage());
                provider = Caching.getCachingProvider();
            }

            cm = provider.getCacheManager();

            if (cm == null) {
                log.warn("Provider returned null CacheManager, creating fallback in-memory CacheManager");
                // Create a fallback in-memory CacheManager
                provider = Caching.getCachingProvider();
                cm = provider.getCacheManager();
                isRedissonProvider = false;
            }

            // CRITICAL: Set default configuration IMMEDIATELY after getting CacheManager
            // This must happen before any caches are created or Hibernate uses it
            // NOTE: In Redisson 3.46.0, setDefaultConfiguration method may not exist
            if (isRedissonProvider && cm != null) {
                boolean configSet = setDefaultConfigurationIfNeeded(cm, jcacheConfiguration, "cacheManagerBean");
                if (!configSet) {
                    log.warn("No se pudo establecer configuracion por defecto en Redisson CacheManager (Redisson 3.46.0 no tiene setDefaultConfiguration)");
                    log.warn("Esto es normal para esta version de Redisson. Los caches se crearan con configuracion explicita.");
                    // NO cambiamos a in-memory porque Redisson funciona bien sin setDefaultConfiguration
                    // Solo necesitamos crear los caches con configuracion explicita
                    log.info("CacheManager bean creado (Redisson, sin default config - se usara config explicita por cache)");
                } else {
                    log.info("CacheManager bean created and default configuration set successfully (Redisson)");
                }
            } else {
                log.info("CacheManager bean created successfully (in-memory, no default config needed)");
            }

            // CRITICAL: Crear TODOS los caches directamente aquí para asegurar que existan
            // Esto es necesario porque el JCacheManagerCustomizer puede no ejecutarse o Spring Cache
            // puede usar un CacheManager diferente
            if (cm != null) {
                log.info("========================================");
                log.info("Creando TODOS los caches en cacheManagerBean (CacheManager @Primary)");
                log.info("CacheManager class: {}", cm.getClass().getName());
                log.info("========================================");

                // Caches críticos de usuario (necesarios para autenticación)
                log.info("Creando caches de usuario...");
                createCache(cm, com.example.demo.repository.UserRepository.USERS_BY_LOGIN_CACHE, jcacheConfiguration);
                createCache(cm, com.example.demo.repository.UserRepository.USERS_BY_EMAIL_CACHE, jcacheConfiguration);
                createCache(cm, com.example.demo.domain.User.class.getName(), jcacheConfiguration);
                createCache(cm, com.example.demo.domain.Authority.class.getName(), jcacheConfiguration);
                createCache(cm, com.example.demo.domain.User.class.getName() + ".authorities", jcacheConfiguration);

                // Caches de entidades de negocio
                log.info("Creando caches de entidades de negocio...");
                createCache(cm, com.example.demo.domain.Evento.class.getName(), jcacheConfiguration);
                createCache(cm, com.example.demo.domain.Evento.class.getName() + ".integrantes", jcacheConfiguration);
                createCache(cm, com.example.demo.domain.EventoTipo.class.getName(), jcacheConfiguration);
                createCache(cm, com.example.demo.domain.Integrante.class.getName(), jcacheConfiguration);
                createCache(cm, com.example.demo.domain.Integrante.class.getName() + ".eventos", jcacheConfiguration);
                createCache(cm, com.example.demo.domain.Venta.class.getName(), jcacheConfiguration);
                createCache(cm, com.example.demo.domain.Asiento.class.getName(), jcacheConfiguration);
                createCache(cm, com.example.demo.domain.AlumnoProfile.class.getName(), jcacheConfiguration);

                // Verificar caches críticos
                log.info("Verificando caches criticos...");
                verifyCacheExists(cm, com.example.demo.repository.UserRepository.USERS_BY_LOGIN_CACHE);
                verifyCacheExists(cm, com.example.demo.repository.UserRepository.USERS_BY_EMAIL_CACHE);

                log.info("========================================");
                log.info("Todos los caches creados en cacheManagerBean");
                log.info("========================================");
            } else {
                log.error("CRITICAL: CacheManager es null, no se pueden crear caches");
            }

            return cm;
        } catch (Exception e) {
            log.error("Unable to obtain JCache provider/cache manager, creating fallback in-memory CacheManager. Error: {}", e.getMessage(), e);
            // Always return a valid CacheManager, even if it's just in-memory
            try {
                if (cm == null) {
                    CachingProvider provider = Caching.getCachingProvider();
                    cm = provider.getCacheManager();
                    log.info("Created fallback in-memory CacheManager");
                }
                return cm;
            } catch (Exception fallbackException) {
                log.error("CRITICAL: Failed to create even fallback CacheManager: {}", fallbackException.getMessage(), fallbackException);
                // This should never happen, but if it does, we need to return something
                // Returning null will cause issues, but it's better than crashing
                return null;
            }
        }
    }
}
