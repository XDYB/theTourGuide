package com.ysq.theTourGuide.controller;

import com.ysq.theTourGuide.base.dto.ResultDTO;
import com.ysq.theTourGuide.base.util.ResultUtil;
import com.ysq.theTourGuide.config.ErrorCode;
import com.ysq.theTourGuide.dto.GuideDTO;
import com.ysq.theTourGuide.dto.GuideResiterDTO;
import com.ysq.theTourGuide.dto.MsgDTO;
import com.ysq.theTourGuide.entity.*;
import com.ysq.theTourGuide.service.*;
import com.ysq.theTourGuide.service.redis.GuideGeoService;
import com.ysq.theTourGuide.utils.Location;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class GuideController {

    @Autowired
    TouristService touristService;

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

    @Autowired
    GuideGeoService guideGeoService;

    @Autowired
    ScenicService scenicService;

    /**
     * 注册成为导游
     * @param guide
     * @return
     * @throws Exception
     */
    @PostMapping("/toBeAGuide")
    @ApiOperation("注册成为导游")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "姓名",name = "name",dataType = "String",paramType = "query"),
            @ApiImplicitParam(value = "电话",name = "phone",dataType = "String",paramType = "query"),
            @ApiImplicitParam(value = "导游证url",name = "touristCertificateUrl",dataType = "String",paramType = "query"),
            @ApiImplicitParam(value = "等级",name = "level",dataType = "int",paramType = "query"),
            @ApiImplicitParam(value = "语言",name = "language",dataType = "String",paramType = "query"),
            @ApiImplicitParam(value = "导游证号",name = "guide_number",dataType = "String",paramType = "query"),
            @ApiImplicitParam(value = "所属组织",name = "organization",dataType = "String",paramType = "query"),
            @ApiImplicitParam(value = "期限",name = "date",dataType = "String",paramType = "query"),
    })
    public ResultDTO toBeAGuide(GuideResiterDTO guide) throws Exception{
        Guide g = new Guide();
        g.setTouristId(guide.getTouristId());
        if(guideService.findByParams(g).size()!=0){
            return ResultUtil.Error(ErrorCode.ISEXIST);
        }
        Tourist tourist = new Tourist();
        tourist.setId(guide.getTouristId());
        tourist.setIsGuide(true);
        touristService.update(tourist);
        return ResultUtil.Success(guideService.save(new Guide(guide)));
    }

    /**
     * 发布信息
     * @param route
     * @param video
     * @param touristId
     * @return
     */
    @PostMapping("/postRoute")
    @ApiOperation("发布信息")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "路线",name = "line",dataType = "String",paramType = "query"),
            @ApiImplicitParam(value = "时长",name = "time",dataType = "String",paramType = "query"),
            @ApiImplicitParam(value = "景点个数",name = "noss",dataType = "String",paramType = "query"),
            @ApiImplicitParam(value = "经典景点个数",name = "nosss",dataType = "String",paramType = "query"),
            @ApiImplicitParam(value = "是否购物",name = "hShop",dataType = "Boolean",paramType = "query"),
            @ApiImplicitParam(value = "人数上限",name = "nOP",dataType = "int",paramType = "query"),
            @ApiImplicitParam(value = "价格",name = "price",dataType = "String",paramType = "query"),
            @ApiImplicitParam(value = "优惠类型id,减免为1，折扣为2",name = "discountTypeId",dataType = "int",paramType = "query"),
            @ApiImplicitParam(value = "优惠额度",name = "discountValue",dataType = "int",paramType = "query"),
            @ApiImplicitParam(value = "服务描述",name = "rDescribe",dataType = "String",paramType = "query"),
            @ApiImplicitParam(value = "路线id",name = "routeId",dataType = "Long",paramType = "query"),
            @ApiImplicitParam(value = "景区id",name = "scenicId",dataType = "Long",paramType = "query"),
            @ApiImplicitParam(value = "视频地址",name = "videoUrl",dataType = "String",paramType = "query"),
            @ApiImplicitParam(value = "视频描述",name = "vDescribe",dataType = "String",paramType = "query"),
    })
    public ResultDTO postMsg(Route route, Video video,Long touristId )throws Exception{
        Long guideId = guideService.findByParams(new Guide(touristId)).get(0).getId();
        route.setGuideId(guideId);
        video.setGuideId(guideId);
        Video saveVideo = videoService.save(video);
        route.setVideoId(saveVideo.getId());
        Route saveRoute = routeService.save(route);
        saveVideo.setRouteId(saveRoute.getId());
        videoService.update(saveVideo);
        return ResultUtil.Success(new MsgDTO(saveRoute,saveVideo));
    }



    /**
     * 获得导游信息
     * @param touristId
     * @return
     * @throws Exception
     */
    @GetMapping("/getGuideMsg")
    @ApiOperation("获得导游信息")
    public ResultDTO getGuideMsg(Long touristId )throws Exception{
        Long guideId = guideService.findByParams(new Guide(touristId)).get(0).getId();

        return ResultUtil.Success(guideService.get(guideId));
    }

    /**
     * 修改导游信息
     * @param guideDTO
     * @param touristId
     * @return
     * @throws Exception
     */
    @PostMapping("/updateGuideMsg")
    @ApiOperation("修改导游信息")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "姓名",name = "name",dataType = "Boolean",paramType = "query"),
            @ApiImplicitParam(value = "电话",name = "phone",dataType = "Boolean",paramType = "query"),
            @ApiImplicitParam(value = "导游证",name = "touristCertificateUrl",dataType = "Boolean",paramType = "query"),
            @ApiImplicitParam(value = "导游证号",name = "theGuideNumber",dataType = "Boolean",paramType = "query"),
            @ApiImplicitParam(value = "所在机构",name = "organization",dataType = "Boolean",paramType = "query"),
            @ApiImplicitParam(value = "期限",name = "date",dataType = "Boolean",paramType = "query"),
            @ApiImplicitParam(value = "导游年份",name = "years",dataType = "Boolean",paramType = "query"),
    })
    public ResultDTO updateGuideMsg(GuideDTO guideDTO,Long touristId)throws Exception{
        Guide guide = new Guide();
        guide.setTouristId(touristId);
        guideDTO.setId(guideService.findByParams(guide).get(0).getId());
        guideService.updateDTO(guideDTO,Guide.class);
        return ResultUtil.Success();
    }

    /**
     * 获得我（导游）的视频
     * @param touristId
     * @return
     * @throws Exception
     */
    @GetMapping("/getVideo")
    @ApiOperation("获得我（导游）的视频")
    public ResultDTO getVideo(Long touristId )throws Exception{
        Long guideId = guideService.findByParams(new Guide(touristId)).get(0).getId();

        return ResultUtil.Success(videoService.findByParams(new Video(guideId)));
    }

    /**
     * 获得我的（导游）的喜欢
     * @param touristId
     * @return
     * @throws Exception
     */
    @GetMapping("/getLike")
    @ApiOperation("获得我的（导游）的喜欢")
    public ResultDTO getLike(Long touristId)throws Exception{
        LikeVideo likeVideo = new LikeVideo();
        likeVideo.setTouristId(touristId);
        List<Video> videoList = new ArrayList<>();
        for(LikeVideo l:likeVideoService.findByParams(likeVideo)){
            videoList.add(videoService.get(l.getLikeVideoId()));
        }
        return ResultUtil.Success(videoList);
    }

    /**
     * 获得我的预约
     * @param touristId
     * @return
     * @throws Exception
     */
    @GetMapping("/getMyOrder")
    @ApiOperation("获得我的预约")
    public ResultDTO getMyOrder(Long touristId)throws Exception {
        Long guideId = guideService.findByParams(new Guide(touristId)).get(0).getId();
        return ResultUtil.Success(orderService.findByParams(new Order(guideId)));
    }


    /**
     * 保存导游位置
     * @param longitude
     * @param latitude
     * @param touristId
     * @return
     */
    @PostMapping("/saveLocation")
    @ApiOperation("保存导游位置")
    public ResultDTO saveLocation(Double longitude,Double latitude,Long touristId){
        guideGeoService.saveGuideLocation(new Location(touristId.toString(),longitude,latitude));
        return ResultUtil.Success();
    }

    /**
     * 得到路线以及视频
     * @param touristId
     * @return
     * @throws Exception
     */
    @GetMapping("/getHisRouteAndVideo")
    @ApiOperation("得到路线以及视频")
    public ResultDTO getHisRouteAndVideo(Long touristId) throws Exception{
        Long guideid = guideService.findByParams(new Guide(touristId)).get(0).getId();
        List<MsgDTO> msgDTOS = new ArrayList<>();
        for(Route r:routeService.findByParams(new Route(guideid))){
            msgDTOS.add(new MsgDTO(r,videoService.get(r.getVideoId())));
        }
        return ResultUtil.Success(msgDTOS);
    }


    /**
     * 得到某城市的景区
     * @param cityName
     * @return
     * @throws Exception
     */
    @GetMapping("/getScenic")
    @ApiOperation("得到某城市的景区")
    public ResultDTO getScenic(String cityName) throws Exception{
        return ResultUtil.Success(scenicService.findByParams(new Scenic(cityName)));
    }
}
