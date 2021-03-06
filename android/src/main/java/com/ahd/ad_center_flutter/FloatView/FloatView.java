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
        //?????????View
        initFloatView(mActivity);

        PackageManager pm = mActivity.getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.SYSTEM_ALERT_WINDOW", getAppName(mActivity)));
        if (permission) {
            showFloatView();
        }else {
            //????????????
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //??????Activity???????????????
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getAppName(mActivity)));
                mActivity.startActivityForResult(intent, 101);
            }
        }

    }

    private static void initFloatView(Activity activity) {
        // ??????WindowManager
        floatView = View.inflate(activity,R.layout.ad_float_window,null);
        ttStateImage = floatView.findViewById(R.id.tt_state_image);
        ksStateImage = floatView.findViewById(R.id.ks_state_image);
        ylhStateImage = floatView.findViewById(R.id.ylh_state_image);
        wm = (WindowManager) activity.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        // ??????
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        // ??????flag
        params.flags|= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????EditText????????????
        params.flags|= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        // ??????????????????????????????????????????????????????
        params.format = PixelFormat.TRANSLUCENT;
        // FLAG_NOT_TOUCH_MODAL???????????????????????????????????????
        // ?????? FLAG_NOT_FOCUSABLE ???????????????????????????????????????????????????????????????????????????
        // ???????????????flag?????????home????????????????????????
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.LEFT|Gravity.CENTER;
        init = true;
    }

    private static void showFloatView() {
        wm.addView(floatView, params);

    }

    /**
     * ????????????????????????
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
