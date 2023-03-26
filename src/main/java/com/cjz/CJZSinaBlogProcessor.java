package com.cjz;

import com.cjz.model.YahooDetail;
import com.cjz.model.YahooListItem;
import com.cjz.pipeline.FileCashPipeline;
import com.cjz.util.RegexUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
public class CJZSinaBlogProcessor  implements PageProcessor {


    //https://auctions.yahoo.co.jp/category/list/23764/?p=%E3%82%AA%E3%83%BC%E3%83%87%E3%82%A3%E3%82%AA%E6%A9%9F%E5%99%A8&auccat=23764&exflg=1&b=1&n=50&s1=bids&o1=a&mode=1
    //https://auctions.yahoo.co.jp/category/list/23764/?p=%E3%82%AA%E3%83%BC%E3%83%87%E3%82%A3%E3%82%AA%E6%A9%9F%E5%99%A8&auccat=23764&exflg=1&b=51&n=50&s1=bids&o1=a&mode=1


    public static final String URL_LIST = "https://auctions.yahoo.co.jp/category/list/.*";

    public static final String URL_POST = "https://page.auctions.yahoo.co.jp/jp/auction/[a-z0-9]*";
    public static final String URL_IMG = "https://.*.jpg.*";

    public static Set<String> LIST_URL = new HashSet<>();


    private Site site = Site
            .me()
            .setDomain("auctions.yahoo.co.jp")
            .setSleepTime(1000)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36");

    @Override
    public void process(Page page) {
        //列表页
        Selectable url = page.getUrl();
        if (url.regex(URL_LIST).match()) {
            List<Selectable> productListItem = page.getHtml().xpath("//div[@class=\"Products__list\"]/ul/li[@class=\"Product\"]").nodes();
            if (CollectionUtils.isNotEmpty(productListItem)) {
                loadListItems(page,productListItem);
            }
            // 爬取所有列表
            List<String> all = page.getHtml().xpath("//div[@class=\"Pager\"]").links().regex(URL_LIST).all();
            List<String> newListUrl = all.stream().filter(s -> !LIST_URL.contains(s)).collect(Collectors.toList());
            LIST_URL.addAll(newListUrl);
            page.addTargetRequests(newListUrl);
        } else {
            //文章页
            loadItem(page);
        }
    }

    private void loadItem(Page page) {
        Selectable url = page.getUrl();
        String id = url.toString().replace("https://page.auctions.yahoo.co.jp/jp/auction/", "");
        if (StringUtils.isNotBlank(id)) {
            YahooDetail detail = new YahooDetail(id,url.toString());
            detail.setTitle(RegexUtil.getHtmlText(page.getHtml().xpath("//div[@class='ProductTitle__title']/h1[@class='ProductTitle__text']").get()));
            String price = RegexUtil.getHtmlText(page.getHtml().xpath("//dd[@class='Price__value']").toString());
            String parentheses = RegexUtil.getParentheses(price);
            if (StringUtils.isNotBlank(parentheses)) {
                detail.setTaxation(RegexUtil.getPriceNum(parentheses));
                price = price.replaceAll(parentheses,"");
            }
            detail.setTakeNowPrice(RegexUtil.getPriceNum(price));
            detail.setContent(page.getHtml().xpath("//div[@id='adoc']//div[@class='ProductExplanation__commentBody js-disabledContextMenu']").get());
            page.putField("detail",detail);
        }
    }

    private void loadListItems(Page page, List<Selectable> productListItem) {
        ResultItems resultItems = page.getResultItems();
        List<YahooListItem> list = new ArrayList<>(productListItem.size());
        for (Selectable productItem : productListItem) {
            String detailUrl = productItem.links().regex(URL_POST).get();
            if (StringUtils.isEmpty(detailUrl)) {
                continue;
            }
            String id = detailUrl.replace("https://page.auctions.yahoo.co.jp/jp/auction/", "");
            if (StringUtils.isEmpty(id)) {
                continue;
            }
            YahooListItem yahooListItem = new YahooListItem(id,detailUrl);
            String img = productItem.xpath("//img[@class=\"Product__imageData\"]").get();
            if (StringUtils.isNotBlank(img)) {
                Document parse = Jsoup.parse(img);
                String imgSrc = parse.getElementsByTag("img").attr("src");
                yahooListItem.setImageUrl(imgSrc);
            }
            List<Selectable> prices = productItem.xpath("//span[@class=\"Product__price\"]").nodes();
            for (Selectable price : prices) {
                String s = price.get();
                if (s.contains("即決")) {
                    yahooListItem.setTakeNowPrice(RegexUtil.getPriceNum(s));
                    yahooListItem.setAuction(true);
                } else if (s.contains("現在")) {
                    yahooListItem.setAuctionPrice(RegexUtil.getPriceNum(s));
                    yahooListItem.setAuction(true);
                } else {
                    yahooListItem.setTakeNowPrice(RegexUtil.getPriceNum(s));
                }
            }
            page.addTargetRequest(detailUrl);
            list.add(yahooListItem);
        }
        resultItems.put("listItem",list);
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        log.info("开始爬虫");
        CJZSinaBlogProcessor processor = new CJZSinaBlogProcessor();
        Spider spider = Spider.create(processor);
        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
        httpClientDownloader.setProxyProvider(SimpleProxyProvider.from(new Proxy("127.0.0.1",49660)));
        spider.setDownloader(httpClientDownloader);
        spider.addUrl("https://auctions.yahoo.co.jp/category/list/23764/?p=%E3%82%AA%E3%83%BC%E3%83%87%E3%82%A3%E3%82%AA%E6%A9%9F%E5%99%A8&auccat=23764&exflg=1&b=51&n=50&s1=bids&o1=a&mode=1");
        spider.addPipeline(new FileCashPipeline("D:\\webmagic\\"));
//        spider.test("https://auctions.yahoo.co.jp/category/list/23764/?p=%E3%82%AA%E3%83%BC%E3%83%87%E3%82%A3%E3%82%AA%E6%A9%9F%E5%99%A8&auccat=23764&exflg=1&b=51&n=50&s1=bids&o1=a&mode=1");
        spider.thread(5);
        spider.run();
    }
}
