package com.ysq.theTourGuide.controller;

import com.ysq.theTourGuide.base.dto.ResultDTO;
import com.ysq.theTourGuide.base.util.ResultUtil;
import com.ysq.theTourGuide.dto.GuideResiterDTO;
import com.ysq.theTourGuide.entity.*;
import com.ysq.theTourGuide.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
public class GuideController {

    @Autowired
    GuideService guideService;

    @Autowired
    RouteService routeService;

    @Autowired
    VideoService videoService;

    @Autowired
    LikeVideoService likeVideoService;

    @Autowired
    OrderService orderService;
    /**
     * 注册成为导游
     * @param guide
     * @return
     * @throws Exception
     */
    @PostMapping("/toBeAGuide")
    public ResultDTO toBeAGuide(GuideResiterDTO guide) throws Exception{
        return ResultUtil.Success(guideService.saveDTO(guide, Guide.class));
    }

    /**
     * 发布信息
     * @param route
     * @return
     */
    @PostMapping("/postMsg")
    public ResultDTO postMsg(Route route, HttpServletRequest request)throws Exception{
        Long guideId = guideService.findByParams(new Guide((Long)request.getSession().getAttribute("touristId"))).get(0).getId();
        route.setGuideId(guideId);
        return ResultUtil.Success(routeService.save(route));
    }

    @PostMapping("/postVideo")
    public ResultDTO postVideo(Video video,HttpServletRequest request)throws Exception{
        Long guideId = guideService.findByParams(new Guide((Long)request.getSession().getAttribute("touristId"))).get(0).getId();
        video.setGuideId(guideId);
        return ResultUtil.Success(videoService.save(video));
    }

    @GetMapping("/getGuideMsg")
    public ResultDTO getGuideMsg(HttpServletRequest request)throws Exception{
        Long guideId = guideService.findByParams(new Guide((Long)request.getSession().getAttribute("touristId"))).get(0).getId();

        return ResultUtil.Success(guideService.get(guideId));
    }

    @GetMapping("/getVideo")
    public ResultDTO getVideo(HttpServletRequest request)throws Exception{
        Long guideId = guideService.findByParams(new Guide((Long)request.getSession().getAttribute("touristId"))).get(0).getId();

        return ResultUtil.Success(videoService.findByParams(new Video(guideId)));
    }

    @GetMapping("/getLike")
    public ResultDTO getLike(HttpServletRequest request)throws Exception{
        Long touristId = (Long)request.getSession().getAttribute("touristId");
        LikeVideo likeVideo = new LikeVideo();
        likeVideo.setTouristId(touristId);
        List<Video> videoList = new ArrayList<>();
        for(LikeVideo l:likeVideoService.findByParams(likeVideo)){
            videoList.add(videoService.get(l.getLikeVideoId()));
        }
        return ResultUtil.Success(videoList);
    }

    @GetMapping("/getMyOrder")
    public ResultDTO getMyOrder(HttpServletRequest request)throws Exception {
        Long guideId = guideService.findByParams(new Guide((Long) request.getSession().getAttribute("touristId"))).get(0).getId();
        return ResultUtil.Success(orderService.findByParams(new Order(guideId)));
    }
}
