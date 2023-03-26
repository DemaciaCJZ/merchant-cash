package com.cjz.pipeline;

import com.alibaba.fastjson.JSON;
import com.cjz.handler.ItemBrandModelHandler;
import com.cjz.handler.ItemHandler;
import com.cjz.handler.ItemPriceHandler;
import com.cjz.handler.ItemXianYuSearchHandler;
import com.cjz.model.YahooDetail;
import com.cjz.model.YahooListItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.FilePersistentBase;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Write results in console.<br>
 * Usually used in test.
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
@Slf4j
public class FileCashPipeline extends FilePersistentBase implements Pipeline {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String, YahooListItem> itemMap = new HashMap<>();

    public FileCashPipeline() {
        this.setPath("/data/webmagic");
    }

    public FileCashPipeline(String path) {
        this.setPath(path);
    }

    private static ItemHandler hander;

    static {
        hander = new ItemBrandModelHandler();
        ItemPriceHandler itemPriceHandler = new ItemPriceHandler();
        ItemXianYuSearchHandler itemXianYuSearchHandler =  new ItemXianYuSearchHandler();
        hander.setNextItemHandler(itemPriceHandler);
        itemPriceHandler.setNextItemHandler(itemXianYuSearchHandler);
    }

    public void process(ResultItems resultItems, Task task) {
        //23-03-19_23/uuid/
        String path = this.path + DateFormatUtils.format(new Date(),"yy-MM-dd_HH") + PATH_SEPERATOR + task.getUUID() + PATH_SEPERATOR;
        if (null != resultItems.get("listItem")) {
            List<YahooListItem> list = resultItems.get("listItem");
            for (YahooListItem yahooListItem : list) {
                log.info("id:{} - yahooListItem: {}" , yahooListItem.getId(),yahooListItem);
                itemMap.put(yahooListItem.getId(),yahooListItem);
            }
        } else if (null != resultItems.get("detail")){
            YahooDetail detail = resultItems.get("detail");
            String id = detail.getId();
            log.info("id:{} - detail: {}" , id,detail);
            YahooListItem yahooListItem = itemMap.get(id);
            yahooListItem.setDetail(detail);
            hander.doHandler(yahooListItem);
            try {
                PrintWriter printWriter = new PrintWriter(new FileWriter(this.getFile(path + (Objects.isNull(id) ? DigestUtils.md5Hex(resultItems.getRequest().getUrl()):id) + ".json")));
                printWriter.write(JSON.toJSONString(yahooListItem));
                printWriter.close();
            } catch (IOException var5) {
                this.logger.warn("write file error", var5);
            } finally {
                itemMap.remove(id);
            }
        } else {
            log.error("找不到对象！:{}" , JSON.toJSONString(resultItems.getAll()));
        }
    }
}
