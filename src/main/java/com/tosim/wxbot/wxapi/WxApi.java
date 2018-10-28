package com.tosim.wxbot.wxapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.EncodeHintType;
import com.tosim.common.sutil.http.SHttpUtil;
import com.tosim.common.sutil.http.SOption;
import com.tosim.common.sutil.http.SRequest;
import com.tosim.common.sutil.http.SResponse;
import com.tosim.wxbot.utils.*;
import com.tosim.wxbot.wxapi.constants.WxConstans;
import com.tosim.wxbot.wxapi.domain.BaseRequest;
import com.tosim.wxbot.wxapi.domain.CheckUpload;
import com.tosim.wxbot.wxapi.domain.Contact;
import com.tosim.wxbot.wxapi.domain.SendMsg;
import com.tosim.wxbot.wxapi.tools.WxTool;
import okhttp3.*;
import static com.tosim.wxbot.wxapi.constants.WxConstans.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

public class WxApi {

    private static final MediaType MEDIA_JSON = MediaType.parse("application/json");
    public final File tmpDir = new File(WxApi.class.getResource("/").getPath() + "/wxBot_tmp");
    private OkHttpClient client;
    private SOption httpOption;
    private String BASE_URL;
    private String BASE_HOST;
    private String skey;
    private String wxsid;
    private String wxuin;
    private String pass_ticket;
    private String deviceID;
    public Contact myAccount;
    private JSONObject syncKey;
    public Map<String, Contact> contactMap;
    private String SYNC_HOST;
    private int fileNo;

    public WxApi() {
        System.setProperty("jsse.enableSNIExtension", "false");
        this.httpOption = new SOption(2 * 60 * 1000, 6 * 60 * 1000);
        contactMap = new ConcurrentHashMap<>();
        if (!tmpDir.exists())
            tmpDir.mkdirs();
        File qrcodedir = new File(tmpDir, "/qrcode");
        if (!qrcodedir.exists())
            qrcodedir.mkdirs();
        File icondir = new File(tmpDir, "/icon");
        if (!icondir.exists())
            icondir.mkdirs();
        File tempdir = new File(tmpDir, "/temp");
        if (!tempdir.exists())
            tempdir.mkdirs();
    }

    public void webwxlogout() throws Exception {
        String url = String.format("https://%s/cgi-bin/mmwebwx-bin/webwxlogout?redirect=%s&type=%s", BASE_HOST, 1, 0);
        String json = String.format("{\"sid\":%s,\"uin\":%s}", wxsid, wxuin);
        SRequest request = SRequest.POST(url);
        request.content(new SRequest.StringContent(SRequest.CONTENT_JSON, json));
        SResponse response = request.call(httpOption);
        if (!response.isSuccessful()) {
            throw new Exception("退出登录失败");
        }
        String resp = response.string();
    }

