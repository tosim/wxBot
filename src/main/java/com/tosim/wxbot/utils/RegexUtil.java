package com.tosim.wxbot.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

    public static String getGroupStr(String str,String regex,int group) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        String ret = null;
        if(matcher.find()){
            ret = matcher.group(group);
        }
        return ret;
    }

    public static void main(String[] args){
//        System.out.println(getGroupStr("(\\d+)(.*)","123avc",1));
    }
}
