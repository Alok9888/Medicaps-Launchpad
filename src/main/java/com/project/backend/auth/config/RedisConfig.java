
package com.project.backend.auth.config;

import com.project.backend.auth.entity.OtpVerification;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, OtpVerification> otpRedisTemplate(
            RedisConnectionFactory cf) {

        RedisTemplate<String, OtpVerification> template =
                new RedisTemplate<>();

        template.setConnectionFactory(cf);

        StringRedisSerializer keySerializer =
                new StringRedisSerializer();

        JacksonJsonRedisSerializer<OtpVerification> valueSerializer =
                new JacksonJsonRedisSerializer<>(OtpVerification.class);

        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);

        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();

        return template;
    }

    // Dedicated connection for Bucket4j
    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, byte[]> bucket4jConnection(
            LettuceConnectionFactory connectionFactory
    ) {

        RedisClient redisClient =
                (RedisClient) connectionFactory.getRequiredNativeClient();

        return redisClient.connect(
                RedisCodec.of(
                        StringCodec.UTF8,
                        ByteArrayCodec.INSTANCE
                )
        );
    }
}