    public boolean login() throws Exception {
        String url = "https://login.weixin.qq.com/jslogin?appid=wx782c26e4c19acffb&fun=new&lang=zh_CN&_=" + System.currentTimeMillis();
        SRequest request = SRequest.GET(url);
        SResponse response = SHttpUtil.http(httpOption, request);
        if (!response.isSuccessful())
            throw new Exception("获取uuid失败");
        String uuidStr = response.string();
        String uuid = uuidStr.substring(uuidStr.indexOf("\"") + 1, uuidStr.lastIndexOf("\""));
        showQRCode(uuid);
        while (true) {
            url = String.format("https://login.weixin.qq.com/cgi-bin/mmwebwx-bin/login?uuid=%s&tip=1&_=%s", uuid, System.currentTimeMillis());
            request = SRequest.GET(url);
            response = SHttpUtil.http(httpOption, request);
            if (!response.isSuccessful()) {
                throw new Exception("获取登录重定向地址失败");
            }
            String msg = response.string();
            String redirectUri = null;
            if (msg.indexOf("window.redirect_uri=") != -1) {
                redirectUri = msg.substring(msg.indexOf("\"") + 1, msg.lastIndexOf("\"")) + "&fun=new";
                BASE_URL = redirectUri.substring(0, redirectUri.lastIndexOf("/"));
                String tempHost = BASE_URL.substring(8);
                BASE_HOST = tempHost.substring(0, tempHost.indexOf("/"));
                url = redirectUri;
            }
            if (null != redirectUri) {
                break;
            }
            Thread.sleep(500);
        }
        request = SRequest.GET(url);
        response = SHttpUtil.http(httpOption, request);
        if (!response.isSuccessful())
            throw new Exception("跳转登录重定向地址失败");
        String msg = response.string();
        Pattern pattern = Pattern.compile("<skey>(.*)</skey><wxsid>(.*)</wxsid><wxuin>(.*)</wxuin><pass_ticket>(.*)</pass_ticket>");
        Matcher m = pattern.matcher(msg);
        if (!m.find()) {
            throw new Exception("获取登录初始化信息失败");
        }
        this.skey = m.group(1);
        this.wxsid = m.group(2);
        this.wxuin = m.group(3);
        this.pass_ticket = m.group(4);
        this.deviceID = "e" + (Math.random() + "").substring(2, 17);
        return true;
    }

    public boolean webWxInit() throws Exception {
        String paramTemplate = BASE_URL + "/webwxinit?r=%s&lang=en_US&pass_ticket=%s&skey=%s";
        String url = String.format(paramTemplate, System.currentTimeMillis(), this.pass_ticket, this.skey);
        String baseRequest = String.format("{\"BaseRequest\":%s}", JSON.toJSONString(new BaseRequest(wxuin, wxsid, skey, deviceID), pascalNameFilter));
        SRequest request = SRequest.POST(url);
        request.content(new SRequest.StringContent(SRequest.CONTENT_JSON,baseRequest));
        SResponse response = SHttpUtil.http(httpOption, request);
        if (!response.isSuccessful())
            throw new Exception("WebWxInit Error");
        String webWxInitRespStr = response.string();
        JSONObject webWxInitResp = JSONObject.parseObject(webWxInitRespStr);
        myAccount = JSONObject.parseObject(webWxInitResp.getJSONObject("User").toJSONString(), Contact.class);
        syncKey = webWxInitResp.getJSONObject("SyncKey");
        return true;
    }

    public boolean webWxStatusNotify() throws Exception {
        String msgId = (System.currentTimeMillis() * 10000) + ((Math.random() + "").substring(0, 5).replace(".", ""));
        String url = BASE_URL + "/webwxstatusnotify?lang=zh_CN&pass_ticket=" + pass_ticket;
        String jsonTemplate = "{\"BaseRequest\": {\"Sid\": \"%s\", \"Skey\": \"%s\", \"DeviceID\": \"%s\", \"Uin\": %s},\"Code\": 3,\"FromUserName\": \"%s\",\"ToUserName\": \"%s\",\"ClientMsgId\": %s}";
        String json = String.format(jsonTemplate, wxsid, skey, deviceID, wxuin, myAccount.getUserName(), myAccount.getUserName(), msgId);
        SRequest request = SRequest.POST(url);
        request.content(new SRequest.StringContent(SRequest.CONTENT_XML, json));
        SResponse response = SHttpUtil.http(httpOption, request);
        if (!response.isSuccessful())
            throw new Exception("开启微信状态通知失败");
        String msg = response.string();
        int Ret = JSON.parseObject(msg).getJSONObject("BaseResponse").getIntValue("Ret");
        return Ret == 0;
    }

