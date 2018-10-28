package com.tosim.wxbot.wxapi.domain;

public class Contact {

    private String UserName;
    private String NickName;
    private String RemarkName;
    private String HeadImgUrl;

    public Contact(){}

    public Contact(String userName, String nickName, String remarkName, String headImgUrl) {
        this.UserName = userName;
        this.NickName = nickName;
        this.RemarkName = remarkName;
        this.HeadImgUrl = headImgUrl;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getNickName() {
        return NickName;
    }

    public void setNickName(String nickName) {
        NickName = nickName;
    }

    public String getRemarkName() {
        return RemarkName;
    }

    public void setRemarkName(String remarkName) {
        RemarkName = remarkName;
    }

    public String getHeadImgUrl() {
        return HeadImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        HeadImgUrl = headImgUrl;
    }
}
