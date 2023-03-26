package com.cjz.handler;

import com.cjz.model.YahooListItem;

public abstract class ItemHandler {

    /**
     * 下一个ItemHandler
     */
    protected ItemHandler nextItemHandler;

    /**
     * 处理业务逻辑
     *
     * @return
     */
    public abstract void doHandler(YahooListItem item);

    /**
     * 设置下一个ItemHandler
     *
     * @return
     */
    public void setNextItemHandler(ItemHandler nextItemHandler) {
        this.nextItemHandler = nextItemHandler;
    }

}
