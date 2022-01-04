
import 'dart:async';
import 'dart:io';

import 'package:ad_center_flutter/pangolin_banner_view.dart';
import 'package:ad_center_flutter/pangolin_native_view.dart';
import 'package:ad_center_flutter/pangolin_splash_view.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

///广告sdk初始化成功
typedef AdCenterInitSuccess();
///广告sdk初始化失败
typedef AdCenterInitError(String error);
///广告播放成功
typedef AdCenterDisplaySuccess(bool isClick);
///广告播放失败
typedef AdCenterDisplayError(String error);
///开屏广告预加载成功
typedef SplashPreLoadSuccess();
///开屏广告预加载失败
typedef SplashPreLoadError(String error);

///开屏广告回调
class PangolinSplashAdCallBack {
  OnAdShow? onShow;
  OnAdFail? onFail;
  OnAdClick? onClick;
  OnSplashAdFinish? onFinish;
  OnSplashAdSkip? onSkip;
  OnSplashAdTimeOut? onTimeOut;

  ///
  /// [onShow] 开屏广告显示
  ///
  /// [onFail] 开屏广告错误
  ///
  /// [onClick] 开屏广告点击
  ///
  /// [onFinish] 开屏广告倒计时结束
  ///
  /// [onSkip] 开屏广告跳过
  ///
  /// [onTimeOut] 开屏广告超时
  ///
  PangolinSplashAdCallBack(
      {this.onShow,
        this.onFail,
        this.onClick,
        this.onFinish,
        this.onSkip,
        this.onTimeOut});
}

///Banner广告回调
class PangolinBannerAdCallBack {

  OnAdShow? onShow;
  OnAdFail? onFail;
  OnAdClick? onClick;

  PangolinBannerAdCallBack({
    this.onFail, this.onShow, this.onClick,
  });
}

///广告显示
typedef OnAdShow = void Function();
///广告错误
typedef OnAdFail = void Function(String error);
///广告点击
typedef OnAdClick = void Function();
///开屏广告倒计时结束
typedef OnSplashAdFinish = void Function();
///开屏广告跳过
typedef OnSplashAdSkip = void Function();
///开屏广告超时
typedef OnSplashAdTimeOut = void Function();

class AdCenterFlutter {
  static const MethodChannel _channel =
      const MethodChannel('ad_center_flutter');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  ///
  /// 初始化sdk
  ///
  /// [appName] app名称
  ///
  /// [pangolinAndroidAppId] 穿山甲appid
  ///
  /// [pangolinRewardAndroidId] 穿山甲激励视频ID
  ///
  /// [tencentAndroidAppId] 优量汇appID
  ///
  /// [tencentRewardAndroidId] 优量汇激励视频ID
  ///
  /// [ksAndroidAppId] 快手appID
  ///
  /// [ksRewardAndroidId] 快手激励视频ID
  ///
  /// [channel] 渠道号 默认渠道：NORMAL:CSJ
  ///
  /// [appId] 内部appid 夕阳红：1  图你好玩：2  疯狂打卡：3
  ///
  /// [userId] 用户ID
  ///
  static Future initAdCenter({
    required String appName,
    required String pangolinAndroidAppId,
    required String pangolinRewardAndroidId,
    required String tencentAndroidAppId,
    required String tencentRewardAndroidId,
    required String ksAndroidAppId,
    required String ksRewardAndroidId,
    required String pangolinIosAppId,
    required String pangolinRewardIosId,
    required String tencentIosAppId,
    required String tencentRewardIosId,
    required String ksIosAppId,
    required String ksRewardIosId,
    String channel = 'NORMAL:CSJ',
    required String appId,
    required String userId,
    AdCenterInitSuccess? onSuccess,
    AdCenterInitError? onError,
  }) async {
    final result = await _channel.invokeMethod('init', {
      'appName':appName,
      'pangolinAndroidAppId':pangolinAndroidAppId,
      'pangolinRewardAndroidId':pangolinRewardAndroidId,
      'tencentAndroidAppId':tencentAndroidAppId,
      'tencentRewardAndroidId':tencentRewardAndroidId,
      'ksAndroidAppId':ksAndroidAppId,
      'ksRewardAndroidId':ksRewardAndroidId,
      'pangolinIosAppId':pangolinIosAppId,
      'pangolinIosRewardId':pangolinRewardIosId,
      'tencentIosAppId':tencentIosAppId,
      'tencentIosRewardId':tencentRewardIosId,
      'ksIosAppId':ksIosAppId,
      'ksIosRewardId':ksRewardIosId,
      'channel':channel,
      'appId':appId,
      'userId':userId,
    });
    if (result['result'] == 'success') {
      if (onSuccess != null) {
        onSuccess();
      }
    }
    if (result['result'] == 'error') {
      if (onError != null) {
        onError(result['message']);
      }
    }
  }

