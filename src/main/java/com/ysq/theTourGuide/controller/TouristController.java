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
    public ResultDTO login(String code,HttpServletRequest request){
        if(!StringUtils.isNotBlank(code)){
            return ResultUtil.Error(ErrorCode.UNKNOWERROR);
        }
        String apiUrl="https://api.weixin.qq.com/sns/jscode2session?appid="+appid+"&secret="+appSecret+"&js_code="+code+"&grant_type=authorization_code";
        System.out.println(apiUrl);
        String responseBody = HttpClientUtil.doGet(apiUrl);
        System.out.println(responseBody);
        JSONObject jsonObject = JSON.parseObject(responseBody);
        request.getSession().setAttribute("touristId",jsonObject.getString("open_id"));
        return ResultUtil.Success(jsonObject);
    }

    /**
     * 提交用户信息，若已存在则对比信息是否一致，否则更新数据库
     * @param userInfo
     * @return
     * @throws Exception
     */
    @PostMapping("/saveUserInfo")
    public ResultDTO saveUserInfo(UserInfo userInfo)throws Exception{
        Tourist tourist = touristService.get(userInfo.getOpenId());
        System.out.println(tourist.toString());
        if(tourist!=null){
            if(!userInfo.equals(new UserInfo(tourist))){
                touristService.update(new Tourist(userInfo));
                return ResultUtil.Success();
            }
        }else{
            return ResultUtil.Success(touristService.save(new Tourist(userInfo)));
        }

        return ResultUtil.Error(ErrorCode.UNKNOWERROR);
    }

    /**
     * 根据参数排序视频给用户
     * @param attr //level，游客等级，distance 离景区距离，goodNums 点赞数
     * @return
     * @throws Exception
     */
    @PostMapping("/recommend")
    public ResultDTO recommend(String attr,double longitude,double latitude,HttpServletRequest request)throws Exception{
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
        if(attr== RecommendAttrs.DIS.getAttr()){
            return ResultUtil.Success(SortUtil.sortByDistance(videoDTOS,"ASC"));
        }else if(attr == RecommendAttrs.LEV.getAttr()){
            return ResultUtil.Success(SortUtil.sortByGuideLevel(videoDTOS,"ASC"));
        }else if(attr == RecommendAttrs.GN.getAttr()){
            return ResultUtil.Success(SortUtil.sortVideoByLikeNums(videoDTOS,"ASC"));
        }else{
            return ResultUtil.Error(ErrorCode.INVALID_PARAMETERS);
        }


    }


    /**
     * 预约订单
     * @param orderDTO
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/booking")
    public ResultDTO booking(OrderDTO orderDTO, HttpServletRequest request)throws Exception{
        orderDTO.setTouristId((Long)request.getSession().getAttribute("touristId"));
        return ResultUtil.Success(orderService.saveDTO(orderDTO, Order.class));
    }

    /**
     * 取消订单
     * @param orderId
     * @return
     * @throws Exception
     */
    @DeleteMapping("/cancelOrder")
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
    public ResultDTO comment(Comment comment)throws Exception{
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

    @GetMapping("/getHisVideo")
    public ResultDTO getHisVideo(Long guideId)throws Exception{
        return ResultUtil.Success(videoService.findByParams(new Video(guideId)));
    }

    @GetMapping("/getHisLike")
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


    @GetMapping("/getMsg")
    public ResultDTO getMsg(HttpServletRequest request)throws Exception{
        return ResultUtil.Success(messageService.findByParams(new Message((Long)request.getSession().getAttribute("touristId"))));
    }
//    @GetMapping("redis")
//    public ResultDTO redis(){
////        CityGeoKey cityGeoKey = new CityGeoKey();
////        System.out.println(redisService.geoAdd(cityGeoKey,new Point(116.405285,39.904989),"北京"));
////        System.out.println(redisService.geoPos(cityGeoKey, "北京"));
////        System.out.println(redisService.geoAdd(cityGeoKey,new Point(121.472644,31.231706),"上海"));
////        System.out.println(redisService.geoPos(cityGeoKey, "上海"));
////        Metric metric = Metrics.KILOMETERS;
////        System.out.println(redisService.geoDist(cityGeoKey, "北京", "上海",metric));
////        redisService.geoRadius(cityGeoKey, new Point(115.405285, 39.904989), new Distance(1550, metric));
//        List<Location> locations = new ArrayList<>();
//
//        locations.add(new Location("hefei", 117.17, 31.52));
//        locations.add(new Location("anqing", 117.02, 30.31));
//        locations.add(new Location("huaibei", 116.47, 33.57));
//        locations.add(new Location("suzhou", 116.58, 33.38));
//        locations.add(new Location("fuyang", 115.48, 32.54));
//        locations.add(new Location("bengbu", 117.21, 32.56));
//        locations.add(new Location("huangshan", 118.18, 29.43));
//
////        System.out.println(geoService.saveLocationToRedis(locations));
//
//        System.out.println(JSON.toJSONString(geoService.getLocationPos(
//                Arrays.asList("anqing", "suzhou", "xxx").toArray(new String[3])
//        )));
//
//        System.out.println(geoService.getTwoLocationDistance("anqing", "suzhou", null).getValue());
//        System.out.println(geoService.getTwoLocationDistance("anqing", "suzhou", Metrics.KILOMETERS).getValue());
//
//
//        Point center = new Point(locations.get(0).getLongitude(), locations.get(0).getLatitude());
//        Distance radius = new Distance(200, Metrics.KILOMETERS);
//        Circle within = new Circle(center, radius);
//
//        System.out.println(JSON.toJSONString(geoService.getPointRadius(within, null)));
//
//        // order by 距离 limit 2, 同时返回距离中心点的距离
//        RedisGeoCommands.GeoRadiusCommandArgs args =
//                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().limit(2).sortAscending();
//        System.out.println(JSON.toJSONString(geoService.getPointRadius(within, args)));
//
//
//        Distance radius1 = new Distance(200, Metrics.KILOMETERS);
//        System.out.println(JSON.toJSONString(geoService.getMemberRadius("suzhou", radius1, null)));
//
//        // order by 距离 limit 2, 同时返回距离中心点的距离
//        RedisGeoCommands.GeoRadiusCommandArgs args1 =
//                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().limit(2).sortAscending();
//        System.out.println(JSON.toJSONString(geoService.getMemberRadius("suzhou", radius1, args1)));
//
//        System.out.println(JSON.toJSONString(geoService.getLocationGeoHash(
//                Arrays.asList("anqing", "suzhou", "xxx").toArray(new String[3])
//        )));
//        return ResultUtil.Success();
//    }
}
