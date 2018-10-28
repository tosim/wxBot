package com.tosim.wxbot.wxapi.domain;

import com.tosim.common.sutil.SUtil;

import java.io.File;

public class CheckUpload {

    private final BaseRequest baseRequest;
    private final String fileMd5;
    private final String fileName;
    private final long fileSize;
    private final int fileType;
    private String fromUserName;
    private String toUserName;


    public CheckUpload(BaseRequest baseRequest, File file, String fromUserName, String toUserName) {
        this.baseRequest = baseRequest;
        this.fileMd5 = SUtil.md5(file);
        this.fileName = file.getName();
        this.fileSize = file.length();
        this.fileType = 7;
        this.fromUserName = fromUserName;
        this.toUserName = toUserName;
    }

    public BaseRequest getBaseRequest() {
        return baseRequest;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getFileType() {
        return fileType;
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
}
