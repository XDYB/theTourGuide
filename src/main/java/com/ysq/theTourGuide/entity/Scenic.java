package com.ysq.theTourGuide.entity;

import lombok.Data;

import javax.persistence.Id;

@Data
public class Scenic {
    @Id
    private Long id;

    /**
     * 景区名字
     */
    private String name;

    /**
     * 标题
     */
    private String title;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    public static final String ID = "id";

    public static final String NAME = "name";

    public static final String TITLE = "title";

    public static final String PROVINCE = "province";

    public static final String CITY = "city";

    public static final String LONGITUDE = "longitude";

    public static final String LATITUDE = "latitude";
}