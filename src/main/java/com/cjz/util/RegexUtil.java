package com.cjz.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

    public static final String HTML_SUF = "<[^>]*>|";

    /**
    * 清除所有Html前后缀
    **/
    public static String getHtmlText(String str) {
        if (StringUtils.isNotBlank(str)) {
            return str.replaceAll(HTML_SUF,"");
        }
        return "";
    }

    /**
    * 清除所有Html前后缀
    **/
    public static String getPriceNum(String str) {
        if (StringUtils.isNotBlank(str)) {
            return str.replaceAll("[^0-9]+","");
        }
        return "";
    }

    /**
    * 获得 （税 0 円） 内的 税 0 円
    **/
    public static String getParentheses(String str) {
        if (StringUtils.isNotBlank(str)) {
            Pattern pattern = Pattern.compile("[(（].*[)）]");
            Matcher matcher = pattern.matcher(str.trim());
            if (matcher.find()) {
                return matcher.group();
            }
        }
        return "";
    }



    public static final void main(String[] arrg ) {
//        System.out.println(getHtmlText("<span class=\"Product__price\"><span class=\"Product__label\">現在</span><span class=\"Product__priceValue u-textRed\">2,751円</span></span>"));
//        System.out.println(getPriceNum("現在137,500円"));
        System.out.println(getPriceNum(getParentheses(",980円（税 0 円）")));
    }

}
