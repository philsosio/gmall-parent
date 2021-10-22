package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.cart.feign.CartFeign;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.feign.ProductFeign;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private CartFeign cartFeign;

    @Autowired
    private ProductFeign productFeign;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 新增订单
     *
     * @param orderInfo
     * @return
     */
    @Override
    public OrderInfo addOrder(OrderInfo orderInfo) {
        //参数校验
        if(orderInfo == null){
            throw new RuntimeException("参数错误!");
        }
        //查询购物车的数据
        Map<String, Object> cartList = cartFeign.getCartList();
        //新增orderDetail,补全数据
        String cartString = (String)cartList.get("cartInfoListNew");
        List<JSONObject> cartInfoList = JSONObject.parseObject(cartString, List.class);
        if(cartInfoList.isEmpty() || cartInfoList.size() == 0){
            throw new RuntimeException("购物车中没有选中任何商品!");
        }
        //补全OrderInfo的信息
        orderInfo.setTotalAmount(new BigDecimal(cartList.get("money").toString()));
        orderInfo.setOrderStatus(OrderStatus.UNPAID.getComment());
        orderInfo.setCreateTime(new Date());
        orderInfo.setExpireTime(new Date(System.currentTimeMillis() + 1800000));
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.getComment());
        //新增OrderInfo,OrderInfo才有id
        int insert = orderInfoMapper.insert(orderInfo);
        if(insert <= 0){
            throw new RuntimeException("新增失败,请重试!");
        }
        //记录扣减库存的商品和数量
        Map<String, Object> numMap = new HashMap<>();
        List<OrderDetail> orderDetails = cartInfoList.stream().map(jsonObject -> {
            CartInfo cartInfo = JSONObject.parseObject(jsonObject.toJSONString(), CartInfo.class);
            //初始化
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderInfo.getId());
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice().multiply(new BigDecimal(cartInfo.getSkuNum())));
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            //新增
            orderDetailMapper.insert(orderDetail);
            numMap.put(cartInfo.getSkuId() + "", cartInfo.getSkuNum());
            //返回结果
            return orderDetail;
        }).collect(Collectors.toList());
        orderInfo.setOrderDetailList(orderDetails);
        //清除购物车
        cartFeign.remove();
        //扣减库存---feign
        if(!productFeign.decountStock(numMap)){
            throw new RuntimeException("扣减库存失败!");
        }
        //发送延迟消息,防止用户一直不支付,消息的ttl的时间为30分钟=1800秒=1800000毫秒
        rabbitTemplate.convertAndSend("order_delay_exchange",
                "order.delay",
                JSONObject.toJSONString(orderInfo), new MessagePostProcessor() {
                    @Override
                    public Message postProcessMessage(Message message) throws AmqpException {
                        //获取消息的属性
                        MessageProperties messageProperties = message.getMessageProperties();
                        //设置消息的超时时间
                        messageProperties.setExpiration("1800000");
                        //返回
                        return message;
                    }
                });
        //返回结果
        return orderInfo;
    }

    /**
     * 取消订单
     *
     * @param id
     * @param status : 0-主动取消 1-超时取消
     */
    @Override
    public void cancleOrder(Long id, Short status) {
        //参数校验
        if(id == null){
            return;
        }
        //查询订单的详细信息
        OrderInfo orderInfo = orderInfoMapper.selectById(id);
        if(orderInfo == null || orderInfo.getId() == null){
            return;
        }
        //修改订单的支付状态---幂等性
        if(orderInfo.getOrderStatus().equals(OrderStatus.UNPAID.getComment())){
            //关闭交易------todo----作业

            //设置订单的状态
            orderInfo.setOrderStatus(
                    status.equals(0)?
                            OrderStatus.CANCLE.getComment():
                            OrderStatus.TIMEOUTUNPAY.getComment());
            orderInfo.setProcessStatus(ProcessStatus.CLOSED.getComment());
            orderInfoMapper.updateById(orderInfo);
            //update order_info set order_status="支付超时/主动取消" where id = #{id} and order_status="未支付";
            //回滚库存--反超卖---查询订单的详情列表
            List<OrderDetail> orderDetails =
                    orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, id));
            Map<String, Object> numMap = new HashMap<>();
            for (OrderDetail orderDetail : orderDetails) {
                numMap.put(orderDetail.getSkuId() + "", orderDetail.getSkuNum());
            }
            //回滚库存
            productFeign.rollbackStock(numMap);
        }
    }

    /**
     * 修改订单的状态
     *
     * @param dataMap
     * @param status:0-微信 1-支付宝
     */
    @Override
    public void updateOrder(Map<String, String> dataMap, Short status) {
        String orderId = dataMap.get("out_trade_no");
        //查询订单的详情
        OrderInfo orderInfo = orderInfoMapper.selectById(Long.valueOf(orderId));
        //幂等性问题
        if(!orderInfo.getOrderStatus().equals(OrderStatus.UNPAID.getComment())){
            return;
        }
        //获取支付的渠道
        if(status.equals(0)){
            //微信支付
            if(dataMap.get("result_code").equals("SUCCESS") &&
                    dataMap.get("return_code").equals("SUCCESS")){
                orderInfo.setOrderStatus(OrderStatus.PAID.getComment());
                orderInfo.setProcessStatus(ProcessStatus.PAID.getComment());
                orderInfo.setOutTradeNo(dataMap.get("transaction_id"));
                orderInfo.setTradeBody(JSONObject.toJSONString(dataMap));
            }else{
                orderInfo.setOrderStatus(OrderStatus.CLOSED.getComment());
                orderInfo.setProcessStatus(ProcessStatus.CLOSED.getComment());
                orderInfo.setTradeBody(JSONObject.toJSONString(dataMap));
            }
        }else{
            //支付宝支付
            if(dataMap.get("trade_status").equals("TRADE_SUCCESS")){
                orderInfo.setOrderStatus(OrderStatus.PAID.getComment());
                orderInfo.setProcessStatus(ProcessStatus.PAID.getComment());
                orderInfo.setOutTradeNo(dataMap.get("trade_no"));
                orderInfo.setTradeBody(JSONObject.toJSONString(dataMap));
            }else{
                orderInfo.setOrderStatus(OrderStatus.CLOSED.getComment());
                orderInfo.setProcessStatus(ProcessStatus.CLOSED.getComment());
                orderInfo.setTradeBody(JSONObject.toJSONString(dataMap));
            }
        }
        //修改订单
        orderInfoMapper.updateById(orderInfo);
    }
}
