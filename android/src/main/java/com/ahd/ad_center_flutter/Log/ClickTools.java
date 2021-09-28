package com.ahd.ad_center_flutter.Log;

/**
 * Author by GuangMingfei
 * Date on 2021/8/24.
 * Email guangmf@neusoft.com
 * Used for
 */
public class ClickTools {
    //两次点击按钮之间的点击间隔不能少于1000毫秒
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;

    public static boolean isFastClick() {
        boolean flag = true;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = false;
        }
        lastClickTime = curClickTime;
        return flag;
    }
}
