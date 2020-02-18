package com.ysq.theTourGuide.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.math.BigDecimal;

@Data
public class Route {
    @Id
    private Long id;

    /**
     * 路线
     */
    private String line;

    /**
     * 时长
     */
    private String time;

    /**
     * 景点个数
     */
    private Integer noss;

    /**
     * 经典景点个数
     */
    private Integer nosss;

    /**
     * 是否购物
     */
    @Column(name = "h_shop")
    private Boolean hShop;

    /**
     * 语言
     */
    private String language;

    /**
     * 人数上限
     */
    @Column(name = "n_o_p")
    private Integer nOP;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 优惠类型id
     */
    @Column(name = "discount_type_id")
    private Integer discountTypeId;

    /**
     * 优惠额度
     */
    @Column(name = "discount_value")
    private Integer discountValue;

    /**
     * 描述
     */
    private String describe;

    /**
     * 导游id
     */
    @Column(name = "guide_id")
    private Long guideId;

    public static final String ID = "id";

    public static final String LINE = "line";

    public static final String TIME = "time";

    public static final String NOSS = "noss";

    public static final String NOSSS = "nosss";

    public static final String H_SHOP = "hShop";

    public static final String LANGUAGE = "language";

    public static final String N_O_P = "nOP";

    public static final String PRICE = "price";

    public static final String DISCOUNT_TYPE_ID = "discountTypeId";

    public static final String DISCOUNT_VALUE = "discountValue";

    public static final String DESCRIBE = "describe";

    public static final String GUIDE_ID = "guideId";
}