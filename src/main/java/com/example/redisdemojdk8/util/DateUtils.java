package com.example.redisdemojdk8.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author wanji
 * @version 1.0
 * Created on 2019/4/16 15:28
 * @Description: [日期工具类]
 */
@Slf4j
public class DateUtils {

    private static final ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<>();

    private static final Object object = new Object();

    private DateUtils() {}

    /**
     * 获取SimpleDateFormat
     *
     * @param pattern 日期格式
     * @return SimpleDateFormat对象
     * @throws RuntimeException 异常：非法日期格式
     */
    public static SimpleDateFormat getDateFormat(String pattern) {
        SimpleDateFormat dateFormat = threadLocal.get();
        if (dateFormat == null) {
            synchronized (object) {
                dateFormat = new SimpleDateFormat(pattern);
                dateFormat.setLenient(false);
                threadLocal.set(dateFormat);
            }
        }
        dateFormat.applyPattern(pattern);
        return dateFormat;
    }

    /**
     * 获取日期中的某数值。如获取月份
     *
     * @param date     日期
     * @param dateType 日期格式
     * @return 数值
     */
    private static int getInteger(Date date, int dateType) {
        int num = 0;
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
            num = calendar.get(dateType);
        }
        return num;
    }

    /**
     * 增加日期中某类型的某数值。如增加日期
     *
     * @param date     日期字符串
     * @param dateType 类型
     * @param amount   数值
     * @return 计算后日期字符串
     */
    private static String addInteger(String date, int dateType, int amount) {
        String dateString = null;
        DateStyle dateStyle = getDateStyle(date);
        if (dateStyle != null) {
            Date myDate = stringToDate(date, dateStyle);
            myDate = addInteger(myDate, dateType, amount);
            dateString = dateToString(myDate, dateStyle);
        }
        return dateString;
    }

    /**
     * 增加日期中某类型的某数值。如增加日期
     *
     * @param date     日期
     * @param dateType 类型
     * @param amount   数值
     * @return 计算后日期
     */
    private static Date addInteger(Date date, int dateType, int amount) {
        Date myDate = null;
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(dateType, amount);
            myDate = calendar.getTime();
        }
        return myDate;
    }

    /**
     * 获取精确的日期
     *
     * @param timestamps 时间long集合
     * @return 日期
     */
    private static Date getAccurateDate(List<Long> timestamps) {
        Date date = null;
        long timestamp = 0;
        Map<Long, long[]> map = new HashMap<>();
        List<Long> absoluteValues = new ArrayList<>();

        if (timestamps != null && !timestamps.isEmpty()) {
            if (timestamps.size() > 1) {
                for (int i = 0; i < timestamps.size(); i++) {
                    for (int j = i + 1; j < timestamps.size(); j++) {
                        long absoluteValue = Math.abs(timestamps.get(i)
                                - timestamps.get(j));
                        absoluteValues.add(absoluteValue);
                        long[] timestampTmp = {timestamps.get(i),
                                timestamps.get(j)};
                        map.put(absoluteValue, timestampTmp);
                    }
                }

                // 有可能有相等的情况。如2012-11和2012-11-01。时间戳是相等的。此时minAbsoluteValue为0
                // 因此不能将minAbsoluteValue取默认值0
                long minAbsoluteValue = -1;
                if (!absoluteValues.isEmpty()) {
                    minAbsoluteValue = absoluteValues.get(0);
                    for (int i = 1; i < absoluteValues.size(); i++) {
                        if (minAbsoluteValue > absoluteValues.get(i)) {
                            minAbsoluteValue = absoluteValues.get(i);
                        }
                    }
                }

                if (minAbsoluteValue != -1) {
                    long[] timestampsLastTmp = map.get(minAbsoluteValue);

                    long dateOne = timestampsLastTmp[0];
                    long dateTwo = timestampsLastTmp[1];
                    if (absoluteValues.size() > 1) {
                        timestamp = Math.abs(dateOne) > Math.abs(dateTwo) ? dateOne
                                : dateTwo;
                    }
                }
            } else {
                timestamp = timestamps.get(0);
            }
        }

        if (timestamp != 0) {
            date = new Date(timestamp);
        }
        return date;
    }

    /**
     * 根据开始时间和结束时间获取间隔的时间（单位/小时）
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static Double getIntervalHours(String startTime, String endTime) {
        Date staDate = DateUtils.stringToDate(startTime, DateStyle.HH_MM_SS);
        Date endDate = DateUtils.stringToDate(endTime, DateStyle.HH_MM_SS);
        if(staDate == null || endDate == null) return null;
        BigDecimal startTimeStamp = BigDecimal.valueOf(staDate.getTime());
        BigDecimal endTimeStamp = BigDecimal.valueOf(endDate.getTime());
        BigDecimal hours = endTimeStamp.subtract(startTimeStamp).divide(new BigDecimal(1000 * 60 * 60), 2, BigDecimal.ROUND_DOWN);
        return hours.doubleValue();
    }

    /**
     * 判断字符串是否为日期字符串
     *
     * @param date 日期字符串
     * @return true or false
     */
    public static boolean isDate(String date) {
        boolean isDate = false;
        if (date != null && getDateStyle(date) != null) {
            isDate = true;
        }
        return isDate;
    }

    /**
     * 获取日期字符串的日期风格。失敗返回null。
     *
     * @param date 日期字符串
     * @return 日期风格
     */
    public static DateStyle getDateStyle(String date) {
        DateStyle dateStyle = null;
        Map<Long, DateStyle> map = new HashMap<>();
        List<Long> timestamps = new ArrayList<>();
        for (DateStyle style : DateStyle.values()) {
            if (style.isShowOnly()) {
                continue;
            }
            Date dateTmp = null;
            if (date != null) {
                try {
                    ParsePosition pos = new ParsePosition(0);
                    dateTmp = getDateFormat(style.getValue()).parse(date, pos);
                    if (pos.getIndex() != date.length()) {
                        dateTmp = null;
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
            if (dateTmp != null) {
                timestamps.add(dateTmp.getTime());
                map.put(dateTmp.getTime(), style);
            }
        }
        Date accurateDate = getAccurateDate(timestamps);
        if (accurateDate != null) {
            dateStyle = map.get(accurateDate.getTime());
        }
        return dateStyle;
    }

    /**
     * 将日期字符串转化为日期。失败返回null。
     *
     * @param date 日期字符串
     * @return 日期
     */
    public static Date stringToDate(String date) {
        DateStyle dateStyle = getDateStyle(date);
        return stringToDate(date, dateStyle);
    }

    /**
     * 将日期字符串转化为日期。失败返回null。
     *
     * @param date    日期字符串
     * @param pattern 日期格式
     * @return 日期
     */
    public static Date stringToDate(String date, String pattern) {
        Date myDate = null;
        if (date != null) {
            try {
                myDate = getDateFormat(pattern).parse(date);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return myDate;
    }


    /**
     * 将日期字符串转化为日期。失败返回null。
     *
     * @param date      日期字符串
     * @param dateStyle 日期风格
     * @return 日期
     */
    public static Date stringToDate(String date, DateStyle dateStyle) {
        Date myDate = null;
        if (dateStyle != null) {
            myDate = stringToDate(date, dateStyle.getValue());
        }
        return myDate;
    }

    /**
     * 将日期转化为日期字符串。失败返回null。
     *
     * @param date    日期
     * @param pattern 日期格式
     * @return 日期字符串
     */
    public static String dateToString(Date date, String pattern) {
        return getDateFormat(pattern).format(date);
    }

    /**
     * 将日期转化为日期字符串。失败返回null。
     *
     * @param date      日期
     * @param dateStyle 日期风格
     * @return 日期字符串
     */
    public static String dateToString(Date date, DateStyle dateStyle) {
        String dateString = "";
        if (dateStyle != null) {
            dateString = dateToString(date, dateStyle.getValue());
        }
        return dateString;
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date       旧日期字符串
     * @param newPattern 新日期格式
     * @return 新日期字符串
     */
    public static String stringToString(String date, String newPattern) {
        DateStyle oldDateStyle = getDateStyle(date);
        return stringToString(date, oldDateStyle, newPattern);
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date         旧日期字符串
     * @param newDateStyle 新日期风格
     * @return 新日期字符串
     */
    public static String stringToString(String date, DateStyle newDateStyle) {
        DateStyle oldDateStyle = getDateStyle(date);
        return stringToString(date, oldDateStyle, newDateStyle);
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date        旧日期字符串
     * @param olddPattern 旧日期格式
     * @param newPattern  新日期格式
     * @return 新日期字符串
     */
    public static String stringToString(String date, String olddPattern,
                                        String newPattern) {
        return dateToString(stringToDate(date, olddPattern), newPattern);
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date         旧日期字符串
     * @param olddDteStyle 旧日期风格
     * @param newParttern  新日期格式
     * @return 新日期字符串
     */
    public static String stringToString(String date, DateStyle olddDteStyle,
                                        String newParttern) {
        String dateString = null;
        if (olddDteStyle != null) {
            dateString = stringToString(date, olddDteStyle.getValue(),
                    newParttern);
        }
        return dateString;
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date         旧日期字符串
     * @param olddPattern  旧日期格式
     * @param newDateStyle 新日期风格
     * @return 新日期字符串
     */
    public static String stringToString(String date, String olddPattern,
                                        DateStyle newDateStyle) {
        String dateString = null;
        if (newDateStyle != null) {
            dateString = stringToString(date, olddPattern,
                    newDateStyle.getValue());
        }
        return dateString;
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date         旧日期字符串
     * @param olddDteStyle 旧日期风格
     * @param newDateStyle 新日期风格
     * @return 新日期字符串
     */
    public static String stringToString(String date, DateStyle olddDteStyle,
                                        DateStyle newDateStyle) {
        String dateString = null;
        if (olddDteStyle != null && newDateStyle != null) {
            dateString = stringToString(date, olddDteStyle.getValue(),
                    newDateStyle.getValue());
        }
        return dateString;
    }

    /**
     * 格式化时间
     *
     * @param date
     * @param dateStyle
     * @return 时间
     */
    public static Date dateForMat(Date date, DateStyle dateStyle) {
        return stringToDate(dateToString(date, dateStyle), dateStyle);
    }

    /**
     * 增加日期的年份。失败返回null。
     *
     * @param date       日期
     * @param yearAmount 增加数量。可为负数
     * @return 增加年份后的日期字符串
     */
    public static String addYear(String date, int yearAmount) {
        return addInteger(date, Calendar.YEAR, yearAmount);
    }

    /**
     * 增加日期的值。失败返回null。
     *
     * @param date   日期
     * @param type   添加的类型 年、月、日、时、分、秒
     * @param amount 增加数量
     * @return
     */
    public static Date add(Date date, int type, int amount) {
        return addInteger(date, type, amount);
    }

    /**
     * 增加日期的年份。失败返回null。
     *
     * @param date       日期
     * @param yearAmount 增加数量。可为负数
     * @return 增加年份后的日期
     */
    public static Date addYear(Date date, int yearAmount) {
        return addInteger(date, Calendar.YEAR, yearAmount);
    }

    /**
     * 增加日期的月份。失败返回null。
     *
     * @param date        日期
     * @param monthAmount 增加数量。可为负数
     * @return 增加月份后的日期字符串
     */
    public static String addMonth(String date, int monthAmount) {
        return addInteger(date, Calendar.MONTH, monthAmount);
    }

    /**
     * 增加日期的月份。失败返回null。
     *
     * @param date        日期
     * @param monthAmount 增加数量。可为负数
     * @return 增加月份后的日期
     */
    public static Date addMonth(Date date, int monthAmount) {
        return addInteger(date, Calendar.MONTH, monthAmount);
    }

    /**
     * 增加日期的天数。失败返回null。
     *
     * @param date      日期字符串
     * @param dayAmount 增加数量。可为负数
     * @return 增加天数后的日期字符串
     */
    public static String addDay(String date, int dayAmount) {
        return addInteger(date, Calendar.DATE, dayAmount);
    }

    /**
     * 增加日期的天数。失败返回null。
     *
     * @param date      日期
     * @param dayAmount 增加数量。可为负数
     * @return 增加天数后的日期
     */
    public static Date addDay(Date date, int dayAmount) {
        return addInteger(date, Calendar.DATE, dayAmount);
    }

    /**
     * 增加日期的小时。失败返回null。
     *
     * @param date       日期字符串
     * @param hourAmount 增加数量。可为负数
     * @return 增加小时后的日期字符串
     */
    public static String addHour(String date, int hourAmount) {
        return addInteger(date, Calendar.HOUR_OF_DAY, hourAmount);
    }

    /**
     * 增加日期的小时。失败返回null。
     *
     * @param date       日期
     * @param hourAmount 增加数量。可为负数
     * @return 增加小时后的日期
     */
    public static Date addHour(Date date, int hourAmount) {
        return addInteger(date, Calendar.HOUR_OF_DAY, hourAmount);
    }

    /**
     * 增加日期的分钟。失败返回null。
     *
     * @param date         日期字符串
     * @param minuteAmount 增加数量。可为负数
     * @return 增加分钟后的日期字符串
     */
    public static String addMinute(String date, int minuteAmount) {
        return addInteger(date, Calendar.MINUTE, minuteAmount);
    }

    /**
     * 增加日期的分钟。失败返回null。
     *
     * @param date         日期
     * @param minuteAmount 增加数量。可为负数
     * @return 增加分钟后的日期
     */
    public static Date addMinute(Date date, int minuteAmount) {
        return addInteger(date, Calendar.MINUTE, minuteAmount);
    }

    /**
     * 增加日期的秒钟。失败返回null。
     *
     * @param date         日期字符串
     * @param secondAmount 增加数量。可为负数
     * @return 增加秒钟后的日期字符串
     */
    public static String addSecond(String date, int secondAmount) {
        return addInteger(date, Calendar.SECOND, secondAmount);
    }

    /**
     * 增加日期的秒钟。失败返回null。
     *
     * @param date         日期
     * @param secondAmount 增加数量。可为负数
     * @return 增加秒钟后的日期
     */
    public static Date addSecond(Date date, int secondAmount) {
        return addInteger(date, Calendar.SECOND, secondAmount);
    }

    /**
     * 获取日期的年份。失败返回0。
     *
     * @param date 日期字符串
     * @return 年份
     */
    public static int getYear(String date) {
        return getYear(stringToDate(date));
    }

    /**
     * 获取日期的年份。失败返回0。
     *
     * @param date 日期
     * @return 年份
     */
    public static int getYear(Date date) {
        return getInteger(date, Calendar.YEAR);
    }

    /**
     * 获取日期的月份。失败返回0。
     *
     * @param date 日期字符串
     * @return 月份
     */
    public static int getMonth(String date) {
        return getMonth(stringToDate(date));
    }

    /**
     * 获取日期的月份。失败返回0。
     *
     * @param date 日期
     * @return 月份
     */
    public static int getMonth(Date date) {
        return getInteger(date, Calendar.MONTH) + 1;
    }

    /**
     * 获取日期的星期。失败返回0
     *
     * @param date 日期
     * @return 星期
     */
    public static int getWeek(String date) {
        return getWeek(stringToDate(date));
    }

    /**
     * 获取日期的星期。失败返回0
     *
     * @param date 日期
     * @return 星期
     */
    public static int getWeek(Date date) {
        return getInteger(date, Calendar.DAY_OF_WEEK) - 1;
    }

    /**
     * 获取日期的天数。失败返回0。
     *
     * @param date 日期字符串
     * @return 天
     */
    public static int getDay(String date) {
        return getDay(stringToDate(date));
    }

    /**
     * 获取日期的天数。失败返回0。
     *
     * @param date 日期
     * @return 天
     */
    public static int getDay(Date date) {
        return getInteger(date, Calendar.DATE);
    }

    /**
     * 获取日期的小时。失败返回0。
     *
     * @param date 日期字符串
     * @return 小时
     */
    public static int getHour(String date) {
        return getHour(stringToDate(date));
    }

    /**
     * 获取日期的小时。失败返回0。
     *
     * @param date 日期
     * @return 小时
     */
    public static int getHour(Date date) {
        return getInteger(date, Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取日期的分钟。失败返回0。
     *
     * @param date 日期字符串
     * @return 分钟
     */
    public static int getMinute(String date) {
        return getMinute(stringToDate(date));
    }

    /**
     * 获取日期的分钟。失败返回0。
     *
     * @param date 日期
     * @return 分钟
     */
    public static int getMinute(Date date) {
        return getInteger(date, Calendar.MINUTE);
    }

    /**
     * 获取日期的秒钟。失败返回0。
     *
     * @param date 日期字符串
     * @return 秒钟
     */
    public static int getSecond(String date) {
        return getSecond(stringToDate(date));
    }

    /**
     * 获取日期的秒钟。失败返回0。
     *
     * @param date 日期
     * @return 秒钟
     */
    public static int getSecond(Date date) {
        return getInteger(date, Calendar.SECOND);
    }

    /**
     * 获取日期 。默认yyyy-MM-dd格式。失败返回null。
     *
     * @param date 日期字符串
     * @return 日期
     */
    public static String getDate(String date) {
        return stringToString(date, DateStyle.YYYY_MM_DD);
    }

    /**
     * 获取日期。默认yyyy-MM-dd格式。失败返回null。
     *
     * @param date 日期
     * @return 日期
     */
    public static String getDate(Date date) {
        return dateToString(date, DateStyle.YYYY_MM_DD);
    }

    /**
     * 获取日期的时间。默认HH:mm:ss格式。失败返回null。
     *
     * @param date 日期字符串
     * @return 时间
     */
    public static String getTime(String date) {
        return stringToString(date, DateStyle.HH_MM_SS);
    }

    /**
     * 获取日期的时间。默认HH:mm:ss格式。失败返回null。
     *
     * @param date 日期
     * @return 时间
     */
    public static String getTime(Date date) {
        return dateToString(date, DateStyle.HH_MM_SS);
    }

    /**
     * 获取两个日期相差的天数
     *
     * @param date      日期字符串
     * @param otherDate 另一个日期字符串
     * @return 相差天数。如果失败则返回-1
     */
    public static int getIntervalDays(String date, String otherDate) {
        return getIntervalDays(stringToDate(date), stringToDate(otherDate));
    }

    /**
     * @param date      日期
     * @param otherDate 另一个日期
     * @return 相差天数。如果失败则返回-1
     */
    public static int getIntervalDays(Date date, Date otherDate) {
        int num = -1;
        Date dateTmp = DateUtils.stringToDate(DateUtils.getDate(date),
                DateStyle.YYYY_MM_DD);
        Date otherDateTmp = DateUtils.stringToDate(DateUtils.getDate(otherDate),
                DateStyle.YYYY_MM_DD);
        if (dateTmp != null && otherDateTmp != null) {
            long time = Math.abs(dateTmp.getTime() - otherDateTmp.getTime());
            num = (int) (time / (24 * 60 * 60 * 1000));
        }
        return num;
    }

    /**
     * @param date      日期
     * @param otherDate 另一个日期
     * @return 相差分钟数。如果失败则返回-1
     */
    public static int getIntervalMinute(Date date, Date otherDate) {
        int num = -1;
        if (date != null && otherDate != null) {
            long time = Math.abs(date.getTime() - otherDate.getTime());
            num = (int) (time / (1000 * 60));
        }
        return num;
    }


    /**
     * Description :获取当前时间yyyyMMddHHmmss字符串
     *
     * @return String 当前时间yyyyMMddHHmmss字符串
     */
    public static String getSecondString() {
        return dateToString(new Date(), DateStyle.YYYYMMDDHHMMSS);
    }

    /**
     * Description : 获取当前时间yyyyMMddHHmmssSSS字符串
     *
     * @return String 当前时间yyyyMMddHHmmssSSS字符串
     */
    public static String getMillisecondString() {
        return dateToString(new Date(), DateStyle.YYYYMMDDHHMMSSSSS);
    }


    /**
     * 比较两个日期的大小
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 1 date1 > date2; -1 date1 < date2 ; 0 date1 = date2
     */
    public static int compareDate(String date1, String date2) {
        Date dt1 = stringToDate(date1);
        Date dt2 = stringToDate(date2);
        return compareDate(dt1, dt2);
    }

    /**
     * 比较两个日期的大小
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 1 date1 > date2; -1 date1 < date2 ; 0 date1 = date2
     */
    public static int compareDate(Date date1, Date date2) {

        if(date1 == null || date2 == null) return 0;
        if (date1.getTime() > date2.getTime()) {
            return 1;
        } else if (date1.getTime() < date2.getTime()) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * Description:[判断两个日期是否相等]
     *
     * @param d1 日期1
     * @param d2 日期2
     * @return boolean 相等true
     * <p>
     * Created on 2019/4/17
     * @author: yinguijin
     */
    public static boolean isSameDate(Date d1, Date d2) {
        if (null == d1 || null == d2)
            return false;
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);
        return cal1.get(0) == cal2.get(0) && cal1.get(1) == cal2.get(1) && cal1.get(6) == cal2.get(6);
    }

    /**
     * Description:[获取两个时间的时间差]
     *
     * @param date1 时间一
     * @param date2 时间二
     * @param type  获取的类型  天 时 分 秒
     * @return 时间一 减 时间二 的值
     * <p>
     * Created on 2019/4/17
     * @author: yinguijin
     */
    public static int dayDiff(Date date1, Date date2, int type) {
        long diff = date1.getTime() - date2.getTime();
        int nd = 1000 * 24 * 60 * 60;
        int nh = 1000 * 60 * 60;
        int nm = 1000 * 60;
        int ns = 1000;
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff / nh;
        // 计算差多少分钟
        long min = diff / nm;
        // 计算差多少秒
        long sec = diff / ns;
        if (Calendar.DATE == type) {
            return (int) day;
        } else if (Calendar.HOUR_OF_DAY == type) {
            return (int) hour;
        } else if (Calendar.MINUTE == type) {
            return (int) min;
        } else if (Calendar.SECOND == type) {
            return (int) sec;
        } else {
            return (int) diff;
        }
    }

    /**
     * Description:[根据参数时间获取前推的整点时间：列2017-11-22 17:13返回17:00；列2017-11-22 17:59返回17:30]
     *
     * @param date 时间
     * @return 列2017-11-22 17:13返回17:00；列2017-11-22 17:59返回17:30
     * <p>
     * Created on 2019/4/17
     * @author: yinguijin
     */
    public static Date wholeHour(Date date) {
        int hour = DateUtils.getHour(date);
        int minute = DateUtils.getMinute(date);
        if (minute >= 30) {
            minute = 30;
        } else {
            minute = 00;
        }
        String endTime = hour + ":" + minute;
        return DateUtils.stringToDate(endTime, DateStyle.HH_MM);
    }


    /**
     * <p>Description:[计算两个日期相差的月数，不足15天按照半个月计算，超过15天按照一个月计算]</p>
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 相差的月数0.5的倍数
     * Created on 2019/4/17
     * @author: yinguijin
     */
    public static double getMonths(Date startDate, Date endDate) {
        int startYear = DateUtils.getYear(startDate);
        int endYear = DateUtils.getYear(endDate);
        int startMonth = DateUtils.getMonth(startDate);
        int endMonth = DateUtils.getMonth(endDate);
        int startDay = DateUtils.getDay(startDate);
        int endDay = DateUtils.getDay(endDate);
        int endLastDay = DateUtils.getLastDay(endDate);
        int startLastDay = DateUtils.getLastDay(startDate);
        int intervalYears = endYear - startYear;
        double intervalMonths;
        int intervalDays;
        if (intervalYears >= 1) {
            intervalDays = (startLastDay - startDay) + endDay + 1;
            intervalMonths = (12.0 - startMonth) + endMonth + 1;
            if (startDay == 1 && endDay == endLastDay) {
                intervalDays = 0;
            } else {
                if (startDay != 1) {
                    intervalMonths -= 1.0;
                } else {
                    intervalDays = endDay;
                }
                if (endDay != endLastDay) {
                    intervalMonths -= 1.0;
                } else {
                    intervalDays = startLastDay - startDay + 1;
                }
            }
        } else {
            intervalMonths = endMonth - startMonth + 1.0;
            if (intervalMonths > 1.0) {
                intervalDays = (startLastDay - startDay) + endDay + 1;
                if (startDay == 1 && endDay == endLastDay) {
                    intervalDays = 0;
                } else {
                    if (startDay != 1) {
                        intervalMonths -= 1.0;
                    } else {
                        intervalDays = endDay;
                    }
                    if (endDay != endLastDay) {
                        intervalMonths -= 1.0;
                    } else {
                        intervalDays = startLastDay - startDay + 1;
                    }
                }
                intervalMonths = intervalMonths < 0.0 ? 0.0 : intervalMonths;
            } else {
                intervalMonths = 0.0;
                intervalDays = endDay - startDay + 1;
            }

        }
        if (intervalDays == 0) {
            intervalMonths += 0.0;
        } else if (intervalDays <= 15) {
            intervalMonths += 0.5;
        } else if (intervalDays >= 31) {
            intervalMonths += 1.0;
            if (intervalDays - 31 > 15) {
                intervalMonths += 1.0;
            } else if (intervalDays - 31 >= 1) {
                intervalMonths += 0.5;
            }
        } else {
            intervalMonths += 1.0;
        }
        for (int i = 1; i < intervalYears; i++) {
            intervalMonths += 12.0;
        }
        return intervalMonths;
    }


    /**
     * <p>Description:[计算当前时间所处的拆分时间段]</p>
     *
     * @return 拆分时间段Integer值
     * Created on 2019/4/17
     * @author: yinguijin
     */
    public static Integer getSplitTimeByNow() {
        return getSplitTime(new Date());
    }

    /**
     * <p>Description:[计算时间所处的拆分时间段]</p>
     *
     * @return 拆分时间段Integer值
     * Created on 2019/4/17
     * @author: yinguijin
     */
    public static Integer getSplitTime(Date date) {
        int hour = DateUtils.getHour(date);
        int minute = DateUtils.getMinute(date);
        if (minute > 0 && minute < 30) {
            minute = 30;
        }
        int splitTime = hour * 2 + 1;
        if (minute >= 30) {
            splitTime += 1;
        }
        return splitTime;
    }

    /**
     * Description:[获取当前月的第一天的日期]
     *
     * @return 当前月第一天
     * <p>
     * Created on 2019/4/17
     * @author: yinguijin
     */
    public static Date getfirstDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
        return stringToDate(getDateFormat(DateStyle.YYYY_MM_DD.getValue()).format(c.getTime()));
    }

    /**
     * Description:[获取当前月的最后一天的日期]
     *
     * @return 当前月的最后一天
     * <p>
     * Created on 2019/4/17
     * @author: yinguijin
     */
    public static Date getlastDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        return stringToDate(getDateFormat(DateStyle.YYYY_MM_DD.getValue()).format(c.getTime()));
    }

    /**
     * Description:[获取指定月的最后一天的日期]
     *
     * @return 指定的最后一天
     * <p>
     * Created on 2019/4/17
     * @author: yinguijin
     */
    public static int getLastDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * Description:[获取指定月的第一天的日期]
     *
     * @return 指定的第一天
     * <p>
     * Created on 2019/4/17
     * @author: yinguijin
     */
    public static int getFirstDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.getActualMinimum(Calendar.DAY_OF_MONTH);
    }

    /**
     * <p>Description:[获取当前日期字符串]</p>
     * Created on 2019/4/17
     *
     * @author: yinguijin
     */
    public static String getCurrentDateString() {
        return dateToString(new Date(), DateStyle.YYYY_MM_DD);
    }

    /**
     * <p>Description:[获取当前日期]</p>
     * Created on 2019/4/17
     *
     * @author: yinguijin
     */
    public static Date getCurrentDate() {
        return stringToDate(getCurrentDateString(), DateStyle.YYYY_MM_DD);
    }


    /**
     * <p>Description:[获取某月最大日期]</p>
     * Created on 2019/4/17
     *
     * @author: yinguijin
     */
    public static int getMonthMaxDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getYear(date), getMonth(date) - 1, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * <p>Discription:[根据传入的两个时间计算相差几个小时，结果保留一位小数]</p>
     *
     * @param dateLast date类型的时间1
     * @param dateNext date类型的时间2
     * @return Double 返回保留一位小数的绝对值
     * Created on 2019/4/17
     * @author: yinguijin
     */
    public static Double calculateHour(Date dateLast, Date dateNext) {
        long millisLast = dateLast.getTime();
        long millisNext = dateNext.getTime();
        double differenceMillis = (double) (millisNext - millisLast);
        double hourDouble = differenceMillis / 1000 / 60 / 60;
        hourDouble = BigDecimal.valueOf(hourDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        return Math.abs(hourDouble);
    }

    /**
     * 判断一个时间是否在一个时间段之间
     *
     * @param date      为目标时间
     * @param startDate 为起始时间
     * @param endDate   为结束时间
     * @return true or false
     * Created on 2019/4/17
     * @author: yinguijin
     */
    public static boolean getInDate(String date, String startDate, String endDate) {
        boolean flag = false;
        Date targetTime = DateUtils.stringToDate(date, DateStyle.HH_MM);
        Date startTime = DateUtils.stringToDate(startDate, DateStyle.HH_MM);
        Date endTime;
        if (endDate.equals("24:00")) {
            endTime = addDay(DateUtils.stringToDate("00:00", DateStyle.HH_MM), 1);
        } else {
            endTime = DateUtils.stringToDate(endDate, DateStyle.HH_MM);
        }

        //目标时间大于等于开始时间 且 目标时间小于结束时间 时返回true
        if (targetTime != null && targetTime.compareTo(startTime) >= 0 && targetTime.compareTo(endTime) < 0) {
            flag = true;
        }
        return flag;
    }

    /**
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return java.lang.String 时段名称
     * @description: 获取时段名称
     * @author yinguijin
     * @date 2019/5/5 20:12
     */
    public static String getSectimeName(String startTime, String endTime) {
        String secname = "平峰";
        String st = startTime.split(":")[0];
        String et = endTime.split(":")[0];
        if (StringUtils.isEmpty(st) || StringUtils.isEmpty(et)) {
            return secname;
        }
        int s = Integer.parseInt(st);
        int e = Integer.parseInt(et);
        if (s >= 0 && e <= 6) {
            secname = "低峰";
        } else if (s >= 6 && e <= 7) {
            secname = "早平峰";
        } else if (s >= 7 && e <= 9) {
            secname = "早高峰";
        } else if (s >= 9 && e <= 12) {
            secname = "平峰";
        } else if (s >= 12 && e <= 14) {
            secname = "次平峰";
        } else if (s >= 14 && e <= 17) {
            secname = "平峰";
        } else if (s >= 17 && e <= 19) {
            secname = "晚高峰";
        } else if (s >= 19 && e <= 22) {
            secname = "次平峰";
        } else if (s >= 22 && e <= 24) {
            secname = "低峰";
        }
        return secname;
    }
    /**
     * @param cntDateBeg 开始时间
     * @param cntDateEnd 结束时间
     * @return
     */
    public static List<String> addDates(String cntDateBeg, String cntDateEnd) {
        List<String> list = new ArrayList<>();
        //拆分成数组
        String[] dateBegs = cntDateBeg.split("-");
        String[] dateEnds = cntDateEnd.split("-");
        //开始时间转换成时间戳
        Calendar start = Calendar.getInstance();
        start.set(Integer.valueOf(dateBegs[0]), Integer.valueOf(dateBegs[1]) - 1, Integer.valueOf(dateBegs[2]));
        Long startTIme = start.getTimeInMillis();
        //结束时间转换成时间戳
        Calendar end = Calendar.getInstance();
        end.set(Integer.valueOf(dateEnds[0]), Integer.valueOf(dateEnds[1]) - 1, Integer.valueOf(dateEnds[2]));
        Long endTime = end.getTimeInMillis();
        //定义一个一天的时间戳时长
        Long oneDay = 1000 * 60 * 60 * 24l;
        Long time = startTIme;
        //循环得出
        while (time <= endTime) {
            list.add(new SimpleDateFormat("yyyy-MM-dd").format(new Date(time)));
            time += oneDay;
        }
        return list;
    }
}