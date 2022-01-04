package com.ahd.ad_center_flutter.OpenListener;

/**
 * Author by GuangMingfei
 * Date on 2021/8/24.
 * Email guangmf@neusoft.com
 * Used for
 */
public interface PlayAdListener {
    void onSuccess(boolean isClick);
    //-1, "数据故障，请重启应用～～
    //-2, "请检查网络是否正常，恢复正常后重试～～
    //-3, "您播放的太快哦，慢慢来～～
    //-4, "今日广告次数已被抢光，明天早点来哦～～
    void onFailed(int errorCode, String message);
}
