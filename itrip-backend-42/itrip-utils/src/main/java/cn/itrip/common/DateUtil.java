package cn.itrip.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 处理日期类型变量的工具类
 * @author donghai
 * @version v1.0
 * @since 2017/04/20
 */
public class DateUtil {
    /**
     * 获取两个日期之间的日期
     * @param start 开始日期
     * @param end 结束日期
     * @return 日期字符串格式的集合
     */
    public static List<Date> getBetweenDates(Date start, Date end) {
        List<Date> result = new ArrayList<Date>();

        //把用户传入的入住时间 转换为毫秒  2019-07-10
        Calendar tempStart = Calendar.getInstance();
        tempStart.setTime(start);

        // //把用户传入的退房时间 转换为毫秒  2019-07-15
        Calendar tempEnd = Calendar.getInstance();
        tempEnd.setTime(end);

        //2019-12-20  -  2019-12-30
        while (tempStart.before(tempEnd)) {
            result.add(tempStart.getTime());
            //把当前天数+1
            tempStart.add(Calendar.DAY_OF_YEAR, 1);
        }
        return result;
    }

    /**
     * 根据日期字符串返回日期
     * @param source
     * @param format
     * @return
     * @throws ParseException
     */
    public static final Date parse(String source,String format) throws ParseException {
        DateFormat df = new SimpleDateFormat(format);
        return df.parse(source);
    }

    /**
     * 根据日期获取格式化的日期字符串
     * @param date
     * @param format
     * @return
     * @throws ParseException
     */
    public static final String format(Date date,String format) throws ParseException {
        DateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }

}
