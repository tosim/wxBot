package com.tosim.wxbot.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PascalNameFilter;
import com.tosim.wxbot.wxapi.domain.BaseRequest;
import com.tosim.wxbot.wxapi.domain.SendMsg;

public class JSONUtil {

    public static final int STRING_UPPER_FIRST = 1;
    public static final int STRING_PRETTY = 2;

    //==============================toJSONString==============================

    public static String toJSONString(Object obj){
        return JSON.toJSONString(obj);
    }

    /**
     * 将javaBean的属性首字母大写序列化
     * @param obj
     * @param isUpperFirst
     * @return
     */
    public static String toJSONString(Object obj,boolean isUpperFirst){
        if(isUpperFirst){
            return JSON.toJSONString(obj,new PascalNameFilter());
        }else{
            return JSON.toJSONString(obj);
        }
    }

    public static String toJSONString(Object obj,int features){
        String jsonStr = null;
        if((features & STRING_UPPER_FIRST) > 0){
            jsonStr = JSON.toJSONString(obj,new PascalNameFilter());
        }
        if((features & STRING_PRETTY) > 0){
            if(null == jsonStr){
                jsonStr = JSON.toJSONString(obj,true);
            }else{
                jsonStr = toPrettyJSONStr(jsonStr);
            }
        }
        return jsonStr;
    }

    public static String toJSONString(JSONObject jsonObject){
        return jsonObject.toJSONString();
    }

    public static String toPrettyJSONStr(Object obj){
        return JSON.toJSONString(obj,true);
    }

    public static String toPrettyJSONStr(String jsonStr){
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        return JSON.toJSONString(jsonObject,true);

    }

    //==============================toObject==============================
    public static <T> T parseObject(String text, Class<T> clazz) {
        return JSON.parseObject(text,clazz);
    }

    public static <T> T parseObject(JSONObject jsonObject, Class<T> clazz) {
        String jsonStr = jsonObject.toJSONString();
        return parseObject(jsonStr,clazz);
    }

    public static JSONObject parseObject(String text){
        return JSON.parseObject(text);
    }

    public static void main(String[] args){
        SendMsg sendMsg = new SendMsg(new BaseRequest("1","2","3","4"),1,"2","3","3",null);
        String ans = JSONUtil.toJSONString(sendMsg,JSONUtil.STRING_PRETTY | JSONUtil.STRING_UPPER_FIRST);
        System.out.println(ans);

        SendMsg copy = JSONUtil.parseObject(ans,SendMsg.class);
        String lowerCaseStrig = JSONUtil.toJSONString(copy,JSONUtil.STRING_PRETTY);
        System.out.println(lowerCaseStrig);

        SendMsg lowerToCopy = JSON.parseObject(lowerCaseStrig,SendMsg.class);
        System.out.println(lowerToCopy.getMsg().getClientMsgId());
    }
}
