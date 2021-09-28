
import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

///广告播放成功
typedef AdCenterDisplaySuccess();
///广告播放失败
typedef AdCenterDisplayError(String error);

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
  /// [channelAndroid] 渠道号 默认渠道：NORMAL:CSJ
  ///
  /// [appIdAndroid] 内部appid 夕阳红：1  图你好玩：2  疯狂打卡：3
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
    String channelAndroid = 'NORMAL:CSJ',
    required String appIdAndroid,
    required String userId,
  }) async {
    if (Platform.isAndroid) {
      return _channel.invokeMethod('init', {
        'appName':appName,
        'pangolinAndroidAppId':pangolinAndroidAppId,
        'pangolinRewardAndroidId':pangolinRewardAndroidId,
        'tencentAndroidAppId':tencentAndroidAppId,
        'tencentRewardAndroidId':tencentRewardAndroidId,
        'ksAndroidAppId':ksAndroidAppId,
        'ksRewardAndroidId':ksRewardAndroidId,
        'channelAndroid':channelAndroid,
        'appIdAndroid':appIdAndroid,
        'userId':userId,
      });
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

  ///广告模块销毁
  static Future destory() async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod('destroy');
    }
  }
}
