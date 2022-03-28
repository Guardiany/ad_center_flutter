package com.ahd.ad_center_flutter;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ahd.ad_center_flutter.Ads.AdListener.AdDisplayListener;
import com.ahd.ad_center_flutter.Ads.AdListener.AdInitListener;
import com.ahd.ad_center_flutter.Ads.AdListener.AdPreLoadListener;
import com.ahd.ad_center_flutter.Ads.KsAd.KsAdCenter;
import com.ahd.ad_center_flutter.Ads.ProMore.ProMoreCenter;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import io.flutter.plugin.common.MethodChannel;
import okhttp3.Call;
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
    public static String APPNAME = "";
    //穿山甲
    public static String TOUTIAOCATID = "";
    public static String JILICATID = "";
    //快手联盟
    public static String KUAISHOUCATID = "";
    public static String KUAISHOUPOSID = "";
    //优量汇广告
    public static String APPCATID = "";
    public static String EDITPOSID = "";
    //ProMore广告
    public static String ProMoreId = "";
    public static String ProMoreJiLiId = "";

    public static final int TTAD = 1, KSAD = 2, YLHAD = 3, PROMOREAD = 4;
    public static boolean TTInitOK = false, KSInitOK = false, YLHInitOK = false, PROMOREInitOk = false;
    public static long TTFailedTime = -1 ,KSFailedTime = -1,YLHFailedTime = -1;
    public static int currentAd = 1;
    public static int nextAd = 2;

    public static boolean preLoadCurrentSuccess = false;
    private boolean groMoreLoadSuccess = false;
    public static AdCenter adCenter;
    private AdInitListener adInitListener;
    private AdPreLoadListener adPreLoadListener;
    private AdDisplayListener adDisplayListener;
    private PlayAdListener mPlayAdListener;
    private Activity mActivity;
    private MethodChannel.Result result;
    private boolean isAdClick = false;
    private boolean userProMore = false;
    public static boolean needWait = true;
    public static boolean hasWaited = false;
    public boolean isWaiting = false;

    private long tolerateTime = 1000 * 60 * 3;

    private TreeMap<Integer,Boolean> preLoadMap = new TreeMap<>();
    private HashMap<Integer,Integer> adFlagFromIndex = new LinkedHashMap<>();
    private int currentPreLoadIndex = 0;

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
    public void updateShowInfo(int adFlag, int state, boolean needPreLoad) {
        if (userProMore) {
            if(adFlag == 4){
                adFlag = 1;
            }
            if (state == 1) {
                //成功每次都上传
                HttpCenter.getInstance().uploadAdResult(currentSource, adFlag, state, 0, new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {

                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                    }
                });
            } else {
                checkAndPreLoad();
            }
            return;
        }
        final int adFlagFinal = adFlag;
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
                        LogTools.printLog(this.getClass(), "上传服务器成功数据：******" + getAdName(adFlagFinal) + ((response.code() == 200) ? "******成功" : "******失败"));
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
                            LogTools.printLog(this.getClass(), "上传服务器失败数据：" + getAdName(adFlagFinal) + ((response.code() == 200) ? "成功" : "失败"));
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
                            LogTools.printLog(this.getClass(), "上传服务器跳过数据：" + getAdName(adFlagFinal) + ((response.code() == 200) ? "成功" : "失败"));
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
                        currentAd = Integer.parseInt(responseAdVideo.data.substring(0,1));
                        nextAd = Integer.parseInt(responseAdVideo.data.substring(1,2));
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

    public void setUserId(String userId) {
        HttpCenter.userId = userId;
    }


    public void initAd(Activity activity, String channelId, String appId, String userId, boolean userProMore, MethodChannel.Result result) {
        if(mActivity != null){
            return;
        }

        preLoadMap.put(4,false);
        preLoadMap.put(2,false);
        preLoadMap.put(3,false);
        adFlagFromIndex.put(0,4);
        adFlagFromIndex.put(1,2);
        adFlagFromIndex.put(2,3);

        this.result = result;
        this.userProMore = userProMore;
        HttpCenter.appId = appId;
        HttpCenter.userId = userId;
        HttpCenter.channel = channelId;
        this.mActivity = activity;
        LogTools.printLog(this.getClass(), "开始初始化SDK");

        //初始化预加载和播放监听器
        initListener();

        //穿山甲广告SDK
        TTAdCenter.getInstance().initSDK(activity, APPNAME, TOUTIAOCATID, JILICATID, adInitListener);

        //初始化快手广告SDK
        KsAdCenter.getInstance().initSDK(activity, APPNAME, KUAISHOUCATID, KUAISHOUPOSID, adInitListener);

        //优量汇初始化
//        YlhAdCenter.getInstance().initSDK(activity, APPNAME, APPCATID, EDITPOSID, adInitListener);

        if (userProMore) {
            //初始化聚合广告SDK
            ProMoreCenter.getInstance().initSDK(activity, APPNAME, "", "", adInitListener);
        }
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
                        break;
                    case KSAD:
                        KSInitOK = true;
                        break;
                    case YLHAD:
                        YLHInitOK = true;
                        break;
                    case PROMOREAD:
                        PROMOREInitOk = true;
                        break;
                }

                if (userProMore) {
                    if (PROMOREInitOk && TTInitOK && KSInitOK /*&& YLHInitOK*/) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Map<String, Object> resultMap = new HashMap<>();
                                resultMap.put("result", "success");
                                resultMap.put("message", "广告Sdk初始化成功");
                                result.success(resultMap);
                            }
                        });
                        checkAndPreLoad();
                    }
                } else {
                    if (TTInitOK && KSInitOK /*&& YLHInitOK*/) {
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
                currentAd = adFlag;
                LogTools.printLog(this.getClass(), "预加载" + getAdName(adFlag) + "广告成功");
//                if (userProMore) {
//                    preLoadMap.put(adFlag,true);
//                    if(isWaiting){
////                        LoadingDialogUtil.getInstance().closeLoadingDialog();
//                        displayAd("10",mPlayAdListener);
//                    }else{
//                        checkAndPreLoad();
//                    }
//                }
                if (adFlag == PROMOREAD) {
                    groMoreLoadSuccess = true;
                }
                preLoadCurrentSuccess = true;
            }

            @Override
            public void onPreLoadFailed(int adFlag, String errorMessage) {
                LogTools.printLog(this.getClass(), "预加载" + getAdName(adFlag) + "失败：" + errorMessage);
                if (userProMore) {
                    preLoadMap.put(adFlag,false);
//                    checkAndPreLoad();
                } else {
                    preLoadCurrentSuccess = false;
                }
//                if (mPlayAdListener != null) {
//                    mPlayAdListener.onFailed(2, "广告拉取失败，请稍后再试。");
//                }
            }
        };
        //初始化广告播放监听级
        adCenter.adDisplayListener = new AdDisplayListener() {
            @Override
            public void onStartDisplay(int adFlag) {
                LogTools.printLog(this.getClass(), "开始播放" + getAdName(adFlag) + "广告");
                if (userProMore) {
                    preLoadMap.put(adFlag,false);
                    //预加载广告已开始播放，请求接下来的广告，逻辑开始循环
                    currentPreLoadIndex = 0;
                    checkAndPreLoad();
                    return;
                }
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
                mPlayAdListener.onSuccess(isAdClick);
                updateShowInfo(adFlag, 1,false);
            }

            @Override
            public void onDisplayFailed(int adFlag, int error, String errorMessage) {
                LogTools.printLog(this.getClass(), "播放" + getAdName(adFlag) + "广告失败");
                //播放失败，上报
                //回调前端是否当前失败原因
                if (userProMore) {
                    preLoadMap.put(adFlag,false);
                } else {
                    currentAd = nextAd;
                    adCenter.preLoadAd();
                }

                //提示播放失败后，直接提示失败，让用户等待，直接加载下一个广告，等待下次点击触发播放广告
                mPlayAdListener.onFailed(2, "休息一会儿，稍后再来！！！");
                updateShowInfo(adFlag, 0,true);
            }

            @Override
            public void onAdClick() {
                isAdClick = true;
                LogTools.printLog(this.getClass(), "----------点击广告--------");
            }
        };
    }

    private void checkAndPreLoad(){
         if(currentPreLoadIndex >= preLoadMap.size()){
            //已经加载一轮
            needWait = false;
            currentPreLoadIndex = 0;
            if(isWaiting){
                displayAd("10",mPlayAdListener);
            }
        }else{
            //仍在加载
            if(!preLoadMap.get(adFlagFromIndex.get(currentPreLoadIndex))){
                //当前AD未预加载
                preLoadAd(KSAD,true);
                preLoadAd(PROMOREAD, true);
            }else{
                //当前AD已经预加载
                currentPreLoadIndex++;
                needWait = true;
                checkAndPreLoad();
            }
        }
    }

    public void preLoadAd(int adFlag, boolean isIndex) {
        //初始化普通激励广告，防止聚合广告加载失败的情况
//        adFlag = KSAD;
//        preLoadAd();
//        if(isIndex){
//            adFlag  = adFlagFromIndex.get(adFlag);
//        }
        if (KSInitOK /*&& YLHInitOK*/ && PROMOREInitOk) {
            LogTools.printLog(this.getClass(), "开始预加载" + getAdName(adFlag) + "广告");
            preLoadCurrentSuccess = false;
            groMoreLoadSuccess = false;
            needWait = true;
            switch (adFlag){
                case PROMOREAD:
                    ProMoreCenter.getInstance().preLoadAd(adPreLoadListener);
                    break;
                case KSAD:
                    KsAdCenter.getInstance().preLoadAd(adPreLoadListener);
                    break;
                case YLHAD:
//                    YlhAdCenter.getInstance().preLoadAd(adPreLoadListener);
                    break;
            }
        } else {
            LogTools.printLog(this.getClass(), "未初始化SDK");
        }
    }

    public void preLoadAd() {
        if (TTInitOK && KSInitOK /*&& YLHInitOK*/) {
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
        isAdClick = false;
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
//        if (!preLoadCurrentSuccess) {
//            mPlayAdListener.onFailed(-3, "广告拉取失败，请稍后再试");
//            updateShowInfo(currentAd, 0, true);
//            preLoadAd(currentPreLoadIndex++,true);
//            return;
//        }

        if ((TTInitOK || PROMOREInitOk) && KSInitOK /*&& YLHInitOK*/) {
            if (userProMore) {
                if (!preLoadCurrentSuccess) {
                    mPlayAdListener.onFailed(-1, "广告加载失败，请稍后再来...");
                    return;
                }
                if (groMoreLoadSuccess) {
                    currentAd = PROMOREAD;
                }
//                if(preLoadMap.get(4)){
//                    currentAd = 4;
//                }else if(preLoadMap.get(2)){
//                    currentAd = 2;
//                }else if(preLoadMap.get(3)){
//                    currentAd = 3;
//                }

                //预加载失败
                if(currentAd == -1){
                    if(needWait){
                        isWaiting = true;
                        LogTools.printLog(this.getClass(), "正在加载数据。。。");
                        //LoadingDialogUtil.getInstance().showLoadingDialog(mActivity, "正在加载数据。。。");
                    }else{
                        if(hasWaited && isWaiting){
                            //LoadingDialogUtil.getInstance().closeLoadingDialog();
                            hasWaited = false;
                            isWaiting = false;
                            mPlayAdListener.onFailed(-1,"提示用户“休息一会儿，稍后再来 （-v-）！！！");
                        }else{
                            if(!hasWaited){
                                //一轮加载完毕，没有成功，预加载任何广告。
                                isWaiting = true;
                                LogTools.printLog(this.getClass(), "正在加载数据。。。");
                                //LoadingDialogUtil.getInstance().showLoadingDialog(mActivity,"正在加载数据。。。");
                                //重制预加载广告索引，重新加载一轮
                                currentPreLoadIndex = 0;
                                hasWaited = true;
                                checkAndPreLoad();
                            }
                        }
                    }
                    return;
                }else{
                    hasWaited = false;
                    isWaiting = false;
                }
            } else {
                if (todayPlayOver) {
                    mPlayAdListener.onFailed(-4, "今日广告次数已被抢光，明天早点来哦～～");
                    return;
                }
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
                case PROMOREAD:
                    ProMoreCenter.getInstance().displayAd(adDisplayListener);
                    break;
                default:
                    mPlayAdListener.onFailed(-1, "休息一会儿，稍后再来...");
                    break;
            }
            LogTools.printLog(this.getClass(), "开始播放" + getAdName(currentAd) + "广告");
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
            case PROMOREAD:
                return  "ProMore";
        }
        return "未识别";
    }

    public void onDestroy(){
//        FloatView.onDestroy();
    }
}
