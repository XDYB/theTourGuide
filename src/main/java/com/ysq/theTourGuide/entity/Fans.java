package com.ysq.theTourGuide.entity;

import javax.persistence.*;
import lombok.Data;

@Data
public class Fans {
    private Long id;

    /**
     * 游客id
     */
    @Column(name = "tourist_id")
    private Long touristId;

    /**
     * 喜欢的导游id
     */
    @Column(name = "guide_id")
    private Long guideId;

    public static final String ID = "id";

    public static final String TOURIST_ID = "touristId";

    public static final String GUIDE_ID = "guideId";
}