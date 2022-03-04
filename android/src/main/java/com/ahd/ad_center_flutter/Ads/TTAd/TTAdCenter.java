package com.ahd.ad_center_flutter.Ads.TTAd;

import static com.bytedance.sdk.openadsdk.TTAdLoadType.LOAD;
import static com.bytedance.sdk.openadsdk.TTAdLoadType.PRELOAD;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ahd.ad_center_flutter.AdCenter;
import com.ahd.ad_center_flutter.Ads.AdFather;
import com.ahd.ad_center_flutter.Ads.AdListener.AdDisplayListener;
import com.ahd.ad_center_flutter.Ads.AdListener.AdInitListener;
import com.ahd.ad_center_flutter.Ads.AdListener.AdPreLoadListener;
import com.ahd.ad_center_flutter.Log.LogTools;
import com.bytedance.msdk.adapter.util.UIUtils;
import com.bytedance.msdk.api.AdError;
import com.bytedance.msdk.api.v2.GMAdConstant;
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAd;
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAdLoadCallback;
import com.bytedance.msdk.api.v2.slot.GMAdSlotSplash;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.bytedance.sdk.openadsdk.TTSplashAd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.flutter.plugin.common.MethodChannel;

/**
 * Author by GuangMingfei
 * Date on 2021/8/23.
 * Email guangmf@neusoft.com
 * Used for
 */
public class TTAdCenter implements AdFather {
    private static TTAdCenter ttAdCenter;
    private static boolean isStart = false;
    private static boolean isDisPlaying = false;
    private TTAdManager ttAdManager;
    private TTRewardVideoAd onlindAd;
    private AdInitListener mAdInitLister;
    private AdPreLoadListener mAdPreLoadListener;
    private AdDisplayListener mAdDisplayListener;
    private Activity mContext;
    private TTSplashAd ttSplashAd;
    private GMSplashAd gmSplashAd;
    private TTNativeExpressAd ttNativeExpressHalfAd;
    private TTNativeExpressAd ttNativeExpressFullAd;
    private TTNativeExpressAd ttNativeBannerAd;
    //头条ID
    private String ksId;
    //快手激励广告ID
    private String encourageId;

    private boolean isInit = false;

    private TTAdCenter() {
    }

    public static TTAdCenter getInstance() {
        if (ttAdCenter == null) {
            ttAdCenter = new TTAdCenter();
        }
        return ttAdCenter;
    }

    public Activity getmContext() {
        return mContext;
    }

    public TTSplashAd getTtSplashAd() {
        return ttSplashAd;
    }

    public GMSplashAd getGmSplashAd() {
        return gmSplashAd;
    }

    public TTNativeExpressAd getTtNativeExpressHalfAd() {
        return ttNativeExpressHalfAd;
    }

    public TTNativeExpressAd getTtNativeExpressFullAd() {
        return ttNativeExpressFullAd;
    }

    public TTNativeExpressAd getTtNativeBannerAd() {
        return ttNativeBannerAd;
    }

    @Override
    public void initSDK(Activity appContext, String appName, String adId, String encourageId, AdInitListener initListener) {
        this.mContext = appContext;
        this.encourageId = encourageId;
        this.mAdInitLister = initListener;
        Log.i("穿山甲版本-->", TTAdSdk.getAdManager().getSDKVersion());
        TTAdSdk.init(appContext, buildConfig(adId), new TTAdSdk.InitCallback() {
            @Override
            public void success() {
                Log.i("TTAdCenter-->", "success: ");
                isInit = true;
                mAdInitLister.onSuccess(AdCenter.TTAD);
            }

            @Override
            public void fail(int code, String msg) {
                Log.i("TTAdCenter-->", "fail:  code = " + code + " msg = " + msg);
                isInit = false;
                mAdInitLister.onFailed(AdCenter.TTAD,msg);
            }
        });
    }

