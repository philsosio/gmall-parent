package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.ProductConst;
import com.atguigu.gmall.list.feign.ListFeign;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理控制台的实现类
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private BaseCategory1Mapper baseCategory1Mapper;
    /**
     * 查询所有的一级分类
     *
     * @return
     */
    @Override
    public List<BaseCategory1> findCategory1() {
        return baseCategory1Mapper.selectList(null);
    }


    @Resource
    private BaseCategory2Mapper baseCategory2Mapper;
    /**
     * 根据一级分类查询二级分类
     *
     * @param id
     * @return
     */
    @Override
    public List<BaseCategory2> findCategory2(Long id) {
        LambdaQueryWrapper<BaseCategory2> wrapper =
                new LambdaQueryWrapper<BaseCategory2>().eq(BaseCategory2::getCategory1Id, id);
        return baseCategory2Mapper.selectList(wrapper);
    }

    @Resource
    private BaseCategory3Mapper baseCategory3Mapper;
    /**
     * 根据二级分类查询三级分类
     *
     * @param id
     * @return
     */
    @Override
    public List<BaseCategory3> findCategory3(Long id) {
        LambdaQueryWrapper<BaseCategory3> wrapper =
                new LambdaQueryWrapper<BaseCategory3>().eq(BaseCategory3::getCategory2Id, id);
        return baseCategory3Mapper.selectList(wrapper);
    }

    @Resource
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Resource
    private BaseAttrValueMapper baseAttrValueMapper;
    /**
     * 保存平台属性
     *
     * @param baseAttrInfo
     * @return
     */
    @Override
    public BaseAttrInfo saveBaseAttrInfo(BaseAttrInfo baseAttrInfo) {
        //参数校验
        if(baseAttrInfo == null){
            throw new RuntimeException("参数错误");
        }

        //判断当前是修改还是新增
        if(baseAttrInfo.getId() == null){
            //新增
            //保存平台属性表信息
            int insert = baseAttrInfoMapper.insert(baseAttrInfo);
            if(insert <= 0){
                throw new RuntimeException("新增失败!!!");
            }
        }else{
            //修改
            baseAttrInfoMapper.updateById(baseAttrInfo);
            //删除value表的数据
            baseAttrValueMapper.delete(
                    new LambdaQueryWrapper<BaseAttrValue>()
                            .eq(BaseAttrValue::getAttrId, baseAttrInfo.getId()));
        }
        //保存平台属性值表的信息----list
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
//        //---方案一:传统方案
//        for (BaseAttrValue baseAttrValue : attrValueList) {
//            //补全平台属性名称的id
//            baseAttrValue.setAttrId(baseAttrInfo.getId());
//            //新增
//            baseAttrValueMapper.insert(baseAttrValue);
//        }
        //---方案二:流方案
        attrValueList = attrValueList.stream().map(value -> {
            //补全平台属性名称的id
            value.setAttrId(baseAttrInfo.getId());
            //新增
            baseAttrValueMapper.insert(value);
            //返回
            return value;
        }).collect(Collectors.toList());
        //放回原对象
        baseAttrInfo.setAttrValueList(attrValueList);
        //返回结果
        return baseAttrInfo;
    }

    /**
     * 根据一级二级三级分类查询平台属性信息
     *
     * @param category1
     * @param category2
     * @param category3
     * @return
     */
    @Override
    public List<BaseAttrInfo> getBaseAttrInfoList(Long category1, Long category2, Long category3) {
        return baseAttrInfoMapper.selectBaseAttrInfoByCategoryId(category1,category2, category3);
    }

    /**
     * 根据平台属性id查询平台属性值
     *
     * @param id
     * @return
     */
    @Override
    public List<BaseAttrValue> getBaseAttrValue(Long id) {
        return baseAttrValueMapper.selectList(new LambdaQueryWrapper<BaseAttrValue>().eq(BaseAttrValue::getAttrId, id));
    }


    @Resource
    private BaseTradeMarkMapper baseTradeMarkMapper;
    /**
     * 查询品牌列表
     *
     * @return
     */
    @Override
    public List<BaseTrademark> getBaseTrademarkList() {
        return baseTradeMarkMapper.selectList(null);
    }


    @Resource
    private BaseSaleAttrMapper baseSaleAttrMapper;
    /**
     * 查询销售属性列表
     *
     * @return
     */
    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }


    @Resource
    private SpuInfoMapper spuInfoMapper;

    @Resource
    private SpuImageMapper spuImageMapper;

    @Resource
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Resource
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    /**
     * 新增spu
     *
     * @param spuInfo
     */
    @Override
    public SpuInfo saveSpu(SpuInfo spuInfo) {
        //参数校验
        if(spuInfo == null){
            throw new RuntimeException("参数错误!");
        }
        //判断当前spuInfo中有没有id,
        if(spuInfo.getId() != null){
            //如果有则是修改
            spuInfoMapper.updateById(spuInfo);
            //---1.删除spu的图片
            spuImageMapper.delete(
                    new LambdaQueryWrapper<SpuImage>()
                            .eq(SpuImage::getSpuId, spuInfo.getId()));
            //2.销售属性
            spuSaleAttrMapper.delete(
                    new LambdaQueryWrapper<SpuSaleAttr>()
                            .eq(SpuSaleAttr::getSpuId, spuInfo.getId()));
            //3.销售属性值信息
            spuSaleAttrValueMapper.delete(
                    new LambdaQueryWrapper<SpuSaleAttrValue>()
                            .eq(SpuSaleAttrValue::getSpuId, spuInfo.getId()));
        }else{
            //如果没有则是新增spuInfo,以后可以获取spu的id
            spuInfoMapper.insert(spuInfo);
        }
        //新增图片表
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        List<SpuImage> spuImageListNew = spuImageList.stream().map(image -> {
            //补全数据
            image.setSpuId(spuInfo.getId());
            //新增
            spuImageMapper.insert(image);
            //返回
            return image;
        }).collect(Collectors.toList());
        spuInfo.setSpuImageList(spuImageListNew);
        //新增销售属性名称表,同时新增销售属性值表
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        List<SpuSaleAttr> spuSaleAttrListNew = spuSaleAttrList.stream().map(saleAttr -> {
            //补全数据
            saleAttr.setSpuId(spuInfo.getId());
            //新增
            spuSaleAttrMapper.insert(saleAttr);
            //新增销售属性值
            List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
            List<SpuSaleAttrValue> spuSaleAttrValueListNew = spuSaleAttrValueList.stream().map(value -> {
                //补全数据
                value.setSpuId(spuInfo.getId());
                value.setSaleAttrName(saleAttr.getSaleAttrName());
                //新增
                spuSaleAttrValueMapper.insert(value);
                //返回
                return value;
            }).collect(Collectors.toList());
            saleAttr.setSpuSaleAttrValueList(spuSaleAttrValueListNew);
            //返回
            return saleAttr;
        }).collect(Collectors.toList());
        spuInfo.setSpuSaleAttrList(spuSaleAttrListNew);
        //返回结果
        return spuInfo;
    }


    /**
     * 根据三级分类id查询spu的列表信息
     *
     * @param cid
     * @param page
     * @param size
     * @return
     */
    @Override
    public IPage<SpuInfo> findSpuByCategory3Id(Long cid, Integer page, Integer size) {
        return spuInfoMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<SpuInfo>().eq(SpuInfo::getCategory3Id, cid));
    }

    /**
     * 根据spu的id查询spu的图片列表
     *
     * @param id
     * @return
     */
    @Override
    public List<SpuImage> findBySpuId(Long id) {
        return spuImageMapper.selectList(new LambdaQueryWrapper<SpuImage>().eq(SpuImage::getSpuId, id));
    }

    /**
     * 根据spu的id查询spu的销售属性列表
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrBySpuId(Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrBySpuId(spuId);
    }

    @Resource
    private SkuInfoMapper skuInfoMapper;

    @Resource
    private SkuImageMapper skuImageMapper;

    @Resource
    private SkuAttrValueMapper skuAttrValueMapper;

    @Resource
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    /**
     * 新增/修改sku
     *
     * @param skuInfo
     * @return
     */
    @Override
    public SkuInfo saveSkuInfo(SkuInfo skuInfo) {
        //参数校验
        if(skuInfo == null){
            throw new RuntimeException("参数错误");
        }
        Long skuId = skuInfo.getId();
        //判断skuInfo中id是否为空,不为空则修改
        if(skuId != null){
            //修改skuinfo
            skuInfoMapper.updateById(skuInfo);
            //删除图片
            skuImageMapper.delete(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId,skuInfo.getId()));
            //删除销售属性
            skuSaleAttrValueMapper.delete(new LambdaQueryWrapper<SkuSaleAttrValue>().eq(SkuSaleAttrValue::getSkuId,skuInfo.getId()));
            //删除平台属性
            skuAttrValueMapper.delete(new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId,skuInfo.getId()));


        }else{
            //为空则新增skuInfo
            skuInfoMapper.insert(skuInfo);
        }
        //新增sku图片
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        List<SkuImage> skuImageListNew = skuImageList.stream().map(image -> {
            //补全参数
            image.setSkuId(skuInfo.getId());
            //新增
            skuImageMapper.insert(image);
            //返回
            return image;
        }).collect(Collectors.toList());
        skuInfo.setSkuImageList(skuImageListNew);
        //新增sku的平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        List<SkuAttrValue> skuAttrValueListNew = skuAttrValueList.stream().map(attrValue -> {
            //补全数据
            attrValue.setSkuId(skuInfo.getId());
            //新增
            skuAttrValueMapper.insert(attrValue);
            //返回
            return attrValue;
        }).collect(Collectors.toList());
        skuInfo.setSkuAttrValueList(skuAttrValueListNew);
        //新增sku的销售属性
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        List<SkuSaleAttrValue> skuSaleAttrValueListNew = skuSaleAttrValueList.stream().map(saleAttr -> {
            //补全数据
            saleAttr.setSpuId(skuInfo.getSpuId());
            saleAttr.setSkuId(skuInfo.getId());
            //新增
            skuSaleAttrValueMapper.insert(saleAttr);
            //返回
            return saleAttr;
        }).collect(Collectors.toList());
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueListNew);
        //返回结果
        return skuInfo;
    }

    /**
     * sku的分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public IPage<SkuInfo> getSkuInfo(Integer page, Integer size) {
        return skuInfoMapper.selectPage(new Page<>(page, size), null);
    }

    @Resource
    private ListFeign listFeign;


    @Resource
    private RabbitTemplate rabbitTemplate;
    /**
     * 商品上下架: 0--下架 1---上架
     *
     * @param skuId
     * @param status
     */
    @Override
    public void updateSkuInfo(Long skuId, Short status) {
        //参数判断
        if(skuId == null || status == null){
            throw new RuntimeException("参数错误!!!");
        }
        //查询商品信息
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if(skuInfo == null || skuInfo.getId() == null){
            throw new RuntimeException("商品不存在!!!");
        }
        //修改商品的上下架状态
        skuInfo.setIsSale(status);
        skuInfoMapper.updateById(skuInfo);
        //存在失败的可能性,后序会进行优化为Mq消息队列实现数据同步--->异步
        if(status.equals(ProductConst.SKU_CANCLE_SALE)){
            //0--下架---将es中的数据进行删除
//            listFeign.del(skuId);
            rabbitTemplate.convertAndSend("product_exchange", "product.cancle.sale", JSONObject.toJSONString(skuInfo));
        }else{
            //1---上架将数据同步到es中去
//            listFeign.upper(skuInfo);
            rabbitTemplate.convertAndSend("product_exchange", "product.on.sale", skuId +"");
        }
    }

    @Resource
    private BaseCategoryViewMapper baseCategoryViewMapper;
    /**
     * 查询首页的分类信息列表
     *
     * @return
     */
    @Override
    public List<JSONObject> getIndexCategory() {
        List<JSONObject> category1JsonList = new ArrayList<>();
        //查询所有的分类信息:包含重复的一级分类,重复的二级分类和不重复的三级分类
        List<BaseCategoryView> categoryViewList = baseCategoryViewMapper.selectList(null);
        //先对一级分类进行分桶:有多少个一级分类就有多少个Key
        Map<Long, List<BaseCategoryView>> category1Map =
                categoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        //对每一个一级分类进行再操作
        for (Map.Entry<Long, List<BaseCategoryView>> category1 : category1Map.entrySet()) {
            JSONObject category1Json = new JSONObject();
            //一级分类的id
            Long category1Id = category1.getKey();
            category1Json.put("categoryId", category1Id);
            //所有一级分类等于当前category1Id的二级和三级分类
            List<BaseCategoryView> category2List = category1.getValue();
            if(!category2List.isEmpty() && category2List.size() > 0){
                String category1Name = category2List.get(0).getCategory1Name();
                category1Json.put("categoryName", category1Name);
            }
            //对二级分类进行分桶:有多少个二级分类,就有多少个key
            Map<Long, List<BaseCategoryView>> category2Map =
                    category2List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            //对每一个二级分类进行遍历
            List<JSONObject> category2JsonList = new ArrayList<>();
            for (Map.Entry<Long, List<BaseCategoryView>> category2 : category2Map.entrySet()) {
                JSONObject category2Json = new JSONObject();
                //获取当前二级分类的id
                Long category2Id = category2.getKey();
                category2Json.put("categoryId", category2Id);
                //获取二级分类的名字
                List<BaseCategoryView> category3List = category2.getValue();
                if(!category3List.isEmpty() && category3List.size() > 0){
                    String category2Name = category3List.get(0).getCategory2Name();
                    category2Json.put("categoryName", category2Name);
                }
                //遍历获取三级分类的信息
                List<JSONObject> category3JsonList = category3List.stream().map(c -> {
                    JSONObject category3Json = new JSONObject();
                    //获取三级分类的id
                    Long category3Id = c.getCategory3Id();
                    category3Json.put("categoryId", category3Id);
                    //获取三级分类的名字
                    String category3Name = c.getCategory3Name();
                    category3Json.put("categoryName", category3Name);
                    //返回结果
                    return category3Json;
                }).collect(Collectors.toList());
                category2Json.put("childCategory", category3JsonList);
                category2JsonList.add(category2Json);
            }
            category1Json.put("childCategory", category2JsonList);
            category1JsonList.add(category1Json);
        }
        return category1JsonList;
    }


}
