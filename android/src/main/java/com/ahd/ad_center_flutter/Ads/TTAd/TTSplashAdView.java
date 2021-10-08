package com.ahd.ad_center_flutter.Ads.TTAd;

import static com.bytedance.sdk.openadsdk.TTAdLoadType.LOAD;
import static com.bytedance.sdk.openadsdk.TTAdLoadType.PRELOAD;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTSplashAd;

import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class TTSplashAdView implements PlatformView {

    private final Context context;
    private final MethodChannel methodChannel;
    private final FrameLayout mExpressContainer;

    TTSplashAdView(Context context, BinaryMessenger messenger, int id, Map<String, Object> params) {
        this.context = context;
        methodChannel = new MethodChannel(messenger, "com.ahd.TTSplashView_" + id);
        String codeId = (String) params.get("androidCodeId");
        mExpressContainer = new FrameLayout(context);
        loadSplashAd(codeId);
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

    }
}
