package com.ysq.theTourGuide.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    private Long id;

    /**
     * 游客id
     */
    @Column(name = "tourist_id")
    private Long touristId;

    /**
     * 订单标题
     */
    private String title;

    /**
     * 导游id
     */
    @Column(name = "guide_id")
    private Long guideId;

    /**
     * 出发点
     */
    private String start;

    /**
     * 人数
     */
    @Column(name = "n_o_p")
    private Integer nOP;

    /**
     * 预定时间
     */
    private String time;

    /**
     * 碰面时间
     */
    @Column(name = "meet_time")
    private Date meetTime;

    /**
     * 订单状态（222为待出行，111为已完成，333为取消）
     */
    private String state;

    /**
     * 姓名
     */
    private String name;

    /**
     * 身份证号
     */
    @Column(name = "ID_number")
    private String idNumber;

    /**
     * 电话
     */
    private String phone;

    public static final String ID = "id";

    public static final String TOURIST_ID = "touristId";

    public static final String TITLE = "title";

    public static final String GUIDE_ID = "guideId";

    public static final String START = "start";

    public static final String N_O_P = "nOP";

    public static final String TIME = "time";

    public static final String MEET_TIME = "meetTime";

    public static final String STATE = "state";

    public static final String NAME = "name";

    public static final String ID_NUMBER = "idNumber";

    public static final String PHONE = "phone";

    public Order(String state){
        this.state = state;
    }

    public Order(Long guideId){
        this.guideId = guideId;
    }
}