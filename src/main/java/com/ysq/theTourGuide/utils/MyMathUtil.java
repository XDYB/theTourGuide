package com.ysq.theTourGuide.utils;


import org.springframework.data.geo.Point;

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
}
