package com.example.redisdemojdk8.constant;

/**
 * @author duanruiming
 * @date 2023/01/16 10:45
 */
public class Constant {


    /**
     * Redis存储Key前缀
     */
    public static final String KEY_PREFIX = "utcsystem_";

    /**
     * 分隔符：减号
     **/
    public static final String SEPARATOR_MINUS = "-";

    public static final String WEBSOCKET_TOPIC_EVENT_WARN = "eventWarn";

    public static final String WEBSOCKET_TOPIC_SYS_SERVICE_STATUS = "sysStatus";
    public static final String WEBSOCKET_TOPIC_CAR_TRAIL_INFO = "carTrailInfo";
    // WebSocket自动解锁路口topic
    public static final String WEBSOCKET_AUTO_UNLOCK = "autoUnlock";
}
