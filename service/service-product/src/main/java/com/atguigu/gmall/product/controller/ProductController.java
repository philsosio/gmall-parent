package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.constant.ProductConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理相关的接口api控制层
 */
@RestController
@RequestMapping(value = "/admin/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 查询所有的一级分类
     * @return
     */
    @GetMapping(value = "/getCategory1")
    public Result<List<BaseCategory1>> getCategory1(){
        return Result.ok(productService.findCategory1());
    }

    /**
     * 根据一级分类查询二级分类
     * @return
     */
    @GetMapping(value = "/getCategory2/{id}")
    public Result<List<BaseCategory2>> getCategory2(@PathVariable(value = "id") Long id){
        return Result.ok(productService.findCategory2(id));
    }

    /**
     * 根据二级分类查询三级分类
     * @return
     */
    @GetMapping(value = "/getCategory3/{id}")
    public Result<List<BaseCategory3>> getCategory3(@PathVariable(value = "id") Long id){
        return Result.ok(productService.findCategory3(id));
    }

    /**
     * 保存平台属性
     * @param baseAttrInfo
     * @return
     */
    @PostMapping(value = "/saveAttrInfo")
    public Result<BaseAttrInfo> saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        return Result.ok(productService.saveBaseAttrInfo(baseAttrInfo));
    }

    /**
     * 根据一级二级三级分类查询平台属性信息
     * @param category1
     * @param category2
     * @param category3
     * @return
     */
    @GetMapping(value = "/attrInfoList/{category1}/{category2}/{category3}")
    public Result<List<BaseAttrInfo>> attrInfoList(@PathVariable(value = "category1") Long category1,
                                                   @PathVariable(value = "category2") Long category2,
                                                   @PathVariable(value = "category3") Long category3){
        return Result.ok(productService.getBaseAttrInfoList(category1,category2,category3));
    }

    /**
     * 根据平台属性的名称查询平台属性的值的列表
     * @param id
     * @return
     */
    @GetMapping(value = "/getAttrValueList/{id}")
    public Result<List<BaseAttrValue>> getAttrValueList(@PathVariable(value = "id") Long id){
        return Result.ok(productService.getBaseAttrValue(id));
    }

    /**
     * 查询品牌列表
     * @return
     */
    @GetMapping(value = "/baseTrademark/getTrademarkList")
    public Result<List<BaseTrademark>> getTrademarkList(){
        return Result.ok(productService.getBaseTrademarkList());
    }



    /**
     * 查询基础的销售属性列表
     * @return
     */
    @GetMapping(value = "/baseSaleAttrList")
    public Result<List<BaseSaleAttr>> baseSaleAttrList(){
        return Result.ok(productService.baseSaleAttrList());
    }

    /**
     * 新增/修改spu的信息
     * @param spuInfo
     * @return
     */
    @PostMapping(value = "/saveSpuInfo")
    public Result<SpuInfo> saveSpuInfo(@RequestBody SpuInfo spuInfo){
        return Result.ok(productService.saveSpu(spuInfo));
    }


    /**
     * 分页查询spu的信息
     * @param category3Id
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/{page}/{size}")
    public Result findSpuByCategory3Id(Long category3Id,
                                       @PathVariable(value = "page") Integer page,
                                       @PathVariable(value = "size") Integer size){
        return Result.ok(productService.findSpuByCategory3Id(category3Id, page, size));
    }

    /**
     * 根据spu的id查询图片列表
     * @param id
     * @return
     */
    @GetMapping(value = "/spuImageList/{id}")
    public Result<List<SpuImage>> spuImageList(@PathVariable(value = "id") Long id){
        return Result.ok(productService.findBySpuId(id));
    }

    /**
     * 根据spu的id查询销售属性列表
     * @param id
     * @return
     */
    @GetMapping(value = "/spuSaleAttrList/{id}")
    public Result<List<SpuSaleAttr>> spuSaleAttrList(@PathVariable(value = "id") Long id){
        return Result.ok(productService.getSpuSaleAttrBySpuId(id));
    }

    /**
     * 新增/修改sku的信息
     * @param skuInfo
     * @return
     */
    @PostMapping(value = "/saveSkuInfo")
    public Result<SkuInfo> saveSkuInfo(@RequestBody SkuInfo skuInfo){
        return Result.ok(productService.saveSkuInfo(skuInfo));
    }

    /**
     * 查询sku的列表信息
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/list/{page}/{size}")
    public Result list(@PathVariable(value = "page") Integer page,
                       @PathVariable(value = "size") Integer size){
        return Result.ok(productService.getSkuInfo(page, size));
    }

    /**
     * 上架
     * @param skuId
     * @return
     */
    @GetMapping(value = "/onSale/{skuId}")
    public Result onSale(@PathVariable(value = "skuId") Long skuId){
        productService.updateSkuInfo(skuId, ProductConst.SKU_ON_SALE);
        return Result.ok();
    }

    /**
     * 下架
     * @param skuId
     * @return
     */
    @GetMapping(value = "/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable(value = "skuId") Long skuId){
        //魔法值
        productService.updateSkuInfo(skuId, ProductConst.SKU_CANCLE_SALE);
        return Result.ok();
    }

    /**
     * 获取首页的分类信息
     * @return
     */
    @GetMapping(value = "/getIndexCategory")
    public Result getIndexCategory(){
        return Result.ok(productService.getIndexCategory());
    }
}
