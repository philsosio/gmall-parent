package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.item.feign.ItemFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

@Controller
@RequestMapping(value = "/page/item")
public class ItemController {

    @Autowired
    private ItemFeign itemFeign;

    /**
     * 打开item的页面
     * @param model
     * @return
     */
    @GetMapping
    public String item(Model model, Long skuId){
        //远程调用item微服务,获取商品的数据
        Map goodsDetail = itemFeign.getGoodsDetail(skuId);
        System.out.println(goodsDetail);
        //将数据存入model中去
        model.addAllAttributes(goodsDetail);
        //打开页面
        return "item";
    }

    @Autowired
    private TemplateEngine templateEngine;
    /**
     * 生成指定商品的静态页面
     * @param skuId
     * @return
     */
    @GetMapping(value = "/create")
    @ResponseBody
    public String create(Long skuId) throws Exception{
        //远程调用item微服务,获取商品的数据
        Map goodsDetail = itemFeign.getGoodsDetail(skuId);
        //创建页面容器
        Context context = new Context();
        context.setVariables(goodsDetail);
        //构建文件对象
        File file = new File("D:/", skuId + ".html");
        //声明输出的写对象
        PrintWriter writer = new PrintWriter(file, "UTF-8");
        //模板引擎技术
        /**
         * 1.模板页面是哪个?
         * 2.页面的容器Model
         * 3.写的io流-->将静态文件写入磁盘保存
         */
        templateEngine.process("item", context, writer);
        //关闭资源
        writer.close();
        return "页面生成成功!";
    }

}
