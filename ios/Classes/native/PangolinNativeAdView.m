//
//  PangolinNativeAdView.m
//  ad_center_flutter
//
//  Created by 爱互动 on 2021/12/27.
//

#import <Foundation/Foundation.h>
#import "PangolinNativeAdView.h"
#import "AdPreLoadManager.h"
#import <ABUAdSDK/ABUAdSDK.h>

@interface PangolinNativeAdView () <FlutterPlugin, BUNativeExpressAdViewDelegate, ABUNativeAdsManagerDelegate, ABUNativeAdViewDelegate>

@end

@implementation PangolinNativeAdView {
    int64_t _viewId;
    BUNativeExpressAdManager *adManager;
    ABUNativeAdsManager *gromoreManager;
    BUNativeExpressAdView *nativeAdView;
    ABUNativeAdView *gromoreNativeView;
    UIWindow *container;
    FlutterMethodChannel *_channel;
    int adX;
    int adY;
    int adWidth;
    int adHeight;
    bool isDispose;
}

- (instancetype)initWithFrame:(CGRect)frame viewIdentifier:(int64_t)viewId arguments:(id)args binaryMessenger:(NSObject<FlutterPluginRegistrar> *)messenger {
    isDispose = false;
    NSString *methodName = [NSString stringWithFormat:@"com.ahd.TTNativeView_%lld", viewId];
    _channel = [FlutterMethodChannel methodChannelWithName:methodName binaryMessenger:messenger.messenger];
    [messenger addMethodCallDelegate:self channel:_channel];
    
    container = [[UIWindow alloc] initWithFrame:frame];
//    container.rootViewController = [[UIApplication sharedApplication] keyWindow].rootViewController;
    
    NSString *typeStr = [NSString stringWithFormat:@"%@",args[@"adType"]];
    int type = [typeStr intValue];
    if (type == 0) {
        nativeAdView = [[AdPreLoadManager instance] getNativeAdHalf];
    } else {
        nativeAdView = [[AdPreLoadManager instance] getNativeAdFull];
    }
    
    if (nativeAdView != nil) {
        if (!isDispose) {
            [container.rootViewController.view addSubview:nativeAdView];
        }
    } else {
        [self initAd:args];
    }
    
    return self;
}

- (void)initAd:(NSDictionary*)args {
    NSDictionary *dic = args;
    NSString *codeId = dic[@"iosCodeId"];
    NSString *widthStr = [NSString stringWithFormat:@"%@",dic[@"width"]];
    NSString *heightStr = [NSString stringWithFormat:@"%@",dic[@"height"]];
    NSString *xStr = [NSString stringWithFormat:@"%@",dic[@"positionX"]];
    NSString *yStr = [NSString stringWithFormat:@"%@",dic[@"positionY"]];
    bool useGromore = [[dic objectForKey:@"useGroMore"] boolValue];
    
    adX = [xStr intValue];
    adY = [yStr intValue];
    adWidth = [widthStr intValue];
    adHeight = [heightStr intValue];
    
    NSValue *sizeValue = [NSValue valueWithCGSize:CGSizeMake(adWidth, adHeight)];
    CGSize size = [sizeValue CGSizeValue];
    
    if (useGromore) {
        ABUAdUnit *slot1 = [[ABUAdUnit alloc] init];
        ABUSize *imgSize1 = [[ABUSize alloc] init];
        imgSize1.width = adWidth;
        imgSize1.height = adHeight;
        slot1.ID = codeId;
        slot1.AdType = ABUAdSlotAdTypeFeed;
        slot1.position = ABUAdSlotPositionFeed;
        slot1.imgSize = imgSize1;
        slot1.isSupportDeepLink = YES;
        slot1.adSize = CGSizeMake(adWidth, adHeight);
        slot1.getExpressAdIfCan = YES;
        if (!self->gromoreManager) {
            self->gromoreManager = [[ABUNativeAdsManager alloc] initWithSlot:slot1];
        }
//        self->gromoreManager.rootViewController = container.rootViewController;
        self->gromoreManager.startMutedIfCan = NO;
        self->gromoreManager.delegate = self;
        if([ABUAdSDKManager configDidLoad]){
            [self->gromoreManager loadAdDataWithCount:1];
        } else {
            [ABUAdSDKManager addConfigLoadSuccessObserver:self withAction:^(id  _Nonnull observer) {
                [self->gromoreManager loadAdDataWithCount:1];
            }];
        }
    } else {
        BUAdSlot *slot1 = [[BUAdSlot alloc] init];
        slot1.ID = codeId;
        slot1.AdType = BUAdSlotAdTypeFeed;
        BUSize *imgSize = [BUSize sizeBy:BUProposalSize_Feed228_150];
        slot1.imgSize = imgSize;
        slot1.position = BUAdSlotPositionFeed;
        if (!self->adManager) {
            self->adManager = [[BUNativeExpressAdManager alloc] initWithSlot:slot1 adSize:CGSizeMake(size.width, size.height)];
        }
        self->adManager.adSize = CGSizeMake(size.width, size.height);
        self->adManager.delegate = self;
        [self->adManager loadAdDataWithCount:1];
    }
}

