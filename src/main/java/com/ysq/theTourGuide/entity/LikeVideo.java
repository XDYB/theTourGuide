package com.ysq.theTourGuide.entity;

import javax.persistence.*;
import lombok.Data;

@Data
@Table(name = "like_video")
public class LikeVideo {
    private Long id;

    /**
     * 导游id
     */
    @Column(name = "guide_id")
    private Integer guideId;

    /**
     * 喜欢的视频id
     */
    @Column(name = "like_video_id")
    private Long likeVideoId;

    public static final String ID = "id";

    public static final String GUIDE_ID = "guideId";

    public static final String LIKE_VIDEO_ID = "likeVideoId";
}