package com.atguigu.gmall.activity.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("seckill_order")
public class SeckillOrder implements Serializable{

   @TableField("id")
   private String id;
   @TableField("goods_id")
   private String goodsId;
   @TableField("money")
   private String money;
   @TableField("user_id")
   private String userId;
   @TableField("create_time")
   private Date createTime;
   @TableField("pay_time")
   private String payTime;
   @TableField("status")
   private String status;
   @TableField("receiver_address")
   private String receiverAddress;
   @TableField("receiver_mobile")
   private String receiverMobile;
   @TableField("receiver")
   private String receiver;
   @TableField("transaction_id")
   private String transactionId;
   @TableField("num")
   private Integer num;

}