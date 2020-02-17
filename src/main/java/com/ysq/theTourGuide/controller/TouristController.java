package com.ysq.theTourGuide.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ysq.theTourGuide.dto.VideoDistanceDTO;
import com.ysq.theTourGuide.dto.VideoGuideLevelDTO;
import com.ysq.theTourGuide.base.dto.ResultDTO;
import com.ysq.theTourGuide.base.util.ResultUtil;
import com.ysq.theTourGuide.config.RecommendAttrs;
import com.ysq.theTourGuide.entity.Scenic;
import com.ysq.theTourGuide.entity.Video;
import com.ysq.theTourGuide.service.GuideService;
import com.ysq.theTourGuide.service.ScenicService;
import com.ysq.theTourGuide.service.VideoService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class TouristController {


    @Value("${app.appid")
    private String appid;

    @Value("${app.appSecret")
    private String appSecret;

    @Value("${app.appTimeOut")
    private long appTimeOut;


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

    @PostMapping("/login")
    public ResultDTO login(String encryptedData, String iv, String code){
        if(!StringUtils.isNotBlank(code)){
            return ResultUtil.Error("202","未获取到用户凭证code");
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
                return ResultUtil.Error("202","解密失败");
            }
        }else{
            return ResultUtil.Error("202","未获取到用户openid 或 session");
        }


    }

    /**
     * 根据参数排序视频给用户
     * @param attr //level，游客等级，distance 离景区距离，goodNums 点赞数
     * @return
     * @throws Exception
     */
    @PostMapping("/recommend")
    public ResultDTO recommend(String attr,double longitude,double latitude)throws Exception{
        List<Video> videoList = videoService.findAll();
        if(attr== RecommendAttrs.DIS.getAttr()){
            List<VideoDistanceDTO> videoDistanceDTOS = new ArrayList<>();
            for(Video v:videoList){
                Scenic scenic = scenicService.get(v.getScenicId());
                videoDistanceDTOS.add(new VideoDistanceDTO(v,
                        MyMathUtil.getTwoPointDist(
                                new Point(longitude,latitude),
                                new Point(scenic.getLongitude(),scenic.getLatitude())
                        )
                ));
            }
            return ResultUtil.Success(videoDistanceDTOS);
        }else if(attr == RecommendAttrs.LEV.getAttr()){
            List<VideoGuideLevelDTO> videoGuideLevelDTOS = new ArrayList<>();
            for(Video v:videoList){
                videoGuideLevelDTOS.add(new VideoGuideLevelDTO(v,guideService.get(v.getGuideId()).getLevel()));
            }
            SortUtil.sortByGuideLevel(videoGuideLevelDTOS,"ASC");
            return ResultUtil.Success(videoGuideLevelDTOS);
        }else if(attr == RecommendAttrs.GN.getAttr()){
            SortUtil.sortVideoByLikeNums(videoList,"ASC");
            return ResultUtil.Success(videoList);
        }else{
            return ResultUtil.Error("202","不正确的参数attr");
        }


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
