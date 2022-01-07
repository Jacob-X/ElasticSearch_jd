package com.jacob.es_jd.service;

import com.alibaba.fastjson.JSON;
import com.jacob.es_jd.pojo.Content;
import com.jacob.es_jd.uitils.htmlParaseUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ContentService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //解析数据，放入es索引中
    public Boolean ParseContent(String keywords) throws IOException {
        List<Content> contents = new htmlParaseUtils().ParseJD(keywords);
        //把所有的数据放到es中
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");
        //循环放入数据
        for (int i = 0; i < contents.size(); i++) {
            bulkRequest.add(
                    new IndexRequest("jd_goods")
                            .source(JSON.toJSONString(contents.get(i)), XContentType.JSON));
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }



    //获取数据，实现基本的搜索功能
    public List<Map<String,Object>> SearchPage(String keywords,int PageNo,int Pagesize) throws IOException {
        if(PageNo<=1){
            PageNo = 1;
        }
        //条件搜索
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //分页
        searchSourceBuilder.from(PageNo);
        searchSourceBuilder.size(Pagesize);
        //精确查找
        TermQueryBuilder termQuery = QueryBuilders.termQuery("title", keywords);
        searchSourceBuilder.query(termQuery);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //执行搜索
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //解析结果
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            list.add(documentFields.getSourceAsMap());
        }
        return list;
    }


    //获取数据，实现高亮的搜索功能
    public List<Map<String,Object>> SearchHighLightPage(String keywords,int PageNo,int Pagesize) throws IOException {
        if(PageNo<=1){
            PageNo = 1;
        }
        //条件搜索
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //分页
        searchSourceBuilder.from(PageNo);
        searchSourceBuilder.size(Pagesize);
        //精确查找
        TermQueryBuilder termQuery = QueryBuilders.termQuery("title", keywords);
        searchSourceBuilder.query(termQuery);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //高亮搜索
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.requireFieldMatch(false);//关闭多个高亮显示
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);


        //执行搜索
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //解析结果
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            //解析高亮的字段
            //1.获取高亮的字段
            Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();//原来的结果
            //高亮就是将原来的字段换为高亮的字段即可
            if(title!=null){
                Text[] fragments = title.getFragments();
                String newTitle = "";
                for (Text fragment : fragments) {
                    newTitle += fragment;
                }
                sourceAsMap.put("title",newTitle);//高亮字段的替换
            }
            list.add(sourceAsMap);
        }
        return list;
    }



}
