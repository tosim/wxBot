package com.tosim.wxbot.utils;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;

public class FileUtil {

    public static String getSuffix(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        return suffix;
    }

    public static String getMimeType(File file) throws Exception {
        String mime = new MimetypesFileTypeMap().getContentType(file);
        if(mime == null) {
            mime = "text/plain";
        }
        return mime;
    }


    public static void main(String args[]) throws Exception {
        File file = new File("/home/yyc/.wxBot/temp/6.rm");
        System.out.println(getMimeType(file));
    }
}
