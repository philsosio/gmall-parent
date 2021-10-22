package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchResponseAttrVo;
import com.atguigu.gmall.model.list.SearchResponseTmVo;
import jdk.nashorn.internal.ir.LiteralNode;
import lombok.extern.log4j.Log4j2;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@Log4j2
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 搜索功能
     *
     * @param searchData
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchData) {
        //返回结果初始化
        Map<String, Object> result = new HashMap<>();
        //构建查询的条件
        SearchRequest searchRequest = new SearchRequest("goods_java0107");
        SearchSourceBuilder builder = buildSearchParams(searchData);
        searchRequest.source(builder);
        try {
            //执行查询
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //解析结果
            result = getSearchResult(search);
            result.put("pageSize", 100);
            result.put("page", builder.from() / 100 + 1);

        }catch (Exception e){
            log.error(e.getMessage());
        }
        return result;
    }

    /**
     * 构建查询条件
     * @param searchData
     * @return
     */
    private SearchSourceBuilder buildSearchParams(Map<String, String> searchData) {
        //初始化条件构造器
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //组合查询的构造器
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //关键字判断
        String keywords = searchData.get("keywords");
        if(!StringUtils.isEmpty(keywords)){
//            builder.query(QueryBuilders.matchQuery("title", keywords));
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", keywords));
        }
        //品牌查询tradeMark=1:华为
        String tradeMark = searchData.get("tradeMark");
        if(!StringUtils.isEmpty(tradeMark)){
            String[] split = tradeMark.split(":");
            boolQueryBuilder.must(QueryBuilders.termQuery("tmId", split[0]));
        }
        //平台属性条件整合:attr_摄像头像素=1:1200万以上&attr_价格=2:0-500元
        for (String s : searchData.keySet()) {
            if(s.startsWith("attr_")){
                String value = searchData.get(s);
                String[] split = value.split(":");
                //只有大于1的时候才作为查询的条件
                if(split.length > 1){
                    NestedQueryBuilder nestedQuery =
                            QueryBuilders.nestedQuery("attrs",
                                    QueryBuilders.boolQuery()
                                            .must(QueryBuilders.termQuery("attrs.attrId",split[0]))
                                            .must(QueryBuilders.termQuery("attrs.attrValue", split[1]))
                                    , ScoreMode.None);
                    boolQueryBuilder.must(nestedQuery);
                }
            }
        }
        //价格: 500-1000元
        String price = searchData.get("price");
        if(!StringUtils.isEmpty(price)){
            //500-1000
            price = price.replace("元", "").replace("以上", "");
            //0---500 1---1000
            String[] split = price.split("-");
            boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gte(split[0]));
            if(split.length > 1){
                boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lt(split[1]));
            }
        }
        //条件整合
        builder.query(boolQueryBuilder);
        //品牌的聚合查询,设置聚合的对象
        builder.aggregation(
                AggregationBuilders.terms("aggTmId").field("tmId")
                    .subAggregation(AggregationBuilders.terms("aggTmName").field("tmName"))
                    .subAggregation(AggregationBuilders.terms("aggTmLogoUrl").field("tmLogoUrl"))
                    .size(10000000)
        );
        //设置平台属性的聚合条件
        builder.aggregation(
                AggregationBuilders.nested("aggAttrs" ,"attrs")
                        .subAggregation(
                                AggregationBuilders.terms("aggAttrId").field("attrs.attrId")
                                    .subAggregation(AggregationBuilders.terms("aggAttrName").field("attrs.attrName"))
                                    .subAggregation(AggregationBuilders.terms("aggAttrValue").field("attrs.attrValue"))
                                    .size(100000000)
                        )
        );
        //设置分页的信息  1---0-99 100-199
        builder.size(100);
        Integer page = getPage(searchData.get("page"));
        builder.from((page - 1)* 100);
        //设置排序:&soft=price:DESC
        String soft = searchData.get("soft");
        if(!StringUtils.isEmpty(soft)){
            String[] split = soft.split(":");
            if(split.length > 1){
                builder.sort(split[0], SortOrder.valueOf(split[1]));
            }
        }else{
            //默认排序规则
            builder.sort("hotScore", SortOrder.DESC);
        }
        //高亮查询
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<font style='color:green'>");
        highlightBuilder.postTags("</font>");
        builder.highlighter(highlightBuilder);

        return builder;
    }

    /**
     * 获取页码
     * @param page
     * @return
     */
    private Integer getPage(String page){
        try {
            return Integer.parseInt(page)<=0?1:Integer.parseInt(page);
        }catch (Exception e){
            return 1;
        }
    }

    /**
     * 解析结果
     * @param search
     * @return
     */
    private Map<String, Object> getSearchResult(SearchResponse search) {
        Map<String, Object> result = new HashMap<>();
        //获取搜索的结果
        SearchHits hits = search.getHits();
        //获取迭代器
        Iterator<SearchHit> iterator = hits.iterator();
        List<Goods> goodsList = new ArrayList<>();
        while (iterator.hasNext()){
            SearchHit next = iterator.next();
            //获取字符串的结果
            String sourceAsString = next.getSourceAsString();
            //反序列化
            Goods goods = JSONObject.parseObject(sourceAsString, Goods.class);
            //获取高亮的数据
            HighlightField highlightField = next.getHighlightFields().get("title");
            if(highlightField != null){
                Text[] fragments = highlightField.getFragments();
                if(fragments.length > 0){
                    String title = "";
                    for (Text fragment : fragments) {
                        title += fragment;
                    }
                    goods.setTitle(title);
                }
            }
            goodsList.add(goods);
        }
        //存储商品列表的数据
        result.put("goodsList", goodsList);
        long totalHits = hits.getTotalHits();
        result.put("totalHits", totalHits);
        //获取所有的聚合结果
        Map<String, Aggregation> aggregationMap = search.getAggregations().asMap();
        //获取品牌的聚合结果
        List<SearchResponseTmVo> searchResponseTmVos = getBaseTradeMarkResult(aggregationMap);
        result.put("searchResponseTmVos", searchResponseTmVos);
        //获取平台属性的聚合结果
        List<SearchResponseAttrVo> searchResponseAttrVos = getBaseAttrInfo(aggregationMap);
        result.put("searchResponseAttrVos", searchResponseAttrVos);
        //返回
        return result;
    }

    /**
     * 获取平台属性的聚合结果
     * @param aggregationMap
     */
    private List<SearchResponseAttrVo> getBaseAttrInfo(Map<String, Aggregation> aggregationMap) {
        List<SearchResponseAttrVo> searchResponseAttrVoList = new ArrayList<>();
        //根据聚合的条件设置的别名获取所有的平台属性nested类型的聚合结果
        ParsedNested aggregation = (ParsedNested)aggregationMap.get("aggAttrs");
        //获取平台属性id的聚合结果
        ParsedLongTerms aggAttrId = aggregation.getAggregations().get("aggAttrId");
        for (Terms.Bucket bucket : aggAttrId.getBuckets()) {
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            //获取平台属性的id
            Number attrId = bucket.getKeyAsNumber();
            searchResponseAttrVo.setAttrId(attrId.longValue());
            //获取平台属性的名称
            ParsedStringTerms aggAttrName = bucket.getAggregations().get("aggAttrName");
            if(!aggAttrName.getBuckets().isEmpty()){
                String attrName = aggAttrName.getBuckets().get(0).getKeyAsString();
                searchResponseAttrVo.setAttrName(attrName);
            }
            //获取平台属性的值
            ParsedStringTerms aggAttrValue = bucket.getAggregations().get("aggAttrValue");
            List<String> attrValueList = new ArrayList<>();
            for (Terms.Bucket valueBucket : aggAttrValue.getBuckets()) {
                String value = valueBucket.getKeyAsString();
                attrValueList.add(value);
            }
            searchResponseAttrVo.setAttrValueList(attrValueList);
            searchResponseAttrVoList.add(searchResponseAttrVo);
        }

        return searchResponseAttrVoList;
    }

    /**
     * 获取品牌的聚合结果
     * @param aggregationMap
     */
    private List<SearchResponseTmVo> getBaseTradeMarkResult(Map<String, Aggregation> aggregationMap) {
        List<SearchResponseTmVo> searchResponseTmVos = new ArrayList<>();
        //根据聚合的条件中设置的别名获取所有的品牌的聚合结果
        ParsedLongTerms aggTmId = (ParsedLongTerms)aggregationMap.get("aggTmId");
        //获取所有的品牌的聚合信息
        for (Terms.Bucket bucket : aggTmId.getBuckets()) {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //获取品牌的id
            Number tmId = bucket.getKeyAsNumber();
            searchResponseTmVo.setTmId(tmId.longValue());
            //获取品牌的名称
            ParsedStringTerms tmNameAgg = bucket.getAggregations().get("aggTmName");
            if(!tmNameAgg.getBuckets().isEmpty() && tmNameAgg.getBuckets().size() > 0){
                String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmName(tmName);
            }
            //品牌的logo
            ParsedStringTerms tmLogoAgg = bucket.getAggregations().get("aggTmLogoUrl");
            if(!tmLogoAgg.getBuckets().isEmpty() && tmLogoAgg.getBuckets().size() > 0){
                String tmLogo = tmLogoAgg.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmLogoUrl(tmLogo);
            }
            searchResponseTmVos.add(searchResponseTmVo);
        }
        return searchResponseTmVos;
    }
}
