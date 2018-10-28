package com.tosim.wxbot.utils;

import okhttp3.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OkHttpUtil {

    private static OkHttpClient client;

    public static OkHttpClient buildNormalClient() {
        return new OkHttpClient();
    }

    public static OkHttpClient buildDefaultCookiedClient() {
        OkHttpClient cookiedClient = new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    private final Map<String, List<Cookie>> cookieStore = new ConcurrentHashMap<>();

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        if (null != cookies) {
                            cookieStore.put(url.host(), cookies);
                        }
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookieList = cookieStore.get(url.host());
                        return cookieList != null ? cookieList : new ArrayList<Cookie>();
                    }
                })
                .build();
        return cookiedClient;
    }

    public static void setGlobalClient(OkHttpClient client) {
        OkHttpUtil.client = client;
    }

    public static Response get(String url) throws Exception {
        return get(url, null, null);
    }

    public static Response postJson(String url, String jsonStr) throws Exception {
        return postJson(url, jsonStr, null, null);
    }

    public static Response get(String url, Map<String, String> urlParams, Map<String, String> headers) throws Exception {
        Request.Builder builder = new Request.Builder();
        setHeadersToBuilder(builder, headers);
        url = getEncodeUrl(url, urlParams);
        Request request = builder.url(url).build();
        return getClient().newCall(request).execute();
    }

    public static Response postJson(String url, String jsonStr, Map<String, String> headers, Map<String, String> urlParams) throws Exception {
        Request.Builder builder = new Request.Builder();
        setHeadersToBuilder(builder, headers);
        url = getEncodeUrl(url, urlParams);
        Request request = builder.url(url).post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonStr)).build();
        return getClient().newCall(request).execute();
    }


    public static Response postFile(String url, File file, String fileKey, Map<String, String> bodyParams) throws Exception {
        return postFile(url, file, fileKey, bodyParams, null, null);
    }

    public static Response postFile(String url, File file, String fileKey, Map<String, String> bodyParams, Map<String, String> headers, Map<String, String> urlParams) throws Exception {
        Request.Builder reqBuilder = new Request.Builder();
        setHeadersToBuilder(reqBuilder, headers);
        url = getEncodeUrl(url, urlParams);

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        //添加普通参数
        if (bodyParams != null) {
            if (bodyParams != null && !bodyParams.isEmpty()) {
                for (String key : bodyParams.keySet()) {
                    bodyBuilder.addFormDataPart(key,bodyParams.get(key));
                }
            }
        }

        RequestBody fileBody = RequestBody.create(MediaType.parse(FileUtil.getMimeType(file)), file);
        bodyBuilder.addFormDataPart(fileKey, file.getName(), fileBody);

        Request request = reqBuilder.url(url).post(bodyBuilder.build()).build();
        return getClient().newCall(request).execute();
    }

    public static void postFileAsync(Callback callBack, String url, File file, String fileKey, Map<String, String> bodyParams, Map<String, String> headers, Map<String, String> urlParams) throws Exception {
        Request.Builder builder = new Request.Builder();
        setHeadersToBuilder(builder, headers);
        url = getEncodeUrl(url, urlParams);
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        //添加普通参数
        if (bodyParams != null) {
            // map 里面是请求中所需要的 key 和 value
            Set<Map.Entry<String, String>> entries = bodyParams.entrySet();
            for (Map.Entry entry : entries) {
                String key = String.valueOf(entry.getKey());
                String value = String.valueOf(entry.getValue());
                requestBody.addFormDataPart(key, value);
            }
        }
        // MediaType.parse() 里面是上传的文件类型。
        RequestBody body = RequestBody.create(MediaType.parse(FileUtil.getMimeType(file)), file);
        String filename = file.getName();
        // 添加文件参数，参数分别为， 请求key ，文件名称 ， RequestBody
        requestBody.addFormDataPart(fileKey, filename, body);
        Request request = new Request.Builder().url(url).post(requestBody.build()).build();
        getClient().newCall(request).enqueue(callBack);
    }

    private static void setHeadersToBuilder(Request.Builder builder, Map<String, String> headers) {
        if (null != headers) {
            for (String key : headers.keySet()) {
                builder.addHeader(key, headers.get(key));
            }
        }
    }

    private static OkHttpClient getClient() {
        if (OkHttpUtil.client == null) {
            OkHttpUtil.client = buildDefaultCookiedClient();
        }
        return OkHttpUtil.client;
    }

    private static String getEncodeUrl(String url, Map<String, String> params) {
        if (null != params) {
            StringBuilder encodeParams = new StringBuilder();
            int pos = 0;
            for (String key : params.keySet()) {
                if (pos != 0) encodeParams.append("&");
                try {
                    encodeParams.append(String.format("%s=%s", key, URLEncoder.encode(params.get(key), "utf-8")));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                pos++;
            }
            url = String.format("%s?%s", url, encodeParams.toString());
        }
        return url;
    }

    public static void main(String[] args) {
        Request request = new Request.Builder().build();
        RequestBody body = new MultipartBody.Builder().build();

    }
}
