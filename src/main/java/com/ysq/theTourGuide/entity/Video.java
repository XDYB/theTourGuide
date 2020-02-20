package com.ysq.theTourGuide.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Video {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    /**
     * 导游id
     */
    @Column(name = "guide_id")
    private Long guideId;

    /**
     * 路线id
     */
    @Column(name = "route_id")
    private Long routeId;


    /**
     * 景区id
     */
    @Column(name = "scenic_id")
    private Long scenicId;

    /**
     * 视频地址
     */
    @Column(name = "video_url")
    private String videoUrl;

    /**
     * 点赞数
     */
    @Column(name = "like_nums")
    private Integer likeNums;

    /**
     * 描述
     */
    private String vDescribe;

    public static final String ID = "id";

    public static final String GUIDE_ID = "guideId";

    public static final String SCENIC_ID = "scenicId";

    public static final String VIDEO_URL = "videoUrl";

    public static final String LIKE_NUMS = "likeNums";

    public static final String VDESCRIBE = "vDescribe";

    public Video(Long guideId){
        this.guideId = guideId;
    }
}