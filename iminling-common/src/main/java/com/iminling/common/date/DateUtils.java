package com.iminling.common.date;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * 日期工具类
 * java8 api: https://blog.csdn.net/chenxun_2010/article/details/72539981
 */
public class DateUtils {

    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /** 默认日期格式 */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    /** 默认时间格式 */
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public static final int minTimestamp = 0; //最小的时间戳
    public static final int maxTimestamp = 1999999999;//最大的时间戳
    public static final int secondOfHour = 3600;
    public static final int secondOfDay = 86400;//一天的秒数
    public static final int secondOfMonth = 2592000; //一个月这里按固定30天算
    public static final int offset = 0;

    /**
     * 返回unix时间戳 (1970年至今的秒数)
     * @return 毫秒数字
     */
    public static Long getUnixStamp(){
        return System.currentTimeMillis() /1000;
    }

    /**
     * 得到昨天的日期
     * @return yyyy-MM-dd
     */
    public static String getYesterdayDate() {
        LocalDate localDate = LocalDate.now();
        localDate = localDate.minusDays(1);
        return localDate.toString();
    }

    /**
     * 时间戳转化为时间格式
     * @param timeStamp 纳秒数字
     * @return yyyy-MM-dd
     */
    public static String timeStampToStr(long timeStamp) {
        return timeStampToStr(timeStamp,"yyyy-MM-dd");
    }

    /**
     * 时间戳转对应格式
     * @param timeStamp 时间戳
     * @param format 时间格式
     * @return 对应时间格式
     */
    public static String timeStampToStr(long timeStamp,String format){
        Instant instant = Instant.ofEpochSecond(timeStamp);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 得到时间  HH:mm:ss
     * @param timeStamp   时间戳
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

        if (time < 60 && time >= minTimestamp) {
            return "刚刚";
        } else if (time >= 60 && time < secondOfHour) {
            return time / 60 + "分钟前";
        } else if (time >= secondOfHour && time < secondOfDay) {
            return time / secondOfHour + "小时前";
        } else if (time >= secondOfDay && time < secondOfMonth) {
            return time / secondOfHour / 24 + "天前";
        } else if (time >= secondOfMonth && time < secondOfHour * 24 * 30 * 12) {
            return time / secondOfHour / 24 / 30 + "个月前";
        } else if (time >= secondOfHour * 24 * 30 * 12) {
            return time / secondOfHour / 24 / 30 / 12 + "年前";
        } else {
            return "刚刚";
        }
    }

    /**
     * 获取当天00:00:00时间戳
     * @return {@link Long}
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
     * @param begin 开始时间 纳秒
     * @param end 结束时间 纳秒
     * @return 相差天数
     */
    public static int getIntervalDays(Long begin, Long end){
        LocalDateTime beginDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(begin), ZoneId.systemDefault());
        LocalDateTime endDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(end), ZoneId.systemDefault());
        Duration duration = Duration.between(beginDate, endDate);
        return Integer.valueOf(duration.toDays()+"");
    }

}
