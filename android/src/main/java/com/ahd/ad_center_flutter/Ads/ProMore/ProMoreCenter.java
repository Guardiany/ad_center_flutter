package com.ahd.ad_center_flutter.Ads.ProMore;

import android.app.Activity;

import com.ahd.ad_center_flutter.AdCenter;
import com.ahd.ad_center_flutter.Ads.AdFather;
import com.ahd.ad_center_flutter.Ads.AdListener.AdDisplayListener;
import com.ahd.ad_center_flutter.Ads.AdListener.AdInitListener;
import com.ahd.ad_center_flutter.Ads.AdListener.AdPreLoadListener;
import com.ahd.ad_center_flutter.Ads.ProMore.Manager.AdRewardManager;
import com.bytedance.msdk.api.AdError;
import com.bytedance.msdk.api.reward.RewardItem;
import com.bytedance.msdk.api.v2.GMAdConstant;
import com.bytedance.msdk.api.v2.GMMediationAdSdk;
import com.bytedance.msdk.api.v2.ad.reward.GMRewardAd;
import com.bytedance.msdk.api.v2.ad.reward.GMRewardedAdListener;
import com.bytedance.msdk.api.v2.ad.reward.GMRewardedAdLoadCallback;

import org.xutils.common.util.LogUtil;

public class ProMoreCenter implements AdFather {

    private static ProMoreCenter proMoreCenter;
    private Boolean mLoadSuccess = false; //是否加载成功
    private boolean mIsLoadedAndShow;//广告加载成功并展示
    private GMRewardedAdListener mGMRewardedAdListener;
    private AdDisplayListener mAdDisplayListener;
    private GMRewardedAdListener mGMRewardedPlayAgainListener;
    private GMRewardAd mGMRewardAd;
    private static AdRewardManager mAdRewardManager;
    private boolean isClicked = true;


    private static Activity mActivity;

    private ProMoreCenter() {};

    public static ProMoreCenter getInstance() {
        if (proMoreCenter == null) {
            proMoreCenter = new ProMoreCenter();
        }
        return proMoreCenter;
    }

    @Override
    public void initSDK(Activity appContext, String appName, String adId, String encourageId, AdInitListener initListener) {
        mActivity = appContext;
        GMAdManagerHolder.init(mActivity);
        mAdRewardManager = new AdRewardManager(mActivity);
        GMMediationAdSdk.requestPermissionIfNecessary(mActivity);
        initListener();
        initAdLoader(initListener);
    }

    @Override
    public void preLoadAd(final AdPreLoadListener adPreLoadListener) {
        mAdRewardManager.loadAd(new GMRewardedAdLoadCallback() {
            @Override
            public void onRewardVideoLoadFail(AdError adError) {
                mLoadSuccess = false;
                mAdRewardManager.printLoadFailAdnInfo();
                adPreLoadListener.onPreLoadFailed(AdCenter.PROMOREAD,adError.message);
            }

            @Override
            public void onRewardVideoAdLoad() {
                //获取本次waterfall加载中，加载失败的adn错误信息。
                if(!mLoadSuccess){
                    mLoadSuccess = true;
                    adPreLoadListener.onPreLoadSuccess(AdCenter.PROMOREAD);
                }
                mAdRewardManager.printLoadAdInfo(); //打印已经加载广告的信息
                mAdRewardManager.printLoadFailAdnInfo();
            }

            @Override
            public void onRewardVideoCached() {
                if(!mLoadSuccess){
                    mLoadSuccess = true;
                    mAdRewardManager.printLoadAdInfo(); //打印已经加载广告的信息
                    adPreLoadListener.onPreLoadSuccess(AdCenter.PROMOREAD);
                }
            }
        });
    }

    @Override
    public void displayAd(AdDisplayListener adDisplayListener) {
        mAdDisplayListener = adDisplayListener;
        if (mLoadSuccess && mAdRewardManager != null) {
            if (mAdRewardManager.getGMRewardAd() != null && mAdRewardManager.getGMRewardAd().isReady()) {
                //在获取到广告后展示,强烈建议在onRewardVideoCached回调后，展示广告，提升播放体验
                //该方法直接展示广告，如果展示失败了（如过期），会回调onVideoError()
                //展示广告，并传入广告展示的场景
                mAdRewardManager.getGMRewardAd().setRewardAdListener(mGMRewardedAdListener);
                mAdRewardManager.getGMRewardAd().setRewardPlayAgainListener(mGMRewardedPlayAgainListener);
                mAdRewardManager.getGMRewardAd().showRewardAd(mActivity);
                mAdRewardManager.printSHowAdInfo();//打印已经展示的广告信息
                mLoadSuccess = false;
            } else {
                adDisplayListener.onDisplayFailed(AdCenter.PROMOREAD,-1,"当前广告不满足显示的条件");
            }
        } else {
            adDisplayListener.onDisplayFailed(AdCenter.PROMOREAD,-1,"预加载广高失败");
        }
    }

