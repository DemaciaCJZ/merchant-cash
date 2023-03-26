package com.cjz.handler;

import com.cjz.model.YahooDetail;
import com.cjz.model.YahooListItem;

public class ItemBrandModelHandler extends ItemHandler {
    public void doHandler(YahooListItem item) {
        YahooDetail detail = item.getDetail();
        String brandModel = detail.getBrandModel();
        if (brandModel.contains("ACOUSTIC")) {


            // 发起推送
            return;
        }


        // 交给下一个处理器判断
        nextItemHandler.doHandler(item);
    }
}
