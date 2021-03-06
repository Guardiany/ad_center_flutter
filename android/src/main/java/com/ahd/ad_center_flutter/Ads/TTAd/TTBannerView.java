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
                    .setCodeId(codeId) //?????????id
                    .setSupportDeepLink(true)
                    .setAdCount(1) //?????????????????????1???3???
                    .setExpressViewAcceptedSize(width,height) //??????????????????view???size,??????dp
                    .setAdLoadType(LOAD)//???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    .build();
            mTTAdNative.loadBannerExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
                @Override
                public void onError(int i, String s) {
                    methodChannel.invokeMethod("error", "Banner?????????????????????"+s);
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
                methodChannel.invokeMethod("click", "Banner????????????");
            }

            @Override
            public void onAdShow(View view, int i) {
                methodChannel.invokeMethod("show", "Banner????????????");
            }

            @Override
            public void onRenderFail(View view, String s, int i) {
                methodChannel.invokeMethod("error", "Banner?????????????????????"+s);
            }

            @Override
            public void onRenderSuccess(View view, float v, float v1) {
                if (mExpressContainer != null) {
                    mExpressContainer.addView(view);
                }
                methodChannel.invokeMethod("render", "Banner??????????????????");
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
