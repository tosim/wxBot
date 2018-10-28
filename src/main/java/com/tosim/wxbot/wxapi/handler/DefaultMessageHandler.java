package com.tosim.wxbot.wxapi.handler;

import com.alibaba.fastjson.JSON;
import com.tosim.wxbot.wxapi.domain.Message;

public class DefaultMessageHandler extends MessageHandler {

    public DefaultMessageHandler() {}

    @Override
    public void handleMsg(Message message) throws Exception {
        System.out.println(JSON.toJSONString(message,true));
    }
}
