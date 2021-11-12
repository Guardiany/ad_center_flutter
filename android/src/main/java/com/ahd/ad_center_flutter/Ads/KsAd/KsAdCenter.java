package com.ahd.ad_center_flutter.Ads.KsAd;

import android.app.Activity;

import com.ahd.ad_center_flutter.AdCenter;
import com.ahd.ad_center_flutter.Ads.AdFather;
import com.ahd.ad_center_flutter.Ads.AdListener.AdDisplayListener;
import com.ahd.ad_center_flutter.Ads.AdListener.AdInitListener;
import com.ahd.ad_center_flutter.Ads.AdListener.AdPreLoadListener;
import com.ahd.ad_center_flutter.Log.LogTools;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsRewardVideoAd;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.SdkConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Author by GuangMingfei
 * Date on 2021/8/23.
 * Email guangmf@neusoft.com
 * Used for
 */
public class KsAdCenter implements AdFather {
    private static KsAdCenter ksAdCenter;
    private static boolean isStart = false;
    private static boolean isDisPlaying = false;
    private KsRewardVideoAd mRewardVideoAd;
    private AdInitListener mAdInitListener;
    private AdPreLoadListener mAdPreLoadListener;
    private AdDisplayListener mAdDisplayListener;
    private Activity mActivity;
    //快手激励广告ID
    private Long encourageId;
    private boolean mIsShowPortrait = true;
    private int screenOrientation = SdkConfig.SCREEN_ORIENTATION_PORTRAIT;

    private KsAdCenter() {
    }

    ;

    public static KsAdCenter getInstance() {
        if (ksAdCenter == null) {
            ksAdCenter = new KsAdCenter();
        }
        return ksAdCenter;
    }

    @Override
    public void initSDK(Activity appContext, String appName, String adId, String encourageId,AdInitListener adInitListener) {
        this.mActivity = appContext;
        this.encourageId = Long.parseLong(encourageId);
        this.mAdInitListener = adInitListener;
        KsAdSDK.init(appContext, new SdkConfig.Builder()
                .appId(adId) // 测试aapId，请联系快⼿平台申请正式AppId，必填
                .appName(appName) // 测试appName，请填写您应⽤的名称，⾮必填
                .showNotification(true) // 是否展示下载通知栏
                .debug(true) // 是否开启sdk 调试⽇志 可选
                .build());
        mAdInitListener.onSuccess(AdCenter.KSAD);

    }

    @Override
    public void preLoadAd(AdPreLoadListener adPreLoadListener) {
        this.mAdPreLoadListener = adPreLoadListener;
        KsScene.Builder builder = new KsScene.Builder(encourageId)
                .screenOrientation(screenOrientation);
        Map<String, String> rewardCallbackExtraData = new HashMap<>();
        rewardCallbackExtraData.put("thirdUserId", "test-uerid-jia");
        rewardCallbackExtraData.put("extraData", "testExtraData");

        builder.rewardCallbackExtraData(rewardCallbackExtraData);
        KsScene scene = builder.build(); // 此为测试posId，请联系快手平台申请正式posId
        // 请求的期望屏幕方向传递为1，表示期望为竖屏
        KsAdSDK.getLoadManager().loadRewardVideoAd(scene, new KsLoadManager.RewardVideoAdListener() {
            @Override
            public void onError(int code, String msg) {
                LogTools.printLog(this.getClass(), "快手返回报错:" + msg);
                //广告播放加载失败
                mAdPreLoadListener.onPreLoadFailed(AdCenter.KSAD, msg);
            }

            @Override
            public void onRequestResult(int adNumber) {
                if (adNumber != 1) {
                    LogTools.printLog(this.getClass(), "快手预加载返回:" + adNumber + "-" + getMessage(adNumber));
                }
            }

            @Override
            public void onRewardVideoAdLoad(List<KsRewardVideoAd> adList) {
                if (adList != null && !adList.isEmpty()) {
                    mRewardVideoAd = adList.get(0);
                    mAdPreLoadListener.onPreLoadSuccess(AdCenter.KSAD);
                } else {
                    mAdPreLoadListener.onPreLoadFailed(AdCenter.KSAD, "快手可播放列表未空");
                }
            }
        });
    }

