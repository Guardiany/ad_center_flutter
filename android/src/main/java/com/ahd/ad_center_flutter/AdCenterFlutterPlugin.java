package com.ahd.ad_center_flutter;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ahd.ad_center_flutter.Ads.TTAd.TTAdCenter;
import com.ahd.ad_center_flutter.Ads.TTAd.TTBannerViewFactory;
import com.ahd.ad_center_flutter.Ads.TTAd.TTSplashAdViewFactory;
import com.ahd.ad_center_flutter.OpenListener.PlayAdListener;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** AdCenterFlutterPlugin */
public class AdCenterFlutterPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Activity activity;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "ad_center_flutter");
    channel.setMethodCallHandler(this);
    flutterPluginBinding.getPlatformViewRegistry().registerViewFactory(
            "com.ahd.TTSplashView", new TTSplashAdViewFactory(flutterPluginBinding.getBinaryMessenger()));
    flutterPluginBinding.getPlatformViewRegistry().registerViewFactory(
            "com.ahd.TTBannerView", new TTBannerViewFactory(flutterPluginBinding.getBinaryMessenger()));
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "getPlatformVersion":
        result.success("Android " + android.os.Build.VERSION.RELEASE);
        break;
      case "init":
        initAdCenter(call, result);
        break;
      case "display":
        displayAd(call, result);
        break;
      case "preLoadSplash":
        preLoadSplash(call, result);
        break;
      case "destroy":
        AdCenter.getInstance().onDestroy();
        result.success(true);
        break;
      default:
        result.notImplemented();
        break;
    }
  }

  private void initAdCenter(MethodCall call, Result result) {
    String appName = call.argument("appName");
    String pangolinAndroidAppId = call.argument("pangolinAndroidAppId");
    String pangolinRewardAndroidId = call.argument("pangolinRewardAndroidId");
    String tencentAndroidAppId = call.argument("tencentAndroidAppId");
    String tencentRewardAndroidId = call.argument("tencentRewardAndroidId");
    String ksAndroidAppId = call.argument("ksAndroidAppId");
    String ksRewardAndroidId = call.argument("ksRewardAndroidId");
    //默认渠道号：NORMAL:CSJ
    String channelAndroid = call.argument("channel");
    String appIdAndroid = call.argument("appId");
    String userId = call.argument("userId");

    AdCenter.APPNAME = appName;
    AdCenter.TOUTIAOCATID = pangolinAndroidAppId;
    AdCenter.JILICATID = pangolinRewardAndroidId;
    AdCenter.APPCATID = tencentAndroidAppId;
    AdCenter.EDITPOSID = tencentRewardAndroidId;
    AdCenter.KUAISHOUCATID = ksAndroidAppId;
    AdCenter.KUAISHOUPOSID = ksRewardAndroidId;

    AdCenter.getInstance().initAd(activity, channelAndroid, appIdAndroid, userId, result);
  }

  private void preLoadSplash(MethodCall call, Result result) {
    String codeId = call.argument("androidCodeId");
    TTAdCenter.getInstance().preLoadSplashAd(codeId, result);
  }

  private boolean isResultUsed = false;

  private void displayAd(MethodCall call, final Result result) {
    isResultUsed = false;
    String functionId = call.argument("functionId");
    AdCenter.getInstance().displayAd(functionId, new PlayAdListener() {
      @Override
      public void onSuccess() {
        Log.e("onSuccess:", "广告播放成功");
        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            if (isResultUsed) {
              return;
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", "success");
            resultMap.put("message", "广告播放成功");
            result.success(resultMap);
            isResultUsed = true;
          }
        });
      }

      @Override
      public void onFailed(final int errorCode, final String message) {
        Log.e("errorCode:" + errorCode, message);
        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            if (isResultUsed) {
              return;
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", "error");
            resultMap.put("errorCode", errorCode);
            resultMap.put("message", message);
            result.success(resultMap);
            isResultUsed = true;
          }
        });
      }
    });
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
  }
}
