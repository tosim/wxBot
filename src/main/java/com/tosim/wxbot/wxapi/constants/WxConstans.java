package com.tosim.wxbot.wxapi.constants;

import com.alibaba.fastjson.serializer.PascalNameFilter;

public class WxConstans {

    //用于序列化时大写首字母
    public static final PascalNameFilter pascalNameFilter = new PascalNameFilter();

    //syncCheck 返回代码retcode和BaseResponse里的Ret含义相同
    public static final int RET_成功 = 0;
    public static final int RET_TICKET错误 = -14;
    public static final int RET_传参错误 = 1;
    public static final int RET_未登录提示 = 1100;
    public static final int RET_未登录检测 = 1101;
    public static final int RET_COOKIE无效 = 1102;
    public static final int RET_WEB环境异常 = 1203;
    public static final int RET_操作太频繁 = 1205;

    //syncCheck 返回代码select含义
    public static final int SELECTOR_正常 = 0;
    public static final int SELECTOR_新消息 = 2;
    public static final int SELECTOR_别人昵称修改 = 4;
    public static final int SELECTOR_好友删除或新增 = 6;
    public static final int SELECTOR_进入或离开聊天界面 = 7;

    //sendMsgType


    //msgType
    public static final int MSGTYPE_文本消息 = 1;
    public static final int MSGTYPE_图片消息 = 3;
    public static final int MSGTYPE_语音消息 = 34;
    public static final int MSYTYPE_好友请求 = 37;
    public static final int MSYTYPE_POSSIBLE_FRIEND_MSG = 40;
    public static final int MSYTYPE_分享名片 = 42;
    public static final int MSGTYPE_小视频消息 = 43;
    public static final int MSGTYPE_表情消息 = 47;
    public static final int MSYTYPE_位置消息 = 48;
    public static final int MSGTYPE_多媒体消息 = 49;
    public static final int MSYTYPE_VOIP_MSG = 50;
    public static final int MSYTYPE_微信初始化消息 = 51;
    public static final int MSYTYPE_VOIP_NOTIFY = 52;
    public static final int MSYTYPE_VOIP_INVITE = 53;
    public static final int MSGTYPE_短视频消息 = 62;
    public static final int MSYTYPE_系统消息 = 10000;
    public static final int MSYTYPE_撤回消息 = 10002;
    public static final int MSYTYPE_SYS_NOTICE = 9999;
}
