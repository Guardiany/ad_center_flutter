//
//  AdCenter.m
//  ad_center_flutter
//
//  Created by 爱互动 on 2021/10/28.
//

#import <AppTrackingTransparency/AppTrackingTransparency.h>
#import <Foundation/Foundation.h>
#import "AdCenter.h"
#import <BUAdSDK/BUAdSDK.h>
#import <KSAdSDK/KSAdSDK.h>
#import "GDTSDKConfig.h"
#import "HttpCenter.h"
#import "JSToastDialogs.h"
#import "GDTRewardVideoAd.h"

@interface AdCenter () <BUNativeExpressRewardedVideoAdDelegate, KSRewardedVideoAdDelegate, GDTRewardedVideoAdDelegate>

@end

@implementation AdCenter {
    NSString *appName;
    NSString *pangolinAppId;
    NSString *pangolinRewardId;
    NSString *tencentAppId;
    NSString *tencentRewardId;
    NSString *ksAppId;
    NSString *ksRewardId;
    NSString *channel;
    NSString *appId;
    NSString *userId;
    int currentAd;
    int nextAd;
    BOOL todayPlayOver;
    BOOL preLoadCurrentSuccess;
    BOOL isAdClick;
    NSString *currentSource;
    long lastClickTime;
    int TTFailedTime;
    int KSFailedTime;
    int YLHFailedTime;
    FlutterResult flutterResult;
    HttpCenter *httpCenter;
    JSToastDialogs *toastIntance;
    
    BUNativeExpressRewardedVideoAd *pangolinRewardedAd;
    KSRewardedVideoAd *ksRewardAd;
    GDTRewardVideoAd *tencentRewardAd;
}

- (void)initAppName:(NSString*)app_name appId:(NSString*)app_id pangolinAppId:(NSString*)pangolin_appId pangolinRewardId:(NSString*)pangolin_rewardId tencentAppId:(NSString*)tencent_appId tencentRewardId:(NSString*)tencent_rewardId ksAppId:(NSString*)ks_appId ksRewardId:(NSString*)ks_rewardId channel:(NSString*)_channel userId:(NSString*)user_id result:(FlutterResult)result {
    appName = app_name;
    appId = app_id;
    pangolinAppId = pangolin_appId;
    pangolinRewardId = pangolin_rewardId;
    tencentAppId = tencent_appId;
    tencentRewardId = tencent_rewardId;
    ksAppId = ks_appId;
    ksRewardId = ks_rewardId;
    channel = _channel;
    userId = user_id;
    flutterResult = result;
    currentAd = 1;
    nextAd = 2;
    todayPlayOver = false;
    preLoadCurrentSuccess = false;
    currentSource = @"0";
    lastClickTime = 0;
    TTFailedTime = -1;
    KSFailedTime = -1;
    YLHFailedTime = -1;
    
    toastIntance = [JSToastDialogs shareInstance];
    
    NSLog(@"%@", @"开始初始化广告SDK");
    if (@available(iOS 14, *)) {
        [ATTrackingManager requestTrackingAuthorizationWithCompletionHandler:^(ATTrackingManagerAuthorizationStatus status) {
            NSLog(@"%@", @"ATT权限");
        }];
    }
    [self initKs];
    [self initTencent];
    [self initPangolin];
    [self getAdFromNet];
}

- (void)initPangolin {
    [BUAdSDKManager setAppID:pangolinAppId];
    [BUAdSDKManager startWithAsyncCompletionHandler:^(BOOL success, NSError *error) {
        if (success) {
            NSLog(@"穿山甲广告SDK初始化成功：%@", BUAdSDKManager.SDKVersion);
            NSDictionary *result = [[NSDictionary alloc] initWithObjectsAndKeys:@"success", @"result", @"", @"message", nil];
            self->flutterResult(result);
        } else {
            NSLog(@"穿山甲广告SDK初始化失败：%@", BUAdSDKManager.SDKVersion);
            NSDictionary *result = [[NSDictionary alloc] initWithObjectsAndKeys:@"error", @"result", error.description, @"message", nil];
            self->flutterResult(result);
        }
    }];
}

- (void)initKs {
    [KSAdSDKManager setAppId:ksAppId];
    NSLog(@"快手广告SDK初始化成功：%@", [KSAdSDKManager SDKVersion]);
}

- (void)initTencent {
    [GDTSDKConfig registerAppId:tencentAppId];
    NSLog(@"优量汇广告SDK初始化成功：%@", [GDTSDKConfig sdkVersion]);
}

