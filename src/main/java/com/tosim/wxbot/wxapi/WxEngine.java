package com.tosim.wxbot.wxapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tosim.wxbot.wxapi.domain.Message;
import com.tosim.wxbot.wxapi.handler.DefaultMessageHandler;
import com.tosim.wxbot.wxapi.handler.MessageHandler;

import java.io.File;
import java.util.*;

public class WxEngine {

    //是否开启调试模式
    private boolean debug = true;
    //消息处理器
    private MessageHandler handler;
    //是否继续处理消息标志
    private volatile boolean dealMsg = true;
    //
    public WxApi wxApi;

    public WxEngine(MessageHandler handler) {
        if (handler == null) {
            this.handler = new DefaultMessageHandler();
        } else {
            this.handler = handler;
        }
        handler.setWxEngine(this);
        wxApi = new WxApi();
    }

    public void run() throws Exception {
        wxApi.login();
        System.out.println("登录成功");
        wxApi.webWxInit();
        System.out.println("初始化成功");
        wxApi.webWxStatusNotify();
        System.out.println("开启微信状态通知成功");
        wxApi.webWxGetContact();
        System.out.println("获取联系人成功");
        wxApi.testSyncCheck();
        System.out.println("测试检测路线成功");
        new ReceiveMsgThread().start();
    }

    public void stopProcessThread() {
        dealMsg = false;
    }

    private class ReceiveMsgThread extends Thread {
        @Override
        public void run() {
            while (dealMsg) {
                try {
                    JSONObject syncResp = wxApi.syncCheck();
                    int retcode = syncResp.getIntValue("retcode");
                    int selector = syncResp.getIntValue("selector");
                    if (retcode == 1100) {
//                        log("[!]你在手机上登出了微信，再见", false);
                        break;
                    } else if (retcode == 1101) {
//                        log("[!]你在其他地方上登录了web微信，再见", false);
                        break;
                    } else if (retcode == 0) {
//                        log("[*]消息同步成功", false);
                        if (selector == 0) {
//                            log("[msg]无新消息", false);
                        } else if (selector == 7) {
//                            log("[msg]进入/离开聊天界面消息", false);
                        } else if (selector == 2) {
                            JSONObject msgObject = wxApi.webWxSync();
                            handleMsg(msgObject);
                        } else if (selector == 6) {
//                            log("[msg]红包消息", false);
                        } else {
//                            log("[errMsg]selector = " + selector + ",未知的selector", false);
                        }
                    } else {
//                        log("[*]消息同步,retcode=" + retcode + ",未知代码", false);
                    }
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//            log("[quit]退出微信,再见", false);
        }
    }

    private void handleMsg(JSONObject msgObject) throws Exception {
        if (0 == msgObject.getJSONObject("BaseResponse").getInteger("Ret")) {
            List<Message> messages = JSONObject.parseArray(msgObject.getString("AddMsgList"), Message.class);
            for (Message message : messages) {
//                this.handler
            }
        }
    }

    public static void main(String[] args) {

        DefaultMessageHandler messageHandler = new DefaultMessageHandler();
        WxEngine wxEngine = new WxEngine(messageHandler);
        messageHandler.setWxEngine(wxEngine);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("被强制退出....\n\n");
                    wxEngine.wxApi.webwxlogout();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
        try {
            wxEngine.run();
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String input = scanner.next();
                if ("sendV".equals(input)) {
                    wxEngine.wxApi.sendAppMsg(new File(wxEngine.wxApi.tmpDir, "temp/x.zip"), wxEngine.wxApi.myAccount.getUserName(), "filehelper");
                } else if ("sendP".equals(input)) {
                    wxEngine.wxApi.sendAppMsg(new File(wxEngine.wxApi.tmpDir, "temp/tt.jpg"), wxEngine.wxApi.myAccount.getUserName(), "filehelper");
                } else if ("ls".equals(input)) {
                    System.out.println(("[list]\n" + JSON.toJSONString(wxEngine.wxApi.contactMap, true)));
                    System.out.println("[info]总共" + wxEngine.wxApi.contactMap.size() + "个");
                } else if ("quit".equals(input)) {
                    wxEngine.wxApi.webwxlogout();
                } else {
                    wxEngine.wxApi.sendTextMsg(input, wxEngine.wxApi.myAccount.getUserName(), "filehelper");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("启动失败");
        }
    }
}
