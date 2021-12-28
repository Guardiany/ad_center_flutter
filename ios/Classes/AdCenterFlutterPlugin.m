#import "AdCenterFlutterPlugin.h"
#import "AdCenter.h"
#import "PangolinSplashViewFactory.h"
#import "PangolinBannerViewFactory.h"
#import "PangolinSplashView.h"
#import "PangolinSplashViewFactory.h"
#import "PangolinNativeAdFactory.h"

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
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
      result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  }
  else if ([@"init" isEqualToString:call.method]) {
      [self initAdCenter:call result:result];
  }
  else if ([@"preLoadSplash" isEqualToString:call.method]) {
//      NSDictionary *dic = call.arguments;
//      NSString *iosCodeId = [dic valueForKey:@"iosCodeId"];
//      splashView = [[PangolinSplashView alloc] init];
//      [splashView preLoadSplash:iosCodeId result:result didLoad:^{
//
//      }];
      NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"success", @"result", @"", @"message", nil];
      result(resultDic);
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
    adCenter = [[AdCenter alloc] init];
    [adCenter initAppName:appName appId:appId pangolinAppId:pangolinAppId pangolinRewardId:pangolinRewardId tencentAppId:tencentAppId tencentRewardId:tencentRewardId ksAppId:ksAppId ksRewardId:ksRewardId channel:channel userId:userId result:result];
}

@end
