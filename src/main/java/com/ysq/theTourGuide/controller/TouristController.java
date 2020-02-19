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

import javax.servlet.http.HttpServletRequest;
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
    OrderService orderService;

    @Autowired
    LikeVideoService likeVideoService;

    @Autowired
    CommentService commentService;

    @Autowired
    LikeCommentService likeCommentService;

    @Autowired
    MessageService messageService;

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
    public ResultDTO login(String code,HttpServletRequest request){
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
    public ResultDTO saveUserInfo(UserInfo userInfo,HttpServletRequest request)throws Exception{
        List<Tourist> touristList = touristService.findByParams(new Tourist(userInfo.getOpenId()));
        if(touristList.size() == 0){
            Tourist save = touristService.save(new Tourist(userInfo));
            request.getSession().setAttribute("touristId",save.getId());
            return ResultUtil.Success(save);
        }else{
            Tourist tourist = touristList.get(0);
            request.getSession().setAttribute("touristId",tourist.getId());
            if(!userInfo.equals(new UserInfo(tourist))){
                touristService.update(new Tourist(userInfo));
                return ResultUtil.Success();
            }else{
                return ResultUtil.Success();
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

    public ResultDTO recommend(String attr,Double longitude,Double latitude,HttpServletRequest request)throws Exception{
        List<Video> videoList = videoService.findAll();
        Long touristId = (Long) request.getSession().getAttribute("touristId");
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
                    v.getLikeNums()
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
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping("/getUserInfo")
    @ApiOperation("获取用户信息")
    public ResultDTO getUserInfo(HttpServletRequest request)throws Exception{
        return ResultUtil.Success(touristService.get((Long)request.getSession().getAttribute("touristId")));
    }

    /**
     * 预约订单
     * @param orderDTO
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/booking")
    @ApiOperation("预约订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "title",value = "路线",paramType = "query",dataType = "String"),
            @ApiImplicitParam(name = "guideId",value = "导游id",paramType = "query",dataType = "Long"),
            @ApiImplicitParam(name = "start",value = "出发点",paramType = "query",dataType = "String"),
            @ApiImplicitParam(name = "nOP",value = "人数",paramType = "query",dataType = "Integer"),
            @ApiImplicitParam(name = "time",value = "时长",paramType = "query",dataType = "String"),
            @ApiImplicitParam(name = "meetTime",value = "碰面时间",paramType = "query",dataType = "String"),
            @ApiImplicitParam(name = "name",value = "路线",paramType = "query",dataType = "String"),
            @ApiImplicitParam(name = "idNumber",value = "身份证",paramType = "query",dataType = "String"),
            @ApiImplicitParam(name = "phone",value = "电话",paramType = "query",dataType = "String"),
    })
    public ResultDTO booking(OrderDTO orderDTO, HttpServletRequest request)throws Exception{
        Long touristId = (Long)request.getSession().getAttribute("touristId");
        orderDTO.setTouristId(touristId);
        messageService.save(new Message(touristId,"预约成功"));
        return ResultUtil.Success(orderService.saveDTO(orderDTO, Order.class));
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
        if(orderService.get(orderId)!=null) {
            orderService.update(new Order(OrderState.CANCEL.getState()));
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
        orderService.update(new Order(OrderState.FINISH.getState()));
        return ResultUtil.Success();
    }

    /**
     * 给视频点赞
     * @param videoId
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/likeVideo")
    @ApiOperation("给视频点赞")
    @ApiImplicitParam(name = "videoId",value = "视频id",paramType = "query",dataType = "Long")
    public ResultDTO likeVideo(Long videoId,HttpServletRequest request)throws Exception{
        Long touristId = (Long)request.getSession().getAttribute("touristId");
        if(likeVideoService.findByParams(new LikeVideo(videoId)).size()==0){
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
    public ResultDTO comment(Comment comment,HttpServletRequest request)throws Exception{
        Long touristId = (Long)request.getSession().getAttribute("touristId");
        comment.setTouristId(touristId);
        comment.setCreatetime(new Date());
        return ResultUtil.Success(commentService.save(comment));
    }

    /**
     * 获取评论
     * @param videoId
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping("/getComments")
    @ApiOperation("获取评论")
    @ApiImplicitParam(name = "videoId",value = "视频id",paramType = "query",dataType = "Long")
    public ResultDTO getComments(Long videoId,HttpServletRequest request)throws Exception{
        List<CommentDTO> commentDTOS = new ArrayList<>();
        Long touristId = (Long) request.getSession().getAttribute("touristId");
        for(Comment c:commentService.findAll()){
            Tourist tourist = touristService.get(c.getTouristId());
            boolean isLike = likeCommentService.findByParams(new LikeComment(touristId,c.getId())).size()==0 ? false : true;
            commentDTOS.add(new CommentDTO(tourist.getAvatarUrl(),
                    tourist.getNickname(),
                    c.getContent(),
                    c.getCreatetime(),
                    isLike)
            );
        }
        return ResultUtil.Success(commentDTOS);
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
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping("/getMsgNums")
    @ApiOperation("获取他的未查阅的消息数量")
    public ResultDTO getMsgNums(HttpServletRequest request)throws Exception{
        return ResultUtil.Success(messageService.countAll(new Message((Long)request.getSession().getAttribute("touristId"))));
    }

    /**
     * 获取他的消息
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping("/getMsgs")
    @ApiOperation("获取他的消息")
    public ResultDTO getMsgs(HttpServletRequest request)throws Exception{
        Message message = new Message();
        message.setTouristId((Long)request.getSession().getAttribute("touristId"));
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
        byte t = 1;
        messageService.update(new Message(messageId,t));
        return ResultUtil.Success(message);
    }
//
}
