package com.cjz.model;

import lombok.Data;

@Data
public class YahooListItem {

    private String id;
    private String url;
    private String imageUrl;
    // 目前133,000 日元 迅速决定145,000 日元
    // 拍卖价
    private String auctionPrice;
    // 立即下单价格
    private String takeNowPrice;
    // 是否为拍卖物品
    private boolean isAuction;
    // 详情
    private YahooDetail detail;

    public YahooListItem(String id, String url) {
        this.id = id;
        this.url = url;
    }
}
