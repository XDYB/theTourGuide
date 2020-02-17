package com.ysq.theTourGuide.entity;

import javax.persistence.*;
import lombok.Data;

@Data
public class Tourist {
    private Long id;

    @Column(name = "open_id")
    private Integer openId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别
     */
    private String gender;

    /**
     * 头像
     */
    @Column(name = "avatar_url")
    private String avatarUrl;

    /**
     * 积分
     */
    private Integer score;

    /**
     * 城市
     */
    private String city;

    /**
     * 省份
     */
    private String province;

    /**
     * 国家
     */
    private String country;

    /**
     * 是否会员
     */
    @Column(name = "is_VIP")
    private Boolean isVip;

    /**
     * 是否导游
     */
    @Column(name = "is_guide")
    private Boolean isGuide;

    public static final String ID = "id";

    public static final String OPEN_ID = "openId";

    public static final String NICKNAME = "nickname";

    public static final String GENDER = "gender";

    public static final String AVATAR_URL = "avatarUrl";

    public static final String SCORE = "score";

    public static final String CITY = "city";

    public static final String PROVINCE = "province";

    public static final String COUNTRY = "country";

    public static final String IS_VIP = "isVip";

    public static final String IS_GUIDE = "isGuide";
}