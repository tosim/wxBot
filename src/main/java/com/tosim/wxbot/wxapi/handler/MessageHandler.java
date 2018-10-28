package com.tosim.wxbot.wxapi.handler;

import com.tosim.wxbot.wxapi.WxEngine;
import com.tosim.wxbot.wxapi.domain.Message;

public abstract class MessageHandler {

    protected WxEngine wxEngine;

    public MessageHandler(){}

    public void setWxEngine(WxEngine wxEngine) {
        this.wxEngine = wxEngine;
    }

    public abstract void handleMsg(Message message) throws Exception;
}
