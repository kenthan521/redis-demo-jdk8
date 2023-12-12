package com.example.redisdemojdk8.util;

import com.example.redisdemojdk8.constant.Constant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @description: redis工具类
 * @author wanji
 * @version 1.0
 * Created on 2019/4/29 20:34
 */
@Component
public class RedisUtils {

    /**
     * 注入redisTemplate
     */
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * @description: 判断是否存在Key
     * @param key redis的Key
     * @return boolean true:有 false:无
     */
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * @description: 添加字符串
     * @param key redis的Key
     * @param value 添加redis的value
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * @description: 添加对象
     * @param key redis的Key
     * @param object 添加redis的value
     */
    public void set(String key, Object object) {
        redisTemplate.opsForValue().set(key, object);
    }

    /**
     * @description: 添加带生命周期的对象
     * @param key redis的Key
     * @param object 添加redis的value
     * @param seconds 失效时间
     */
    public void setAndExpire(String key, Object object, int seconds) {
        redisTemplate.opsForValue().set(key, object);
        redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    /**
     * @description: 添加带生命周期的对象
     * @param key redis的Key
     * @param value 添加redis的value
     * @param seconds 失效时间
     */
    public void setAndExpire(String key, String value, int seconds) {
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    /**
     * @description: 获取对象
     * @param key redis的Key
     * @return Object 返回对象
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * @description: 存入redis的hash
     * @param key redis的Key
     * @param field 字段值
     * @param value 存入的值
     */
    public void setHash(String key, String field, String value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * @description: 存入redis的hash
     * @param key redis的Key
     * @param field 字段值
     * @param value 存入的值
     */
    public void setHash(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * @description: 根据key和字段值获取内容值
     * @param key redis的Key
     * @param field 字段值
     * @return String 返回字符串
     */
    public String getHash(String key, String field) {
        return (String)redisTemplate.opsForHash().get(key, field);
    }

    /**
     * @description: 根据field删除值
     * @param key redis的Key
     * @param field 字段值
     */
    public void delHashMap(String key, String field) {
        redisTemplate.boundHashOps(key).delete(field);
    }

    /**
     * @description: 存入hash集合
     * @param key redis的Key
     * @param hashmap 存入的Map集合
     */
    public void setHashMap(String key, Map<String, Object> hashmap){
        redisTemplate.opsForHash().putAll(key, hashmap);
    }
    
    /**
     * @description: 取出hash集合
     * @param key redis的Key
     * @return Map<Object, Object> 返回Map集合
     */
    public Map<Object, Object> getHashMap(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * @description: 存入redis的Set
     * @param key redis的Key
     * @param object 对象
     */
    public void setSet(String key,Object object){
        redisTemplate.opsForSet().add(key, object);
    }

    /**
     * @description: 获取redis的Set
     * @param key redis的Key
     * @return Set<Object> Set集合
     */
    public Set<Object> getSet(String key){
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * @discription: 查看值是否是set成员
     * @param key set的key
     * @param value set的成员
     * @return 是否是set成员
     */
    public Boolean isSetMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * @description:设置key的过期时间，endTime格式：yyyy-MM-dd hh:mm:ss
     * @param key redis的Key
     * @param endTime 结束时间
     */
    public void setExpire(String key, Date endTime) {
        long seconds = endTime.getTime() - System.currentTimeMillis();
        redisTemplate.expire(key, (int) (seconds / 1000), TimeUnit.SECONDS);
    }

    /**
     * @description: 设置key的过期时间
     * @param key redis的Key
     * @param time 过期时间（秒）
     */
    public void setExpire(String key, int time) {
        redisTemplate.expire(key, time, TimeUnit.SECONDS);
    }

    /**
     * <p>Discription:获取key的过期时间
     * @param key redis的Key
     * @return 过期时间（秒）
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }


    /**
     * @description: 在redis消息队列队尾插入数据
     * @param key redis的Key
     * @param object 添加的对象
     */
    public void tailPush(String key, Object object){
        redisTemplate.opsForList().rightPush(key, object);
    }

    /**
     * @description: 在redis消息队列对头插入数据
     * @param key redis的Key
     * @param object 添加的对象
     */
    public void headPush(String key,Object object){
        redisTemplate.opsForList().leftPush(key, object);
    }

    /**
     * @description: 在redis消息队列队尾删除数据
     * @param key redis的Key
     * @return Object 删除的对象
     */
    public Object tailPop(String key){
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * @description: 在redis消息队列队头删除数据
     * @param key redis的Key
     * @return Object 删除的对象
     */
    public Object headPop(String key){
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * @description: 删除redis的值
     * @param key redis的Key
     */
    public void del(String key) {
        if (hasKey(key)) {
            redisTemplate.delete(key);
        }
    }
    
    /**
     * @description: 清理redis缓存
     */
    public void flushDB(){
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    /**
     * @description: 根据类型生成版本号
     * @param type 类型key区分
     * @return 版本号
     * @author wanji
     * @date 2019/5/5 19:26
    */
    public String getVersion(String type) {
        String formatDate = DateUtils.dateToString(new Date(), DateStyle.YYYYMMDD);
        String key = Constant.KEY_PREFIX + type + formatDate;
        //当前时间到第二天还剩多少时间
        Date newDate = DateUtils.dateForMat(DateUtils.addDay(new Date(), 1), DateStyle.YYYY_MM_DD);
        int liveTime = DateUtils.dayDiff(newDate, new Date(), Calendar.MILLISECOND);
        //获取自增号
        Long incr = getIncr(key, liveTime);
        if(incr == 0) {
            incr = getIncr(key, liveTime);//从001开始
        }
        DecimalFormat df = new DecimalFormat("000");//三位序列号
        return formatDate + Constant.SEPARATOR_MINUS + df.format(incr);
    }

    /**
     * 自增ID
     * @param key 建
     * @param liveTime 过期时间
     * @return 自增结果
     */
    public Long getIncr(String key, long liveTime) {
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        Long increment = entityIdCounter.getAndIncrement();

        if ((null == increment || increment.longValue() == 0) && liveTime > 0) {//初始设置过期时间
            entityIdCounter.expire(liveTime, TimeUnit.MILLISECONDS);//单位毫秒
        }
        return increment;
    }

    /**
     * 获取全部Redis的key
     * @return
     */
    public Set<String> keys() {
        return redisTemplate.keys("*");
    }
}