    //播放广告
    @Override
    public void displayAd(AdDisplayListener adDisplayListener) {
        this.mAdDisplayListener = adDisplayListener;
        if (mRewardVideoAd != null && mRewardVideoAd.isAdEnable()) {
            mRewardVideoAd.setRewardAdInteractionListener(new KsRewardVideoAd.RewardAdInteractionListener() {
                @Override
                public void onAdClicked() {
                    LogTools.printLog(this.getClass(), "快手播放监听器:onAdClicked");
                }

                @Override
                public void onPageDismiss() {
                    LogTools.printLog(this.getClass(), "快手播放监听器:onPageDismiss");
                    mAdDisplayListener.onDisplaySuccess(AdCenter.KSAD);
                    isStart = false;
                }

                @Override
                public void onVideoPlayError(int code, int extra) {
                    LogTools.printLog(this.getClass(), "快手播放监听器:onVideoPlayError");
//                            ToastUtil.showToast(getActivity(), "激励视频广告播放出错");
                    mAdDisplayListener.onDisplayFailed(AdCenter.KSAD, 0, "播放出错");

                }

                @Override
                public void onVideoPlayEnd() {
                    LogTools.printLog(this.getClass(), "快手播放监听器:onVideoPlayEnd");
                    mAdDisplayListener.onVideoComplete(AdCenter.KSAD);
//                            ToastUtil.showToast(getActivity(), "激励视频广告播放完成");
                }

                @Override
                public void onVideoSkipToEnd(long l) {
                    LogTools.printLog(this.getClass(), "快手播放监听器:onVideoSkipToEnd");
                }

                @Override
                public void onVideoPlayStart() {
                    LogTools.printLog(this.getClass(), "快手播放监听器:onVideoPlayStart");
                    isDisPlaying = true;
                    mAdDisplayListener.onStartDisplay(AdCenter.KSAD);
                }

                @Override
                public void onRewardVerify() {
                    LogTools.printLog(this.getClass(), "快手播放监听器:onRewardVerify");
                    mAdDisplayListener.onVideoComplete(AdCenter.KSAD);
                }

                @Override
                public void onRewardStepVerify(int i, int i1) {
                    LogTools.printLog(this.getClass(), "快手播放监听器:onRewardStepVerify");
                }
            });

            startTimerListener();
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isStart = true;
                    isDisPlaying = false;
                    mRewardVideoAd.showRewardVideoAd(mActivity,null);
                }
            });
        } else {
            //未加载失败
            mAdDisplayListener.onDisplayFailed(AdCenter.KSAD, 1, "未加载完成请击,请稍后..");
        }
    }

    //                40001 没有⽹络
//                40002 数据解析失败
//                40003 ⼴告数据为空
//                40004 缓存视频资源失
//                100001 参数有误
//                100002 服务器错误
//                100003 不允许的操作
//                100004 服务不可⽤
//                310001 appId未注册
//                310002 appId⽆效
//                310003 appId已封禁
//                310004 packageName与注册的packageName不⼀致
//                310005 操作系统与注册的不⼀致
//                320002 appId对应账号⽆效
//                320003 appId对应账号已封禁
//                330001 posId未注册
//                330002 posId⽆效
//                330003 posId已封禁
//                330004 posid与注册的appId信息不⼀致
    private String getMessage(Integer number) {
        HashMap<Integer, String> errorMessages = new HashMap<>();
        errorMessages.put(40001, "没有⽹络");
        errorMessages.put(40002, "数据解析失败");
        errorMessages.put(40003, "告数据为空");
        errorMessages.put(40004, "缓存视频资源失");
        errorMessages.put(100001, "参数有误");
        errorMessages.put(100002, "服务器错误");
        errorMessages.put(100003, "不允许的操作");
        errorMessages.put(100004, "服务不可⽤");
        errorMessages.put(310001, "appId未注册");
        errorMessages.put(310002, "appId⽆效");
        errorMessages.put(310003, "appId已封禁");
        errorMessages.put(310004, "packageName与注册的packageName不⼀致");
        errorMessages.put(310005, "操作系统与注册的不⼀致");
        errorMessages.put(320002, "appId对应账号⽆效");
        errorMessages.put(320003, "appId对应账号已封禁");
        errorMessages.put(330001, "posId未注册");
        errorMessages.put(330002, "posId⽆效");
        errorMessages.put(330003, "posId已封禁");
        errorMessages.put(330004, "posid与注册的appId信息不⼀致");
        return errorMessages.get(number);
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
