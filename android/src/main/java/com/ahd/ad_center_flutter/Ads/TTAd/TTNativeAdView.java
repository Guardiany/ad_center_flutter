package com.ahd.ad_center_flutter.Ads.TTAd;

import static com.bytedance.sdk.openadsdk.TTAdLoadType.PRELOAD;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.ahd.ad_center_flutter.Log.LogTools;
import com.bytedance.msdk.api.AdError;
import com.bytedance.msdk.api.format.TTMediaView;
import com.bytedance.msdk.api.v2.GMMediationAdSdk;
import com.bytedance.msdk.api.v2.GMSettingConfigCallback;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAd;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAdListener;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAdLoadCallback;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeExpressAdListener;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMUnifiedNativeAd;
import com.bytedance.msdk.api.v2.slot.GMAdOptionUtil;
import com.bytedance.msdk.api.v2.slot.GMAdSlotNative;
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
    com.bytedance.msdk.api.format.TTNativeAdView superView;
    private int topX = 0;
    private int lefY = 0;
    private TTNativeExpressAd expressAd;
    private GMNativeAd gmNativeAd;
    private int adType = 0;
    private boolean useGroMore;

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
        Integer adType = (Integer) params.get("adType");
        Boolean userGroMore = (Boolean) params.get("useGroMore");

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
        int type = 0;
        if (adType != null) {
            type = adType;
        }
        boolean ugm = false;
        if (userGroMore != null) {
            ugm = userGroMore;
        }
        useGroMore = ugm;

        loadNativeAd(codeId, fWidth, fHeight, type, ugm);
    }

    private void loadNativeAd(final String codeId, final float adWidth, final float adHeight, int type, final boolean useGroMore) {
        adType = type;
        if (useGroMore) {
            if (type == 0) {
                gmNativeAd = TTAdCenter.getInstance().getGmAdNativeHalfAd();
            } else {
                gmNativeAd = TTAdCenter.getInstance().getGmAdNativeFullAd();
            }
            if (gmNativeAd != null) {
                render(true, (int)adWidth, (int)adHeight);
            } else {
                if (GMMediationAdSdk.configLoadSuccess()) {
                    loadGmNativeAd(codeId, adWidth, adHeight);
                } else {
                    GMMediationAdSdk.registerConfigCallback(new GMSettingConfigCallback() {
                        @Override
                        public void configLoad() {
                            loadGmNativeAd(codeId, adWidth, adHeight);
                        }
                    });
                }
            }
            return;
        }
        if (type == 0) {
            expressAd = TTAdCenter.getInstance().getTtNativeExpressHalfAd();
        } else {
            expressAd = TTAdCenter.getInstance().getTtNativeExpressFullAd();
        }
        if (expressAd != null) {
            render(false, (int)adWidth, (int)adHeight);
        } else {
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
                        render(false, (int)adWidth, (int)adHeight);
                    }
                }
            });
        }
    }

    private void loadGmNativeAd(String codeId, final float adWidth, final float adHeight) {
        GMUnifiedNativeAd gmAdNative = new GMUnifiedNativeAd(context, codeId);
        GMAdSlotNative adSlotNative = new GMAdSlotNative.Builder()
                .setGMAdSlotBaiduOption(GMAdOptionUtil.getGMAdSlotBaiduOption().build())//百度相关的配置
//                    .setGMAdSlotGDTOption(adSlotNativeBuilder.build())//gdt相关的配置
                .setAdmobNativeAdOptions(GMAdOptionUtil.getAdmobNativeAdOptions())//admob相关配置
                .setAdStyleType(com.bytedance.msdk.api.AdSlot.TYPE_EXPRESS_AD)//表示请求的模板广告还是原生广告，com.bytedance.msdk.api.AdSlot.TYPE_EXPRESS_AD：模板广告 ； com.bytedance.msdk.api.AdSlot.TYPE_NATIVE_AD：原生广告
                // 备注
                // 1:如果是信息流自渲染广告，设置广告图片期望的图片宽高 ，不能为0
                // 2:如果是信息流模板广告，宽度设置为希望的宽度，高度设置为0(0为高度选择自适应参数)
                .setImageAdSize((int) adWidth, (int) adHeight)// 必选参数 单位dp ，详情见上面备注解释
                .setAdCount(1)//请求广告数量为1到3条
                .build();
        gmAdNative.loadAd(adSlotNative, new GMNativeAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull List<GMNativeAd> list) {
                if (!list.isEmpty()) {
                    gmNativeAd = list.get(0);
                    render(true, (int)adWidth, (int)adHeight);
                }
            }

            @Override
            public void onAdLoadedFail(@NonNull AdError adError) {
                LogTools.printLog(this.getClass(), "聚合信息流广告预加载失败："+adError.message);
            }
        });
    }

    private void render(boolean userGroMore, final int sWidth, final int sHeight) {
        if (userGroMore) {
            gmNativeAd.setNativeAdListener(new GMNativeExpressAdListener() {
                @Override
                public void onRenderFail(View view, String s, int i) {
                    methodChannel.invokeMethod("error", "聚合信息流广告加载失败："+s);
                }

                @Override
                public void onRenderSuccess(float v, float v1) {
                    superView = new com.bytedance.msdk.api.format.TTNativeAdView(context);
                    View view = gmNativeAd.getExpressView();
                    if (view != null) {
//                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(sWidth, sHeight);
                        superView.removeAllViews();
                        superView.addView(view);
                        mExpressContainer.addView(superView);
                    }
                    methodChannel.invokeMethod("render", "聚合信息流广告渲染成功");
                }

                @Override
                public void onAdClick() {
                    methodChannel.invokeMethod("click", "聚合信息流广告点击");
                }

                @Override
                public void onAdShow() {
                    methodChannel.invokeMethod("show", "聚合信息流广告显示");
                }
            });
            gmNativeAd.render();
            return;
        }
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

    @Override
    public View getView() {
        return mExpressContainer;
    }

    @Override
    public void dispose() {
        mExpressContainer.removeAllViews();
        if (expressAd != null) {
            expressAd.destroy();
            expressAd = null;
        }
//        if (useGroMore) {
//            if (adType == 0) {
//                TTAdCenter.getInstance().destroyGmAdNativeHalf();
//            } else {
//                TTAdCenter.getInstance().destroyGmAdNativeFull();
//            }
//        }
    }
}
