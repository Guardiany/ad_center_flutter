//
//  PangolinSplashView.m
//  ad_center_flutter
//
//  Created by 爱互动 on 2021/11/1.
//

#import <Foundation/Foundation.h>
#import "PangolinSplashView.h"
#import "AdPreLoadManager.h"

@interface PangolinSplashView () <BUSplashAdDelegate>

@end

@implementation PangolinSplashView {
    int64_t _viewId;
    BUSplashAdView *splashView;
    UIView *container;
    FlutterMethodChannel *_channel;
    FlutterResult rootResult;
    BOOL isPreLoad;
}

- (instancetype)init{
    return [super init];
}

- (instancetype)initWithFrame:(CGRect)frame viewIdentifier:(int64_t)viewId arguments:(id)args binaryMessenger:(NSObject<FlutterBinaryMessenger> *)messenger {
    
    NSString *methodName = [NSString stringWithFormat:@"com.ahd.TTSplashView_%lld", viewId];
    _channel = [FlutterMethodChannel methodChannelWithName:methodName binaryMessenger:messenger];
    
    
    container = [[UIView alloc] initWithFrame:frame];
//    container.rootViewController = [[UIApplication sharedApplication] keyWindow].rootViewController;
    
    splashView = [[AdPreLoadManager instance] getSplashView];
    
    if (splashView == nil) {
        isPreLoad = false;
        NSDictionary *dic = args;
        NSString *codeId = dic[@"iosCodeId"];
        [self configSplash: codeId];
    } else {
        isPreLoad = true;
        splashView.delegate = self;
        [container addSubview:splashView];
    }

    _viewId = viewId;
    return self;
}

- (void)configSplash: (NSString*)codeId{
    CGRect frame = [UIScreen mainScreen].bounds;
    splashView = [[BUSplashAdView alloc] initWithSlotID:codeId frame:frame];
    splashView.tolerateTimeout = 10;
    splashView.delegate = self;
    splashView.rootViewController = [[UIApplication sharedApplication] keyWindow].rootViewController;
    [splashView loadAdData];
}

- (void)removeView {
    [NSThread sleepForTimeInterval:0.5];
    [splashView removeFromSuperview];
    [container removeFromSuperview];
//    splashView = nil;
}

- (nonnull UIView *)view {
    return container;
}

- (void)splashAdDidLoad:(BUSplashAdView *)splashAd {
    NSLog(@"%@", @"开屏广告加载成功");
    if (!isPreLoad) {
        [container addSubview:splashAd];
    } else {
        NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"success", @"result", @"", @"message", nil];
        if (rootResult) {
            rootResult(resultDic);
        }
    }
}

- (void)splashAd:(BUSplashAdView *)splashAd didFailWithError:(NSError * _Nullable)error {
    NSLog(@"开屏广告加载失败:%@", error.description);
    if (isPreLoad) {
        if (rootResult) {
            NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"error", @"result", error.description, @"message", nil];
            rootResult(resultDic);
        }
    } else {
        [_channel invokeMethod:@"error" arguments:error.description];
        [self removeView];
    }
}

- (void)splashAdWillVisible:(BUSplashAdView *)splashAd {
    NSLog(@"开屏广告显示");
    [_channel invokeMethod:@"show" arguments:@""];
}

- (void)splashAdDidClick:(BUSplashAdView *)splashAd {
    NSLog(@"开屏广告点击");
    [_channel invokeMethod:@"click" arguments:@""];
}

- (void)splashAdDidClickSkip:(BUSplashAdView *)splashAd {
    NSLog(@"开屏广告跳过");
    [_channel invokeMethod:@"skip" arguments:@""];
    [self removeView];
}

- (void)splashAdCountdownToZero:(BUSplashAdView *)splashAd {
    NSLog(@"开屏广告倒计时结束");
    [_channel invokeMethod:@"finish" arguments:@""];
    [self removeView];
}

- (void)splashAdDidClose:(BUSplashAdView *)splashAd {
    NSLog(@"开屏广告关闭");
}

- (void)splashAdDidCloseOtherController:(BUSplashAdView *)splashAd interactionType:(BUInteractionType)interactionType {
    NSLog(@"开屏广告关闭其他Controller");
    [_channel invokeMethod:@"finish" arguments:@""];
    [self removeView];
}

@end
