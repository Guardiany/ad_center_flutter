# 爱互动广告集成Flutter插件

## 简介
  ad_center_flutter集成了穿山甲、优量汇和快手广告的Flutter插件

## 官方文档
* 穿山甲
[Android](https://www.pangle.cn/union/media/union/download/detail?id=4&docId=5de8d9b3b1afac0012933105&osType=android)

[IOS](https://www.pangle.cn/union/media/union/download/detail?id=16&osType=ios)

* 优量汇
[Android](https://developers.adnet.qq.com/doc/android/access_doc)

[IOS](https://developers.adnet.qq.com/doc/ios/guide)

* 快手
[Android](https://static.yximgs.com/udata/pkg/KS-Android-KSAdSDk/doc/Android-AdSDK-3316.pdf)

[IOS](https://static.yximgs.com/udata/pkg/KSAdSDKTarGz/doc/ksadsdk-iOS-readme-ad-3.3.16.pdf)

## 集成步骤
#### 1、pubspec.yaml
```Dart
ad_center_flutter:
  git: https://github.com/Guardiany/ad_center_flutter.git
```

#### 2、IOS
Info.plist中添加：
```
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
	<true/>
</dict>
<key>NSUserTrackingUsageDescription</key>
<string>该标识符将用于向您投放个性化广告</string>
<key>SKAdNetworkItems</key>
<array>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>238da6jt44.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>x2jnk7ly8j.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>22mmun2rn5.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>r3y5dwb26t.skadnetwork</string>
    </dict>
</array>
<key>io.flutter.embedded_views_preview</key>
<true/>
```

#### 3、Android
必要权限请参考官方文档

AndroidManifest.xml里面添加：
```
<manifest
    xmlns:tools="http://schemas.android.com/tools">

<application
    tools:replace="android:label">

<provider
    android:name="com.bytedance.sdk.openadsdk.TTFileProvider"
    android:authorities="包名.TTFileProvider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
         android:name="android.support.FILE_PROVIDER_PATHS"
         android:resource="@xml/file_paths" />
</provider>

<provider
     android:name="com.bytedance.sdk.openadsdk.multipro.TTMultiProvider"
     android:authorities="包名.TTMultiProvider"
     android:exported="false" />

<provider
     android:name="com.qq.e.comm.GDTFileProvider"
     android:authorities="包名.gdt.fileprovider"
     android:exported="false"
     android:grantUriPermissions="true">
     <meta-data
          android:name="android.support.FILE_PROVIDER_PATHS"
          android:resource="@xml/gdt_file_path" />
</provider>
```
gdt_file_path.xml
```Xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-cache-path
        name="gdt_sdk_download_path1"
        path="com_qq_e_download" />
    <cache-path
        name="gdt_sdk_download_path2"
        path="com_qq_e_download" />
</paths>
```

file_paths.xml
```Xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <!--为了适配所有路径可以设置 path = "." -->
    <external-path name="tt_external_root" path="." />
    <external-path name="tt_external_download" path="Download" />
    <external-files-path name="tt_external_files_download" path="Download" />
    <files-path name="tt_internal_file_download" path="Download" />
    <cache-path name="tt_internal_cache_download" path="Download" />

    <external-path path="Android/data/com.ahd.ahd_fun_camera/" name="files_root" />
    <external-path path="." name="external_storage_root" />
    <paths>
        <external-path path="Android/data/packagename/" name="files_root" />
        <external-path path="." name="external_storage_root" />
    </paths>
</paths>
```

请解压提供的⼴告SDK，在压缩包中找到kssdk-ad--3.3.15-publishRelease-4533d8764.aar

找到您的App⼯程下的libs⽂件夹，将上⾯的aar拷⻉到该⽬录下

在app的build.gradle⽂件中添加如下依赖:
```
allprojects {
    repositories {
        //本地⽂件仓库依赖
        flatDir { dirs 'libs'}
    }
}

dependencies {
    // 快⼿SDK aar包，请将提供的aar包拷⻉到libs⽬录下，添加依赖。根据接⼊版本修改SDK包名
    implementation files('libs/kssdk-ad--3.3.15-publishRelease-4533d8764.aar')
    def version = "1.3.1"
    // supprot库依赖，SDK内部依赖如下support，请确保添加
    implementation "androidx.appcompat:appcompat:$version"
    implementation "androidx.recyclerview:recyclerview:1.2.0"
}
```

## 使用

#### 1、SDK初始化
```Dart
AdCenterFlutter.initAdCenter(
        appName: _appName,
        pangolinAndroidAppId: _pangolinAndroidAppId,
        pangolinRewardAndroidId: _pangolinRewardAndroidId,
        tencentAndroidAppId: _tencentAndroidAppId,
        tencentRewardAndroidId: _tencentRewardAndroidId,
        ksAndroidAppId: _ksAndroidAppId,
        ksRewardAndroidId: _ksRewardAndroidId,
        appIdAndroid: _appid,
        userId: _userId,
        channelAndroid: _channel,
      );
```

#### 2、播放激励广告
```Dart
AdCenterFlutter.displayAd(
      functionId: '1.签到',
      onSuccess: (bool isAdClick) {
        print('播放成功');
      },
      onError: (error) {

      },
    );
```

#### 3、开屏广告
```Dart
///预加载开屏广告
await AdCenterFlutter.preLoadPangolinSplash(
        androidCodeId: _pangolinSplashAndroidId,
        success: () {
          ///预加载成功
        },
        error: (e) {
          ///预加载失败
          print(e);
        },
      );

///显示开屏广告
@override
Widget build(BuildContext context) {
  return AdCenterFlutter.pangolinSplashView(
    androidCodeId: _pangolinSplashAndroidId,
    callBack: callBack,
  );
}
```

#### 4、Banner广告
```Dart
///预加载banner广告
AdCenterFlutter.preLoadBannerAd(
  androidCodeId: 'androidCodeId',
  iosCodeId: 'iosCodeId',
  width: width,
  height: height,
);

///显示Banner广告
@override
Widget build(BuildContext context) {
  return AdCenterFlutter.pangolinBannerView(
    androidCodeId: 'androidCodeId',
    iosCodeId: 'iosCodeId',
    callBack: callBack,
    width: width,
    height: height,
  );
}
```

#### 5、信息流广告
```Dart
///预加载信息流广告
AdCenterFlutter.preLoadPangolinNativeAd(
  androidCodeId: 'androidCode',
  iosCodeId: 'iosCode',
  positionX: positionX,
  positionY: positionY,
  width: width,
  height: height,
  adType: type,/// 0: 半屏   1: 全屏
);

///显示信息流广告
@override
Widget build(BuildContext context) {
  return AdCenterFlutter.pangolinNativeAdView(
    androidCodeId: 'androidCodeId',
    iosCodeId: 'iosCodeId',
    positionX: positionX,
    positionY: positionY,
    width: width,
    height: height,
    adType: type,/// 0: 半屏   1: 全屏
  );
}
```