- (void)getAdFromNet {
    if (!httpCenter) {
        httpCenter = [[HttpCenter alloc] initConfig:appId userId:userId channel:channel toast:toastIntance];
    }
    [httpCenter getNextAdFromWeb:^(NSDictionary *result) {
        NSString *code = [result valueForKey:@"errCode"];
        NSString *data = [result valueForKey:@"data"];
        NSString *errMsg = [result valueForKey:@"errMsg"];
        if ([@"200" isEqualToString:code]) {
            if (data.length == 2) {
                NSString *currentAdStr = [data substringToIndex:1];
                NSString *nextAdStr = [data substringFromIndex:1];
                self->currentAd = [currentAdStr intValue];
                self->nextAd = [nextAdStr intValue];
                self->todayPlayOver = false;
                [self preLoadAd];
            } else if (data.length == 1) {
                if ([@"0" isEqualToString:data]) {
                    self->todayPlayOver = true;
                    self->preLoadCurrentSuccess = false;
                }
            }
        } else {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[JSToastDialogs shareInstance] makeToast:errMsg duration:1.0];
            });
        }
    }];
}

- (void)preLoadAd {
    NSLog(@"下一个广告：%@, 备用广告：%@", [self getAdName:currentAd], [self getAdName:nextAd]);
    switch (currentAd) {
        case 1:
            {
                [self pangolinRewardVideoPreLoad];
            }
            break;
        case 2:
            {
                [self ksRewardVideoPreLoad];
            }
            break;
        case 3:
            {
                [self tencentRewardVideoPreLoad];
            }
            break;
        default:
            break;
    }
}

- (void)setUserId:(NSString*)uId {
    userId = uId;
    if (!httpCenter) {
        httpCenter = [[HttpCenter alloc] initConfig:appId userId:userId channel:channel toast:toastIntance];
    } else {
        [httpCenter setUserId:userId];
    }
    [self getAdFromNet];
}

- (void)displayAd:(NSString*)source result:(FlutterResult)result {
    isAdClick = false;
    currentSource = [NSString stringWithString:source];
    flutterResult = result;
    if ([self isFastClick]) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[JSToastDialogs shareInstance] makeToast:@"重复点击" duration:1.0];
        });
        NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"error", @"result", @"重复点击", @"message", nil];
        flutterResult(resultDic);
        return;
    }
    if (todayPlayOver) {
        NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"error", @"result", @"今日广告次数已被抢光，明天早点来哦～～", @"message", nil];
        flutterResult(resultDic);
        return;
    }
    if (!preLoadCurrentSuccess) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[JSToastDialogs shareInstance] makeToast:@"您播放的太快哦，慢慢来～～" duration:1.0];
        });
        [self updateShowInfo:0 adType:currentAd];
        NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"error", @"result", @"广告未预加载", @"message", nil];
        flutterResult(resultDic);
        return;
    }
    switch (currentAd) {
        case 1:
            [self displayPangolinAd:flutterResult];
            break;
        case 2:
            [self displayKsAd:flutterResult];
            break;
        case 3:
            [self displayTencentAd:flutterResult];
            break;
        default:
            break;
    }
}

- (void)startDisplay {
    if (nextAd == 0) {
        todayPlayOver = true;
        preLoadCurrentSuccess = true;
    } else {
        currentAd = nextAd;
        [self preLoadAd];
    }
}

- (void)displaySuccess: (int)adType {
    NSLog(@"播放%@广告成功", [self getAdName:adType]);
    [self updateShowInfo:1 adType:adType];
    NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"success", @"result", @"播放广告成功", @"message", [NSNumber numberWithBool:isAdClick], @"adClick", nil];
    flutterResult(resultDic);
}

- (void)displayError:(NSString*)msg adType:(int)adType {
    NSLog(@"播放%@广告失败", [self getAdName:currentAd]);
    NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"error", @"result", msg, @"message", nil];
    flutterResult(resultDic);
    [self updateShowInfo:0 adType:adType];
    currentAd = nextAd;
    [self preLoadAd];
}

