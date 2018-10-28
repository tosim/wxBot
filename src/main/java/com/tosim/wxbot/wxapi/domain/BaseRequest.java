package com.tosim.wxbot.wxapi.domain;

public class BaseRequest {

    private String uin;
    private String sid;
    private String skey;
    private String deviceID;

    public BaseRequest(){}

    public BaseRequest(String uin,String sid,String skey,String deviceId){
        this.uin = uin;
        this.sid = sid;
        this.skey = skey;
        this.deviceID = deviceId;
    }

    public String getUin() {
        return uin;
    }

    public void setUin(String uin) {
        this.uin = uin;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getSkey() {
        return skey;
    }

    public void setSkey(String skey) {
        this.skey = skey;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
}
