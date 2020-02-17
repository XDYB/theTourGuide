package com.ysq.theTourGuide.entity;

import java.util.Date;
import javax.persistence.*;
import lombok.Data;

@Data
public class Comment {
    private Long id;

    /**
     * 评论的视频id
     */
    @Column(name = "video_id")
    private Long videoId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 游客id
     */
    @Column(name = "tourist_id")
    private Long touristId;

    /**
     * 点赞数
     */
    @Column(name = "like_nums")
    private String likeNums;

    /**
     * 发布时间
     */
    private Date createtime;

    /**
     * 状态（0为待审核，1通过，2不通过）
     */
    private Integer state;

    public static final String ID = "id";

    public static final String VIDEO_ID = "videoId";

    public static final String CONTENT = "content";

    public static final String TOURIST_ID = "touristId";

    public static final String LIKE_NUMS = "likeNums";

    public static final String CREATETIME = "createtime";

    public static final String STATE = "state";
}