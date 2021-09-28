import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ad_center_flutter/ad_center_flutter.dart';

void main() {
  const MethodChannel channel = MethodChannel('ad_center_flutter');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await AdCenterFlutter.platformVersion, '42');
  });
}
