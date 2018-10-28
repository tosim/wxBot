package com.tosim.wxbot.wxapi.listener;

import com.tosim.wxbot.wxapi.domain.Message;

public interface WxEngineListener {

    void afterLogin();
    void beforeLogout();
    void beforeHandleMessage(Message message);
    void afterHandleMessage(Message message);
    void beforeReciveMsg();
    void afterSyncCheck(int retcode, int selector);
}
