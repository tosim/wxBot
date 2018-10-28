package com.tosim.wxbot.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class XUtil {

    private static final char[] HEX = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String md5(String str) {
        try {
            return hash("MD5", str.getBytes("UTF-8"));
        } catch (Exception var2) {
            return null;
        }
    }

    public static String md5(File file) {
        try {
            return hash("MD5", file);
        } catch (Exception var2) {
            var2.printStackTrace();
            return null;
        }
    }


    public static String hash(String algorithm, byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        messageDigest.update(bytes);
        return bytesToHex(messageDigest.digest());
    }

    public static String hash(String algorithm, File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        FileInputStream fileInputStream = new FileInputStream(file);
        Throwable var4 = null;

        try {
            DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, messageDigest);
            Throwable var6 = null;

            try {
                byte[] buffer = new byte[131072];

                while(digestInputStream.read(buffer) > 0) {
                    ;
                }

                String var8 = bytesToHex(digestInputStream.getMessageDigest().digest());
                return var8;
            } catch (Throwable var31) {
                var6 = var31;
                throw var31;
            } finally {
                if (digestInputStream != null) {
                    if (var6 != null) {
                        try {
                            digestInputStream.close();
                        } catch (Throwable var30) {
                            var6.addSuppressed(var30);
                        }
                    } else {
                        digestInputStream.close();
                    }
                }

            }
        } catch (Throwable var33) {
            var4 = var33;
            throw var33;
        } finally {
            if (fileInputStream != null) {
                if (var4 != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable var29) {
                        var4.addSuppressed(var29);
                    }
                } else {
                    fileInputStream.close();
                }
            }

        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];

        for(int i = 0; i < bytes.length; ++i) {
            byte b = bytes[i];
            chars[i << 1] = HEX[b >>> 4 & 15];
            chars[(i << 1) + 1] = HEX[b & 15];
        }

        return new String(chars);
    }
}