- (void)updateShowInfo:(int)state adType:(int)adType {
    if (!httpCenter) {
        httpCenter = [[HttpCenter alloc] initConfig:appId userId:userId channel:channel toast:toastIntance];
    }
    if (state == 1) {
        switch (currentAd) {
            case 1:
                TTFailedTime = -1;
                break;
            case 2:
                KSFailedTime = -1;
                break;
            case 3:
                YLHFailedTime = -1;
                break;
            default:
                break;
        }
        [httpCenter uploadAdResult:currentSource adFlag:adType flag:state jumpFlag:0 callback:^{
            NSLog(@"广告上传服务器成功数据：%@", [self getAdName:self->currentAd]);
            [self getAdFromNet];
        }];
    } else {
        BOOL needUploadData = false;
        int tolerateTime = 60 * 3;
        switch (currentAd) {
            case 1:
            {
                if (TTFailedTime == -1) {
                    TTFailedTime = [[NSDate date] timeIntervalSince1970];
                } else {
                    if ([[NSDate date] timeIntervalSince1970] - TTFailedTime > tolerateTime) {
                        TTFailedTime = [[NSDate date] timeIntervalSince1970];
                        needUploadData = true;
                    }
                }
            }
                break;
            case 2:
            {
                if (KSFailedTime == -1) {
                    KSFailedTime = [[NSDate date] timeIntervalSince1970];
                } else {
                    if ([[NSDate date] timeIntervalSince1970] - KSFailedTime > tolerateTime) {
                        KSFailedTime = [[NSDate date] timeIntervalSince1970];
                        needUploadData = true;
                    }
                }
            }
                break;
            case 3:
            {
                if (YLHFailedTime == -1) {
                    YLHFailedTime = [[NSDate date] timeIntervalSince1970];
                } else {
                    if ([[NSDate date] timeIntervalSince1970] - YLHFailedTime > tolerateTime) {
                        YLHFailedTime = [[NSDate date] timeIntervalSince1970];
                        needUploadData = true;
                    }
                }
            }
                break;
            default:
                break;
        }
        if (needUploadData) {
            [httpCenter uploadAdResult:currentSource adFlag:adType flag:state jumpFlag:0 callback:^{
                NSLog(@"广告上传服务器失败数据：%@", [self getAdName:self->currentAd]);
                [self getAdFromNet];
            }];
        } else {
            [httpCenter uploadAdResult:currentSource adFlag:adType flag:state jumpFlag:1 callback:^{
                NSLog(@"广告上传服务器跳过数据：%@", [self getAdName:self->currentAd]);
                [self getAdFromNet];
            }];
        }
    }
}

- (NSString*)getAdName:(int)type {
    switch (type) {
        case 1:
            return @"头条";
        case 2:
            return @"快手";
        case 3:
            return @"优量汇";
        default:
            return @"未识别";
    }
}

- (BOOL)isFastClick {
    BOOL flag = true;
    NSDate* date = [NSDate date];
    long timeSp = [date timeIntervalSince1970];
    if ((timeSp - lastClickTime) >= 1) {
        flag = false;
    }
    lastClickTime = timeSp;
    return flag;
}

#pragma mark - 穿山甲广告

- (void)displayPangolinAd:(FlutterResult)result {
    if (pangolinRewardedAd) {
        UIViewController *rootViewController = [[UIApplication sharedApplication] keyWindow].rootViewController;
        [pangolinRewardedAd showAdFromRootViewController:rootViewController];
    } else {
        NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"error", @"result", @"未加载完成请重新点击", @"message", nil];
        result(resultDic);
    }
}

- (void)pangolinRewardVideoPreLoad {
    BURewardedVideoModel *model = [[BURewardedVideoModel alloc] init];
    model.userId = userId;
    pangolinRewardedAd = [[BUNativeExpressRewardedVideoAd alloc] initWithSlotID:pangolinRewardId rewardedVideoModel:model];
    pangolinRewardedAd.delegate = self;
//    self.pangolinRewardedAd.rewardPlayAgainInteractionDelegate = self;
    [pangolinRewardedAd loadAdData];
}

- (void)nativeExpressRewardedVideoAd:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd didFailWithError:(NSError *)error {
    NSLog(@"预加载头条广告失败：%@", error.description);
    self->preLoadCurrentSuccess = false;
}

- (void)nativeExpressRewardedVideoAdDidDownLoadVideo:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd {
    self->preLoadCurrentSuccess = true;
    NSLog(@"预加载头条广告成功");
}

- (void)nativeExpressRewardedVideoAdDidVisible:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd {
    NSLog(@"头条广告显示");
    //此处预加载会重置BUNativeExpressRewardedVideoAd的代理，导致点击、跳过、发放奖励和关闭的回调失效，所以应当把预加载放到close中去
//    [self startDisplay];
}

- (void)nativeExpressRewardedVideoAdServerRewardDidSucceed:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd verify:(BOOL)verify {
    NSLog(@"广告回调发放奖励");
}

- (void)nativeExpressRewardedVideoAdDidClose:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd {
    [self displaySuccess:1];
//    self.pangolinRewardedAd = nil;
    [self startDisplay];
}

- (void)nativeExpressRewardedVideoAdDidPlayFinish:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd didFailWithError:(NSError *_Nullable)error {
    if (error) {
        NSLog(@"头条广告播放错误: %@", error.description);
        [self displayError:error.description adType:1];
    }
}

