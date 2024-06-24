package org.example.redis_cache;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RedisCacheConfig implements CachingConfigurer
{
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisServerProperties redisServerProperties;

    @Override
    @Nullable
    @Bean
    public CacheManager cacheManager() 
    {
        var conversionService = new DefaultFormattingConversionService();
        
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();

        Map<String, RedisCacheConfiguration> cacheConfigs = 
            Map.of(
                "ItemCache", 
                redisCacheConfiguration
                    .entryTtl(Duration.ofSeconds(86400))
                    .disableCachingNullValues()
                    .withConversionService(conversionService)
                    .serializeValuesWith(
                        RedisSerializationContext
                            .SerializationPair
                                .fromSerializer(customRedisValueSerializer())
                    )
            );

        return 
            RedisCacheManager
                    .builder(redisConnectionFactory())
                    .initialCacheNames(cacheConfigs.keySet())
                    .withInitialCacheConfigurations(cacheConfigs)
                    .build();
    }
    
    @Bean
    public LettuceConnectionFactory redisConnectionFactory()
    {
        return new LettuceConnectionFactory(redisStandaloneConfiguration());
    }

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory)
    {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, Object> builder = 
            RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, Object> serializationContext = builder.value(serializer).build();

        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, serializationContext);
    }

    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration()
    {
        var redisServerConfig = 
            new RedisStandaloneConfiguration(
                redisServerProperties.getHost(),
                redisServerProperties.getPort()
            );

        return redisServerConfig;
    }

    @Override
    @Nullable
    @Bean
    public CacheErrorHandler errorHandler() {
        
        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                
                log.error("GET " + cache.getName() + ":", exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key,
                    @Nullable Object value) {
                
                log.error("PUT " + cache.getName() + ":", exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                
                log.error("EVICT " + cache.getName() + ":", exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                
                log.error("CLEAR " + cache.getName() + ":", exception);

            }
            
        };
    }

    @Bean
    public CustomRedisValueSerializer customRedisValueSerializer() 
    {
        return new CustomRedisValueSerializer(objectMapper);
    }    
}
