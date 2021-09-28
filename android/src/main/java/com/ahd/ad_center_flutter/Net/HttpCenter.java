package com.ahd.ad_center_flutter.Net;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ahd.ad_center_flutter.Log.LogTools;
import com.google.gson.Gson;

import org.xutils.http.RequestParams;
import org.xutils.x;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Author by GuangMingfei
 * Date on 2021/8/23.
 * Email guangmf@neusoft.com
 * Used for
 */
public class HttpCenter {
    private String baseUrl = "https://adv.ahd168.com";
//    private String baseUrl = "http://192.168.101.5:9001";
    private static HttpCenter httpCenter;
    public static String appId,channel,userId;

    public static HttpCenter getInstance() {
        if (httpCenter == null) {
            httpCenter = new HttpCenter();
        }
        return httpCenter;
    }

    public boolean checkInternet(Activity activity) {
        ConnectivityManager connectivityManager;//用于判断是否有网络
        connectivityManager = (ConnectivityManager) activity.getSystemService(activity.CONNECTIVITY_SERVICE);//获取当前网络的连接服务
        NetworkInfo info = connectivityManager.getActiveNetworkInfo(); //获取活动的网络连接信息
        if (info == null) {   //当前没有已激活的网络连接（表示用户关闭了数据流量服务，也没有开启WiFi等别的数据服务）
            return false;
        } else {              //当前有已激活的网络连接
            return true;
        }
    }

    public void getNextAdFromWeb(Callback callback) {
        /**
         * {
         *   "appId": 0,
         *   "channel": "string",
         *   "kind": 0,
         *   "listingId": "string",
         *   "source": "string",
         *   "userId": 0,
         *   "version": "string"
         * }*/
        RequestAdVideo questAdVideo = new RequestAdVideo();
        questAdVideo.appId = appId;
        questAdVideo.userId = userId;
        //信息流提交
        getFactory(questAdVideo, callback);
    }

    //上报广告播放情况，source:功能板块，adFlag：平台，flag：1-成功，flag：0-失败
    public void uploadAdResult(String source,int adFlag, int flag,int jumpFlag, Callback callback) {
        //拼装失败的报文
        RequestUploadVideoData requestUploadVideoData = new RequestUploadVideoData();
        requestUploadVideoData.appId = appId;
        requestUploadVideoData.channel = channel;
        requestUploadVideoData.userId = userId;
        requestUploadVideoData.source = source;
        requestUploadVideoData.kind = String.valueOf(flag);
        requestUploadVideoData.listingId = String.valueOf(adFlag);
        requestUploadVideoData.version = android.os.Build.VERSION.RELEASE;
        if(jumpFlag == 1){
            requestUploadVideoData.status = "1";
        }else{
            requestUploadVideoData.status = "0";
        }

        postFactory(new Gson().toJson(requestUploadVideoData), callback);
    }

    /**
     * 测试okhttp的get方法
     */
    private void getFactory(RequestAdVideo questAdVideo,Callback callback) {
        String url = baseUrl + "/adv/video/videoNumber?appId="+questAdVideo.appId+"&userId="+questAdVideo.userId+"&jumpId="+questAdVideo.status;
        LogTools.printLog(this.getClass(), "网络请求报文："+url);
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).get().build();
        OkHttpClient okHttpClient = new OkHttpClient();
        final Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public void postFactory(String data, Callback callback) {
        LogTools.printLog(this.getClass(), "网络请求报文："+data);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), String.valueOf(data));
        Request request = new Request.Builder()
                .url(baseUrl + "/adv/video/insert")
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

}
