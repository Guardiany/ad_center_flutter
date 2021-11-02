#import "AdCenterFlutterPlugin.h"
#import "AdCenter.h"
#import "PangolinSplashViewFactory.h"
#import "PangolinBannerViewFactory.h"
#import "PangolinSplashView.h"

PangolinSplashViewFactory *splashViewFactory;

@implementation AdCenterFlutterPlugin {
    AdCenter *adCenter;
}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"ad_center_flutter"
            binaryMessenger:[registrar messenger]];
  AdCenterFlutterPlugin* instance = [[AdCenterFlutterPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
    splashViewFactory = [[PangolinSplashViewFactory alloc] initWithMessenger:registrar.messenger];
    [registrar registerViewFactory: splashViewFactory withId:@"com.ahd.TTSplashView"];
    [registrar registerViewFactory:[[PangolinBannerViewFactory alloc] initWithMessenger:registrar] withId:@"com.ahd.TTBannerView"];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
      result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  }
  else if ([@"init" isEqualToString:call.method]) {
      [self initAdCenter:call result:result];
  }
  else if ([@"preLoadSplash" isEqualToString:call.method]) {
//      NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"success", @"result", @"", @"message", nil];
//      result(resultDic);
      NSDictionary *dic = call.arguments;
      NSString *iosCodeId = [dic valueForKey:@"iosCodeId"];
      PangolinSplashView *splashView = [[PangolinSplashView alloc] init];
      [splashView preLoadSplash:iosCodeId result:result didLoad:^{
          [splashViewFactory setSplashView:splashView];
      }];
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
