package com.ysq.theTourGuide.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Video {
    @Id
    private Long id;

    /**
     * 导游id
     */
    @Column(name = "guide_id")
    private Long guideId;

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

    public static final String ID = "id";

    public static final String GUIDE_ID = "guideId";

    public static final String SCENIC_ID = "scenicId";

    public static final String VIDEO_URL = "videoUrl";

    public static final String LIKE_NUMS = "likeNums";

    public Video(Long guideId){
        this.guideId = guideId;
    }
}