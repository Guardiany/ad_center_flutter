//
//  GromoreSplashView.m
//  ad_center_flutter
//
//  Created by 爱互动 on 2022/3/16.
//

#import <Foundation/Foundation.h>
#import <ABUAdSDK/ABUAdSDK.h>
#import "GromoreSplashView.h"
#import "GromorePreLoadManager.h"

@interface GromoreSplashView () <ABUSplashAdDelegate>

@property (nonatomic, strong) ABUSplashAd *splashAd;

@property (nonatomic, retain) FlutterMethodChannel *_channel;

@end

@implementation GromoreSplashView {
    int64_t _viewId;
    UIWindow *container;
    FlutterResult rootResult;
    BOOL isPreLoad;
}

- (instancetype)init{
    return [super init];
}

- (instancetype)initWithFrame:(CGRect)frame viewIdentifier:(int64_t)viewId arguments:(id)args binaryMessenger:(NSObject<FlutterBinaryMessenger> *)messenger {
    NSString *methodName = [NSString stringWithFormat:@"com.ahd.GromoreSpalsh_%lld", viewId];
    self._channel = [FlutterMethodChannel methodChannelWithName:methodName binaryMessenger:messenger];
    container = [[UIWindow alloc] initWithFrame:frame];
    
    self.splashAd = [[GromorePreLoadManager instance] getSplashView];
    if (self.splashAd == nil) {
        isPreLoad = false;
        NSString *codeId = args[@"iosCodeId"];
        self.splashAd = [[ABUSplashAd alloc] initWithAdUnitID:codeId];
        self.splashAd.rootViewController = [[UIApplication sharedApplication] keyWindow].rootViewController;
        self.splashAd.delegate = self;
        self.splashAd.tolerateTimeout = 3.f;
        [self.splashAd loadAdData];
    } else {
        isPreLoad = true;
        self.splashAd.delegate = self;
        [self.splashAd showInWindow:[[UIApplication sharedApplication] keyWindow]];
    }
    
    _viewId = viewId;
    return self;
}

- (nonnull UIView *)view {
    return container;
}

- (void)removeView {
    [NSThread sleepForTimeInterval:0.5];
    [self.splashAd destoryAd];
    [container removeFromSuperview];
}

//广告加载成功
- (void)splashAdDidLoad:(ABUSplashAd *)splashAd {
    NSLog(@"%@", @"聚合开屏广告加载成功");
    if (!isPreLoad) {
        [self.splashAd showInWindow:[[UIApplication sharedApplication] keyWindow]];
    } else {
        NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"success", @"result", @"", @"message", nil];
        if (rootResult) {
            rootResult(resultDic);
        }
    }
}

//广告加载失败
- (void)splashAd:(ABUSplashAd *)splashAd didFailWithError:(NSError *)error {
    NSLog(@"聚合开屏广告加载失败:%@", error.description);
    if (isPreLoad) {
        if (rootResult) {
            NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"error", @"result", error.description, @"message", nil];
            rootResult(resultDic);
        }
    } else {
//        [_channel invokeMethod:@"error" arguments:error.description];
        [self removeView];
    }
}

//广告展示成功
- (void)splashAdWillVisible:(ABUSplashAd *)splashAd {
    NSLog(@"聚合开屏广告显示");
    [self._channel invokeMethod:@"show" arguments:@""];
}

//广告展示失败
- (void)splashAdDidShowFailed:(ABUSplashAd *)splashAd error:(NSError *)error {
    NSLog(@"聚合开屏广告展示失败");
    [self._channel invokeMethod:@"error" arguments:error.description];
    [self removeView];
}

//广告点击事件
- (void)splashAdDidClick:(ABUSplashAd *)splashAd {
    NSLog(@"聚合开屏广告点击");
    [self._channel invokeMethod:@"click" arguments:@""];
}

//跳转详情页或appstore
- (void)splashAdWillPresentFullScreenModal:(ABUSplashAd *)splashAd {
    NSLog(@"聚合开屏广告跳转详情页或appstore");
}

//详情页或appstore关闭(不包括App间跳转)
- (void)splashAdWillDissmissFullScreenModal:(ABUSplashAd *)splashAd {
    NSLog(@"聚合开屏广告详情页或appstore关闭");
    [self._channel invokeMethod:@"finish" arguments:@""];
    [self removeView];
}

//广告关闭
- (void)splashAdDidClose:(ABUSplashAd *)splashAd {
    NSLog(@"聚合开屏广告关闭");
    [self._channel invokeMethod:@"finish" arguments:@""];
    [self removeView];
}

//广告倒计时结束回调
- (void)splashAdCountdownToZero:(ABUSplashAd *)splashAd {
    NSLog(@"聚合开屏广告倒计时结束");
}

@end
