package com.ahd.ad_center_flutter.Ads.TTAd;

import static com.bytedance.sdk.openadsdk.TTAdLoadType.LOAD;
import static com.bytedance.sdk.openadsdk.TTAdLoadType.PRELOAD;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;

import com.bytedance.msdk.api.v2.ad.banner.GMBannerAd;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class TTBannerView implements PlatformView {

    private final Context context;
    private final MethodChannel methodChannel;
    private final FrameLayout mExpressContainer;
    private TTNativeExpressAd bannerAd;
    private boolean useGroMore;

    TTBannerView(Context context, BinaryMessenger messenger, int id, Map<String, Object> params) {
        this.context = context;
        methodChannel = new MethodChannel(messenger, "com.ahd.TTBannerView_" + id);
        String codeId = (String) params.get("androidCodeId");
        Double width = (Double) params.get("width");
        Double height = (Double) params.get("height");
        Boolean userGroMore = (Boolean) params.get("useGroMore");
        float fWidth = 640;
        float fHeight = 70;
        if (width != null) {
            fWidth = width.floatValue();
        }
        if (height != null) {
            fHeight = height.floatValue();
        }
        boolean ugm = false;
        if (userGroMore != null) {
            ugm = userGroMore;
        }
        useGroMore = ugm;
        mExpressContainer = new FrameLayout(context);
        mExpressContainer.setBackgroundColor(Color.WHITE);
        loadBannerAd(codeId, fWidth, fHeight);
    }

    private void loadBannerAd(String codeId, float width, float height) {
//        if (useGroMore) {
//            mTTBannerViewAd = new GMBannerAd(context, codeId);
//            return;
//        }
        bannerAd = TTAdCenter.getInstance().getTtNativeBannerAd();
        if (bannerAd != null) {
            renderAd();
        } else {
            TTAdNative mTTAdNative = TTAdSdk.getAdManager().createAdNative(context);
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
                    methodChannel.invokeMethod("error", "Banner广告加载失败："+s);
                }

                @Override
                public void onNativeExpressAdLoad(List<TTNativeExpressAd> list) {
                    if (!list.isEmpty()) {
                        bannerAd = list.get(0);
                        renderAd();
                    }
                }
            });
        }
    }

    private void renderAd() {
        View view = bannerAd.getExpressAdView();
        if (view != null && mExpressContainer != null) {
            mExpressContainer.removeAllViews();
        }
        bannerAd.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            @Override
            public void onAdClicked(View view, int i) {
                methodChannel.invokeMethod("click", "Banner广告点击");
            }

            @Override
            public void onAdShow(View view, int i) {
                methodChannel.invokeMethod("show", "Banner广告显示");
            }

            @Override
            public void onRenderFail(View view, String s, int i) {
                methodChannel.invokeMethod("error", "Banner广告加载失败："+s);
            }

            @Override
            public void onRenderSuccess(View view, float v, float v1) {
                if (mExpressContainer != null) {
                    mExpressContainer.addView(view);
                }
                methodChannel.invokeMethod("render", "Banner广告渲染成功");
            }
        });
        bannerAd.render();
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
