package com.ysq.theTourGuide.utils;

import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author 叶三秋
 * @date 2020/2/11
 */
public class HttpClientUtil {

    /**
     * 向目标url发送get请求
     * @param url
     * @param params 参数列表
     * @return String
     */
    public static String doGet(String url, MultiValueMap<String, String> params){

        RestTemplate client = new RestTemplate();
        //新建HTTP头。add方法可以添加参数
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpMethod httpMethod = HttpMethod.GET;
        //一表单的方式提交
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //请求将头部和参数合成一个请求
        HttpEntity<MultiValueMap<String,String>> requestEntity = new HttpEntity<>(params,httpHeaders);
        //执行HTTP请求，将返回的结构使用String 类格式化
        ResponseEntity<String> response = client.exchange(url,httpMethod,requestEntity,String.class);
        return response.getBody();
    }

    public static String doGet(String url){
        return doGet(url,null);
    }

    /**
     * 向目的url发送post请求
     * @param url
     * @param params 参数列表
     * @return String
     */
    public static String doPost(String url,MultiValueMap<String,String> params){
        RestTemplate client = new RestTemplate();
        //新建HTTP头。add方法可以添加参数
        HttpHeaders httpHeaders = new HttpHeaders();
        //设置请求发送方式
        HttpMethod httpMethod = HttpMethod.POST;
        //一表单的方式提交
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //将请求头部和参数合成
        HttpEntity<MultiValueMap<String,String>> requestEntity = new HttpEntity<>(params,httpHeaders);
        //执行http请求，将返回的结构使用String 类格式化
        ResponseEntity<String> response = client.exchange(url,httpMethod,requestEntity,String.class);
        return response.getBody();
    }

    public static String doPost(String url){
        return doPost(url, null);
    }
}
