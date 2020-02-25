package com.ysq.theTourGuide.utils;


import org.springframework.data.geo.Point;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MyMathUtil {

    public static Double getTwoPointDist(Point p1,Point p2){
        double EARTH_RADIUS = 6378.137;
        double firstRadianLongitude = p1.getX();
        double firstRadianLatitude = p1.getY();
        double secondRadianLongitude = p2.getX();
        double secondRadianLatitude = p2.getY();
        double a = firstRadianLatitude - secondRadianLatitude;
        double b = firstRadianLongitude - secondRadianLongitude;
        double cal = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(firstRadianLatitude) * Math.cos(secondRadianLatitude)
                * Math.pow(Math.sin(b / 2), 2)));
        cal = cal * EARTH_RADIUS;

        return Math.round(cal * 10000d) / 10000d;
    }

    public static List<Date> returnSelectDays(Date startDay,Integer day){
        List<Date> dates = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.setTime(startDay);
        c.add(Calendar.DAY_OF_MONTH, -1);
        Date previousDay = c.getTime();
        dates.add(previousDay);
        dates.add(startDay);
        Date today = startDay;
        if(day != 0){
            for(int i = 0; i < day; i++){
                c.setTime(today);
                c.add(Calendar.DAY_OF_MONTH,+1);
                today = c.getTime();
                dates.add(today);
            }
        }
        return dates;
    }

    public static String getTime(Date time,Integer l){
        Calendar c = Calendar.getInstance();
        c.setTime(time);
        c.add(Calendar.DAY_OF_MONTH,l);
        Date lastDay = c.getTime();
        DateFormat df = DateFormat.getDateInstance();
        return df.format(time) + " - "  + df.format(lastDay);
    }
}
