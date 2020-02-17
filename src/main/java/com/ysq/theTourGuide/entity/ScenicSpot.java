package com.ysq.theTourGuide.entity;

import javax.persistence.*;
import lombok.Data;

@Data
@Table(name = "scenic_spot")
public class ScenicSpot {
    private Long id;

    /**
     * 景区id
     */
    @Column(name = "scenic_id")
    private Long scenicId;

    /**
     * 景点名字
     */
    private String name;

    public static final String ID = "id";

    public static final String SCENIC_ID = "scenicId";

    public static final String NAME = "name";
}