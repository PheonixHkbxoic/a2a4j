package io.github.pheonixhkbxoic.a2a4j.storage.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author PheonixHkbxoic
 * @date 2025/5/14 15:24
 * @desc
 */
@Slf4j
@AutoConfigureAfter(RedisAutoConfiguration.class)
@ConditionalOnClass(RedisConnectionFactory.class)
@Configuration
public class RedisTaskStoreAutoConfiguration {

    @ConditionalOnClass(RedisConnectionFactory.class)
    @Bean(name = "a2a4jRedisTemplate")
    public RedisTemplate<String, Object> a2a4jRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @ConditionalOnMissingBean(RedisTaskStore.class)
    @Bean
    public RedisTaskStore redisTaskStore(@Autowired @Qualifier("a2a4jRedisTemplate") RedisTemplate<String, Object> a2a4jRedisTemplate) {
        RedisTaskStore redisTaskStore = new RedisTaskStore(a2a4jRedisTemplate);
        log.debug("RedisTaskStore has been created: {}", redisTaskStore);
        return redisTaskStore;
    }

}
