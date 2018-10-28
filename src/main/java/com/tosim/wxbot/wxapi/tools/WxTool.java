package com.tosim.wxbot.wxapi.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class WxTool {

    private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String fileType(File file) {
        switch (WxTool.fileSuffix(file)) {
            case "bmp":
            case "png":
            case "jpeg":
            case "jpg":
                return "pic";
            case "mp4":
                return "video";
            default:
                return "doc";
        }
    }

    public static String fileSuffix(File file) {
        try (FileInputStream is = new FileInputStream(file)) {
            byte[] b = new byte[3];
            is.read(b, 0, b.length);
            String fileCode = bytesToHex(b);

            switch (fileCode) {
                case "ffd8ff":
                    return "jpg";
                case "89504e":
                    return "png";
                case "474946":
                    return "gif";
                default:
                    if (fileCode.startsWith("424d")) {
                        return "bmp";
                    } else if (file.getName().lastIndexOf('.') > 0) {
                        return file.getName().substring(file.getName().lastIndexOf('.') + 1);
                    } else {
                        return "";
                    }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            chars[i << 1] = HEX[b >>> 4 & 0xf];
            chars[(i << 1) + 1] = HEX[b & 0xf];
        }
        return new String(chars);
    }

    public static void main(String[] args){
        System.out.println(fileType(new File("/home/yyc/.wxBot/temp/tt.jpg")));
    }
}
