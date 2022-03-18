//
//  GromorePreLoadManager.m
//  ad_center_flutter
//
//  Created by 爱互动 on 2022/3/16.
//

#import <Foundation/Foundation.h>
#import "GromorePreLoadManager.h"

@interface GromorePreLoadManager () <ABUSplashAdDelegate>

@end

@implementation GromorePreLoadManager {
    ABUSplashAd *splashAd;
    LoadSuccess splashLoadSuccess;
    LoadError splashloadError;
}

+ (GromorePreLoadManager *)instance {
    static GromorePreLoadManager *single;
    if (single == nil) {
        single = [[GromorePreLoadManager alloc] init];
    }
    return single;
}

- (void)preLoadSplash:(NSString*)appid codeId:(NSString*)codeId loadSuccess:(LoadSuccess)didLoad loadError:(LoadError)loadError {
    splashLoadSuccess = didLoad;
    splashloadError = loadError;
//    ABUSplashUserData *userData = [[ABUSplashUserData alloc] init];
//    userData.adnType = ABUAdnPangle;
//    userData.appID = appid;
//    userData.rit = codeId;
//    NSError *error = nil;
    splashAd = [[ABUSplashAd alloc] initWithAdUnitID:codeId];
    splashAd.rootViewController = [[UIApplication sharedApplication] keyWindow].rootViewController;
//    [splashAd setUserData:userData error:&error];
    splashAd.delegate = self;
    splashAd.tolerateTimeout = 3.f;
//    if (error) {
//        NSLog(@"%@",error.description);
//        return;
//    }
    [splashAd loadAdData];
}

- (ABUSplashAd*)getSplashView {
    return splashAd;
}

//广告加载成功
- (void)splashAdDidLoad:(ABUSplashAd *)splashAd {
    NSLog(@"%@", @"聚合开屏广告加载成功");
    self->splashAd = splashAd;
    if (splashLoadSuccess != nil) {
        splashLoadSuccess();
    }
}

//广告加载失败
- (void)splashAd:(ABUSplashAd *)splashAd didFailWithError:(NSError *)error {
    NSLog(@"聚合开屏广告加载失败:%@", error.description);
    if (splashloadError != nil) {
        splashloadError();
    }
}

//广告展示成功
- (void)splashAdWillVisible:(ABUSplashAd *)splashAd {
    
}

//广告展示失败
- (void)splashAdDidShowFailed:(ABUSplashAd *)splashAd error:(NSError *)error {
    
}

//广告点击事件
- (void)splashAdDidClick:(ABUSplashAd *)splashAd {
    
}

//跳转详情页或appstore
- (void)splashAdWillPresentFullScreenModal:(ABUSplashAd *)splashAd {
    
}

//详情页或appstore关闭(不包括App间跳转)
- (void)splashAdWillDissmissFullScreenModal:(ABUSplashAd *)splashAd {
    
}

//广告关闭
- (void)splashAdDidClose:(ABUSplashAd *)splashAd {
    
}

@end
