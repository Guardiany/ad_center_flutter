package com.ahd.ad_center_flutter.Ads.ProMore.Manager;

import android.app.Activity;
import android.util.Log;

import com.bytedance.msdk.adapter.util.Logger;
import com.bytedance.msdk.adapter.util.UIUtils;
import com.bytedance.msdk.api.GMAdEcpmInfo;
import com.bytedance.msdk.api.v2.GMAdConstant;
import com.bytedance.msdk.api.v2.GMNetworkRequestInfo;
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAd;
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAdListener;
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAdLoadCallback;
import com.bytedance.msdk.api.v2.slot.GMAdSlotSplash;

import java.util.List;

public class AdSplashManager {
    private static final String TAG = "ProMoreAdSplashManager ";

    private GMSplashAd mSplashAd;
    private Activity mActivity;
    //开屏广告加载超时时间,建议大于1000,这里为了冷启动第一次加载到广告并且展示,示例设置了2000ms
    private static final int AD_TIME_OUT = 4000;
    //强制使用兜底广告
    private boolean mForceLoadBottom = false;
    private GMSplashAdLoadCallback mGMSplashAdLoadCallback;
    private GMSplashAdListener mSplashAdListener;

    public AdSplashManager(Activity activity, boolean forceLoadBottom, GMSplashAdLoadCallback splashAdLoadCallback, GMSplashAdListener splashAdListener) {
        mActivity = activity;
        mForceLoadBottom = forceLoadBottom;
        mGMSplashAdLoadCallback = splashAdLoadCallback;
        mSplashAdListener = splashAdListener;
    }

    public GMSplashAd getSplashAd() {
        return mSplashAd;
    }

    /**
     * 加载开屏广告
     */
    public void loadSplashAd(String unitId) {
        /**
         * 注：每次加载开屏广告的时候需要新建一个TTSplashAd，否则可能会出现广告填充问题
         * （ 例如：mTTSplashAd = new TTSplashAd(this, mAdUnitId);）
         */
        mSplashAd = new GMSplashAd(mActivity, unitId);
        mSplashAd.setAdSplashListener(mSplashAdListener);

        //创建开屏广告请求参数AdSlot,具体参数含义参考文档
        GMAdSlotSplash adSlot = new GMAdSlotSplash.Builder()
                .setImageAdSize(UIUtils.getScreenWidth(mActivity), UIUtils.getScreenHeight(mActivity)) // 单位px
                .setTimeOut(AD_TIME_OUT)//设置超时
                .setSplashButtonType(GMAdConstant.SPLASH_BUTTON_TYPE_FULL_SCREEN)
                .setDownloadType(GMAdConstant.DOWNLOAD_TYPE_POPUP)
                .setForceLoadBottom(mForceLoadBottom) //强制加载兜底开屏广告，只能在GroMore提供的demo中使用，其他情况设置无效
                .build();

        //自定义兜底方案 选择使用
//        GMNetworkRequestInfo networkRequestInfo = SplashUtils.getGMNetworkRequestInfo();

        //请求广告，调用开屏广告异步请求接口，对请求回调的广告作渲染处理
        mSplashAd.loadAd(adSlot, mGMSplashAdLoadCallback);
    }


    /**
     * 打印其他信息
     */
    public void printInfo() {
        if (mSplashAd != null) {
            /**
             * 获取已经加载的clientBidding ，多阶底价广告的相关信息
             */
            List<GMAdEcpmInfo> gmAdEcpmInfos = mSplashAd.getMultiBiddingEcpm();
            if (gmAdEcpmInfos != null) {
                for (GMAdEcpmInfo info : gmAdEcpmInfos) {
                    Log.e(TAG, "多阶+client相关信息 AdNetworkPlatformId" + info.getAdNetworkPlatformId()
                            + "  AdNetworkRitId:" + info.getAdNetworkRitId()
                            + "  ReqBiddingType:" + info.getReqBiddingType()
                            + "  PreEcpm:" + info.getPreEcpm()
                            + "  LevelTag:" + info.getLevelTag()
                            + "  ErrorMsg:" + info.getErrorMsg()
                            + "  request_id:" + info.getRequestId());
                }
            }

            /**
             * 获取实时填充/缓存池中价格最优的代码位信息即相关价格信息，每次只有一个信息
             */
            GMAdEcpmInfo gmAdEcpmInfo = mSplashAd.getBestEcpm();
            if (gmAdEcpmInfo != null) {
                Log.e(TAG, "***实时填充/缓存池中价格最优的代码位信息*** AdNetworkPlatformId" + gmAdEcpmInfo.getAdNetworkPlatformId()
                        + "  AdNetworkRitId:" + gmAdEcpmInfo.getAdNetworkRitId()
                        + "  ReqBiddingType:" + gmAdEcpmInfo.getReqBiddingType()
                        + "  PreEcpm:" + gmAdEcpmInfo.getPreEcpm()
                        + "  LevelTag:" + gmAdEcpmInfo.getLevelTag()
                        + "  ErrorMsg:" + gmAdEcpmInfo.getErrorMsg()
                        + "  request_id:" + gmAdEcpmInfo.getRequestId());
            }

            /**
             * 获取获取当前缓存池的全部信息
             */
            List<GMAdEcpmInfo> gmCacheInfos = mSplashAd.getCacheList();
            if (gmCacheInfos != null) {
                for (GMAdEcpmInfo info : gmCacheInfos) {
                    Log.e(TAG, "***缓存池的全部信息*** AdNetworkPlatformId" + info.getAdNetworkPlatformId()
                            + "  AdNetworkRitId:" + info.getAdNetworkRitId()
                            + "  ReqBiddingType:" + info.getReqBiddingType()
                            + "  PreEcpm:" + info.getPreEcpm()
                            + "  LevelTag:" + info.getLevelTag()
                            + "  ErrorMsg:" + info.getErrorMsg()
                            + "  request_id:" + info.getRequestId());
                }
            }

            /**
             * 获取获展示广告的部信息
             */
            GMAdEcpmInfo showGMAdEcpmInfo = mSplashAd.getShowEcpm();

            if (showGMAdEcpmInfo != null) {
                Logger.e(TAG, "展示的广告信息： adNetworkPlatformName: " + showGMAdEcpmInfo.getAdNetworkPlatformName() + "   adNetworkRitId：" + showGMAdEcpmInfo.getAdNetworkRitId() + "   preEcpm: " + showGMAdEcpmInfo.getPreEcpm());
            }
            // 获取本次waterfall加载中，加载失败的adn错误信息。
            if (mSplashAd != null)
                Log.d(TAG, "ad load infos: " + mSplashAd.getAdLoadInfoList());
        }
    }

    public void destroy() {
        if (mSplashAd != null) {
            mSplashAd.destroy();
        }
        mActivity = null;
        mGMSplashAdLoadCallback = null;
        mSplashAdListener = null;
    }

}
