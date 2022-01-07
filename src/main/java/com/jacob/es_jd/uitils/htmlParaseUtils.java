package com.jacob.es_jd.uitils;

import com.jacob.es_jd.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class htmlParaseUtils {
//
//    public static void main(String[] args) throws IOException {
//        //获取请求 https://search.jd.com/Search?keyword=java
//        //前提 需要联网
//
//        String url = "https://search.jd.com/Search?keyword=java";
//
//        //解析网页Jsoup的document就是浏览器的document对象
//        Document document = Jsoup.parse(new URL(url), 30000);
//        Element element = document.getElementById("J_goodsList");
//
//        //获取所有的LI元素
//        Elements elements = element.getElementsByTag("li");
//        //获取元素里的内容
//        for (Element element1 : elements) {
//            //关于图片，所有的图片都是延时加载的
//            //source-data-lazy-img
//            String imgs = element1.getElementsByTag("img").eq(0).attr("data-lazy-img");
//            String price = element1.getElementsByClass("p-price").eq(0).text();
//            String title = element1.getElementsByClass("p-name").eq(0).text();
//
//            System.out.println("++++++++++++++++++++++");
//            System.out.println(imgs);
//            System.out.println(price);
//            System.out.println(title);
//
//        }
//
//    }
//
//    public static void main(String[] args) throws IOException {
//        new htmlParaseUtils().ParseJD("rtx3080").forEach(System.out::println);
//    }

    //解析京东请求
    public List<Content> ParseJD(String keywords) throws IOException {

        String url = "https://search.jd.com/Search?keyword="+keywords;
        Document document = Jsoup.parse(new URL(url), 30000);
        Element element = document.getElementById("J_goodsList");
        Elements elements = element.getElementsByTag("li");

        ArrayList<Content> goods = new ArrayList<>();

        for (Element element1 : elements) {

            String imgs = element1.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = element1.getElementsByClass("p-price").eq(0).text();
            String title = element1.getElementsByClass("p-name").eq(0).text();

            Content content = new Content();
            content.setImg(imgs);
            content.setPrice(price);
            content.setTitle(title);

            goods.add(content);
        }

        return goods;
    }

}