- (nonnull UIView *)view {
    return container;
}

//聚合广告加载成功
- (void)nativeAdsManagerSuccessToLoad:(ABUNativeAdsManager *)adsManager nativeAds:(NSArray<ABUNativeAdView *> *)nativeAdViewArray {
    if (nativeAdViewArray.count) {
        NSLog(@"聚合广告加载成功");
        [nativeAdViewArray enumerateObjectsUsingBlock:^(ABUNativeAdView * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            ABUNativeAdView *adView = (ABUNativeAdView *)obj;
//            adView.rootViewController = container.rootViewController;
            adView.frame = CGRectMake(adX, adY, adWidth, adHeight);
            adView.backgroundColor = [UIColor whiteColor];
            adView.delegate = self;
            [adView render];
        }];
    }
}

//聚合广告加载失败
- (void)nativeAdsManager:(ABUNativeAdsManager *)adsManager didFailWithError:(NSError *)error {
    NSLog(@"聚合广告加载失败: %@", error.description);
}

//聚合广告渲染成功
- (void)nativeAdExpressViewRenderSuccess:(ABUNativeAdView *)nativeExpressAdView {
    gromoreNativeView = nativeExpressAdView;
    if (!isDispose) {
        [[[UIApplication sharedApplication] keyWindow].rootViewController.view addSubview:nativeExpressAdView];
    }
}

//聚合广告渲染失败
- (void)nativeAdExpressViewRenderFail:(ABUNativeAdView *)nativeExpressAdView error:(NSError *)error {
    NSLog(@"聚合广告渲染失败: %@", error.description);
}

//聚合广告显示
- (void)nativeAdDidBecomeVisible:(ABUNativeAdView *)nativeAdView {
    NSLog(@"聚合广告显示");
}

//广告加载失败
- (void)nativeExpressAdFailToLoad:(BUNativeExpressAdManager *)nativeExpressAdManager error:(NSError *)error {
    NSLog(@"native广告加载失败: %@", error.description);
}

//渲染失败
- (void)nativeExpressAdViewRenderFail:(BUNativeExpressAdView *)nativeExpressAdView error:(NSError *)error {
    NSLog(@"native广告渲染失败: %@", error.description);
}

//广告视图加载成功
- (void)nativeExpressAdSuccessToLoad:(BUNativeExpressAdManager *)nativeExpressAdManager views:(NSArray<__kindof BUNativeExpressAdView *> *)views {
    if (views.count) {
        NSLog(@"native广告加载成功");
        [views enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            BUNativeExpressAdView *expressView = (BUNativeExpressAdView *)obj;
            expressView.rootViewController = container.rootViewController;
            expressView.frame = CGRectMake(adX, adY, adWidth, adHeight);
            expressView.backgroundColor = [UIColor whiteColor];
            [expressView render];
        }];
    }
}

//渲染成功
- (void)nativeExpressAdViewRenderSuccess:(BUNativeExpressAdView *)nativeExpressAdView {
    NSLog(@"native广告渲染成功");
    nativeAdView = nativeExpressAdView;
    if (!isDispose) {
        [[[UIApplication sharedApplication] keyWindow].rootViewController.view addSubview:nativeExpressAdView];
    }
}

- (void)handleMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result {
    if ([@"dispose" isEqualToString:call.method]) {
        isDispose = true;
        if (nativeAdView) {
            [nativeAdView removeFromSuperview];
            nativeAdView = nil;
        }
        if (gromoreNativeView) {
            [gromoreNativeView removeFromSuperview];
            gromoreNativeView = nil;
        }
//        if (self->gromoreManager != nil) {
//
//        }
    }
}

+ (void)registerWithRegistrar:(nonnull NSObject<FlutterPluginRegistrar> *)registrar {
    
}

@end
