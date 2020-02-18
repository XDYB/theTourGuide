package com.ysq.theTourGuide.dto;

import com.ysq.theTourGuide.base.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuideResiterDTO extends BaseDTO {

    private String name;

    private String phone;

    private String touristCertificateUrl;

    private Integer level;

    private String language;

    private String guide_number;

    private String organization;

    private Date date;
}
