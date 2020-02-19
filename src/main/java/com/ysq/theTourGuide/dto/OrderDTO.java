package com.ysq.theTourGuide.dto;

import com.ysq.theTourGuide.base.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO extends BaseDTO {
    private Long id;
    private Long touristId;
    private String title;
    private Long guideId;
    private String start;
    private Integer nOP;
    private String time;
    private String meetTime;
    private String name;
    private String idNumber;
    private String phone;

}
