package com.cjz.model;

import lombok.Data;

@Data
public class YahooDetail {

    private String id;
    private String url;
    // 标题
    private String title;
    // 拍卖价
    private String auctionPrice;
    // 立即下单价格
    private String takeNowPrice;
    // 税费
    private String taxation;
    // 品牌
    private String brandModel;
    // 商品详情
    private String content;

    public YahooDetail(String id, String url) {
        this.id = id;
        this.url = url;
    }

}
