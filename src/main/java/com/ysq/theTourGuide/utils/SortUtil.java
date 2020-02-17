package com.ysq.theTourGuide.utils;

import com.ysq.theTourGuide.dto.VideoDistanceDTO;
import com.ysq.theTourGuide.dto.VideoGuideLevelDTO;
import com.ysq.theTourGuide.entity.Video;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 排序工具类
 * @author 叶三秋
 * @date 2020/2/16
 */
public class SortUtil {

    /**
     * 排序视频根据点赞数
     * @param list
     * @param orderBy ASC为正序，DESC为倒序
     * @return
     */
    public static List<Video> sortVideoByLikeNums(List<Video> list,String orderBy){
        if(orderBy == "ASC") {
            Collections.sort(list,
//                    (o1,o2) -> o1.getLikeNums() - o2.getLikeNums() 等同于以下语句
                    Comparator.comparingInt(Video::getLikeNums)
            );
            return list;
        }else if(orderBy == "DESC"){
            Collections.sort(list, (o1,o2) -> o2.getLikeNums() - o1.getLikeNums());
            return list;
        }else{
            return null;
        }
    }

    /**
     * 排序视频根据导游等级
     * @param list
     * @param orderBy ASC为正序，DESC为倒序
     * @return
     */
    public static List<VideoGuideLevelDTO> sortByGuideLevel(List<VideoGuideLevelDTO> list, String orderBy){
        if(orderBy == "ASC"){
            Collections.sort(list,
//                    (o1, o2) -> o1.getGuideLevel() - o2.getGuideLevel()
                    Comparator.comparingInt(VideoGuideLevelDTO::getGuideLevel)
            );
            return list;
        }else if(orderBy == "DESC"){
            Collections.sort(list, (o1,o2) -> o2.getGuideLevel() - o1.getGuideLevel());
            return list;
        }else {
            return null;
        }
    }


    /**
     * 排序视频根据距离
     * @param list
     * @param orderBy ASC为正序，DESC为倒序
     * @return
     */
    public static List<VideoDistanceDTO> sortByDistance(List<VideoDistanceDTO> list,String orderBy){
        if(orderBy == "ASC"){
            Collections.sort(list,
//                    (o1, o2) -> o1.getDistance() - o2.getDistance()
                    Comparator.comparingInt(VideoDistanceDTO::getDistance)
            );
            return list;
        }else if(orderBy == "DESC"){
            Collections.sort(list, (o1,o2) -> o2.getDistance() - o1.getDistance());
            return list;
        }else {
            return null;
        }
    }
}