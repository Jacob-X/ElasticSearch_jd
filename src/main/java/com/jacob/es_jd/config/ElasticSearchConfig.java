package com.jacob.es_jd.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;


//两步骤：
//1.找对象
//2.放Springboot中用
//es放spring boot集成，直接用client对象

@Service
public class ElasticSearchConfig {

    @Bean
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost",9200,"http")
                )
        );
        return client;
    }




}