    public boolean webWxGetContact() throws Exception {
        String url = BASE_URL + "/webwxgetcontact?r=" + System.currentTimeMillis();
        SRequest request = SRequest.POST(url);
        request.content(new SRequest.StringContent(SRequest.CONTENT_JSON, "{}"));
        SResponse response = SHttpUtil.http(httpOption, request);
        if (!response.isSuccessful())
            throw new Exception("获取联系人失败");
        String msg = response.string();
        List<Contact> contactList = JSONObject.parseArray(JSONObject.parseObject(msg).getString("MemberList"), Contact.class);
        if (contactList == null) {
            throw new Exception("获取到联系人失败");
        }
        contactList.forEach(contact -> {
            contact.setHeadImgUrl(BASE_URL.replace("/cgi-bin/mmwebwx-bin", "") + contact.getHeadImgUrl() + skey);
        });
        for (Contact contact : contactList) {
            if (contact.getRemarkName() == null && contact.getNickName() == null) {
                continue;
            }
            if (!contact.getRemarkName().equals("")) {
                contactMap.put(contact.getRemarkName(), contact);
            } else {
                contactMap.put(contact.getNickName(), contact);
            }
        }
        return true;
    }

    public boolean testSyncCheck() throws Exception {
        boolean flag = false;
        String[] hosts = new String[]{"webpush.wx.qq.com"};
        for (String host : hosts) {
            SYNC_HOST = host;
            int retcode = syncCheck().getIntValue("retcode");
            if (0 == retcode) {
                flag = true;
                break;
            }
            Thread.sleep(250);
        }
        if (flag == false) {
            throw new Exception("没有畅通的主机地址");
        }
        return flag;
    }

    public JSONObject syncCheck() throws Exception {
        String params = "r=%s&sid=%s&uin=%s&skey=%s&deviceid=%s&synckey=%s&_=%s";
        String url = "https://" + SYNC_HOST + "/cgi-bin/mmwebwx-bin/synccheck?" + String.format(params, System.currentTimeMillis(), wxsid, wxuin, skey, deviceID, genSyncKey(), System.currentTimeMillis());
        SRequest request = SRequest.GET(url);
        SResponse response = SHttpUtil.http(httpOption, request);
        if (!response.isSuccessful())
            throw new Exception("微信消息同步失败");
        String msg = response.string();
        Pattern pattern = Pattern.compile("retcode:\"(.*)\",selector:\"(.*)\"");
        Matcher m = pattern.matcher(msg);
        if (!m.find()) {
            throw new Exception("微信消息同步返回值错误");
        }
        JSONObject syncCheckResp = new JSONObject();
        syncCheckResp.put("retcode", Integer.parseInt(m.group(1)));
        syncCheckResp.put("selector", Integer.parseInt(m.group(2)));
        return syncCheckResp;
    }

    public JSONObject webWxSync() throws Exception {
        String params = "sid=%s&skey=%s&lang=en_US&pass_ticket=%s";
        String postMsg = "{\"BaseRequest\" : {\"Uin\":%s,\"Sid\":\"%s\"},\"SyncKey\" : %s,\"rr\" :%s}";
        String url = BASE_URL + "/webwxsync?" + String.format(params, wxsid, skey, pass_ticket);
        String json = String.format(postMsg, wxuin, wxsid, syncKey.toJSONString(), System.currentTimeMillis());
        SRequest request = SRequest.POST(url);
        request.content(new SRequest.StringContent(SRequest.CONTENT_JSON, json));
        SResponse response = SHttpUtil.http(httpOption, request);
        if (!response.isSuccessful())
            throw new Exception("微信获取消息同步失败");
        String msg = response.string();
        JSONObject msgObject = JSONObject.parseObject(msg);
        this.syncKey = msgObject.getJSONObject("SyncCheckKey");
        return msgObject;
    }

