package com.ysq.theTourGuide.controller;

import com.ysq.theTourGuide.base.dto.ResultDTO;
import com.ysq.theTourGuide.base.util.ResultUtil;
import com.ysq.theTourGuide.config.ErrorCode;
import com.ysq.theTourGuide.dto.GuideDTO;
import com.ysq.theTourGuide.dto.GuideResiterDTO;
import com.ysq.theTourGuide.entity.*;
import com.ysq.theTourGuide.service.*;
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
            @ApiImplicitParam(value = "语言",name = "language",dataType = "Boolean",paramType = "query"),
            @ApiImplicitParam(value = "人数上限",name = "nOP",dataType = "Boolean",paramType = "query"),
            @ApiImplicitParam(value = "价格",name = "price",dataType = "Boolean",paramType = "query"),
            @ApiImplicitParam(value = "优惠类型id",name = "discountTypeId",dataType = "Boolean",paramType = "query"),
            @ApiImplicitParam(value = "优惠额度",name = "discountValue",dataType = "Boolean",paramType = "query"),
            @ApiImplicitParam(value = "描述",name = "describe",dataType = "Boolean",paramType = "query"),
    })
    public ResultDTO postMsg(Route route, Long touristId )throws Exception{
        Long guideId = guideService.findByParams(new Guide(touristId)).get(0).getId();
        route.setGuideId(guideId);
        return ResultUtil.Success(routeService.save(route));
    }

    /**
     * 上传视频
     * @param video
     * @param touristId
     * @return
     * @throws Exception
     */
    @PostMapping("/postVideo")
    @ApiOperation("上传视频")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "路线id",name = "routeId",dataType = "Boolean",paramType = "query"),
            @ApiImplicitParam(value = "景区id",name = "scenicId",dataType = "Boolean",paramType = "query"),
            @ApiImplicitParam(value = "视频地址",name = "videoUrl",dataType = "Boolean",paramType = "query"),
    })
    public ResultDTO postVideo(Video video,Long touristId )throws Exception{
        Long guideId = guideService.findByParams(new Guide(touristId)).get(0).getId();
        video.setGuideId(guideId);
        return ResultUtil.Success(videoService.save(video));
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
    public ResultDTO getMyOrder(Long touristId)throws Exception {
        Long guideId = guideService.findByParams(new Guide(touristId)).get(0).getId();
        return ResultUtil.Success(orderService.findByParams(new Order(guideId)));
    }
}
