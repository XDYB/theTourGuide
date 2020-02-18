package com.ysq.theTourGuide.dto;

import com.ysq.theTourGuide.entity.Tourist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    private Long openId;
    private String nickName;
    private String avatarUrl;
    private String gender;
    private String province;
    private String city;
    private String country;

    public UserInfo(Tourist tourist) {
        this.openId = tourist.getOpenId();
        this.nickName = tourist.getNickname();
        this.avatarUrl = tourist.getAvatarUrl();
        this.gender = tourist.getGender();
        this.province = tourist.getProvince();
        this.city = tourist.getCity();
        this.country = tourist.getCountry();
    }


    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }
        if(this == obj){
            return true;
        }

        if(obj instanceof UserInfo){
            UserInfo userInfo = (UserInfo)obj;
            if(userInfo.openId == this.openId &&
                    userInfo.nickName == this.nickName &&
                    userInfo.avatarUrl == this.avatarUrl &&
                    userInfo.province == this.province &&
                    userInfo.city == this.city &&
                    userInfo.country == this.country &&
                    userInfo.gender == this.gender){
                return true;
            }else {
                return false;
            }
        }
        return false;
    }
}