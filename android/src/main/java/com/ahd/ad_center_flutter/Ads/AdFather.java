package com.ahd.ad_center_flutter.Ads;

import android.app.Activity;

import com.ahd.ad_center_flutter.Ads.AdListener.AdDisplayListener;
import com.ahd.ad_center_flutter.Ads.AdListener.AdInitListener;
import com.ahd.ad_center_flutter.Ads.AdListener.AdPreLoadListener;

/**
 * Author by GuangMingfei
 * Date on 2021/8/23.
 * Email guangmf@neusoft.com
 * Used for
 */
public interface AdFather {

    void initSDK(Activity appContext, String appName, String adId, String encourageId, AdInitListener initListener);

    void preLoadAd(AdPreLoadListener adPreLoadListener);

    void displayAd(AdDisplayListener adDisplayListener);

}
