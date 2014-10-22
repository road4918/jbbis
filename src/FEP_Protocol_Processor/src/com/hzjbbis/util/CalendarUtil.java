package com.hzjbbis.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * ���ڹ�����
 * @author ������
 */
public class CalendarUtil {

    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat shortDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat shortTimeFormat = new SimpleDateFormat("HH:mm");

    /**
     * ȡ�õ��������ʱ��
     * @return ��ǰ����
     */
    public static Calendar getBeginOfToday() {
        Calendar date = Calendar.getInstance();
        clearTimePart(date);
        return date;
    }
    
    /**
     * ȡ�õ��������ʱ��
     * @return ��ǰ����
     */
    public static Calendar getEndOfToday() {
        Calendar date = Calendar.getInstance();
        setLastTimeOfDay(date);
        return date;
    }
    
    /**
     * ������ڵ�ʱ�䲿�֣�����ʱ���֡��롢���벿������
     * @param date ��Ҫ���ʱ�������
     * @return �����ʱ�䲿�ֺ������
     */
    public static Calendar clearTimePart(Calendar date) {
        //date.clear(Calendar.HOUR);		//clear���������⣬ֻ���ڸǣ���û��ʵ�����㡣by yangdh---2007/03/22
        date.set(Calendar.HOUR_OF_DAY,0);
        date.set(Calendar.MINUTE,0);
        date.set(Calendar.SECOND,0);
        date.set(Calendar.MILLISECOND,0);
        //date.clear(Calendar.AM_PM);
        return date;
    }
    
    /**
     * �������ڵ�ʱ�䲿��Ϊ��������ʱ�䣬�� 23:59:59.999
     * @param date ��Ҫ����ʱ�������
     * @return ������ʱ��������
     */
    public static Calendar setLastTimeOfDay(Calendar date) {
        date.set(Calendar.AM_PM, Calendar.PM);
        date.set(Calendar.HOUR_OF_DAY, 23);
        date.set(Calendar.MINUTE, 59);
        date.set(Calendar.SECOND, 59);
        date.set(Calendar.MILLISECOND, 999);
        return date;
    }
    
    /**
     * ȡ��ĳһ��ĵ�һ��
     * @param year ���
     * @return ĳһ��ĵ�һ�졣�� 2000-1-1 00:00:00.000
     */
    public static Calendar getFirstDayOfYear(int year) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, 0);
        date.set(Calendar.DATE, 1);
        clearTimePart(date);
        
        return date;
    }
    
    /**
     * ȡ��ĳһ������һ��
     * @param year ���
     * @return ĳһ������һ�졣�� 2000-12-31 23:59:59.999
     */
    public static Calendar getLastDayOfYear(int year) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, 11);
        date.set(Calendar.DATE, 31);
        date.set(Calendar.HOUR, 11);
        setLastTimeOfDay(date);
        
        return date;
    }
    
    /**
     * ���������ַ��������ڸ�ʽ������ yyyy-MM-dd HH:mm:ss��yyyy-MM-dd HH:mm��yyyy-MM-dd��
     * HH:mm:ss��HH:mm �е��κ�һ��
     * @param val �����ַ���
     * @return Calendar ����
     */
    public static Calendar parse(String val) {
        if (val == null) {
            return null;
        }
        
        try {
            Date date = null;
            String s = val.trim();
            int indexOfDateDelim = s.indexOf("-");
            int indexOfTimeDelim = s.indexOf(":");
            int indexOfTimeDelim2 = s.indexOf(":", indexOfTimeDelim + 1);
            if (indexOfDateDelim < 0 && indexOfTimeDelim > 0) {
                if (indexOfTimeDelim2 > 0) {
                    date = timeFormat.parse(s);
                }
                else {
                    date = shortTimeFormat.parse(s);
                }
            }
            else if (indexOfDateDelim > 0 && indexOfTimeDelim < 0) {
                date = dateFormat.parse(s);
            }
            else {
                if (indexOfTimeDelim2 > 0) {
                    date = dateTimeFormat.parse(s);
                }
                else {
                    date = shortDateTimeFormat.parse(s);
                }
            }
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        }
        catch (ParseException ex) {
            throw new IllegalArgumentException(val + " is invalid date format");
        }
    }
    
    /**
     * ����һ�����ڵ�ʱ�䲿�ֵĺ�����
     * @param time ʱ��
     * @return ʱ��ĺ�����
     */
    public static long getTimeMillis(Calendar time) {
        if (time == null) {
            return 0L;
        }	
        //by yangdh  2007-11-19  ��ʾת��int to long
        return ((long)time.get(Calendar.HOUR_OF_DAY) * 3600000L
                + (long)time.get(Calendar.MINUTE) * 60000L
                + (long)time.get(Calendar.SECOND) * 1000L
                + (long)time.get(Calendar.MILLISECOND));
    }
    
    /**
     * �Ƚ������ճ�ʱ�䲿�ֵĴ�С
     * @param time1 ʱ��1
     * @param time2 ʱ��2
     * @return -1, 0 �� 1����� time1 < time2, time1 == time2 �� time1 > time2
     */
    public static int compareTime(Calendar time1, Calendar time2) {
        long ms1 = getTimeMillis(time1);
        long ms2 = getTimeMillis(time2);
        if (ms1 == ms2) {
            return 0;
        }
        else if (ms1 > ms2){
            return 1;
        }
        else {
            return -1;
        }
    }
    
    /**
     * ���������ַ��������ڸ�ʽ������ yyyy-MM-dd HH:mm:ss��yyyy-MM-dd HH:mm��yyyy-MM-dd��
     * HH:mm:ss��HH:mm �е��κ�һ��
     * @param val �����ַ���
     * @param defaultValue ȱʡֵ����� val �Ƿ�����ʹ�ø�ֵ
     * @return Calendar ����
     */
    public static Calendar parse(String val, Calendar defaultValue) {
        try {
            return parse(val);
        }
        catch (Exception ex) {
            return defaultValue;
        }
    }
}
