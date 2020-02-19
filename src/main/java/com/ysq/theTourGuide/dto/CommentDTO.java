package com.ysq.theTourGuide.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {

    private String avatar_url;
    private String nickname;
    private String content;
    private Date time;
    private boolean isLike;
}
