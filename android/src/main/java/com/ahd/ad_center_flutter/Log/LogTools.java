package com.ahd.ad_center_flutter.Log;

import android.util.Log;

import com.ahd.ad_center_flutter.AdCenter;

/**
 * Author by GuangMingfei
 * Date on 2021/8/24.
 * Email guangmf@neusoft.com
 * Used for
 */
public class LogTools {
    public static void printLog(Class clazz,String message){
        if(AdCenter.isDebug){
            Log.i(clazz.getName(),"<----->"+message+"<----->");
        }
    }
}
