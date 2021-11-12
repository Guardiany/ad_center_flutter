//
//  PangolinBannerView.m
//  ad_center_flutter
//
//  Created by 爱互动 on 2021/11/1.
//

#import <Foundation/Foundation.h>
#import "PangolinBannerView.h"

@interface PangolinBannerView () <BUNativeExpressBannerViewDelegate, FlutterPlugin>

@end

@implementation PangolinBannerView {
    int64_t _viewId;
    BUNativeExpressBannerView *bannerView;
    UIWindow *container;
    FlutterMethodChannel *_channel;
}

- (instancetype)initWithFrame:(CGRect)frame viewIdentifier:(int64_t)viewId arguments:(id)args binaryMessenger:(NSObject<FlutterPluginRegistrar> *)messenger {
    
    NSString *methodName = [NSString stringWithFormat:@"com.ahd.TTBannerView_%lld", viewId];
    _channel = [FlutterMethodChannel methodChannelWithName:methodName binaryMessenger:messenger.messenger];
    [messenger addMethodCallDelegate:self channel:_channel];
    
    container = [[UIWindow alloc] initWithFrame:frame];
    container.rootViewController = [[UIApplication sharedApplication] keyWindow].rootViewController;
    
    NSDictionary *dic = args;
    NSString *codeId = dic[@"iosCodeId"];
    NSString *widthStr = [NSString stringWithFormat:@"%@",dic[@"width"]];
    NSString *heightStr = [NSString stringWithFormat:@"%@",dic[@"height"]];
    int width = [widthStr intValue];
    int height = [heightStr intValue];
    
    NSValue *sizeValue = [NSValue valueWithCGSize:CGSizeMake(width, height)];
    CGSize size = [sizeValue CGSizeValue];
    
    bannerView = [[BUNativeExpressBannerView alloc] initWithSlotID:codeId rootViewController:[[UIApplication sharedApplication] keyWindow].rootViewController adSize:size];
    int rootHeight = [[UIScreen mainScreen] bounds].size.height;
    bannerView.frame = CGRectMake(0, rootHeight - height, size.width, size.height);
    bannerView.delegate = self;
    [bannerView loadAdData];
    
    return self;
}

- (nonnull UIView *)view {
    return container;
}

- (void)handleMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result {
    if ([@"dispose" isEqualToString:call.method]) {
        [bannerView removeFromSuperview];
        bannerView = nil;
    }
}

+ (void)registerWithRegistrar:(nonnull NSObject<FlutterPluginRegistrar> *)registrar {
    
}

- (void)nativeExpressBannerAdViewDidLoad:(BUNativeExpressBannerView *)bannerAdView {
    NSLog(@"Banner广告加载成功");
//    [container.rootViewController.view addSubview:bannerAdView];
}

- (void)nativeExpressBannerAdView:(BUNativeExpressBannerView *)bannerAdView didLoadFailWithError:(NSError *_Nullable)error {
    NSLog(@"Banner广告错误：%@", error.description);
}

- (void)nativeExpressBannerAdViewRenderFail:(BUNativeExpressBannerView *)bannerAdView error:(NSError * __nullable)error {
    NSLog(@"Banner广告错误：%@", error.description);
}

- (void)nativeExpressBannerAdViewRenderSuccess:(BUNativeExpressBannerView *)bannerAdView {
    NSLog(@"Banner广告渲染成功");
    [container.rootViewController.view addSubview:bannerAdView];
}

@end