  ///
  /// 播放广告
  ///
  /// [functionId] 功能模块ID
  ///
  /// [onSuccess] 播放广告成功回调
  ///
  /// [onError] 播放广告错误回调
  ///
  static Future displayAd({
    required String functionId,
    AdCenterDisplaySuccess? onSuccess,
    AdCenterDisplayError? onError,
  }) async {
    final result = await _channel.invokeMethod('display', {
      'functionId':functionId
    });
    if (result['result'] == 'success') {
      if (onSuccess != null) {
        bool isClick = result['adClick'] ?? false;
        onSuccess(isClick);
      }
    }
    if (result['result'] == 'error') {
      if (onError != null) {
        onError(result['message']);
      }
    }
  }

  ///开屏广告预加载
  static Future preLoadPangolinSplash({
    required String androidCodeId,
    required String iosCodeId,
    SplashPreLoadSuccess? success,
    SplashPreLoadError? error,
  }) async {
    final result = await _channel.invokeMethod('preLoadSplash', {
      'androidCodeId':androidCodeId,
      'iosCodeId':iosCodeId
    });
    if (result['result'] == 'success') {
      if (success != null) {
        success();
      }
    }
    if (result['result'] == 'error') {
      if (error != null) {
        error(result['message']);
      }
    }
  }

  ///获取穿山甲开屏广告
  static Widget pangolinSplashView({
    required String androidCodeId,
    required String iosCodeId,
    PangolinSplashAdCallBack? callBack,
  }) {
    return PangolinSplashView(
      codeId: androidCodeId,
      iosCodeId: iosCodeId,
      callBack: callBack,
    );
  }

  ///预加载banner广告
  static Future preLoadBannerAd({
    required String androidCodeId,
    required String iosCodeId,
    double? width,
    double? height,
  }) async {
    if (Platform.isAndroid) {
      return await _channel.invokeMethod('preLoadBanner', {
        'androidCodeId':androidCodeId,
        'iosCodeId':iosCodeId,
        'width': width,
        'height': height,
      });
    } else {
      return true;
    }
  }

  ///获取穿山甲banner广告
  static Widget pangolinBannerView({
    required String androidCodeId,
    required String iosCodeId,
    PangolinBannerAdCallBack? callBack,
    double? width,
    double? height,
  }) {
    return PangolinBannerView(
      codeId: androidCodeId,
      iosCodeId: iosCodeId,
      callBack: callBack,
      width: width,
      height: height,
    );
  }

  ///预加载信息流广告
  static Future preLoadPangolinNativeAd({
    required String androidCodeId,
    required String iosCodeId,
    double? width,
    double? height,
    double? positionX,
    double? positionY,
    /// 广告类型： 0: 半屏广告  1: 全屏广告
    int adType = 0,
  }) async {
    if (Platform.isAndroid) {
      return await _channel.invokeMethod('preLoadNative', {
        'androidCodeId':androidCodeId,
        'iosCodeId':iosCodeId,
        'width': width,
        'height': height,
        'positionX': positionX,
        'positionY': positionY,
        'adType': adType,
      });
    } else {
      return true;
    }
  }

  ///获取穿山甲信息流广告
  static Widget pangolinNativeAdView({
    required String androidCodeId,
    required String iosCodeId,
    PangolinBannerAdCallBack? callBack,
    double? width,
    double? height,
    double? positionX,
    double? positionY,
    /// 广告类型： 0: 半屏广告  1: 全屏广告
    int adType = 0,
  }) {
    return PangolinNativeView(
      androidCodeId: androidCodeId,
      iosCodeId: iosCodeId,
      width: width,
      height: height,
      positionX: positionX,
      positionY: positionY,
      adType: adType,
    );
  }

  ///广告模块销毁
  static Future destory() async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod('destroy');
    }
  }
}
