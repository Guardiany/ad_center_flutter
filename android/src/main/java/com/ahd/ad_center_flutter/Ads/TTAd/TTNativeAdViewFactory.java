package com.ahd.ad_center_flutter.Ads.TTAd;

import android.content.Context;

import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class TTNativeAdViewFactory extends PlatformViewFactory {

    private final BinaryMessenger messenger;

    public TTNativeAdViewFactory(BinaryMessenger messenger) {
        super(StandardMessageCodec.INSTANCE);
        this.messenger = messenger;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PlatformView create(Context context, int viewId, Object args) {
        return new TTNativeAdView(context, messenger, viewId, (Map<String, Object>) args);
    }
}
