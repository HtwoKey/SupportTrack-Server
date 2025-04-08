package com.yibei.supporttrack.service.impl;

import com.yibei.supporttrack.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisServiceImpl implements RedisService {

    private static final String KEY_CANNOT_BE_NULL = "Redis key cannot be null or empty";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //----------------------- 公共方法 -----------------------
    private void validateKey(String key) {
        if (!StringUtils.hasText(key)) {
            log.error("Invalid key: {}", key);
            throw new IllegalArgumentException(KEY_CANNOT_BE_NULL);
        }
    }

    private boolean setExpireIfNeeded(String key, Long time) {
        if (time != null && time > 0) {
            Boolean result = redisTemplate.expire(key, time, TimeUnit.SECONDS);
            if (result == null || !result) {
                log.warn("Set expiration failed for key: {}", key);
            }
            return result != null && result;
        }
        return false;
    }

    //----------------------- String 操作 -----------------------

    @Override
    public void set(String key, Object value, long time) {
        validateKey(key);
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    @Override
    public void set(String key, Object value) {
        validateKey(key);
        redisTemplate.opsForValue().set(key, value);
    }

    //----------------------- Hash 操作 -----------------------
    @Override
    public Boolean hSet(String key, String hashKey, Object value, long time) {
        validateKey(key);
        redisTemplate.opsForHash().put(key, hashKey, value);
        return setExpireIfNeeded(key, time);
    }

    // 修改 get 方法实现
    @Override
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return clazz.isInstance(value) ? clazz.cast(value) : null;
    }

    @Override
    public Boolean del(String key) {
        validateKey(key);
        return redisTemplate.delete(key);
    }

    @Override
    public Long del(List<String> keys) {
        return redisTemplate.delete(keys);
    }

    @Override
    public Boolean expire(String key, long time) {
        validateKey(key);
        return redisTemplate.expire(key, time, TimeUnit.SECONDS);
    }

    @Override
    public Long getExpire(String key) {
        validateKey(key);
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    @Override
    public Boolean hasKey(String key) {
        validateKey(key);
        return redisTemplate.hasKey(key);
    }

    @Override
    public Long incr(String key, long delta) {
        validateKey(key);
        if (delta < 0) {
            throw new RuntimeException("delta must be greater than 0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    @Override
    public Long decr(String key, long delta) {
        validateKey(key);
        if (delta < 0) {
            throw new RuntimeException("delta must be greater than 0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    @Override
    public Object hGet(String key, String hashKey) {
        validateKey(key);
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    @Override
    public void hSet(String key, String hashKey, Object value) {
        validateKey(key);
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    @Override
    public Map<Object, Object> hGetAll(String key) {
        validateKey(key);
        return redisTemplate.opsForHash().entries(key);
    }

    @Override
    public Boolean hSetAll(String key, Map<String, Object> map, long time) {
        validateKey(key);
        redisTemplate.opsForHash().putAll(key, map);
        return expire(key, time);
    }

    @Override
    public void hSetAll(String key, Map<String, ?> map) {
        validateKey(key);
        redisTemplate.opsForHash().putAll(key, map);
    }

    @Override
    public void hDel(String key, Object... hashKey) {
        validateKey(key);
        redisTemplate.opsForHash().delete(key, hashKey);
    }

    @Override
    public Boolean hHasKey(String key, String hashKey) {
        validateKey(key);
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    @Override
    public Long hIncr(String key, String hashKey, Long delta) {
        validateKey(key);
        if (delta < 0) {
            throw new RuntimeException("delta must be greater than 0");
        }
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    @Override
    public Long hDecr(String key, String hashKey, Long delta) {
        validateKey(key);
        if (delta < 0) {
            throw new RuntimeException("delta must be greater than 0");
        }
        return redisTemplate.opsForHash().increment(key, hashKey, -delta);
    }

    @Override
    public Set<Object> sMembers(String key) {
        validateKey(key);
        return redisTemplate.opsForSet().members(key);
    }

    @Override
    public Long sAdd(String key, Object... values) {
        validateKey(key);
        return redisTemplate.opsForSet().add(key, values);
    }

    @Override
    public Long sAdd(String key, long time, Object... values) {
        validateKey(key);
        Long count = redisTemplate.opsForSet().add(key, values);
        setExpireIfNeeded(key, time);
        return count;
    }

    @Override
    public Boolean sIsMember(String key, Object value) {
        validateKey(key);
        return redisTemplate.opsForSet().isMember(key, value);
    }

    @Override
    public Long sSize(String key) {
        validateKey(key);
        return redisTemplate.opsForSet().size(key);
    }

    @Override
    public Long sRemove(String key, Object... values) {
        validateKey(key);
        return redisTemplate.opsForSet().remove(key, values);
    }

    @Override
    public List<Object> lRange(String key, long start, long end) {
        validateKey(key);
        return redisTemplate.opsForList().range(key, start, end);
    }

    @Override
    public Long lSize(String key) {
        validateKey(key);
        return redisTemplate.opsForList().size(key);
    }

    @Override
    public Object lIndex(String key, long index) {
        validateKey(key);
        return redisTemplate.opsForList().index(key, index);
    }

    @Override
    public Long lPush(String key, Object value) {
        validateKey(key);
        return redisTemplate.opsForList().rightPush(key, value);
    }

    @Override
    public Long lPush(String key, Object value, long time) {
        validateKey(key);
        Long index = redisTemplate.opsForList().rightPush(key, value);
        expire(key, time);
        return index;
    }

    @Override
    public Long lPushAll(String key, Object... values) {
        validateKey(key);
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    @Override
    public Long lPushAll(String key, Long time, Object... values) {
        validateKey(key);
        Long count = redisTemplate.opsForList().rightPushAll(key, values);
        expire(key, time);
        return count;
    }

    @Override
    public Long lRemove(String key, long count, Object value) {
        validateKey(key);
        return redisTemplate.opsForList().remove(key, count, value);
    }
}
