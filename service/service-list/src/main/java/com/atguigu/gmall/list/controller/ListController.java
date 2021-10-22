package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.product.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 搜索创建映射创建索引的控制层
 */
@RestController
@RequestMapping(value = "/admin/list/")
public class ListController {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 创建索引创建映射
     * @return
     */
    @GetMapping(value = "/create")
    public Result create(){
        //创建索引
        elasticsearchRestTemplate.createIndex(Goods.class);
        //创建映射
        elasticsearchRestTemplate.putMapping(Goods.class);
        //返回结果
        return Result.ok();
    }

    @Autowired
    private ListService listService;

    /**
     * 商品存入es
     * @param skuInfo
     * @return
     */
    @PostMapping(value = "/upper")
    public Result upper(@RequestBody SkuInfo skuInfo){
        listService.addGoods(skuInfo);
        return Result.ok();
    }

    /**
     * 从es中删除商品
     * @param id
     * @return
     */
    @GetMapping(value = "/del/{id}")
    public Result del(@PathVariable(value = "id")Long id){
        listService.delGoods(id);
        return Result.ok();
    }

    /**
     * 修改热度值
     * @param id
     * @return
     */
    @GetMapping(value = "/addHotScore/{id}")
    public Result addHotScore(@PathVariable(value = "id") Long id){
        listService.addHotScore(id);
        return Result.ok();
    }

    @Autowired
    private SearchService searchService;

    /**
     * 搜索
     * @param searchData
     * @return
     */
    @GetMapping(value = "/search")
    public Map search(@RequestParam Map<String, String> searchData){
        return searchService.search(searchData);
    }

}
