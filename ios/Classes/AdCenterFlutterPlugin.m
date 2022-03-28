#import "AdCenterFlutterPlugin.h"
#import "AdCenter.h"
#import "PangolinSplashViewFactory.h"
#import "PangolinBannerViewFactory.h"
#import "PangolinSplashView.h"
#import "PangolinSplashViewFactory.h"
#import "PangolinNativeAdFactory.h"
#import "AdPreLoadManager.h"
#import "GromorePreLoadManager.h"
#import "GromoreSplashViewFactory.h"

@implementation AdCenterFlutterPlugin {
    AdCenter *adCenter;
}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"ad_center_flutter"
            binaryMessenger:[registrar messenger]];
  AdCenterFlutterPlugin* instance = [[AdCenterFlutterPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
    [registrar registerViewFactory: [[PangolinSplashViewFactory alloc] initWithMessenger:registrar.messenger] withId:@"com.ahd.TTSplashView"];
    [registrar registerViewFactory:[[PangolinBannerViewFactory alloc] initWithMessenger:registrar] withId:@"com.ahd.TTBannerView"];
    [registrar registerViewFactory:[[PangolinNativeAdFactory alloc] initWithMessenger:registrar] withId:@"com.ahd.TTNativeView"];
    [registrar registerViewFactory:[[GromoreSplashViewFactory alloc] initWithMessenger:registrar.messenger] withId:@"com.ahd.GromoreSpalsh"];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
      result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  }
  else if ([@"init" isEqualToString:call.method]) {
      [self initAdCenter:call result:result];
  }
  else if ([@"preLoadSplash" isEqualToString:call.method]) {
      NSDictionary *dic = call.arguments;
      NSString *iosCodeId = [dic valueForKey:@"iosCodeId"];
      NSString *appid = [dic valueForKey:@"appId"];
      bool useGromore = [[dic objectForKey:@"userGroMore"] boolValue];
      if (useGromore) {
          [[GromorePreLoadManager instance] preLoadSplash:appid codeId:iosCodeId loadSuccess:^{
              NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"success", @"result", @"", @"message", nil];
              result(resultDic);
          } loadError:^{
              NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"error", @"result", @"开屏广告预加载失败", @"message", nil];
              result(resultDic);
          }];
//          NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"success", @"result", @"", @"message", nil];
//          result(resultDic);
      } else {
          [[AdPreLoadManager instance] preLoadSplash:iosCodeId loadSuccess:^{
              NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"success", @"result", @"", @"message", nil];
              result(resultDic);
          } loadError:^{
              NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"error", @"result", @"开屏广告预加载失败", @"message", nil];
              result(resultDic);
          }];
      }
//      NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"success", @"result", @"", @"message", nil];
//      result(resultDic);
  }
  else if ([@"display" isEqualToString:call.method]) {
      if (adCenter) {
          NSDictionary *dic = call.arguments;
          NSString *functionId = [dic valueForKey:@"functionId"];
          [adCenter displayAd:functionId result:result];
      } else {
          result([NSNumber numberWithBool:false]);
      }
  }
  else if ([@"preLoadBanner" isEqualToString:call.method]) {
//      [[AdPreLoadManager instance] preLoadBanner:call.arguments];
      result([NSNumber numberWithBool:true]);
  }
  else if ([@"preLoadNative" isEqualToString:call.method]) {
//      [[AdPreLoadManager instance] preLoadNative:call.arguments];
      result([NSNumber numberWithBool:true]);
  }
  else if ([@"setUserId" isEqualToString:call.method]) {
      if (adCenter) {
          NSString *userId = [call.arguments valueForKey:@"userId"];
          [adCenter setUserId:userId];
          result([NSNumber numberWithBool:true]);
      } else {
          result([NSNumber numberWithBool:false]);
      }
  }
  else {
      result(FlutterMethodNotImplemented);
  }
}

- (void)initAdCenter:(FlutterMethodCall*)call result:(FlutterResult)result {
    NSDictionary *dic = call.arguments;
    NSString *appName = [dic valueForKey:@"appName"];
    NSString *appId = [dic valueForKey:@"appId"];
    NSString *pangolinAppId = [dic valueForKey:@"pangolinIosAppId"];
    NSString *pangolinRewardId = [dic valueForKey:@"pangolinIosRewardId"];
    NSString *tencentAppId = [dic valueForKey:@"tencentIosAppId"];
    NSString *tencentRewardId = [dic valueForKey:@"tencentIosRewardId"];
    NSString *ksAppId = [dic valueForKey:@"ksIosAppId"];
    NSString *ksRewardId = [dic valueForKey:@"ksIosRewardId"];
    NSString *channel = [dic valueForKey:@"channel"];
    NSString *userId = [dic valueForKey:@"userId"];
    bool useGroMore = [[dic objectForKey:@"userProMore"] boolValue];
    adCenter = [[AdCenter alloc] init];
    [adCenter initAppName:appName appId:appId pangolinAppId:pangolinAppId pangolinRewardId:pangolinRewardId tencentAppId:tencentAppId tencentRewardId:tencentRewardId ksAppId:ksAppId ksRewardId:ksRewardId channel:channel userId:userId useGroMore:useGroMore
        arguments:(NSDictionary*)dic result:result];
}

@end
