package com.my.fmall.list.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.my.fmall.bean.SkuLsInfo;
import com.my.fmall.bean.SkuLsParams;
import com.my.fmall.bean.SkuLsResult;
import com.my.fmall.config.RedisUtil;
import com.my.fmall0911.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * author:zxy
 *
 * @create 2021-09-23 15:04
 */
@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;

    @Autowired
    private RedisUtil redisUtil;

    public static final String ES_INDEX = "fmall";
    public static final String ES_TYPE = "SkuInfo";

    /**
     * 定义动作 增加
     * 执行动作
     *
     * @param skuLsInfo
     */
    @Override
    public void saveSkuInfo(SkuLsInfo skuLsInfo) {
        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();

        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 搜索商品
     *
     * @param skuLsParams
     * @return
     */
    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {
        /*
         * 1、定义dsl语句
         * 2、定义动作
         * 3、执行动作
         * 4、获取结果集
         * */
        //动态生成的dsl语句
        String query = makeQueryStringForSearch(skuLsParams);
        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SkuLsResult skuLsResult = makeResultForSearch(searchResult,skuLsParams);
        return skuLsResult;
    }


    //完全根据手写的dsl语句！
    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {
        //定义一个查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //判断关键字keyword是否为空
        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0) {
            //创建match
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            //创建must
            boolQueryBuilder.must(matchQueryBuilder);
            //设置高亮
            HighlightBuilder highlighter = searchSourceBuilder.highlighter();
            //设置高亮的规则
            highlighter.field("skuName");
            highlighter.preTags("<span style=color:red>");
            highlighter.postTags("</span>");

            //将设置号的高亮对象放入查询器中
            searchSourceBuilder.highlight(highlighter);
        }

        //判断平台属性值Id
        if(skuLsParams.getValueId()!=null && skuLsParams.getValueId().length > 0){
            for (String valueId : skuLsParams.getValueId()) {
                //创建term
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId",valueId);
                //创建filter 并添加term
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        //判断 三级分类Id
        if(skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length() > 0){
            //创建term
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id());
            //创建filter 并添加term
            boolQueryBuilder.filter(termQueryBuilder);
        }

        searchSourceBuilder.query(boolQueryBuilder);
        //设置分页
        //from 从第几条开始查询
        //10条 每页 3 第一页：0 3，第二页 3 3，第三页 6，3
        int from = (skuLsParams.getPageNo()-1)* skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        //size 每页显示的条数
        searchSourceBuilder.size(skuLsParams.getPageSize());

        //设置排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        //聚合
        //创建一个对象 aggs:--terms
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr");
        //"field": "skuAttrValueList.valueId"
        groupby_attr.field("skuAttrValueList.valueId");
        //aggs 放入查询器
        searchSourceBuilder.aggregation(groupby_attr);

        String query = searchSourceBuilder.toString();

        System.out.println("query:="+query);

        return query;
    }

    /**
     *
     * @param searchResult  通过dsl 语句查询出来的结果
     * @param skuLsParams  查询的参数
     * @return
     */
    private SkuLsResult makeResultForSearch(SearchResult searchResult,SkuLsParams skuLsParams) {
        //声明对象
        SkuLsResult skuLsResult = new SkuLsResult();
        //声明一个集合来存储SkuLsInfo数据
        List<SkuLsInfo> skuLsInfoArrayList = new ArrayList<>();
        //给集合赋值
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);   
        //循环遍历
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo skuLsInfo = hit.source;
            //获取skuName的高亮
            if(hit.highlight!=null && hit.highlight.size() > 0){
                Map<String,List<String>> highlight =   hit.highlight;
                List<String> list = highlight.get("skuName");
                String skuNameHI = list.get(0);
                skuLsInfo.setSkuName(skuNameHI);
            }
            skuLsInfoArrayList.add(skuLsInfo);
        }
        skuLsResult.setSkuLsInfoList(skuLsInfoArrayList);
        skuLsResult.setTotal(searchResult.getTotal());
        //如何计算总页数
        //10 条数据 每页显示3条 几页？4
        long totalPages = (searchResult.getTotal()+skuLsParams.getPageSize()-1)/ skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPages);

        //声明一个集合来存储平台属性值Id
        ArrayList<String> stringArrayList = new ArrayList<>();
        //获取平台属性值Id
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        //循环遍历
        for (TermsAggregation.Entry bucket : buckets) {
            String valueId = bucket.getKey();
            stringArrayList.add(valueId);

        }
        skuLsResult.setAttrValueIdList(stringArrayList);
        return skuLsResult;
    }

    /**
     * 更新热度评分
     * @param skuId
     */
    @Override
    public void incrHotScore(String skuId) {
        Jedis jedis = redisUtil.getJedis();

        int timeToEs = 10; //点击10次，更新ES一次，（每点一次，更新redis一次）
        Double hotScore = jedis.zincrby("hotScore", 1, "skuId:" + skuId);

        if(hotScore % timeToEs == 0){
            updateHotScore(skuId,Math.round(hotScore));
        }

    }
    /*
    * 更新ES
    * */
    private void updateHotScore(String skuId, long hotScore) {
        String updJson ="{\n" +
                "    \"doc\":{\n" +
                "       \"hotScore\":"+hotScore+"\n" +
                "       \n" +
                "    }\n" +
                "  \n" +
                "}";
        /*
        * 1、写DSL语句
        * 2、定义动作
        * 3、执行动作
        * 4、获取结果集
        * */

        Update update = new Update.Builder(updJson).index(ES_INDEX).type(ES_TYPE).id(skuId).build();
        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
