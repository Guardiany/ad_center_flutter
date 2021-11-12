
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'ad_center_flutter.dart';

class PangolinBannerView extends StatefulWidget {
  const PangolinBannerView({
    Key? key,
    required this.codeId,
    required this.iosCodeId,
    this.callBack,
    this.width,
    this.height,
  }) : super(key: key);

  final String codeId;
  final String iosCodeId;
  final PangolinBannerAdCallBack? callBack;
  final double? width;
  final double? height;

  @override
  _PangolinBannerViewState createState() => _PangolinBannerViewState();
}

class _PangolinBannerViewState extends State<PangolinBannerView> {

  String _viewType = "com.ahd.TTBannerView";

  MethodChannel? _channel;

  @override
  Widget build(BuildContext context) {
    if (Platform.isAndroid) {
      return AndroidView(
        viewType: _viewType,
        creationParams: {
          'androidCodeId': widget.codeId,
          'iosCodeId': widget.iosCodeId,
          'width': widget.width,
          'height': widget.height,
        },
        onPlatformViewCreated: _registerChannel,
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else {
      return UiKitView(
        viewType: _viewType,
        creationParams: {
          'androidCodeId': widget.codeId,
          'iosCodeId': widget.iosCodeId,
          'width': widget.width,
          'height': widget.height,
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
      ///广告点击
      case 'click':
        if (widget.callBack?.onClick != null) {
          widget.callBack?.onClick!();
        }
        break;
    }
  }

  @override
  void dispose() {
    if (_channel != null && Platform.isIOS) {
      _channel?.invokeMethod('dispose');
    }
    super.dispose();
  }
}
