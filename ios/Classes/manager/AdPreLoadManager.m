//
//  AdPreLoadManager.m
//  ad_center_flutter
//
//  Created by 爱互动 on 2022/1/4.
//

#import <Foundation/Foundation.h>
#import "AdPreLoadManager.h"

@interface AdPreLoadManager () <BUSplashAdDelegate, BUNativeExpressBannerViewDelegate, BUNativeExpressAdViewDelegate>

@end

@implementation AdPreLoadManager {
    BUSplashAdView *splashView;
    BUNativeExpressBannerView *bannerView;
    BUNativeExpressAdView *nativeAdViewHalf;
    BUNativeExpressAdView *nativeAdViewFull;
    int nativeAdType;
    int nativeAdX;
    int nativeAdY;
    int nativeAdWidth;
    int nativeAdHeight;
    LoadSuccess splashLoadSuccess;
    LoadError splashloadError;
}

+ (AdPreLoadManager *)instance {
    static AdPreLoadManager *single;
    if (single == nil) {
        single = [[AdPreLoadManager alloc] init];
    }
    return single;
}

- (void)preLoadSplash:(NSString *)codeId loadSuccess:(LoadSuccess)didLoad loadError:(LoadError)loadError {
    splashLoadSuccess = didLoad;
    splashloadError = loadError;
    CGRect frame = [UIScreen mainScreen].bounds;
    splashView = [[BUSplashAdView alloc] initWithSlotID:codeId frame:frame];
    splashView.tolerateTimeout = 10;
    splashView.delegate = self;
    splashView.rootViewController = [[UIApplication sharedApplication] keyWindow].rootViewController;
    [splashView loadAdData];
}

- (void)preLoadBanner:(NSDictionary*)args {
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
}

- (void)preLoadNative:(NSDictionary*)args {
    NSString *typeStr = [NSString stringWithFormat:@"%@",args[@"adType"]];
    nativeAdType = [typeStr intValue];
    
    NSString *codeId = args[@"iosCodeId"];
    NSString *widthStr = [NSString stringWithFormat:@"%@",args[@"width"]];
    NSString *heightStr = [NSString stringWithFormat:@"%@",args[@"height"]];
    NSString *xStr = [NSString stringWithFormat:@"%@",args[@"positionX"]];
    NSString *yStr = [NSString stringWithFormat:@"%@",args[@"positionY"]];
    
    nativeAdX = [xStr intValue];
    nativeAdY = [yStr intValue];
    nativeAdWidth = [widthStr intValue];
    nativeAdHeight = [heightStr intValue];
    
    NSValue *sizeValue = [NSValue valueWithCGSize:CGSizeMake(nativeAdWidth, nativeAdHeight)];
    CGSize size = [sizeValue CGSizeValue];
    BUAdSlot *slot1 = [[BUAdSlot alloc] init];
    slot1.ID = codeId;
    slot1.AdType = BUAdSlotAdTypeFeed;
    BUSize *imgSize = [BUSize sizeBy:BUProposalSize_Feed228_150];
    slot1.imgSize = imgSize;
    slot1.position = BUAdSlotPositionFeed;
    
    BUNativeExpressAdManager *adManager = [[BUNativeExpressAdManager alloc] initWithSlot:slot1 adSize:CGSizeMake(size.width, size.height)];
    adManager.adSize = CGSizeMake(size.width, size.height);
    adManager.delegate = self;
    [adManager loadAdDataWithCount:1];
}

- (BUSplashAdView *)getSplashView {
    return splashView;
}

- (BUNativeExpressBannerView*)getBannerView {
    return bannerView;
}

- (BUNativeExpressAdView*)getNativeAdHalf {
    return nativeAdViewHalf;
}

- (BUNativeExpressAdView*)getNativeAdFull {
    return nativeAdViewFull;
}

#pragma mark - 开屏广告

- (void)splashAdDidLoad:(BUSplashAdView *)splashAd {
    NSLog(@"%@", @"开屏广告加载成功");
    splashView = splashAd;
    if (splashLoadSuccess != nil) {
        splashLoadSuccess();
    }
}

- (void)splashAd:(BUSplashAdView *)splashAd didFailWithError:(NSError * _Nullable)error {
    NSLog(@"开屏广告加载失败:%@", error.description);
    if (splashloadError != nil) {
        splashloadError();
    }
}

#pragma mark - banner广告

- (void)nativeExpressBannerAdViewDidLoad:(BUNativeExpressBannerView *)bannerAdView {
    NSLog(@"Banner广告加载成功");
}

- (void)nativeExpressBannerAdView:(BUNativeExpressBannerView *)bannerAdView didLoadFailWithError:(NSError *_Nullable)error {
    NSLog(@"Banner广告错误：%@", error.description);
}

- (void)nativeExpressBannerAdViewRenderFail:(BUNativeExpressBannerView *)bannerAdView error:(NSError * __nullable)error {
    NSLog(@"Banner广告错误：%@", error.description);
}

- (void)nativeExpressBannerAdViewRenderSuccess:(BUNativeExpressBannerView *)bannerAdView {
    NSLog(@"Banner广告渲染成功");
    bannerView = bannerAdView;
}

#pragma mark - native广告

- (void)nativeExpressAdFailToLoad:(BUNativeExpressAdManager *)nativeExpressAdManager error:(NSError *)error {
    NSLog(@"native广告加载失败: %@", error.description);
}

- (void)nativeExpressAdViewRenderFail:(BUNativeExpressAdView *)nativeExpressAdView error:(NSError *)error {
    NSLog(@"native广告渲染失败: %@", error.description);
}

- (void)nativeExpressAdSuccessToLoad:(BUNativeExpressAdManager *)nativeExpressAdManager views:(NSArray<__kindof BUNativeExpressAdView *> *)views {
    if (views.count) {
        [views enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            NSLog(@"native广告加载成功");
            BUNativeExpressAdView *expressView = (BUNativeExpressAdView *)obj;
            expressView.rootViewController = [[UIApplication sharedApplication] keyWindow].rootViewController;
            expressView.frame = CGRectMake(nativeAdX, nativeAdY, nativeAdWidth, nativeAdHeight);
            expressView.backgroundColor = [UIColor whiteColor];
            [expressView render];
        }];
    }
}

- (void)nativeExpressAdViewRenderSuccess:(BUNativeExpressAdView *)nativeExpressAdView {
    NSLog(@"native广告渲染成功");
    if (nativeAdType == 0) {
        nativeAdViewHalf = nativeExpressAdView;
    } else {
        nativeAdViewFull = nativeExpressAdView;
    }
}

@end
