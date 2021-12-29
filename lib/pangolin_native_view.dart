
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class PangolinNativeView extends StatefulWidget {
  const PangolinNativeView({
    Key? key,
    required this.androidCodeId,
    required this.iosCodeId,
    required this.width,
    required this.height,
    required this.positionX,
    required this.positionY,
  }) : super(key: key);

  final String androidCodeId;
  final String iosCodeId;
  final double? width;
  final double? height;
  final double? positionX;
  final double? positionY;

  @override
  _PangolinNativeViewState createState() => _PangolinNativeViewState();
}

class _PangolinNativeViewState extends State<PangolinNativeView> {

  String _viewType = "com.ahd.TTNativeView";

  MethodChannel? _channel;

  @override
  Widget build(BuildContext context) {
    if (Platform.isAndroid) {
      return AndroidView(
        viewType: _viewType,
        creationParams: {
          'androidCodeId': widget.androidCodeId,
          'iosCodeId': widget.iosCodeId,
          'width': widget.width,
          'height': widget.height,
          'positionX': widget.positionX,
          'positionY': widget.positionY,
        },
        onPlatformViewCreated: _registerChannel,
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else {
      return UiKitView(
        viewType: _viewType,
        creationParams: {
          'androidCodeId': widget.androidCodeId,
          'iosCodeId': widget.iosCodeId,
          'width': widget.width,
          'height': widget.height,
          'positionX': widget.positionX,
          'positionY': widget.positionY,
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

  }

  @override
  void dispose() {
    if (_channel != null && Platform.isIOS) {
      _channel?.invokeMethod('dispose');
    }
    super.dispose();
  }
}
