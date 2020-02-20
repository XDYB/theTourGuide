package com.ysq.theTourGuide.dto;

import com.ysq.theTourGuide.entity.Guide;
import com.ysq.theTourGuide.entity.Route;
import com.ysq.theTourGuide.entity.Tourist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteDTO {

    private Guide guide;

    private Route route;

    private Tourist tourist;
}
