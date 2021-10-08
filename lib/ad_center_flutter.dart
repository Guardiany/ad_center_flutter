
import 'dart:async';
import 'dart:io';

import 'package:ad_center_flutter/pangolin_splash_view.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

///广告sdk初始化成功
typedef AdCenterInitSuccess();
///广告sdk初始化失败
typedef AdCenterInitError(String error);
///广告播放成功
typedef AdCenterDisplaySuccess();
///广告播放失败
typedef AdCenterDisplayError(String error);
///开屏广告预加载成功
typedef SplashPreLoadSuccess();
///开屏广告预加载失败
typedef SplashPreLoadError(String error);

///开屏广告回调
class PangolinSplashAdCallBack {
  OnSplashAdShow? onShow;
  OnSplashAdFail? onFail;
  OnSplashAdClick? onClick;
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
///开屏广告显示
typedef OnSplashAdShow = void Function();
///开屏广告错误
typedef OnSplashAdFail = void Function(String error);
///开屏广告点击
typedef OnSplashAdClick = void Function();
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
    String channel = 'NORMAL:CSJ',
    required String appId,
    required String userId,
    AdCenterInitSuccess? onSuccess,
    AdCenterInitError? onError,
  }) async {
    if (Platform.isAndroid) {
    final result = await _channel.invokeMethod('init', {
      'appName':appName,
      'pangolinAndroidAppId':pangolinAndroidAppId,
      'pangolinRewardAndroidId':pangolinRewardAndroidId,
      'tencentAndroidAppId':tencentAndroidAppId,
      'tencentRewardAndroidId':tencentRewardAndroidId,
      'ksAndroidAppId':ksAndroidAppId,
      'ksRewardAndroidId':ksRewardAndroidId,
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
    if (Platform.isAndroid) {
      final result = await _channel.invokeMethod('display', {
        'functionId':functionId
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
  }

  ///开屏广告预加载
  static Future preLoadPangolinSplash({
    required String androidCodeId,
    SplashPreLoadSuccess? success,
    SplashPreLoadError? error,
  }) async {
    if (Platform.isAndroid) {
      final result = await _channel.invokeMethod('preLoadSplash', {
        'androidCodeId':androidCodeId
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
  }

  ///获取穿山甲开屏广告
  static Widget pangolinSplashView({
    required String androidCodeId,
    PangolinSplashAdCallBack? callBack,
  }) {
    return PangolinSplashView(
      codeId: androidCodeId,
      callBack: callBack,
    );
  }

//  /直接以dialog显示穿山甲开屏广告
  // static void showPangolinSplashView({
  //   required BuildContext context,
  //   required String codeId,
  //   PangolinSplashAdCallBack? callBack,
  // }) {
  //   showDialog(context: context, barrierColor: Colors.black, builder: (con) {
  //     return pangolinSplashView(
  //       androidCodeId: codeId,
  //       callBack: PangolinSplashAdCallBack(
  //         onSkip: () {
  //           Navigator.pop(con);
  //           if (callBack != null) {
  //             if (callBack.onSkip != null) {
  //               callBack.onSkip!();
  //             }
  //           }
  //         },
  //         onFinish: () {
  //           Navigator.pop(con);
  //           if (callBack != null) {
  //             if (callBack.onFinish != null) {
  //               callBack.onFinish!();
  //             }
  //           }
  //         },
  //         onFail: (e) {
  //           Navigator.pop(con);
  //           if (callBack != null) {
  //             if (callBack.onFail != null) {
  //               callBack.onFail!(e);
  //             }
  //           }
  //         },
  //         onTimeOut: () {
  //           Navigator.pop(con);
  //           if (callBack != null) {
  //             if (callBack.onTimeOut != null) {
  //               callBack.onTimeOut!();
  //             }
  //           }
  //         },
  //         onClick: () {
  //           if (callBack != null) {
  //             if (callBack.onClick != null) {
  //               callBack.onClick!();
  //             }
  //           }
  //         },
  //         onShow: () {
  //           if (callBack != null) {
  //             if (callBack.onShow != null) {
  //               callBack.onShow!();
  //             }
  //           }
  //         },
  //       ),
  //     );
  //   });
  // }

  ///广告模块销毁
  static Future destory() async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod('destroy');
    }
  }
}
