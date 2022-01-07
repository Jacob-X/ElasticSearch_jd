package com.jacob.es_jd.controller;


import com.jacob.es_jd.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class ContentController {

    @Autowired
    private ContentService contentService;


    @GetMapping("/parse/{keywords}")
    public Boolean parse(@PathVariable("keywords") String keywords) throws IOException {
        return contentService.ParseContent(keywords);
    }

//普通的搜索
//    @GetMapping("/search/{keywords}/{PageNo}/{Pagesize}")
//    public List<Map<String,Object>> SearchPage(@PathVariable("keywords") String keywords,
//                                               @PathVariable("PageNo")int PageNo,
//                                               @PathVariable("Pagesize")int Pagesize) throws IOException {
//        return contentService.SearchPage(keywords,PageNo,Pagesize);
//    }

    //高亮的关键字的搜索
    @GetMapping("/search/{keywords}/{PageNo}/{Pagesize}")
    public List<Map<String,Object>> SearchHighLightPage(@PathVariable("keywords") String keywords,
                                               @PathVariable("PageNo")int PageNo,
                                               @PathVariable("Pagesize")int Pagesize) throws IOException {
        return contentService.SearchHighLightPage(keywords,PageNo,Pagesize);
    }


}
