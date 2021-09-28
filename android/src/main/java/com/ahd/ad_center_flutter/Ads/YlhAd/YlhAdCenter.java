package com.ahd.ad_center_flutter.Ads.YlhAd;

import android.app.Activity;
import android.widget.Toast;

import com.ahd.ad_center_flutter.AdCenter;
import com.ahd.ad_center_flutter.Ads.AdFather;
import com.ahd.ad_center_flutter.Ads.AdListener.AdDisplayListener;
import com.ahd.ad_center_flutter.Ads.AdListener.AdInitListener;
import com.ahd.ad_center_flutter.Ads.AdListener.AdPreLoadListener;
import com.ahd.ad_center_flutter.Log.LogTools;
import com.kwad.sdk.api.SdkConfig;
import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.ads.rewardvideo.ServerSideVerificationOptions;
import com.qq.e.comm.managers.GDTAdSdk;
import com.qq.e.comm.util.AdError;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Author by GuangMingfei
 * Date on 2021/8/23.
 * Email guangmf@neusoft.com
 * Used for
 */
public class YlhAdCenter implements AdFather {
    private static YlhAdCenter ylhAdCenter;
    private static boolean isStart = false;
    private static boolean isDisPlaying = false;
    private RewardVideoAD rewardVideoAD;
    private String currentPosId;
    private AdInitListener mAdInitListener;
    private AdPreLoadListener mAdPreLoadListener;
    private AdDisplayListener mAdDisplayListener;
    private Activity mActivity;
    private boolean isLoaded = false;
    //优量汇激励广告ID
    private String encourageId;
    private boolean mIsShowPortrait = true;
    private int screenOrientation = SdkConfig.SCREEN_ORIENTATION_PORTRAIT;

    private YlhAdCenter() {
    }

    public static YlhAdCenter getInstance() {
        if (ylhAdCenter == null) {
            ylhAdCenter = new YlhAdCenter();
        }
        return ylhAdCenter;
    }

    @Override
    public void initSDK(Activity appContext, String appName, String adId, String encourageId, AdInitListener adInitListener) {
        mActivity = appContext;
        this.encourageId = encourageId;
        this.mAdInitListener = adInitListener;
        GDTAdSdk.init(appContext, adId);
        adInitListener.onSuccess(AdCenter.YLHAD);
    }


    @Override
    public void preLoadAd(AdPreLoadListener adPreLoadListener) {
        mAdPreLoadListener = adPreLoadListener;
        rewardVideoAD = new RewardVideoAD(mActivity, encourageId, new RewardVideoADListener() {

            @Override
            public void onADLoad() {
                LogTools.printLog(this.getClass(), "优量汇onADLoad");
                //加载成功
                isLoaded = true;
                mAdPreLoadListener.onPreLoadSuccess(AdCenter.YLHAD);
            }

            @Override
            public void onVideoCached() {
                LogTools.printLog(this.getClass(), "优量汇onVideoCached");
                //视频素材缓存成功，可在此回调后进行广告展示
                isLoaded = true;
            }

            @Override
            public void onADShow() {
                LogTools.printLog(this.getClass(), "优量汇onADShow");
                mAdDisplayListener.onStartDisplay(AdCenter.YLHAD);
                isDisPlaying = true;
            }

            @Override
            public void onADExpose() {
                LogTools.printLog(this.getClass(), "优量汇onADExpose");
            }

            @Override
            public void onReward(Map<String, Object> map) {
                LogTools.printLog(this.getClass(), "优量汇onReward");
                mAdDisplayListener.onVideoComplete(AdCenter.YLHAD);
            }

            @Override
            public void onADClick() {
                LogTools.printLog(this.getClass(), "优量汇onADClick");
            }

            @Override
            public void onVideoComplete() {
                LogTools.printLog(this.getClass(), "优量汇onVideoComplete");
            }

            @Override
            public void onADClose() {
                LogTools.printLog(this.getClass(), "优量汇onADClose");
                mAdDisplayListener.onDisplaySuccess(AdCenter.YLHAD);
                isStart = false;
            }

            @Override
            public void onError(AdError adError) {
                LogTools.printLog(this.getClass(), "优量汇onError" + adError.getErrorMsg());
                if (AdCenter.isDebug) {
                    Toast.makeText(mActivity, "优量汇报错：" + adError.getErrorMsg(), Toast.LENGTH_LONG).show();
                }
                if (mAdPreLoadListener != null) {
                    mAdPreLoadListener.onPreLoadFailed(AdCenter.YLHAD, adError.getErrorMsg());
                } else if (mAdDisplayListener != null) {
                    mAdPreLoadListener.onPreLoadFailed(AdCenter.YLHAD, adError.getErrorMsg());
                } else if (mAdInitListener != null) {
                    mAdInitListener.onFailed(AdCenter.YLHAD, adError.getErrorMsg());
                }
            }
        }, true);
        ServerSideVerificationOptions options = new ServerSideVerificationOptions.Builder()
                .setCustomData("APP's custom data") // 设置激励视频服务端验证的自定义信息
                .setUserId("APP's user id for server verify") // 设置服务端验证的用户信息
                .build();
        rewardVideoAD.setServerSideVerificationOptions(options);
        currentPosId = encourageId;
        isLoaded = false;
        rewardVideoAD.loadAD();
    }

    @Override
    public void displayAd(AdDisplayListener adDisplayListener) {
        mAdDisplayListener = adDisplayListener;
        if (isLoaded) {
            if (!rewardVideoAD.hasShown()) {//广告展示检查2：当前广告数据还没有展示过
                startTimerListener();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isStart = true;
                        isDisPlaying = false;
                        rewardVideoAD.showAD();
                    }
                });
            } else {
                //Toast.makeText(mActivity, "此条广告已经展示过，请再次请求广告后进行广告展示！", Toast.LENGTH_LONG).show();
                mAdDisplayListener.onDisplayFailed(AdCenter.YLHAD, 1, "未加载完成请击,请稍后...");
            }
        } else {
            mAdDisplayListener.onDisplayFailed(AdCenter.YLHAD, 1, "未加载完成请击,请稍后...");
        }
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
