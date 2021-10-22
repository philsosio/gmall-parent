package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ItemService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(rollbackFor = Exception.class)
public class ItemServiceImpl implements ItemService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    /**
     * 根据id查询sku的详细信息
     *
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        return skuInfoMapper.selectById(skuId);
    }

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 根据id查询sku的详细信息
     *
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfoFromRedisOrDb(Long skuId) {
        //从redis中获取sku的数据,有数据直接返回: sku:1:info
        SkuInfo skuInfo = (SkuInfo)redisTemplate.boundValueOps("sku:" + skuId + ":info").get();
        if(skuInfo != null){
            return skuInfo;
        }else{
            //获取锁:sku:1:lock
            RLock lock = redissonClient.getLock("sku:" + skuId + ":lock");
            try {
                //尝试加锁,加锁成功,则查询数据库
                if(lock.tryLock(5, 2, TimeUnit.SECONDS)){
                    skuInfo = skuInfoMapper.selectById(skuId);
                    //将数据缓存到redis中去
                    if(skuInfo == null || skuInfo.getId() == null){
                        //将空值也进行缓存,防止穿透
                        skuInfo = new SkuInfo();
                        redisTemplate.boundValueOps("sku:" + skuId + ":info").set(skuInfo, 180, TimeUnit.SECONDS);
                    }else{
                        //缓存值到reids中去
                        redisTemplate.boundValueOps("sku:" + skuId + ":info").set(skuInfo, 24 * 60 * 60, TimeUnit.SECONDS);
                    }
                    //返回数据
                    return skuInfo;
                }
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }finally {
                lock.unlock();
            }
        }
        return null;
    }

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;
    /**
     * 根据三级分类查询分类的详情
     *
     * @param c3id
     * @return
     */
    @Override
    public BaseCategoryView getCategory(Long c3id) {
        return baseCategoryViewMapper
                .selectOne(
                        new LambdaQueryWrapper<BaseCategoryView>()
                                .eq(BaseCategoryView::getCategory3Id, c3id));
    }

    @Autowired
    private SkuImageMapper skuImageMapper;
    /**
     * 查询图片的列表
     *
     * @param skuId
     * @return
     */
    @Override
    public List<SkuImage> getImageList(Long skuId) {
        return skuImageMapper
                .selectList(
                        new LambdaQueryWrapper<SkuImage>()
                                .eq(SkuImage::getSkuId, skuId));
    }

    /**
     * 查询商品的价格
     *
     * @param skuId
     * @return
     */
    @Override
    public BigDecimal getPrice(Long skuId) {
        return skuInfoMapper.selectById(skuId).getPrice();
    }

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    /**
     * 根据sku和spu的id查询当前商品的所有的销售属性名称和值,并标注出当前的sku的销售属性是哪个
     *
     * @param spuId
     * @param skuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrBySpuAndSku(Long spuId, Long skuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrBySpuIdAndSkuId(spuId, skuId);
    }

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    /**
     * 获取spu下所有sku的键值对
     *
     * @param spuId
     * @return
     */
    @Override
    public Map getSkuSaleAttrValueList(Long spuId) {
        Map<Object, Object> result = new HashMap();
        //获取所有的键值对
        List<Map> maps = skuSaleAttrValueMapper.getSkuSaleAttrValueKeysBySpuId(spuId);
        for (Map map : maps) {
            //skuid = 1
            Object skuId = map.get("sku_id");
            //键值对: 1,4,7
            Object values = map.get("sku_sale_values");
            result.put(values, skuId);
        }
        //遍历获取数据
        return result;
    }

    @Autowired
    private BaseTradeMarkMapper baseTradeMarkMapper;
    /**
     * 查询品牌详情
     *
     * @param id
     * @return
     */
    @Override
    public BaseTrademark getBaseTrademark(Long id) {
        return baseTradeMarkMapper.selectById(id);
    }

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    /**
     * 根据skuid查询平台属性名称和平台属性值
     *
     * @param skuId
     * @return
     */
    @Override
    public List<BaseAttrInfo> getBaseAttrInfoBySkuId(Long skuId) {
        return baseAttrInfoMapper.selectBaseAttrInfoBySkuId(skuId);
    }

    /**
     * 扣减库存
     *
     * @param numMap
     */
    @Override
    public Boolean decountStock(Map<String, Object> numMap) {
        //遍历扣减
        for (Map.Entry<String, Object> entry : numMap.entrySet()) {
            //商品的id
            Long id = Long.parseLong(entry.getKey().toString());
            //商品的库存数量
            Integer num = Integer.parseInt(entry.getValue().toString());
            int i = skuInfoMapper.updateStock(id, num);
            if(i <= 0){
                throw new RuntimeException("修改失败!");
            }
        }
        //返回修改成功
        return true;
    }

    /**
     * 回滚库存
     *
     * @param numMap
     */
    @Override
    public Boolean rollbackStock(Map<String, Object> numMap) {
        //遍历回滚
        for (Map.Entry<String, Object> entry : numMap.entrySet()) {
            //商品的id
            Long id = Long.parseLong(entry.getKey().toString());
            //商品的库存数量
            Integer num = Integer.parseInt(entry.getValue().toString());
            int i = skuInfoMapper.rollbackStock(id, num);
            if(i <= 0){
                throw new RuntimeException("回滚失败!");
            }
        }
        //返回回滚成功
        return true;
    }
}
