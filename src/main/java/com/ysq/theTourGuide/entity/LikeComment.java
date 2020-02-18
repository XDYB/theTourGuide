package com.ysq.theTourGuide.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "like_comment")
@AllArgsConstructor
@NoArgsConstructor
public class LikeComment {
    @Id
    private Long id;

    @Column(name = "tourist_id")
    private Long touristId;

    @Column(name = "comment_id")
    private Long commentId;

    public static final String ID = "id";

    public static final String TOURIST_ID = "touristId";

    public static final String COMMENT_ID = "commentId";

    public LikeComment(Long touristId,Long commentId){
        this.touristId = touristId;
        this.commentId = commentId;
    }
}