package com.ahd.ad_center_flutter.Ads.AdListener;

/**
 * Author by GuangMingfei
 * Date on 2021/8/30.
 * Email guangmf@neusoft.com
 * Used for
 */
public interface AdInitListener {
    void onSuccess(int adFlag);
    void onFailed(int adFlag, String errorMessage);
}