- (void)nativeExpressRewardedVideoAdDidClick:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd {
    isAdClick = true;
    NSLog(@"头条广告点击");
}

- (void)nativeExpressRewardedVideoAdDidClickSkip:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd {
    NSLog(@"头条广告跳过");
}

#pragma mark - 快手广告

- (void)displayKsAd:(FlutterResult)result {
    if (ksRewardAd) {
        UIViewController *rootViewController = [[UIApplication sharedApplication] keyWindow].rootViewController;
        [ksRewardAd showAdFromRootViewController: rootViewController];
    } else {
        NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"error", @"result", @"未加载完成请重新点击", @"message", nil];
        result(resultDic);
    }
}

- (void)ksRewardVideoPreLoad {
    KSRewardedVideoModel *model = [KSRewardedVideoModel new];
    model.userId = userId;
    ksRewardAd = [[KSRewardedVideoAd alloc] initWithPosId:ksRewardId rewardedVideoModel:model];
    ksRewardAd.delegate = self;
    [ksRewardAd loadAdData];
}

- (void)rewardedVideoAd:(KSRewardedVideoAd *)rewardedVideoAd didFailWithError:(NSError *)error {
    NSLog(@"预加载快手广告失败：%@", error.description);
    self->preLoadCurrentSuccess = false;
}

- (void)rewardedVideoAdDidLoad:(KSRewardedVideoAd *)rewardedVideoAd {
    self->preLoadCurrentSuccess = true;
    NSLog(@"预加载快手广告成功");
}

- (void)rewardedVideoAdDidVisible:(KSRewardedVideoAd *)rewardedVideoAd {
    NSLog(@"快手广告显示");
//    [self startDisplay];
}

- (void)rewardedVideoAd:(KSRewardedVideoAd *)rewardedVideoAd hasReward:(BOOL)hasReward {
    NSLog(@"广告回调发放奖励");
}

- (void)rewardedVideoAdDidClose:(KSRewardedVideoAd *)rewardedVideoAd {
    [self displaySuccess:2];
//    ksRewardAd = nil;
    [self startDisplay];
}

- (void)rewardedVideoAdDidPlayFinish:(KSRewardedVideoAd *)rewardedVideoAd didFailWithError:(NSError *_Nullable)error {
    if (error) {
        [self displayError:error.description adType:2];
    }
}

- (void)rewardedVideoAdDidClick:(KSRewardedVideoAd *)rewardedVideoAd {
    isAdClick = true;
    NSLog(@"快手广告点击");
}

#pragma mark - 优量汇广告

- (void)displayTencentAd:(FlutterResult)result {
    if (tencentRewardAd) {
        UIViewController *rootViewController = [[UIApplication sharedApplication] keyWindow].rootViewController;
        [tencentRewardAd showAdFromRootViewController:rootViewController];
    } else {
        NSDictionary *resultDic = [[NSDictionary alloc] initWithObjectsAndKeys:@"error", @"result", @"未加载完成请重新点击", @"message", nil];
        result(resultDic);
    }
}

- (void)tencentRewardVideoPreLoad {
    tencentRewardAd = [[GDTRewardVideoAd alloc] initWithPlacementId:tencentRewardId];
    tencentRewardAd.delegate = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        [self->tencentRewardAd loadAd];
    });
}

- (void)gdt_rewardVideoAd:(GDTRewardVideoAd *)rewardedVideoAd didFailWithError:(NSError *)error {
    if (self->preLoadCurrentSuccess) {
        [self displayError:error.description adType:3];
    } else {
        NSLog(@"预加载优量汇广告失败：%@", error.description);
        self->preLoadCurrentSuccess = false;
    }
}

- (void)gdt_rewardVideoAdVideoDidLoad:(GDTRewardVideoAd *)rewardedVideoAd {
    self->preLoadCurrentSuccess = true;
    NSLog(@"预加载优量汇广告成功");
}

- (void)gdt_rewardVideoAdDidExposed:(GDTRewardVideoAd *)rewardedVideoAd {
    [self startDisplay];
}

- (void)gdt_rewardVideoAdDidRewardEffective:(GDTRewardVideoAd *)rewardedVideoAd info:(NSDictionary *)info {
    NSLog(@"广告回调发放奖励");
}

- (void)gdt_rewardVideoAdDidClose:(GDTRewardVideoAd *)rewardedVideoAd {
    [self displaySuccess:3];
}

- (void)gdt_rewardVideoAdDidClicked:(GDTRewardVideoAd *)rewardedVideoAd {
    isAdClick = true;
    NSLog(@"优量汇广告点击");
}

@end
