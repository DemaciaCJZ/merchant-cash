package com.cjz.handler;

import com.cjz.model.YahooDetail;
import com.cjz.model.YahooListItem;

public class ItemPriceHandler extends ItemHandler {
    public void doHandler(YahooListItem item) {
        YahooDetail detail = item.getDetail();
        String takeNowPrice = detail.getTakeNowPrice();
        String auctionPrice = detail.getAuctionPrice();


        // 交给下一个处理器判断
        nextItemHandler.doHandler(item);
    }
}
