package com.ysq.theTourGuide.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ysq.theTourGuide.base.dto.ResultDTO;
import com.ysq.theTourGuide.base.util.ResultUtil;
import com.ysq.theTourGuide.config.ErrorCode;
import com.ysq.theTourGuide.config.OrderState;
import com.ysq.theTourGuide.config.RecommendAttrs;
import com.ysq.theTourGuide.dto.*;
import com.ysq.theTourGuide.entity.*;
import com.ysq.theTourGuide.service.*;
import com.ysq.theTourGuide.service.redis.IGeoService;
import com.ysq.theTourGuide.service.redis.RedisService;
import com.ysq.theTourGuide.utils.HttpClientUtil;
import com.ysq.theTourGuide.utils.MyMathUtil;
import com.ysq.theTourGuide.utils.SortUtil;
import com.ysq.theTourGuide.utils.WechatGetUserInfoUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Point;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class TouristController {


    @Value("${app.appid}")
    private String appid;

    @Value("${app.appSecret}")
    private String appSecret;

    @Value("${app.appTimeOut}")
    private long appTimeOut;


    @Autowired
    TouristService touristService;
    @Autowired
    VideoService videoService;

    @Autowired
    RedisService redisService;

    @Autowired
    IGeoService geoService;

    @Autowired
    GuideService guideService;

    @Autowired
    ScenicService scenicService;

    @Autowired
    TheOrderService theOrderService;

    @Autowired
    LikeVideoService likeVideoService;

    @Autowired
    CommentService commentService;

    @Autowired
    LikeCommentService likeCommentService;

    @Autowired
    MessageService messageService;

    @Autowired
    RouteService routeService;

//    @PostMapping("/login")
    public ResultDTO login(String encryptedData, String iv, String code){
        if(!StringUtils.isNotBlank(code)){
            return ResultUtil.Error(ErrorCode.INVALID_PARAMETERS);
        }
        String apiUrl="https://api.weixin.qq.com/sns/jscode2session?appid="+appid+"&secret="+appSecret+"&js_code="+code+"&grant_type=authorization_code";
        System.out.println(apiUrl);
        String responseBody = HttpClientUtil.doGet(apiUrl);
        System.out.println(responseBody);
        JSONObject jsonObject = JSON.parseObject(responseBody);
        if(StringUtils.isNotBlank(jsonObject.getString("openid"))&&StringUtils.isNotBlank(jsonObject.getString("session_key"))){
            //解密获取用户信息
            JSONObject userInfoJSON= WechatGetUserInfoUtil.getUserInfo(encryptedData,jsonObject.getString("session_key"),iv);
            if(userInfoJSON!=null){
                //这步应该set进实体类
                Map userInfo = new HashMap();
                userInfo.put("openId", userInfoJSON.get("openId"));
                userInfo.put("nickName", userInfoJSON.get("nickName"));
                userInfo.put("gender", userInfoJSON.get("gender"));
                userInfo.put("city", userInfoJSON.get("city"));
                userInfo.put("province", userInfoJSON.get("province"));
                userInfo.put("country", userInfoJSON.get("country"));
                userInfo.put("avatarUrl", userInfoJSON.get("avatarUrl"));
                // 解密unionId & openId;
                if (userInfoJSON.get("unionId")!=null) {
                    userInfo.put("unionId", userInfoJSON.get("unionId"));
                }
                //然后根据openid去数据库判断有没有该用户信息，若没有则存入数据库，有则返回用户数据
                Map<String,Object> dataMap = new HashMap<>();
                dataMap.put("userInfo", userInfo);
                String uuid=UUID.randomUUID().toString();
                dataMap.put("WXTOKEN", uuid);
                redisService.set(uuid,userInfo,appTimeOut);
//                redisTemplate.opsForValue().set(uuid,userInfo);
//                redisTemplate.expire(uuid,appTimeOut, TimeUnit.SECONDS);
                return ResultUtil.Success(dataMap);
            }else{
                return ResultUtil.Error(ErrorCode.UNKNOWERROR);
            }
        }else{
            return ResultUtil.Error(ErrorCode.UNKNOWERROR);
        }


    }

    /**
     * 登录接口返回openid 以及 session_key
     * @param code
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("登录接口返回openid 以及 session_key")
    public ResultDTO login(String code){
        if(!StringUtils.isNotBlank(code)){
            return ResultUtil.Error(ErrorCode.UNKNOWERROR);
        }
        String apiUrl="https://api.weixin.qq.com/sns/jscode2session?appid="+appid+"&secret="+appSecret+"&js_code="+code+"&grant_type=authorization_code";
        System.out.println(apiUrl);
        String responseBody = HttpClientUtil.doGet(apiUrl);
        System.out.println(responseBody);
        JSONObject jsonObject = JSON.parseObject(responseBody);

        return ResultUtil.Success(jsonObject);
    }

    /**
     * 提交用户信息，若已存在则对比信息是否一致，否则更新数据库
     * @param userInfo
     * @return
     * @throws Exception
     */
    @PostMapping("/saveUserInfo")
    @ApiOperation("提交用户信息，若已存在则对比信息是否一致，否则更新数据库")
    public ResultDTO saveUserInfo(UserInfo userInfo)throws Exception{
        List<Tourist> touristList = touristService.findByParams(new Tourist(userInfo.getOpenId()));
        if(touristList.size() == 0){
            Tourist save = touristService.save(new Tourist(userInfo));
            return ResultUtil.Success(save);
        }else{
            Tourist tourist = touristList.get(0);
            if(!userInfo.equals(new UserInfo(tourist))){
                touristService.update(new Tourist(userInfo));
                return ResultUtil.Success(tourist);
            }else{
                return ResultUtil.Success(tourist);
            }
        }

    }


    /**
     * 根据参数排序视频给用户
     * @param attr //level，游客等级，distance 离景区距离，goodNums 点赞数
     * @return
     * @throws Exception
     */
    @PostMapping("/recommend")
    @ApiOperation("根据参数排序视频给用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "attr",value = "level，游客等级，distance 离景区距离，goodNums 点赞数",paramType = "query",dataType = "String"),
            @ApiImplicitParam(name = "longitude",value = "经度",paramType = "query",dataType = "Double"),
            @ApiImplicitParam(name = "latitude",value = "维度",paramType = "query",dataType = "Double"),
    })

    public ResultDTO recommend(String attr,Double longitude,Double latitude,Long touristId)throws Exception{
        List<Video> videoList = videoService.findAll();
        List<VideoDTO> videoDTOS = new ArrayList<>();
        for(Video v:videoList){
            Scenic scenic = scenicService.get(v.getScenicId());
            Guide guide = guideService.get(v.getGuideId());
            Tourist tourist = touristService.get(guide.getTouristId());
            boolean isLike = likeVideoService.findByParams(new LikeVideo(v.getId(),touristId)).size()==0 ? false : true;
            Integer comment_counts = commentService.findByParams(new Comment(v.getId())).size();
            videoDTOS.add(new VideoDTO(v,
                    MyMathUtil.getTwoPointDist(
                            new Point(longitude,latitude),
                            new Point(scenic.getLongitude(),scenic.getLatitude())),
                    tourist.getAvatarUrl(),
                    guide.getLevel(),
                    isLike,
                    comment_counts,
                    v.getLikeNums(),
                    guide.getName()
            ));
        }
        if(attr.equals(RecommendAttrs.DIS.getAttr()) ){
            return ResultUtil.Success(SortUtil.sortByDistance(videoDTOS,"ASC"));
        }else if(attr.equals(RecommendAttrs.LEV.getAttr()) ){
            return ResultUtil.Success(SortUtil.sortByGuideLevel(videoDTOS,"ASC"));
        }else if(attr.equals(RecommendAttrs.GN.getAttr()) ){
            return ResultUtil.Success(SortUtil.sortVideoByLikeNums(videoDTOS,"ASC"));
        }else{
            return ResultUtil.Error(ErrorCode.INVALID_PARAMETERS);
        }


    }

    /**
     *获取用户信息
     * @param touristId
     * @return
     * @throws Exception
     */
    @GetMapping("/getUserInfo")
    @ApiOperation("获取用户信息")
    public ResultDTO getUserInfo(Long touristId)throws Exception{
        return ResultUtil.Success(touristService.get(touristId));
    }

    /**
     * 预约订单
     * @param order
     * @return
     * @throws Exception
     */
    @PostMapping("/booking")
    @ApiOperation("预约订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "touristId",value = "游客id",paramType = "query",dataType = "Long"),
            @ApiImplicitParam(name = "routeId",value = "路线id",paramType = "query",dataType = "Long"),
            @ApiImplicitParam(name = "guideId",value = "导游id",paramType = "query",dataType = "Long"),
            @ApiImplicitParam(name = "tStart",value = "出发点",paramType = "query",dataType = "String"),
            @ApiImplicitParam(name = "nOP",value = "人数",paramType = "query",dataType = "Integer"),
            @ApiImplicitParam(name = "time",value = "开始日期，格式为'yyyy-MM-dd",paramType = "query",dataType = "Date"),
            @ApiImplicitParam(name = "meetTime",value = "碰面时间,格式为‘yyyy-MM-dd HH:mm:ss",paramType = "query",dataType = "Date"),
            @ApiImplicitParam(name = "tName",value = "名字",paramType = "query",dataType = "String"),
            @ApiImplicitParam(name = "idNumber",value = "身份证",paramType = "query",dataType = "String"),
            @ApiImplicitParam(name = "phone",value = "电话",paramType = "query",dataType = "String"),
    })
    public ResultDTO booking(TheOrder order)throws Exception{
        order.setState("222");
        if (theOrderService.findByParams(new TheOrder(order.getTouristId(),order.getRouteId(),order.getTime())).size()==0) {
            TheOrder save = theOrderService.save(order);
            messageService.save(new Message(save.getTouristId(),"订单号为" + save.getId() + "的订单预约成功"));
            return ResultUtil.Success(save);
        }else{
            return ResultUtil.Error(ErrorCode.ISEXIST);
        }
    }

    /**
     * 取消订单
     * @param orderId
     * @return
     * @throws Exception
     */
    @DeleteMapping("/cancelOrder")
    @ApiOperation("取消订单")
    @ApiImplicitParam(value = "订单id",name = "orderId",paramType = "query",dataType = "Long")
    public ResultDTO cancelOrder(Long orderId)throws Exception{
        if(theOrderService.get(orderId)!=null) {
            theOrderService.update(new TheOrder(OrderState.CANCEL.getState()));
            return ResultUtil.Success();
        }else{
            return ResultUtil.Error(ErrorCode.NOEXIST);
        }
    }

    /**
     * 完成订单
     * @param orderId
     * @return
     * @throws Exception
     */
    @PostMapping("/finishOrder")
    @ApiOperation("完成订单")
    @ApiImplicitParam(value = "订单id",name = "orderId",paramType = "query",dataType = "Long")
    public ResultDTO finishOrder(Long orderId)throws Exception{
        theOrderService.update(new TheOrder(OrderState.FINISH.getState()));
        return ResultUtil.Success();
    }

    /**
     * 给视频点赞
     * @param videoId
     * @param touristId
     * @return
     * @throws Exception
     */
    @PostMapping("/likeVideo")
    @ApiOperation("给视频点赞")
    @ApiImplicitParam(name = "videoId",value = "视频id",paramType = "query",dataType = "Long")
    public ResultDTO likeVideo(Long videoId,Long touristId)throws Exception{
        if(likeVideoService.findByParams(new LikeVideo(videoId,touristId)).size()==0){
            int beforeLikeNums = 0;
            try {
                beforeLikeNums = videoService.get(videoId).getLikeNums();
            } catch (Exception e) {
                beforeLikeNums = 0;
            }
            videoService.update(new Video(videoId,beforeLikeNums+1));
            return ResultUtil.Success(likeVideoService.save(new LikeVideo(videoId,touristId)));
        }else {
            return ResultUtil.Error(ErrorCode.ISEXIST);
        }

    }

    /**
     * 发表评论
     * @param comment
     * @return
     * @throws Exception
     */
    @PostMapping("/comment")
    @ApiOperation("发表评论")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "videoId",value = "视频id",paramType = "query",dataType = "Long"),
            @ApiImplicitParam(name = "content",value = "评论内容",paramType = "query",dataType = "String"),
            @ApiImplicitParam(name = "likeNums",value = "点赞数",paramType = "query",dataType = "String")
    })
    public ResultDTO comment(Comment comment,Long touristId)throws Exception{
        comment.setTouristId(touristId);
        comment.setCreatetime(new Date());
        comment.setState(1);
        return ResultUtil.Success(commentService.save(comment));
    }

    /**
     * 获取评论
     * @param videoId
     * @param touristId
     * @return
     * @throws Exception
     */
    @GetMapping("/getComments")
    @ApiOperation("获取评论")
    @ApiImplicitParam(name = "videoId",value = "视频id",paramType = "query",dataType = "Long")
    public ResultDTO getComments(Long videoId,Long touristId)throws Exception{
        List<CommentDTO> commentDTOS = new ArrayList<>();
        for(Comment c:commentService.findAll()){
            Tourist tourist = touristService.get(c.getTouristId());
            boolean isLike = likeCommentService.findByParams(new LikeComment(touristId,c.getId())).size()==0 ? false : true;
            commentDTOS.add(new CommentDTO(tourist.getAvatarUrl(),
                    tourist.getNickname(),
                    isLike,
                    c)
            );
        }
        return ResultUtil.Success(commentDTOS);
    }

    /**
     * 给评论点赞
     * @param commentId
     * @param touristId
     * @return
     * @throws Exception
     */
    @PostMapping("/likeComment")
    @ApiOperation("给评论点赞")
    public ResultDTO likeComment(Long commentId,Long touristId)throws Exception{
        if(likeCommentService.findByParams(new LikeComment(touristId,commentId)).size()==0){
            int beforeLikeNums = 0;
            try {
                beforeLikeNums = commentService.get(commentId).getLikeNums();
            } catch (Exception e) {
                beforeLikeNums = 0;
            }
            commentService.update(new Comment(commentId,beforeLikeNums+1));
            return ResultUtil.Success(likeCommentService.save(new LikeComment(touristId,commentId)));
        }else {
            return ResultUtil.Error(ErrorCode.ISEXIST);
        }
    }

    /**
     * 获取他的视频
     * @param guideId
     * @return
     * @throws Exception
     */
    @GetMapping("/getHisVideo")
    @ApiOperation("获取他的视频")
    @ApiImplicitParam(name = "guideId",value = "导游id",paramType = "query",dataType = "Long")
    public ResultDTO getHisVideo(Long guideId)throws Exception{
        return ResultUtil.Success(videoService.findByParams(new Video(guideId)));
    }

    /**
     * 获取它的喜欢
     * @param guideId
     * @return
     * @throws Exception
     */
    @GetMapping("/getHisLike")
    @ApiOperation("获取它的喜欢")
    @ApiImplicitParam(name = "guideId",value = "导游id",paramType = "query",dataType = "Long")
    public ResultDTO getHisLike(Long guideId)throws Exception{
        Long touristId = guideService.get(guideId).getTouristId();
        LikeVideo likeVideo = new LikeVideo();
        likeVideo.setTouristId(touristId);
        List<Video> videoList = new ArrayList<>();
        for(LikeVideo l:likeVideoService.findByParams(likeVideo)){
            videoList.add(videoService.get(l.getLikeVideoId()));
        }
        return ResultUtil.Success(videoList);
    }




    /**
     * 获取他的未查阅的消息数量
     * @param touristId
     * @return
     * @throws Exception
     */
    @GetMapping("/getMsgNums")
    @ApiOperation("获取他的未查阅的消息数量")
    public ResultDTO getMsgNums(Long touristId )throws Exception{
        return ResultUtil.Success(messageService.countAll(new Message(touristId)));
    }

    /**
     * 获取他的消息
     * @param touristId
     * @return
     * @throws Exception
     */
    @GetMapping("/getMsgs")
    @ApiOperation("获取他的消息")
    public ResultDTO getMsgs(Long touristId )throws Exception{
        Message message = new Message();
        message.setTouristId(touristId);
        return ResultUtil.Success(messageService.findByParams(message));
    }

    /**
     * 获取消息详情
     * @param messageId
     * @return
     * @throws Exception
     */
    @GetMapping("/getMsg")
    @ApiOperation("获取消息详情")
    @ApiImplicitParam(name = "messageId",value = "消息id",paramType = "query",dataType = "Long")
    public ResultDTO getMsg(Long messageId)throws Exception{
        Message message = messageService.get(messageId);
        Byte state = 1;
        messageService.update(new Message(messageId,state));
        return ResultUtil.Success(message);
    }

    /**
     * 获取路线信息
     * @param videoId
     * @return
     * @throws Exception
     */
    @GetMapping("/getRoute")
    @ApiOperation("获取路线信息")
    public ResultDTO getRoute(Long videoId)throws Exception{
        Video video = videoService.get(videoId);
        Guide guide = guideService.get(video.getGuideId());
        return ResultUtil.Success(new RouteDTO(guide,
                routeService.get(video.getRouteId()),
                touristService.get(guide.getTouristId())));
    }


    /**
     * 返回该路线不能选择的天数
     */
    @GetMapping("/getSelectDays")
    @ApiOperation("返回该路线不能选择的天数")
    public ResultDTO getSelectDays(Long routeId)throws Exception{
        Route route = routeService.get(routeId);
        List list = new ArrayList();
        for(TheOrder t:theOrderService.findByParams(new TheOrder(route.getGuideId(),"222"))){
            list.add(MyMathUtil.returnSelectDays(t.getTime(),route.getRDay()));
        }
        return ResultUtil.Success(list);
    }

    /**
     * 得到订单详情
     * @param orderId
     * @return
     * @throws Exception
     */
    @GetMapping("/getOrder")
    @ApiOperation("得到订单详情")
    public ResultDTO getOrder(Long orderId)throws Exception{
        TheOrder theOrder = theOrderService.get(orderId);
        Guide guide = guideService.get(theOrder.getGuideId());
        return ResultUtil.Success(new ReturnOrderDTO(theOrder,guide,touristService.get(guide.getTouristId())));
    }
//
}
