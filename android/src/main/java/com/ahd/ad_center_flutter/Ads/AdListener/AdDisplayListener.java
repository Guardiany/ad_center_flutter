package com.ahd.ad_center_flutter.Ads.AdListener;

/**
 * Author by GuangMingfei
 * Date on 2021/8/23.
 * Email guangmf@neusoft.com
 * Used for
 */
public interface AdDisplayListener {
    //播放开始
    void onStartDisplay(int adFlag);
    //播放完成
    void onVideoComplete(int adFlag);
    //播放成功
    void onDisplaySuccess(int adFlag);
    //播放失败
    void onDisplayFailed(int adFlag, int error, String errorMessage);
    //广告点击
    void onAdClick();
}
