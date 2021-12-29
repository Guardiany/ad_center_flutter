package com.ahd.ad_center_flutter.Ads.TTAd;

import static com.bytedance.sdk.openadsdk.TTAdLoadType.PRELOAD;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class TTNativeAdView implements PlatformView {

    private final Context context;
    private final MethodChannel methodChannel;
    private final FrameLayout mExpressContainer;
    private int topX = 0;
    private int lefY = 0;
    private TTNativeExpressAd expressAd;

    TTNativeAdView(Context context, BinaryMessenger messenger, int id, Map<String, Object> params) {
        this.context = context;
        methodChannel = new MethodChannel(messenger, "com.ahd.TTNativeView_" + id);
        mExpressContainer = new FrameLayout(context);
        mExpressContainer.setBackgroundColor(Color.WHITE);

        String codeId = (String) params.get("androidCodeId");
        Double width = (Double) params.get("width");
        Double height = (Double) params.get("height");
        Double positionX = (Double) params.get("positionX");
        Double positionY = (Double) params.get("positionY");

        float fWidth = 640;
        float fHeight = 70;
        if (width != null) {
            fWidth = width.floatValue();
        }
        if (height != null) {
            fHeight = height.floatValue();
        }
        if (positionX != null) {
            topX = positionX.intValue();
        }
        if (positionY != null) {
            lefY = positionY.intValue();
        }

        loadNativeAd(codeId, fWidth, fHeight);
    }

    private void loadNativeAd(String codeId, float adWidth, float adHeight) {
        TTAdNative mTTAdNative = TTAdSdk.getAdManager().createAdNative(context);
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
                methodChannel.invokeMethod("error", "信息流广告加载失败："+s);
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> list) {
                if (!list.isEmpty()) {
                    expressAd = list.get(0);
                    View view = expressAd.getExpressAdView();
                    if (view != null && mExpressContainer != null) {
                        mExpressContainer.removeAllViews();
                    }
                    expressAd.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
                        @Override
                        public void onAdClicked(View view, int i) {
                            methodChannel.invokeMethod("click", "信息流广告点击");
                        }

                        @Override
                        public void onAdShow(View view, int i) {
                            methodChannel.invokeMethod("show", "信息流广告显示");
                        }

                        @Override
                        public void onRenderFail(View view, String s, int i) {
                            methodChannel.invokeMethod("error", "信息流广告加载失败："+s);
                        }

                        @Override
                        public void onRenderSuccess(View view, float v, float v1) {
                            if (mExpressContainer != null) {
                                mExpressContainer.addView(view);
                            }
                            methodChannel.invokeMethod("render", "信息流广告渲染成功");
                        }
                    });
                    expressAd.render();
                }
            }
        });
    }

    @Override
    public View getView() {
        return mExpressContainer;
    }

    @Override
    public void dispose() {
        mExpressContainer.removeAllViews();
        if (expressAd != null) {
            expressAd.destroy();
        }
    }
}
