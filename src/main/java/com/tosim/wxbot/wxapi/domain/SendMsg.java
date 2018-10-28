package com.tosim.wxbot.wxapi.domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class SendMsg {

    private BaseRequest baseRequest;
    private Msg msg;
    private Integer scene;

    public SendMsg(){}

    public SendMsg(BaseRequest baseRequest,Integer type, String content, String fromUserName, String toUserName, String mediaId) {
        this.baseRequest = baseRequest;
        this.msg = new Msg();

        String tmp = (Timestamp.valueOf(LocalDateTime.now()).getTime() * 1000) + ((Math.random() + "").substring(0, 5).replace(".", ""));
        this.msg.localID = tmp;
        this.msg.clientMsgId = tmp;
        this.msg.type = type;
        this.msg.content = content;
        this.msg.fromUserName = fromUserName;
        this.msg.toUserName = toUserName;
        this.msg.mediaId = mediaId;

        this.scene = 0;
    }

    public BaseRequest getBaseRequest() {
        return baseRequest;
    }

    public void setBaseRequest(BaseRequest baseRequest) {
        this.baseRequest = baseRequest;
    }

    public Msg getMsg() {
        return msg;
    }

    public void setMsg(Msg msg) {
        this.msg = msg;
    }

    public Integer getScene() {
        return scene;
    }

    public void setScene(Integer scene) {
        this.scene = scene;
    }

    public class Msg {

        private Integer type;
        private String content;
        private String fromUserName;
        private String toUserName;
        private String localID;//时间戳左移4位随后补上4位随机数
        private String clientMsgId;//同上
        private String mediaId; //如果是图片才传这个值

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getFromUserName() {
            return fromUserName;
        }

        public void setFromUserName(String fromUserName) {
            this.fromUserName = fromUserName;
        }

        public String getToUserName() {
            return toUserName;
        }

        public void setToUserName(String toUserName) {
            this.toUserName = toUserName;
        }

        public String getLocalID() {
            return localID;
        }

        public void setLocalID(String localID) {
            this.localID = localID;
        }

        public String getClientMsgId() {
            return clientMsgId;
        }

        public void setClientMsgId(String clientMsgId) {
            this.clientMsgId = clientMsgId;
        }

        public String getMediaId() {
            return mediaId;
        }

        public void setMediaId(String mediaId) {
            this.mediaId = mediaId;
        }
    }
}