    public JSONObject sendMsg(int type, String url, String content, String fromUserName, String toUserName, String mediaId) throws Exception {
        if (url == null) {
            throw new NullPointerException("发送消息url为null");
        }
        SendMsg sendMsg = new SendMsg(new BaseRequest(wxuin, wxsid, skey, deviceID), type, content, fromUserName, toUserName, mediaId);
        SRequest request = SRequest.POST(url);
        request.content(new SRequest.StringContent(SRequest.CONTENT_JSON, JSON.toJSONString(sendMsg, pascalNameFilter)));
        SResponse response = SHttpUtil.http(httpOption, request);
        if (!response.isSuccessful()) {
            throw new Exception("发送请求(sendMsg)失败:\n"
                    + JSONUtil.toJSONString(sendMsg, JSONUtil.STRING_UPPER_FIRST | JSONUtil.STRING_PRETTY)
                    + "\nsendResp:\n"
                    + response.string());
        }
        JSONObject sendResp = JSON.parseObject(response.string());
        if (sendResp.getJSONObject("BaseResponse").getIntValue("Ret") != 0) {
            throw new Exception("发送请求(sendMsg)失败:\n"
                    + JSONUtil.toJSONString(sendMsg, JSONUtil.STRING_UPPER_FIRST | JSONUtil.STRING_PRETTY)
                    + "\nsendResp:\n"
                    + JSONUtil.toPrettyJSONStr(sendResp));
        }
        return sendResp;
    }

    public JSONObject sendTextMsg(String content, String fromUserName, String toUserName) throws Exception {
        String url = String.format(BASE_URL + "/webwxsendmsg?pass_ticket=%s", pass_ticket);
        return sendMsg(WxConstans.MSGTYPE_文本消息, url, content, fromUserName, toUserName, null);
    }

    public JSONObject sendImgMsg(File file, String fromUserName, String toUserName) throws Exception {
        String url = String.format(BASE_URL + "/webwxsendmsgimg?pass_ticket=%s&fun=async&f=json", pass_ticket);
        JSONObject resp = uploadMedia(file, fromUserName, toUserName);
        String mediaId = resp.getString("MediaId");
        resp = sendMsg(WxConstans.MSGTYPE_图片消息, url, mediaId, fromUserName, toUserName, mediaId);
        return resp;
    }

    public JSONObject sendAppMsg(File file, String fromUserName, String toUserName) throws Exception {
        String url = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxsendappmsg?fun=async&f=json&lang=zh_CN&pass_ticket=" + this.pass_ticket;
        JSONObject resp = uploadMedia(file, fromUserName, toUserName);
        String mediaId = resp.getString("MediaId");
        String content = String.format("<appmsg appid='wxeb7ec651dd0aefa9' sdkver=''><title>%s</title><des></des><action>"
                        + "</action><type>%d</type><content></content><url></url><lowurl></lowurl>"
                        + "<appattach><totallen>%s</totallen><attachid>%s</attachid><fileext>%s</fileext></appattach><extinfo></extinfo></appmsg>"
                , file.getName(), 6, String.valueOf(file.length()), mediaId, FileUtil.getSuffix(file.getName()));
        resp = sendMsg(6, url, content, fromUserName, toUserName, mediaId);
        return resp;
    }

    private JSONObject webWxCheckUpload(File file, String fromUserName, String toUserName) throws IOException {
        SRequest request = SRequest.POST(String.format("https://%s/cgi-bin/mmwebwx-bin/webwxcheckupload", BASE_HOST));
       request.content(new SRequest.StringContent(SRequest.CONTENT_JSON, JSON.toJSONString(new CheckUpload(new BaseRequest(wxuin,wxsid,skey,deviceID),file,fromUserName,toUserName),pascalNameFilter)));
        SResponse response = request.call(httpOption);
        String resp = response.string();
        System.out.println(resp);
        return JSON.parseObject(resp);
    }

