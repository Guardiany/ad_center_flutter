package com.ahd.ad_center_flutter.Ads.AdListener;

/**
 * Author by GuangMingfei
 * Date on 2021/8/23.
 * Email guangmf@neusoft.com
 * Used for
 */
public interface AdPreLoadListener {
    //播放成功
    public void onPreLoadSuccess(int adFlag);
    //播放失败
    public void onPreLoadFailed(int adFlag, String errorMessage);
}
