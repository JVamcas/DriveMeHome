package com.owambo.jvamcas.drivemehome.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static String ICON_PATH_PATTERN = ".*(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}).*";

    public static String today(){
        return new SimpleDateFormat(DATE_FORMAT,Locale.US).format(new Date());
    }

    public static Date parseDate(String date){
        try {
            return new SimpleDateFormat(DATE_FORMAT, Locale.US).parse(date);
        } catch (ParseException ignore) {
        }return null;
    }
}
