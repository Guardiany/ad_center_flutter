package com.ahd.ad_center_flutter.FloatView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.ahd.ad_center_flutter.AdCenter;
import com.ahd.ad_center_flutter.R;

/**
 * Author by GuangMingfei
 * Date on 2021/8/30.
 * Email guangmf@neusoft.com
 * Used for
 */
public class FloatView {
    private static WindowManager wm;
    private static WindowManager.LayoutParams params;
    private static View floatView;
    private static ImageView ttStateImage,ksStateImage,ylhStateImage;
    private static final int INIT_STATE = 0,LOAD_STATE = 1,FAILED = 2,SUCCESS = 3;
    private static boolean init = false;

    public static void initAndShowFloatView(Activity activity){
        showFloatView();
    }

    public static void change(int adKey,int state){
        if(!init){
            return;
        }
        switch (adKey){
            case AdCenter.TTAD:
                changeSingle(ttStateImage,state);
                break;
            case AdCenter.KSAD:
                changeSingle(ksStateImage,state);
                break;
            case AdCenter.YLHAD:
                changeSingle(ylhStateImage,state);
                break;
        }
    }

    private static void changeSingle(ImageView adStateView,int state){
        switch (state){
            case INIT_STATE:
                adStateView.setVisibility(View.VISIBLE);
                adStateView.setImageResource(R.drawable.ad_init_load_color);
                adStateView.setEnabled(true);
                break;
            case LOAD_STATE:
                adStateView.setImageResource(R.drawable.ad_init_load_color);
                adStateView.setEnabled(false);
                break;
            case FAILED:
                adStateView.setImageResource(R.drawable.ad_success_failed_color);
                adStateView.setActivated(false);
                break;
            case SUCCESS:
                adStateView.setImageResource(R.drawable.ad_success_failed_color);
                adStateView.setActivated(true);
                break;
        }
    }

    public static void getShowFlowViewPermission(Activity mActivity) {
        //初始化View
        initFloatView(mActivity);

        PackageManager pm = mActivity.getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.SYSTEM_ALERT_WINDOW", getAppName(mActivity)));
        if (permission) {
            showFloatView();
        }else {
            //判断权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //启动Activity让用户授权
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getAppName(mActivity)));
                mActivity.startActivityForResult(intent, 101);
            }
        }

    }

    private static void initFloatView(Activity activity) {
        // 获取WindowManager
        floatView = View.inflate(activity,R.layout.ad_float_window,null);
        ttStateImage = floatView.findViewById(R.id.tt_state_image);
        ksStateImage = floatView.findViewById(R.id.ks_state_image);
        ylhStateImage = floatView.findViewById(R.id.ylh_state_image);
        wm = (WindowManager) activity.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        // 类型
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        // 设置flag
        params.flags|= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//没有这句的话，手机会出现一个奇怪的现象。除了点击弹窗，屏幕上的其他东西都点不了，用户看到的手机就像卡死了一样。如果弹窗没有正常显示，用户就感觉手机莫名其妙的卡，卡爆了。但是EditText不能输入
        params.flags|= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        // 不设置这个弹出框的透明遮罩显示为黑色
        params.format = PixelFormat.TRANSLUCENT;
        // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
        // 设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按
        // 不设置这个flag的话，home页的划屏会有问题
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.LEFT|Gravity.CENTER;
        init = true;
    }

    private static void showFloatView() {
        wm.addView(floatView, params);

    }

    /**
     * 获取应用程序名称
     */
    private static synchronized String getAppName(Activity mActivity) {
        try {
            PackageManager packageManager = mActivity.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    mActivity.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return mActivity.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void onDestroy(){
        if(init){
            if(floatView != null && wm != null){
                wm.removeViewImmediate(floatView);
            }
        }
    }
}
