package com.ahd.ad_center_flutter.Ads.ProMore;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ahd.ad_center_flutter.AdCenter;
import com.bytedance.msdk.api.v2.GMAdConfig;
import com.bytedance.msdk.api.v2.GMAdConstant;
import com.bytedance.msdk.api.v2.GMConfigUserInfoForSegment;
import com.bytedance.msdk.api.v2.GMMediationAdSdk;
import com.bytedance.msdk.api.v2.GMPangleOption;
import com.bytedance.msdk.api.v2.GMPrivacyConfig;

import java.util.HashMap;
import java.util.Map;

public class GMAdManagerHolder {

    private static boolean sInit;

    public static void init(Context context) {
        doInit(context);
    }

    //step1:接入网盟广告sdk的初始化操作，详情见接入文档和穿山甲平台说明
    private static void doInit(@NonNull Context context) {
        if (!sInit) {
            GMMediationAdSdk.initialize(context,buildV2Config(context));
            sInit = true;
        }
    }

    public static GMAdConfig buildV2Config(Context context) {
        GMConfigUserInfoForSegment userInfo = new GMConfigUserInfoForSegment();

        Map<String, String> customInfos = new HashMap<>();
        userInfo.setCustomInfos(customInfos);

        Map<String, Object> initConfig = new HashMap<>();
        Log.e("!!!!!!!########", AdCenter.ProMoreId);
        return new GMAdConfig.Builder()
                .setAppId(AdCenter.ProMoreId)
                .setAppName(AdCenter.APPNAME)
                .setDebug(true)
                .setPublisherDid(getAndroidId(context))
                .setOpenAdnTest(false)
                .setConfigUserInfoForSegment(userInfo)
                .setPangleOption(new GMPangleOption.Builder()
                        .setIsPaid(false)
                        .setTitleBarTheme(GMAdConstant.TITLE_BAR_THEME_DARK)
                        .setAllowShowNotify(true)
                        .setAllowShowPageWhenScreenLock(true)
                        .setDirectDownloadNetworkType(GMAdConstant.NETWORK_STATE_WIFI, GMAdConstant.NETWORK_STATE_3G)
                        .setIsUseTextureView(true)
                        .setNeedClearTaskReset()
                        .setKeywords("")
                        .build())
                .setPrivacyConfig(new GMPrivacyConfig() {
                    // 重写相应的函数，设置需要设置的权限开关，不重写的将采用默认值
                    // 例如，重写isCanUsePhoneState函数返回true，表示允许使用ReadPhoneState权限。
                    @Override
                    public boolean isCanUsePhoneState() {
                        return true;
                    }

                    //当isCanUseWifiState=false时，可传入Mac地址信息，穿山甲sdk使用您传入的Mac地址信息
                    @Override
                    public String getMacAddress() {
                        return "";
                    }
                })
                .setLocalExtra(initConfig)
                .build();
    }

    public static String getAndroidId(Context context) {
        String androidId = null;
        try {
            androidId = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return androidId;
    }

}