    public void initListener() {
        mGMRewardedAdListener = new GMRewardedAdListener() {

            /**
             * 广告的展示回调 每个广告仅回调一次
             */
            public void onRewardedAdShow() {
                LogUtil.i("聚合-激励onRewardedAdShow！");
                mAdRewardManager.printLoadAdInfo(); //打印已经加载广告的信息
                mAdDisplayListener.onStartDisplay(AdCenter.PROMOREAD);
            }

            /**
             * show失败回调。如果show时发现无可用广告（比如广告过期或者isReady=false），会触发该回调。
             * 开发者应该结合自己的广告加载、展示流程，在该回调里进行重新加载。
             * @param adError showFail的具体原因
             */
            @Override
            public void onRewardedAdShowFail(AdError adError) {
                if (adError == null) {
                    return;
                }
                mAdDisplayListener.onDisplayFailed(AdCenter.PROMOREAD,-1,adError.message);
                LogUtil.i("聚合-onRewardedAdShowFail！" + adError.message);
                // 开发者应该结合自己的广告加载、展示流程，在该回调里进行重新加载
            }

            /**
             * 注意Admob的激励视频不会回调该方法
             */
            @Override
            public void onRewardClick() {
                LogUtil.i("聚合-onRewardClick！");
                mAdDisplayListener.onAdClick();

            }

            /**
             * 广告关闭的回调
             */
            public void onRewardedAdClosed() {
                LogUtil.i("聚合-onRewardedAdClosed！");
                mAdDisplayListener.onDisplaySuccess(AdCenter.PROMOREAD);

            }

            /**
             * 视频播放完毕的回调 Admob广告不存在该回调
             */
            public void onVideoComplete() {
                mAdDisplayListener.onVideoComplete(AdCenter.PROMOREAD);
                LogUtil.i("聚合-onVideoComplete！");

            }

            /**
             * 1、视频播放失败的回调
             */
            public void onVideoError() {
                LogUtil.i("聚合-onVideoError！");
            }

            /**
             * 激励视频播放完毕，验证是否有效发放奖励的回调
             */
            public void onRewardVerify(RewardItem rewardItem) {
                LogUtil.i("聚合-onRewardVerify！");
            }

            /**
             * - Mintegral GDT Admob广告不存在该回调
             */
            @Override
            public void onSkippedVideo() {
                LogUtil.i("聚合-onSkippedVideo！");
            }

        };

        //穿山甲再看一次监听
        mGMRewardedPlayAgainListener = new GMRewardedAdListener() {
            /**
             * 广告的展示回调 每个广告仅回调一次
             */
            public void onRewardedAdShow() {
                LogUtil.i("聚合-再看一次监听-onRewardedAdShow！");

            }

            /**
             * show失败回调。如果show时发现无可用广告（比如广告过期或者isReady=false），会触发该回调。
             * 开发者应该结合自己的广告加载、展示流程，在该回调里进行重新加载。
             * @param adError showFail的具体原因
             */
            @Override
            public void onRewardedAdShowFail(AdError adError) {
                if (adError == null) {
                    return;
                }
                LogUtil.i("聚合-再看一次监听-onRewardedAdShowFail！");
                // 开发者应该结合自己的广告加载、展示流程，在该回调里进行重新加载
            }

            /**
             * 注意Admob的激励视频不会回调该方法
             */
            @Override
            public void onRewardClick() {
                LogUtil.i("聚合-再看一次监听-onRewardClick！");

            }

            /**
             * 广告关闭的回调
             */
            public void onRewardedAdClosed() {
                LogUtil.i("聚合-再看一次监听-onRewardedAdClosed！");

            }

            /**
             * 视频播放完毕的回调 Admob广告不存在该回调
             */
            public void onVideoComplete() {
                LogUtil.i("聚合-再看一次监听-onVideoComplete！");

            }

            /**
             * 1、视频播放失败的回调
             */
            public void onVideoError() {
                LogUtil.i("聚合-再看一次监听-onVideoError！");
            }

            /**
             * 激励视频播放完毕，验证是否有效发放奖励的回调
             */
            public void onRewardVerify(RewardItem rewardItem) {
                LogUtil.i("聚合-再看一次监听-onRewardVerify！");
            }

            /**
             * - Mintegral GDT Admob广告不存在该回调
             */
            @Override
            public void onSkippedVideo() {
                LogUtil.i("聚合-onSkippedVideo！");
            }
        };
    }

    public void initAdLoader(AdInitListener initListener) {
        mAdRewardManager.loadAdWithCallback(mActivity,AdCenter.ProMoreJiLiId, GMAdConstant.VERTICAL,initListener);
    }

    public void onDestroy(){
        mAdRewardManager.destroy();
    }
}
