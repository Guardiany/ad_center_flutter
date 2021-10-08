package com.ahd.ad_center_flutter;

import android.app.Activity;
import android.telecom.Call;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ahd.ad_center_flutter.Ads.AdListener.AdDisplayListener;
import com.ahd.ad_center_flutter.Ads.AdListener.AdInitListener;
import com.ahd.ad_center_flutter.Ads.AdListener.AdPreLoadListener;
import com.ahd.ad_center_flutter.Ads.KsAd.KsAdCenter;
import com.ahd.ad_center_flutter.Ads.TTAd.TTAdCenter;
import com.ahd.ad_center_flutter.Ads.YlhAd.YlhAdCenter;
//import com.ahd.ad_center_flutter.FloatView.FloatView;
import com.ahd.ad_center_flutter.Log.ClickTools;
import com.ahd.ad_center_flutter.Log.LogTools;
import com.ahd.ad_center_flutter.Net.HttpCenter;
import com.ahd.ad_center_flutter.Net.ResponseAdVideo;
import com.ahd.ad_center_flutter.OpenListener.PlayAdListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;
import okhttp3.Response;

/**
 * Author by GuangMingfei
 * Date on 2021/8/23.
 * Email guangmf@neusoft.com
 * Used for
 */
public class AdCenter {
    public static boolean isDebug = true;
    public boolean todayPlayOver = false;
    public String  currentSource = "0";
    public boolean mNeedPreLoad = false;
    //APP名称
    public static String APPNAME = "夕阳红";
    //穿山甲
    public static String TOUTIAOCATID = "5188306";
    public static String JILICATID = "946306237";
    //快手联盟
    public static String KUAISHOUCATID = "561000009";
    public static String KUAISHOUPOSID = "5610000009";
    //优量汇广告
    public static String APPCATID = "1111943439";
    public static String EDITPOSID = "7092501339756510";

    public static final int TTAD = 1, KSAD = 2, YLHAD = 3;
    public static boolean TTInitOK = false, KSInitOK = false, YLHInitOK = false;
    public static long TTFailedTime = -1 ,KSFailedTime = -1,YLHFailedTime = -1;
    public static int currentAd = 1;
    public static int nextAd = 2;

    public static boolean preLoadCurrentSuccess = false;
    public static AdCenter adCenter;
    private AdInitListener adInitListener;
    private AdPreLoadListener adPreLoadListener;
    private AdDisplayListener adDisplayListener;
    private PlayAdListener mPlayAdListener;
    private Activity mActivity;
    private MethodChannel.Result result;

    private long tolerateTime = 1000 * 60 * 3;

    private AdCenter() {
    }

    public static AdCenter getInstance() {
        if (adCenter == null) {
            adCenter = new AdCenter();
        }
        return adCenter;
    }