    private static TTAdConfig buildConfig(String ttId) {
        return new TTAdConfig.Builder()
                .appId(ttId)
                .useTextureView(true) //使用TextureView控件播放视频,默认为SurfaceView,当有SurfaceView冲突的场景，可以使用TextureView
                .allowShowNotify(true) //是否允许sdk展示通知栏提示
//                .allowShowPageWhenScreenLock(true) // 锁屏下穿山甲SDK不会再出落地页，此API已废弃，调用没有任何效果
                .debug(true) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI, TTAdConstant.NETWORK_STATE_3G, TTAdConstant.NETWORK_STATE_4G) //允许直接下载的网络状态集合
                .supportMultiProcess(true)//是否支持多进程
                .needClearTaskReset()
                .build();
    }

    public void preLoadSplashAd(String codeId, boolean userGroMore, final MethodChannel.Result result) {
        if(!isInit){
            LogTools.printLog(this.getClass(), "头条初始化未完成");
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", "error");
            resultMap.put("message", "初始化未完成");
            sendResult(resultMap, result);
            //初始化失败
            return;
        }
        if (userGroMore) {
            final GMSplashAd loadSplashAd = new GMSplashAd(mContext, codeId);
            //创建开屏广告请求参数AdSlot,具体参数含义参考文档
            GMAdSlotSplash adSlot = new GMAdSlotSplash.Builder()
                    .setImageAdSize(UIUtils.getScreenWidth(mContext), UIUtils.getScreenHeight(mContext)) // 单位px
                    .setTimeOut(4000)//设置超时
                    .setSplashButtonType(GMAdConstant.SPLASH_BUTTON_TYPE_FULL_SCREEN)
                    .setDownloadType(GMAdConstant.DOWNLOAD_TYPE_POPUP)
//                    .setForceLoadBottom(mForceLoadBottom) //强制加载兜底开屏广告，只能在GroMore提供的demo中使用，其他情况设置无效
                    .build();
            //请求广告，调用开屏广告异步请求接口，对请求回调的广告作渲染处理
            loadSplashAd.loadAd(adSlot, new GMSplashAdLoadCallback() {
                @Override
                public void onSplashAdLoadFail(@NonNull AdError adError) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("result", "error");
                    resultMap.put("message", "聚合开屏广告加载失败："+adError.message);
                    sendResult(resultMap, result);
                }

                @Override
                public void onSplashAdLoadSuccess() {
                    TTAdCenter.this.gmSplashAd = loadSplashAd;
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("result", "success");
                    resultMap.put("message", "聚合开屏广告加载成功");
                    sendResult(resultMap, result);
                }

                @Override
                public void onAdLoadTimeout() {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("result", "error");
                    resultMap.put("message", "聚合开屏广告加载超时");
                    sendResult(resultMap, result);
                }
            });
        } else {
            ttAdManager = TTAdSdk.getAdManager();
            TTAdNative mTTAdNative = ttAdManager.createAdNative(mContext);
            AdSlot adSlot = new AdSlot.Builder()
                    .setCodeId(codeId)
                    .setImageAcceptedSize(1080, 1920)
                    .setAdLoadType(PRELOAD)//推荐使用，用于标注此次的广告请求用途为预加载（当做缓存）还是实时加载，方便后续为开发者优化相关策略
                    .build();
            mTTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
                @Override
                public void onError(int i, String s) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("result", "error");
                    resultMap.put("message", "开屏广告加载失败："+s);
                    sendResult(resultMap, result);
                }

                @Override
                public void onTimeout() {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("result", "error");
                    resultMap.put("message", "开屏广告加载超时");
                    sendResult(resultMap, result);
                }

                @Override
                public void onSplashAdLoad(TTSplashAd ttSplashAd) {
                    if (ttSplashAd == null) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("result", "error");
                        resultMap.put("message", "拉取广告失败");
                        sendResult(resultMap, result);
                        return;
                    }
                    TTAdCenter.this.ttSplashAd = ttSplashAd;
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("result", "success");
                    resultMap.put("message", "广告加载成功");
                    sendResult(resultMap, result);
                }
            }, 3000);
        }
    }

    public void preLoadBannerAdView(String codeId, float width, float height) {
        TTAdNative mTTAdNative = TTAdSdk.getAdManager().createAdNative(mContext);
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(width,height) //期望模板广告view的size,单位dp
                .setAdLoadType(LOAD)//推荐使用，用于标注此次的广告请求用途为预加载（当做缓存）还是实时加载，方便后续为开发者优化相关策略
                .build();
        mTTAdNative.loadBannerExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int i, String s) {
                LogTools.printLog(this.getClass(), "Banner广告预加载失败："+s);
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> list) {
                if (!list.isEmpty()) {
                    TTAdCenter.this.ttNativeBannerAd = list.get(0);
                }
            }
        });
    }

    public void preLoadNativeAdView(String codeId, float adWidth, float adHeight, final int type) {
        if(!isInit) {
            return;
        }
        ttAdManager = TTAdSdk.getAdManager();
        TTAdNative mTTAdNative = ttAdManager.createAdNative(mContext);
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(adWidth,adHeight) //期望模板广告view的size,单位dp
                .setAdLoadType(PRELOAD)//推荐使用，用于标注此次的广告请求用途为预加载（当做缓存）还是实时加载，方便后续为开发者优化相关策略
                .build();
        mTTAdNative.loadNativeExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int i, String s) {
                LogTools.printLog(this.getClass(), "信息流广告预加载失败："+s);
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> list) {
                if (!list.isEmpty()) {
                    if (type == 0) {
                        TTAdCenter.this.ttNativeExpressHalfAd = list.get(0);
                    } else {
                        TTAdCenter.this.ttNativeExpressFullAd = list.get(0);
                    }
                }
            }
        });
    }

    private void sendResult(final Map<String, Object> resultMap, final MethodChannel.Result result) {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                result.success(resultMap);
            }
        });
    }

    @Override
    public void preLoadAd(AdPreLoadListener adPreLoadListener) {
        this.mAdPreLoadListener = adPreLoadListener;
        if(!isInit){
            LogTools.printLog(this.getClass(), "头条初始化未完成");
            //初始化失败
            return;
        }
        ttAdManager = TTAdSdk.getAdManager();
        TTAdNative mTTAdNative = ttAdManager.createAdNative(mContext);
        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        //模板广告需要设置期望个性化模板广告的大小,单位dp,激励视频场景，只要设置的值大于0即可
        //        且仅是模板渲染的代码位ID使用，非模板渲染代码位切勿使用
        //必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(encourageId)
                //模板广告需要设置期望个性化模板广告的大小,单位dp,激励视频场景，只要设置的值大于0即可
//        且仅是模板渲染的代码位ID使用，非模板渲染代码位切勿使用
                .setExpressViewAcceptedSize(500, 500)
                .setOrientation(TTAdConstant.VERTICAL) //必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .build();

        mTTAdNative.loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                mAdPreLoadListener.onPreLoadFailed(AdCenter.TTAD, "播放出错" + message);
                LogTools.printLog(this.getClass(), "头条");
            }

            //视频广告加载后，视频资源缓存到本地的回调，在此回调后，播放本地视频，流畅不阻塞。
            @Override
            public void onRewardVideoCached() {
                LogTools.printLog(this.getClass(), "头条缓存本地成功onRewardVideoCached()");


            }

            public void onRewardVideoCached(TTRewardVideoAd ad) {
                onlindAd = ad;
                LogTools.printLog(this.getClass(), "onRewardVideoCached()");
            }

            //视频广告的素材加载完毕，比如视频url等，在此回调后，可以播放在线视频，网络不好可能出现加载缓冲，影响体验。
            @Override
            public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
                onlindAd = ad;
                LogTools.printLog(this.getClass(), "头条素材加载完毕onRewardVideoAdLoad");
                mAdPreLoadListener.onPreLoadSuccess(AdCenter.TTAD);
            }
        });
    }

    @Override
    public void displayAd(AdDisplayListener adDisplayListener) {
        this.mAdDisplayListener = adDisplayListener;
        if(!isInit){
            //初始化失败
            this.mAdDisplayListener.onDisplayFailed(AdCenter.TTAD,1,"未加载完成请重新点击。。");
            return;
        }
        if(onlindAd == null){
            this.mAdDisplayListener.onDisplayFailed(AdCenter.TTAD,1,"未加载完成请重新点击");
            return;
        }
        onlindAd.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {

            @Override
            public void onAdShow() {
                mAdDisplayListener.onStartDisplay(AdCenter.TTAD);
                isDisPlaying = true;
                Log.d("TAG", "Callback --> rewardVideoAd show");
            }

            @Override
            public void onAdVideoBarClick() {
                Log.d("TAG", "Callback --> rewardVideoAd bar click");
                mAdDisplayListener.onAdClick();
            }

            //视频关闭回调
            @Override
            public void onAdClose() {
                mAdDisplayListener.onDisplaySuccess(AdCenter.TTAD);
                isStart = false;
                Log.d("TAG", "Callback --> rewardVideoAd close");
            }

            //视频播放完成回调
            @Override
            public void onVideoComplete() {
                Log.d("TAG", "Callback --> rewardVideoAd complete");
            }

            @Override
            public void onVideoError() {
                mAdDisplayListener.onDisplayFailed(AdCenter.TTAD,0,"播放失败");
                Log.e("TAG", "Callback --> rewardVideoAd error");
            }

            //视频播放完成后，奖励验证回调，rewardVerify：是否有效，rewardAmount：奖励梳理，rewardName：奖励名称
            @Override
            public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int errorCode, String errorMsg) {
                mAdDisplayListener.onVideoComplete(AdCenter.TTAD);
            }

            @Override
            public void onSkippedVideo() {
                Log.e("TAG", "Callback --> rewardVideoAd has onSkippedVideo");
            }
        });

        startTimerListener();
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isStart = true;
                isDisPlaying = false;
                onlindAd.showRewardVideoAd(mContext);
            }
        });
    }

    private void startTimerListener(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * 10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(isStart && !isDisPlaying){
                    mAdDisplayListener.onDisplayFailed(AdCenter.TTAD,0,"头条广告播放失败");
                }
            }
        };
        //触发播放，10秒内没有正常开始播放，按播放失败处理
        Timer timer = new Timer();//实例化Timer类
        timer.schedule(timerTask, 1000 * 10);//五百毫秒
    }
}
