package com.iminling.common.date;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * 日期工具类 java8 api: https://blog.csdn.net/chenxun_2010/article/details/72539981
 */
public class DateUtils {

    /**
     * 默认完整日期-时间格式
     */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * 默认日期格式
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    /**
     * 默认时间格式
     */
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public static final int SECOND_MINI = 0; //最小的时间戳
    public static final int SECOND_OF_MINUTE = 60;
    public static final int SECOND_OF_HOUR = 60 * SECOND_OF_MINUTE;
    public static final int SECOND_OF_DAY = 24 * SECOND_OF_HOUR;//一天的秒数
    public static final int SECOND_OF_MONTH = 30 * SECOND_OF_DAY; //一个月这里按固定30天算
    public static final int SECOND_OF_YEAR = 12 * SECOND_OF_MONTH;//一年的秒数

    /**
     * 返回unix时间戳 (1970年至今的秒数)
     *
     * @return 毫秒数字
     */
    public static Long getUnixStamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 得到昨天的日期
     *
     * @return yyyy-MM-dd
     */
    public static String getYesterdayDate() {
        LocalDate localDate = LocalDate.now();
        localDate = localDate.minusDays(1);
        return localDate.toString();
    }

    /**
     * 时间戳转化为时间格式
     *
     * @param timeStamp 纳秒数字
     * @return yyyy-MM-dd
     */
    public static String timeStampToStr(long timeStamp) {
        return timeStampToStr(timeStamp, DEFAULT_DATE_FORMAT);
    }

    /**
     * 时间戳转对应格式
     *
     * @param timeStamp 时间戳
     * @param format    时间格式
     * @return 对应时间格式
     */
    public static String timeStampToStr(long timeStamp, String format) {
        Instant instant = Instant.ofEpochSecond(timeStamp);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 得到时间  HH:mm:ss
     *
     * @param timeStamp 时间戳
     * @return HH:mm:ss
     */
    public static String getTime(long timeStamp) {
        Instant instant = Instant.ofEpochSecond(timeStamp);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        LocalTime localTime = localDateTime.toLocalTime();
        return localTime.toString();
    }

    /**
     * 将一个时间戳转换成提示性时间字符串，如刚刚，1秒前
     *
     * @param timeStamp 毫秒数字
     * @return {@link String}
     */
    public static String convertTimeToFormat(long timeStamp) {
        long curTime = getUnixStamp();
        long time = curTime - timeStamp;
        if (time < SECOND_OF_MINUTE && time >= SECOND_MINI) {
            return "刚刚";
        } else if (time >= SECOND_OF_MINUTE && time < SECOND_OF_HOUR) {
            return time / SECOND_OF_MINUTE + "分钟前";
        } else if (time >= SECOND_OF_HOUR && time < SECOND_OF_DAY) {
            return time / SECOND_OF_HOUR + "小时前";
        } else if (time >= SECOND_OF_DAY && time < SECOND_OF_MONTH) {
            return time / SECOND_OF_DAY + "天前";
        } else if (time >= SECOND_OF_MONTH && time < SECOND_OF_YEAR) {
            return time / SECOND_OF_MONTH + "个月前";
        } else if (time >= SECOND_OF_YEAR) {
            return time / SECOND_OF_YEAR + "年前";
        } else {
            return "刚刚";
        }
    }

    /**
     * 获取当天00:00:00时间戳
     *
     * @return {@link Long} 单位秒
     */
    public static Long getDayStartTime() {
        LocalDateTime localDateTime = LocalDateTime.now()
            .withHour(0)
            .withMinute(0)
            .withSecond(0);
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    /**
     * 计算两个时间戳之间相隔的天数
     *
     * @param begin 开始时间 秒
     * @param end   结束时间 秒
     * @return 相差天数
     */
    public static int getIntervalDays(Long begin, Long end) {
        LocalDateTime beginDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(begin), ZoneId.systemDefault());
        LocalDateTime endDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(end), ZoneId.systemDefault());
        Duration duration = Duration.between(beginDate, endDate);
        return Long.valueOf(duration.toDays()).intValue();
    }

}
