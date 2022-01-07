package com.jacob.es_jd;

import com.alibaba.fastjson.JSON;
import com.jacob.es_jd.pojo.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class EsJdApplicationTests {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    //高级客户端。索引api内容
    //测试索引的创建
    @Test
    void creatIndex() throws IOException {
        //1.创建索引请求
        CreateIndexRequest jacob_index = new CreateIndexRequest("jacob_index");
        //2.执行创建请求 indicesClient,请求后获得相应
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(jacob_index, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse.toString());
    }


    @Test
        //获取索引
    void getIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("jacob_index");
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    @Test
        //删除索引
    void deleteIndex() throws IOException {
        DeleteIndexRequest deleteRequest = new DeleteIndexRequest("jacob_index");
        AcknowledgedResponse delete = restHighLevelClient.indices().delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(delete);
    }


    @Test
        //添加文档测试
    void testAddText() throws IOException {

        //创建对象
        User user = new User("张三", 2);
        //创建请求
        IndexRequest request = new IndexRequest("jacob_index");
        //规则 put /jacob_index/_doc/1
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));

        //将数据放入请求,是Json格式的
        IndexRequest source = request.source(JSON.toJSONString(user), XContentType.JSON);

        //客户端发送请求,获取相应结果
        IndexResponse index = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        System.out.println(index.toString());
        System.out.println(index.status());//对应命令返回的状态
    }

    //获取文档，判断文档是否存在
    //规则 get /index/1
    @Test
    void getText() throws IOException {

        GetRequest getRequest = new GetRequest("jacob_index", "1");

        //不获取
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");
        boolean exists = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    //修改文档信息
    @Test
    void updateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("jacob_index", "1");
        request.timeout("1s");
        User user = new User("李四", 2);
        UpdateRequest doc = request.doc(JSON.toJSONString(user),XContentType.JSON);
        UpdateResponse update = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        System.out.println(update);

    }


    //删除文档信息
    @Test
    void DeleteDocument() throws IOException {

        DeleteRequest deleteRequest = new DeleteRequest("jacob_index", "1");
        deleteRequest.timeout("1s");
        DeleteResponse delete = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(delete.status());
    }

    //批量导入数据
    @Test
    void BulkAddDocument() throws IOException {

        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");
        ArrayList<User> userArrayList = new ArrayList<>();
        userArrayList.add(new User("张三1",22));
        userArrayList.add(new User("张三2",22));
        userArrayList.add(new User("张三3",22));
        userArrayList.add(new User("张三4",22));
        userArrayList.add(new User("张三5",22));
        userArrayList.add(new User("张三6",22));
        userArrayList.add(new User("张三7",22));
        userArrayList.add(new User("张三8",22));


        //批处理请求
        //批量删除和批量更新都在这里操作
        for (int i = 0; i < userArrayList.size(); i++) {
            //不用id会随机生成id
            bulkRequest.add(
                    new IndexRequest("jacob_index")
                            .id(""+(i+1))
                            .source(JSON.toJSONString(userArrayList.get(i)),XContentType.JSON));

        }

        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulk);
    }



    //查询
    //步骤
    //1.searchRequest 搜索请求
    //2.SearchSourceBuilder 搜索的条件构造
    //XXXX Builder对应实现不同的功能，SearchSourceBuilder源码里都有

    @Test
    void Search() throws IOException {
        SearchRequest searchRequest = new SearchRequest("jacob_index");

        //构建搜索条件
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //查询条件，使用QueryBuilders快速匹配
        //termQuery精确
        //QueryBuilders.matchAllQuery()匹配所有

        TermQueryBuilder termQuery = QueryBuilders.termQuery("name", "张");
        builder.query(termQuery);
        //分页from、size
        //builder.from(0);
        //builder.size(2);

        builder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(builder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);


        //循环遍历查询结果
        System.out.println(JSON.toJSONString(searchResponse.getHits()));
        System.out.println("+++++++++++++++++++++++++++++++++");
        for (SearchHit documentFields : searchResponse.getHits()) {
            System.out.println(documentFields.getSourceAsMap());
        }

    }

}
