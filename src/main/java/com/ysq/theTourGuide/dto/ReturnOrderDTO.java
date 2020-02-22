package com.ysq.theTourGuide.dto;

import com.ysq.theTourGuide.entity.Guide;
import com.ysq.theTourGuide.entity.TheOrder;
import com.ysq.theTourGuide.entity.Tourist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnOrderDTO {

    private TheOrder theOrder;

    private Guide guide;

    private Tourist tourist;

}
