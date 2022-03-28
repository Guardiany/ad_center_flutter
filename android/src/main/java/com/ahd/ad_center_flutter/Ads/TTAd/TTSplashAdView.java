package com.ahd.ad_center_flutter.Ads.TTAd;

import static com.bytedance.sdk.openadsdk.TTAdLoadType.LOAD;
import static com.bytedance.sdk.openadsdk.TTAdLoadType.PRELOAD;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.bytedance.msdk.adapter.util.UIUtils;
import com.bytedance.msdk.api.AdError;
import com.bytedance.msdk.api.v2.GMAdConstant;
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAd;
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAdListener;
import com.bytedance.msdk.api.v2.ad.splash.GMSplashAdLoadCallback;
import com.bytedance.msdk.api.v2.slot.GMAdSlotSplash;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTSplashAd;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class TTSplashAdView implements PlatformView {

    private final Context context;
    private final MethodChannel methodChannel;
    private final FrameLayout mExpressContainer;
    private GMSplashAdListener gmSplashAdListener;

    TTSplashAdView(Context context, BinaryMessenger messenger, int id, Map<String, Object> params) {
        this.context = context;
        methodChannel = new MethodChannel(messenger, "com.ahd.TTSplashView_" + id);
        String codeId = (String) params.get("androidCodeId");
        Boolean userGroMore = (Boolean) params.get("userGroMore");
        boolean ugm = false;
        if (userGroMore != null) {
            ugm = userGroMore;
        }
        mExpressContainer = new FrameLayout(context);
        if (ugm) {
            gmSplashAdListener = new GMSplashAdListener() {
                @Override
                public void onAdClicked() {
                    methodChannel.invokeMethod("click","聚合开屏广告点击");
                }

                @Override
                public void onAdShow() {
                    methodChannel.invokeMethod("show","聚合开屏广告显示");
                }

                @Override
                public void onAdShowFail(@NonNull AdError adError) {
                    methodChannel.invokeMethod("error","聚合开屏广告显示失败："+adError.message);
                }

                @Override
                public void onAdSkip() {
                    methodChannel.invokeMethod("skip","聚合开屏广告跳过");
                }

                @Override
                public void onAdDismiss() {
                    methodChannel.invokeMethod("finish","聚合开屏广告结束");
                }
            };
            loadGmSplashAd(codeId);
        } else {
            loadSplashAd(codeId);
        }
    }
    
    private void loadGmSplashAd(String codeId) {
        GMSplashAd loadSplashAd = TTAdCenter.getInstance().getGmSplashAd();
        if (loadSplashAd == null) {
            loadSplashAd = new GMSplashAd(TTAdCenter.getInstance().getmContext(), codeId);
            //创建开屏广告请求参数AdSlot,具体参数含义参考文档
            GMAdSlotSplash adSlot = new GMAdSlotSplash.Builder()
                    .setImageAdSize(UIUtils.getScreenWidth(context), UIUtils.getScreenHeight(context)) // 单位px
                    .setTimeOut(3000)//设置超时
                    .setSplashButtonType(GMAdConstant.SPLASH_BUTTON_TYPE_FULL_SCREEN)
                    .setDownloadType(GMAdConstant.DOWNLOAD_TYPE_POPUP)
//                    .setForceLoadBottom(mForceLoadBottom) //强制加载兜底开屏广告，只能在GroMore提供的demo中使用，其他情况设置无效
                    .build();
            //请求广告，调用开屏广告异步请求接口，对请求回调的广告作渲染处理
            final GMSplashAd finalLoadSplashAd = loadSplashAd;
            loadSplashAd.loadAd(adSlot, new GMSplashAdLoadCallback() {
                @Override
                public void onSplashAdLoadFail(@NonNull AdError adError) {
                    methodChannel.invokeMethod("error", "聚合开屏广告加载失败："+adError.message);
                }

                @Override
                public void onSplashAdLoadSuccess() {
                    finalLoadSplashAd.showAd(mExpressContainer);
                    finalLoadSplashAd.setAdSplashListener(gmSplashAdListener);
                }

                @Override
                public void onAdLoadTimeout() {
                    methodChannel.invokeMethod("timeOut", "聚合开屏广告加载超时");
                }
            });
        } else {
            loadSplashAd.showAd(mExpressContainer);
            loadSplashAd.setAdSplashListener(gmSplashAdListener);
        }
    }

    private void loadSplashAd(String codeId) {
        TTSplashAd ttSplashAdPre = TTAdCenter.getInstance().getTtSplashAd();
        if (ttSplashAdPre == null) {
            AdSlot adSlot = new AdSlot.Builder()
                    .setCodeId(codeId)
                    .setImageAcceptedSize(1080, 1920)
                    .setAdLoadType(LOAD)//推荐使用，用于标注此次的广告请求用途为预加载（当做缓存）还是实时加载，方便后续为开发者优化相关策略
                    .build();
            TTAdManager ttAdManager = TTAdSdk.getAdManager();
            TTAdNative mTTAdNative = ttAdManager.createAdNative(context);
            mTTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
                @Override
                public void onError(int i, String s) {
                    methodChannel.invokeMethod("error", "开屏广告加载失败："+s);
                }

                @Override
                public void onTimeout() {
                    methodChannel.invokeMethod("timeOut", "开屏广告加载超时");
                }

                @Override
                public void onSplashAdLoad(TTSplashAd ttSplashAd) {
                    if (ttSplashAd == null) {
                        methodChannel.invokeMethod("error","拉取广告失败");
                        return;
                    }
                    View view = ttSplashAd.getSplashView();
                    if (view != null && mExpressContainer != null) {
                        mExpressContainer.removeAllViews();
                        mExpressContainer.addView(view);
                    }
                    ttSplashAd.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {
                        @Override
                        public void onAdClicked(View view, int i) {
                            methodChannel.invokeMethod("click","开屏广告点击");
                        }

                        @Override
                        public void onAdShow(View view, int i) {
                            methodChannel.invokeMethod("show","开屏广告显示");
                        }

                        @Override
                        public void onAdSkip() {
                            methodChannel.invokeMethod("skip","开屏广告跳过");
                        }

                        @Override
                        public void onAdTimeOver() {
                            methodChannel.invokeMethod("finish","开屏广告倒计时结束");
                        }
                    });
                }
            }, 3500);
        } else {
            View view = ttSplashAdPre.getSplashView();
            if (view != null && mExpressContainer != null) {
                mExpressContainer.removeAllViews();
                mExpressContainer.addView(view);
            }
            ttSplashAdPre.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {
                @Override
                public void onAdClicked(View view, int i) {
                    methodChannel.invokeMethod("click","开屏广告点击");
                }

                @Override
                public void onAdShow(View view, int i) {
                    methodChannel.invokeMethod("show","开屏广告显示");
                }

                @Override
                public void onAdSkip() {
                    methodChannel.invokeMethod("skip","开屏广告跳过");
                }

                @Override
                public void onAdTimeOver() {
                    methodChannel.invokeMethod("finish","开屏广告倒计时结束");
                }
            });
        }
    }

    @Override
    public View getView() {
        return mExpressContainer;
    }

    @Override
    public void dispose() {
        mExpressContainer.removeAllViews();
    }
}