    private JSONObject uploadMedia(File file, String fromUserName, String toUserName) throws Exception {
        if (file.length() >= 25L * 1024L * 1024L) {
            JSONObject respCheckUpload = webWxCheckUpload(file, fromUserName, toUserName);
            System.out.println("respCheck = \n" + JSON.toJSONString(respCheckUpload,true));
            return respCheckUpload;
        }

        fileNo = (fileNo + 1) % Integer.MAX_VALUE;
        String url = String.format("https://file.%s/cgi-bin/mmwebwx-bin/webwxuploadmedia?f=json", BASE_HOST);
        String mimeType = Files.probeContentType(Paths.get(file.getAbsolutePath()));
        Map<String, Object> uploadMediaReq = new HashMap<>();
        uploadMediaReq.put("UploadType", 2);
        uploadMediaReq.put("BaseRequest", new BaseRequest(wxuin, wxsid, skey, deviceID));
        uploadMediaReq.put("ClientMediaId", String.valueOf(new Date().getTime()) + String.valueOf(new Random().nextLong()).substring(0, 4));
        uploadMediaReq.put("TotalLen", file.length());
        uploadMediaReq.put("StartPos", 0);
        uploadMediaReq.put("DataLen", file.length());
        uploadMediaReq.put("MediaType", 4);
        uploadMediaReq.put("FromUserName", fromUserName);
        uploadMediaReq.put("ToUserName", toUserName);
        uploadMediaReq.put("FileMd5", XUtil.md5(file));

        if (file.length() < 1024L * 1024L) {
            SRequest request = SRequest.POST(url);
            request.content("id", "WU_FILE_" + this.fileNo);
            request.content("name", file.getName());
            request.content("type", mimeType);
            request.content("lastModifiedDate", new SimpleDateFormat("yyyy MM dd HH:mm:ss").format(new Date()));
            request.content("mediatype", WxTool.fileType(file)); //video,pic,doc中的一种
            request.content("uploadmediarequest", JSON.toJSONString(uploadMediaReq, pascalNameFilter));
            request.content("size", String.valueOf(file.length()));
            request.content("pass_ticket", pass_ticket);
            request.content("webwx_data_ticket", httpOption.cookieJar().getCookie("webwx_data_ticket"));
            request.content("filename", file);
            SResponse response = SHttpUtil.http(httpOption, request);
            if (!response.isSuccessful()) {
                throw new Exception("上传图片失败");
            }
            String resp = response.string();
            return JSON.parseObject(resp);
        } else {
            long bufSize = 512L * 1024L;
            long chunk = 0, chunks = (long) (Math.ceil(file.length() / bufSize));
            DataInputStream distream = new DataInputStream(new FileInputStream(file));
            byte[] buf = new byte[512 * 1024];
            String resp = "";
            while (chunk < chunks) {
                int saveCnt = 0;
                int readCnt;
                while ((readCnt = distream.read(buf, saveCnt, buf.length - saveCnt)) != -1) {
                    saveCnt += readCnt;
                    if (saveCnt >= buf.length) {
                        break;
                    }
                }
                SRequest request = SRequest.POST(url);
                request.content("id", "WU_FILE_" + this.fileNo);
                request.content("name", file.getName());
                request.content("type", mimeType);
                request.content("lastModifiedDate", new SimpleDateFormat("yyyy MM dd HH:mm:ss").format(new Date()));
                request.content("mediatype", WxTool.fileType(file)); //video,pic,doc中的一种
                request.content("uploadmediarequest", JSON.toJSONString(uploadMediaReq, pascalNameFilter));
                request.content("size", String.valueOf(file.length()));
                request.content("pass_ticket", pass_ticket);
                request.content("webwx_data_ticket", httpOption.cookieJar().getCookie("webwx_data_ticket"));
                request.content("chunks", String.valueOf(chunks));
                request.content("chunk", String.valueOf(chunk));
                request.content("filename",buf,mimeType,file.getName());
                SResponse response = SHttpUtil.http(httpOption, request);
                resp = response.string();
                System.out.println("发送" + chunk + "/" + chunks + ",resp = \n" + resp);
                chunk++;
            }
            return JSON.parseObject(resp);
        }
    }