    /**容忍机制说明：
     * 1：首次错误不上报
     * 2：第二次错误两分钟之内不上报
     * 3：成功之后，下次错误按首次计算
     * 4：两次失败，连续，且间隔2分钟以上，上报失败。
     * 5：上报失败之后，重新记录失败时间
    */
    public void updateShowInfo(final int adFlag, int state, boolean needPreLoad) {
        this.mNeedPreLoad = needPreLoad;
        if (state == 1) {
            switch (adFlag){
                case TTAD:
                    TTFailedTime = -1;
                    break;
                case KSAD:
                    KSFailedTime = -1;
                    break;
                case YLHAD:
                    YLHFailedTime = -1;
                    break;
            }
            //成功每次都上传
            HttpCenter.getInstance().uploadAdResult(currentSource, adFlag, state,0, new okhttp3.Callback() {
                @Override
                public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                    if(response.code() == 200){
                        LogTools.printLog(this.getClass(), "上传服务器成功数据：******" + getAdName(adFlag) + ((response.code() == 200) ? "******成功" : "******失败"));
                    }
                    adCenter.getAdFromNet(mNeedPreLoad);
                }

                @Override
                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {

                }
            });
        } else {
            //失败时判断是否容忍
            //添加初次加载失败容忍机制，两分钟之内加载失败，全都不上报。
            boolean needUploadData = false;
            switch (adFlag){
                case TTAD:
                    if(TTFailedTime == -1){
                        TTFailedTime = System.currentTimeMillis();
                    }else{
                        if(System.currentTimeMillis() - TTFailedTime > tolerateTime){
                            TTFailedTime = System.currentTimeMillis();
                            needUploadData = true;
                        }
                    }
                    break;
                case KSAD:
                    if(KSFailedTime == -1){
                        KSFailedTime = System.currentTimeMillis();
                    }else{
                        if(System.currentTimeMillis() - KSFailedTime > tolerateTime){
                            KSFailedTime = System.currentTimeMillis();
                            needUploadData = true;
                        }
                    }
                    break;
                case YLHAD:
                    if(YLHFailedTime == -1){
                        YLHFailedTime = System.currentTimeMillis();
                    }else{
                        if(System.currentTimeMillis() - YLHFailedTime > tolerateTime){
                            YLHFailedTime = System.currentTimeMillis();
                            needUploadData = true;
                        }
                    }
                    break;

            }

            if(needUploadData){
                HttpCenter.getInstance().uploadAdResult(currentSource, adFlag, state,0, new okhttp3.Callback() {
                    @Override
                    public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                        if(response.code() == 200){
                            LogTools.printLog(this.getClass(), "上传服务器失败数据：" + getAdName(adFlag) + ((response.code() == 200) ? "成功" : "失败"));
                        }
                        getAdFromNet(mNeedPreLoad);
                    }

                    @Override
                    public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {

                    }
                });
            }else {
                HttpCenter.getInstance().uploadAdResult(currentSource, adFlag, state,1, new okhttp3.Callback() {
                    @Override
                    public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                        if(response.code() == 200){
                            LogTools.printLog(this.getClass(), "上传服务器跳过数据：" + getAdName(adFlag) + ((response.code() == 200) ? "成功" : "失败"));
                        }
                        getAdFromNet(mNeedPreLoad);
                    }

                    @Override
                    public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                        
                    }
                });
            }
        }
    }

    public void getAdFromNet(final boolean needPreLoad) {
        HttpCenter.getInstance().getNextAdFromWeb(new okhttp3.Callback() {
            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                final String requestData = response.body().string();
                if(response.code() != 200){
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mActivity, requestData, Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                LogTools.printLog(this.getClass(), "网络请求返回："+requestData);
                Gson gson = new Gson();
                final ResponseAdVideo responseAdVideo = gson.fromJson(requestData, ResponseAdVideo.class);
                if("200".equals(responseAdVideo.errCode)){
                    if(responseAdVideo.data.length() == 2){
                        currentAd = Integer.valueOf(responseAdVideo.data.substring(0,1));
                        nextAd = Integer.valueOf(responseAdVideo.data.substring(1,2));
                        if (needPreLoad) {
                            LogTools.printLog(this.getClass(), "------------------------------------------------------");
                            LogTools.printLog(this.getClass(), "网络返回数据：下一个广告:" + getAdName(currentAd) + ",备用广告：" + getAdName(nextAd));
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    preLoadAd();
                                }
                            });
                        }
                    }else if(responseAdVideo.data.length() == 1){
                        if("0".equals(responseAdVideo.data)){
                            todayPlayOver = true;
                            preLoadCurrentSuccess = true;
                        }
                    }
                }else{
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mActivity, "网络请求报错："+responseAdVideo.errMsg, Toast.LENGTH_LONG).show();
                        }
                    });
                    LogTools.printLog(this.getClass(), "网络请求报错："+responseAdVideo.errMsg);
                }
            }

            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                LogTools.printLog(this.getClass(), "网络请求报错："+e.getMessage());
            }
        });
    }


    public void showFlowView() {
        //初始化悬浮窗
//        FloatView.initAndShowFloatView(mActivity);
    }


    public void initAd(Activity activity, String channelId, String appId, String userId, MethodChannel.Result result) {
        if(mActivity != null){
            return;
        }
        this.result = result;
        HttpCenter.appId = appId;
        HttpCenter.userId = userId;
        HttpCenter.channel = channelId;
        this.mActivity = activity;
        LogTools.printLog(this.getClass(), "开始初始化SDK");

//        if (isDebug) {
//            FloatView.getShowFlowViewPermission(mActivity);
//        }

        //初始化预加载和播放监听器
        initListener();

//        //穿山甲广告SDK
        TTAdCenter.getInstance().initSDK(activity, APPNAME, TOUTIAOCATID, JILICATID, adInitListener);

        //初始化快手广告SDK
        KsAdCenter.getInstance().initSDK(activity, APPNAME, KUAISHOUCATID, KUAISHOUPOSID, adInitListener);

        //优量汇初始化
        YlhAdCenter.getInstance().initSDK(activity, APPNAME, APPCATID, EDITPOSID, adInitListener);


        //初始化本地计数
        //MockCountData.getInstance().initTodayAd(activity);
    }

    private void initListener() {
        //广告SDK初始化监听器
        adCenter.adInitListener = new AdInitListener() {
            @Override
            public void onSuccess(int adFlag) {
                LogTools.printLog(this.getClass(), getAdName(adFlag) + "初始化成功");
                switch (adFlag) {
                    case TTAD:
                        TTInitOK = true;
//                        FloatView.change(TTAD, 0);
                        break;
                    case KSAD:
                        KSInitOK = true;
//                        FloatView.change(KSAD, 0);
                        break;
                    case YLHAD:
                        YLHInitOK = true;
//                        FloatView.change(YLHAD, 0);
                        break;
                }

                if (TTInitOK && KSInitOK && YLHInitOK) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, Object> resultMap = new HashMap<>();
                            resultMap.put("result", "success");
                            resultMap.put("message", "广告Sdk初始化成功");
                            result.success(resultMap);
                        }
                    });
                    getAdFromNet(true);
                }
            }

            @Override
            public void onFailed(final int adFlag, final String errorMessage) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("result", "error");
                        resultMap.put("message", getAdName(adFlag)+"Sdk初始化失败："+errorMessage);
                        result.success(resultMap);
                    }
                });
                LogTools.printLog(this.getClass(), getAdName(adFlag) + "初始化失败：" + errorMessage);
            }
        };
        //初始化广告预加载监听级
        adCenter.adPreLoadListener = new AdPreLoadListener() {
            @Override
            public void onPreLoadSuccess(int adFlag) {
                LogTools.printLog(this.getClass(), "预加载" + getAdName(adFlag) + "广告成功");
//                FloatView.change(adFlag, 3);
                preLoadCurrentSuccess = true;
            }

            @Override
            public void onPreLoadFailed(int adFlag, String errorMessage) {
                LogTools.printLog(this.getClass(), "预加载" + getAdName(adFlag) + "失败：" + errorMessage);
//                FloatView.change(adFlag, 2);
                preLoadCurrentSuccess = false;
                //上报加载失败马上预加载下一个
                //currentAd = nextAd;
                //adCenter.preLoadAd();
                //updateShowInfo(adFlag, 0);
                //getAdFromNet(true);
            }
        };
        //初始化广告播放监听级
        adCenter.adDisplayListener = new AdDisplayListener() {
            @Override
            public void onStartDisplay(int adFlag) {
                LogTools.printLog(this.getClass(), "开始播放" + getAdName(adFlag) + "广告");
//                FloatView.change(adFlag, 0);
                //预加载广告已开始播放，请求接下来的广告，逻辑开始循环
                if(nextAd == 0){
                    todayPlayOver = true;
                    preLoadCurrentSuccess = true;
                }else{
                    currentAd = nextAd;
                    preLoadAd();
                }
            }

            @Override
            public void onVideoComplete(int adFlag) {
                LogTools.printLog(this.getClass(), getAdName(adFlag) + "回调发放奖励");
            }

            @Override
            public void onDisplaySuccess(int adFlag) {
                LogTools.printLog(this.getClass(), "播放" + getAdName(adFlag) + "广告成功");
                mPlayAdListener.onSuccess();
                updateShowInfo(adFlag, 1,false);
            }

            @Override
            public void onDisplayFailed(int adFlag, int error, String errorMessage) {
                LogTools.printLog(this.getClass(), "播放" + getAdName(adFlag) + "广告失败");
                //播放失败，上报
                //回调前端是否当前失败原因
                //提示播放失败后，直接提示失败，让用户等待，直接加载下一个广告，等待下次点击触发播放广告
                mPlayAdListener.onFailed(2, "广告还在加载哦，请稍后再试，谢谢");
                currentAd = nextAd;
                adCenter.preLoadAd();
                updateShowInfo(adFlag, 0,true);
                //adCenter.getAdFromNet(true);
            }
        };
    }

    public void preLoadAd() {
        if (TTInitOK && KSInitOK && YLHInitOK) {
//            FloatView.change(currentAd, 1);
            LogTools.printLog(this.getClass(), "开始预加载" + getAdName(currentAd) + "广告");
            switch (currentAd) {
                case TTAD:
                    TTAdCenter.getInstance().preLoadAd(adPreLoadListener);
                    break;
                case KSAD:
                    KsAdCenter.getInstance().preLoadAd(adPreLoadListener);
                    break;
                case YLHAD:
                    YlhAdCenter.getInstance().preLoadAd(adPreLoadListener);
                    break;
            }
        } else {
            LogTools.printLog(this.getClass(), "未初始化SDK");
        }
    }

    public void displayAd(String source,PlayAdListener playAdListener) {
        this.currentSource = source;
        if (ClickTools.isFastClick()) {
            LogTools.printLog(this.getClass(), "重复点击");
            return;
        }
        mPlayAdListener = playAdListener;

        if(mActivity == null){
            mPlayAdListener.onFailed(-1, "数据故障，请重启应用～～");
            return;
        }

        if(!HttpCenter.getInstance().checkInternet(mActivity)){
            mPlayAdListener.onFailed(-2, "请检查网络是否正常，恢复正常后重试～～");
            return;
        }

        //预加载失败
        if(!preLoadCurrentSuccess){
            mPlayAdListener.onFailed(-3, "您播放的太快哦，慢慢来～～");
            updateShowInfo(currentAd, 0,true);
            return;
        }


        if (TTInitOK && KSInitOK && YLHInitOK) {
            if (todayPlayOver) {
                mPlayAdListener.onFailed(-4, "今日广告次数已被抢光，明天早点来哦～～");
                return;
            }
            switch (currentAd) {
                case TTAD:
                    TTAdCenter.getInstance().displayAd(adDisplayListener);
                    break;
                case KSAD:
                    KsAdCenter.getInstance().displayAd(adDisplayListener);
                    break;
                case YLHAD:
                    try {
                        YlhAdCenter.getInstance().displayAd(adDisplayListener);
                    }catch (Exception e){
                        mPlayAdListener.onFailed(-1, "您播放的太快哦，慢慢来～～");
                        updateShowInfo(currentAd, 0,true);
                    }
                    break;
            }
            LogTools.printLog(this.getClass(), "开播放" + getAdName(currentAd) + "广告");
        } else {
            LogTools.printLog(this.getClass(), "未初始化SDK");
        }
    }

    private String getAdName(int key) {
        switch (key) {
            case TTAD:
                return "头条";
            case KSAD:
                return "快手";
            case YLHAD:
                return "优量汇";
        }
        return "未识别";
    }

    public void onDestroy(){
//        FloatView.onDestroy();
    }
}
