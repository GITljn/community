package com.nowcoder.community.service.impl;

import com.nowcoder.community.service.DataService;
import com.nowcoder.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataServiceImpl implements DataService {
    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    // 将uv的ip存入redis
    @Override
    public void saveUV(String ip) {
        String uvKey = RedisKeyUtil.getUVKey(sdf.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(uvKey, ip);
    }

    // 得到指定日期范围内uv的数量
    @Override
    public Long getUVNum(Date start, Date end) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        List<String> uvKeyList = new ArrayList<>();
        while (!calendar.getTime().after(end)) {
            uvKeyList.add(RedisKeyUtil.getUVKey(sdf.format(calendar.getTime())));
            calendar.add(Calendar.DATE, 1);
        }
        String uvKeyUnion = RedisKeyUtil.getUVKey(sdf.format(start), sdf.format(end));
        redisTemplate.opsForHyperLogLog().delete(uvKeyUnion);
        redisTemplate.opsForHyperLogLog().union(uvKeyUnion, uvKeyList.toArray());

        Long uvNum = redisTemplate.opsForHyperLogLog().size(uvKeyUnion);
        return uvNum;
    }

    // 将dau的id存入redis
    @Override
    public void saveDAU(int userId) {
        String dauKey = RedisKeyUtil.getDAUKey(sdf.format(new Date()));
        redisTemplate.opsForValue().setBit(dauKey, userId, true);
    }

    // 得到指定日期范围内dao的数量
    @Override
    public long getDAUNum(Date start, Date end) {
        List<byte[]> daoKeyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            daoKeyList.add(RedisKeyUtil.getDAUKey(sdf.format(calendar.getTime())).getBytes());
            calendar.add(Calendar.DATE, 1);
        }
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String dauKeyOr = RedisKeyUtil.getDAUKey(sdf.format(start), sdf.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR, dauKeyOr.getBytes(),
                        daoKeyList.toArray(new byte[0][0]));
                return connection.bitCount(dauKeyOr.getBytes());
            }
        });
    }
}
