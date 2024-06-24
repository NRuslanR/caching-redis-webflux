package org.example.redis_cache;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class CustomRedisValueSerializer implements RedisSerializer<Object>
{
    private final ObjectMapper objectMapper;
    private byte[] serializedObject;

    @Override
    @Nullable
    public byte[] serialize(@Nullable Object value) throws SerializationException 
    {
        try 
        {
            Mono<?> mono = (Mono<?>)value;

            mono.subscribe(this::serializeObject);

            return serializedObject;
        } 

        catch (Exception e) 
        {
            e.printStackTrace();
            log.error(e.getMessage());
        }

        return null;
    }

    @Override
    @Nullable
    public Object deserialize(@Nullable byte[] bytes) throws SerializationException 
    {
        if (Objects.isNull(bytes))
            return null;

        try
        {
            var objectString = new String(bytes, StandardCharsets.UTF_8);

            var object = objectMapper.readValue(objectString, Object.class);

            return Mono.just(object);
        }

        catch(Exception e)
        {
            e.printStackTrace();
            log.error(e.getMessage());
        }

        return Mono.empty();
    }

    @SneakyThrows
    private byte[] serializeObject(Object object)
    {
        serializedObject = objectMapper.writeValueAsBytes(object);

        return serializedObject;
    }

}
