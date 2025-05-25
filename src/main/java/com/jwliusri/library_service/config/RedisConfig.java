package com.jwliusri.library_service.config;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.stereotype.Component;

import com.jwliusri.library_service.security.mfa.MfaOtp;

@Configuration
@EnableCaching
@EnableRedisRepositories(keyspaceConfiguration = RedisConfig.CustomKeyspaceConfiguration.class)
public class RedisConfig {

    private static Long MfaOtpExpirationMinutes;

    @Value("${security.mfa-otp-minutes}")
    public void setMfaOtpExpirationMinutes(Long val) {
        RedisConfig.MfaOtpExpirationMinutes = val;
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // Cache expiration time
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    @Component
    public static class CustomKeyspaceConfiguration extends KeyspaceConfiguration {

        @Override
        protected Iterable<KeyspaceSettings> initialConfiguration() {
            KeyspaceSettings keyspaceSettings = new KeyspaceSettings(MfaOtp.class, "mfa_otp");
            keyspaceSettings.setTimeToLive(TimeUnit.MINUTES.toSeconds(MfaOtpExpirationMinutes));
            return Collections.singleton(keyspaceSettings);
        }
    }
}