    //    private String dealVoiceMsg(Message message) throws Exception {
//        String url = BASE_URL + "/webwxgetvoice?";
//        String params = "msgid=%s&skey=%s";
//        Response response = get(url + String.format(params, message.getMsgId(), skey));
//        if (!response.isSuccessful())
//            return "处理语音消息失败！";
//        byte[] buf = new byte[1024];
//        int len = -1;
//        InputStream in = response.body().byteStream();
//        File file = new File(tempRootDir, "/temp/" + message.getMsgId() + ".mp3");
//        OutputStream out = new FileOutputStream(file);
//        while ((len = in.read(buf)) != -1) {
//            out.write(buf, 0, len);
//        }
//        in.close();
//        out.close();
//        return "处理成功！";
//    }
//
//    private String dealPicMsg(Message message) throws Exception {
//        String url = BASE_URL + "/webwxgetmsgimg?&MsgID=%s&skey=%s&type=slave";
//        Response response = get(String.format(url, message.getMsgId(), skey));
//        if (!response.isSuccessful())
//            return "处理图片消息失败！";
//        byte[] buf = new byte[1024];
//        int len = -1;
//        InputStream in = response.body().byteStream();
//        File file = new File(tempRootDir, "/temp/" + message.getMsgId() + ".jpg");
//        OutputStream out = new FileOutputStream(file);
//        while ((len = in.read(buf)) != -1) {
//            out.write(buf, 0, len);
//        }
//        in.close();
//        out.close();
//        return "处理成功！";
//    }

    private String genSyncKey() {
        String encodeSyncKey = "";
        JSONArray array = this.syncKey.getJSONArray("List");
        for (int i = 0; i < array.size(); i++) {
            JSONObject item = array.getJSONObject(i);
            String key = item.getString("Key");
            String value = item.getString("Val");
            encodeSyncKey += "|" + key + "_" + value;
        }
        encodeSyncKey = encodeSyncKey.substring(1);
        return encodeSyncKey;
    }

    private void showQRCode(String uuid) throws Exception {
        String text = String.format("https://login.weixin.qq.com/l/%s", uuid); // 二维码内容
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");   // 内容所使用字符集编码
        QRCode qrCode = Encoder.encode(text, ErrorCorrectionLevel.L, hints);
        File outputFile = new File(tmpDir, "/qrcode/qrcode.png");
        if (SystemUtil.IS_OS_LINUX) {
            int width = 40; // 二维码图片宽度
            int height = 40; // 二维码图片高度
            String format = "png";// 二维码的图片格式
            BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, width, hints);
            StringBuilder sb = new StringBuilder();
            for (int rows = 0; rows < bitMatrix.getHeight(); rows++) {
                for (int cols = 0; cols < bitMatrix.getWidth(); cols++) {
                    boolean x = bitMatrix.get(rows, cols);
                    if (!x) {
                        sb.append("\033[40m  \033[0m");
                    } else {
                        sb.append("\033[47m  \033[0m");
                    }
                }
                sb.append("\n");
            }
            System.out.println(sb.toString());
        } else if (SystemUtil.IS_OS_WINDOWS) {
            int width = 300; // 二维码图片宽度
            int height = 300; // 二维码图片高度
            String format = "png";// 二维码的图片格式
            BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            // 生成二维码
            MatrixToImageWriter.writeToFile(bitMatrix, format, outputFile);
            Runtime run = Runtime.getRuntime();
            run.exec("cmd /c start " + outputFile.getAbsolutePath().replace(".\\", "").replace(":\\", ":\\\\"));
        } else if (SystemUtil.IS_OS_MAC) {
            int width = 300; // 二维码图片宽度
            int height = 300; // 二维码图片高度
            String format = "png";// 二维码的图片格式
            BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            // 生成二维码
            MatrixToImageWriter.writeToFile(bitMatrix, format, outputFile);
            Runtime run = Runtime.getRuntime();
            run.exec("open " + outputFile.getAbsolutePath());
        } else {
            throw new RuntimeException("未知操作系统平台，无法生成二维码");
        }
    }
}
