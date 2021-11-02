
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'ad_center_flutter.dart';

class PangolinSplashView extends StatefulWidget {
  const PangolinSplashView({
    Key? key,
    required this.codeId,
    required this.iosCodeId,
    this.callBack,
  }) : super(key: key);

  final String codeId;
  final String iosCodeId;
  final PangolinSplashAdCallBack? callBack;

  @override
  _PangolinSplashViewState createState() => _PangolinSplashViewState();
}

class _PangolinSplashViewState extends State<PangolinSplashView> {

  String _viewType = "com.ahd.TTSplashView";

  MethodChannel? _channel;

  @override
  Widget build(BuildContext context) {
    if (Platform.isAndroid) {
      return AndroidView(
        viewType: _viewType,
        creationParams: {
          'androidCodeId': widget.codeId,
        },
        onPlatformViewCreated: _registerChannel,
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else {
      return UiKitView(
        viewType: _viewType,
        creationParams: {
          'iosCodeId': widget.iosCodeId,
        },
        onPlatformViewCreated: _registerChannel,
        creationParamsCodec: const StandardMessageCodec(),
      );
    }
  }

  ///注册cannel
  void _registerChannel(int id) {
    _channel = MethodChannel("${_viewType}_$id");
    _channel?.setMethodCallHandler(_platformCallHandler);
  }

  ///监听原生view传值
  Future<dynamic> _platformCallHandler(MethodCall call) async {
    if (widget.callBack == null) {
      return;
    }
    switch (call.method) {
      ///显示广告
      case 'show':
        if (widget.callBack?.onShow != null) {
          widget.callBack?.onShow!();
        }
        break;
      ///广告加载失败
      case 'error':
        if (widget.callBack?.onFail != null) {
          widget.callBack?.onFail!(call.arguments);
        }
        break;
      ///开屏广告点击
      case 'click':
        if (widget.callBack?.onClick != null) {
          widget.callBack?.onClick!();
        }
        break;
      ///开屏广告跳过
      case 'skip':
        if (widget.callBack?.onSkip != null) {
          widget.callBack?.onSkip!();
        }
        break;
      ///开屏广告倒计时结束
      case 'finish':
        if (widget.callBack?.onFinish != null) {
          widget.callBack?.onFinish!();
        }
        break;
      ///开屏广告加载超时
      case 'timeOut':
        if (widget.callBack?.onTimeOut != null) {
          widget.callBack?.onTimeOut!();
        }
        break;
    }
  }
}