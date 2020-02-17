package com.ysq.theTourGuide.service.impl;

import com.ysq.theTourGuide.base.service.impl.BaseServiceImpl;
import com.ysq.theTourGuide.entity.Fans;
import com.ysq.theTourGuide.entity.Order;
import com.ysq.theTourGuide.mapper.FansMapper;
import com.ysq.theTourGuide.mapper.OrderMapper;
import com.ysq.theTourGuide.service.OrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends BaseServiceImpl<OrderMapper, Order> implements OrderService {
}
