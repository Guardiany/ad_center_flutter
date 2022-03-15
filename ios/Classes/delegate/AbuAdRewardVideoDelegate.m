//
//  AbuAdRewardVideoDelegate.m
//  ad_center_flutter
//
//  Created by 爱互动 on 2022/3/14.
//

#import <Foundation/Foundation.h>
#import "AbuAdRewardVideoDelegate.h"

@interface AbuAdRewardVideoDelegate () <ABURewardedVideoAdDelegate>

@end

@implementation AbuAdRewardVideoDelegate

//广告加载成功
- (void)rewardedVideoAdDidLoad:(ABURewardedVideoAd *)rewardedVideoAd {
    
}

//广告加载失败
- (void)rewardedVideoAd:(ABURewardedVideoAd *)rewardedVideoAd didFailWithError:(NSError *)error {
    
}

//广告缓存(视频)成功
- (void)rewardedVideoAdDidDownLoadVideo:(ABURewardedVideoAd *)rewardedVideoAd {
    
}

//广告展示失败
- (void)rewardedVideoAdDidShowFailed:(ABURewardedVideoAd *)rewardedVideoAd error:(NSError *)error {
    
}

//广告展示成功
- (void)rewardedVideoAdDidVisible:(ABURewardedVideoAd *)rewardedVideoAd {
    
}

//广告点击事件
- (void)rewardedVideoAdDidClick:(ABURewardedVideoAd *)rewardedVideoAd {
    
}

//广告跳过事件
- (void)rewardedVideoAdDidSkip:(ABURewardedVideoAd *)rewardedVideoAd {
    
}

//广告关闭
- (void)rewardedVideoAdDidClose:(ABURewardedVideoAd *)rewardedVideoAd {
    
}

//奖励发放的标识
- (void)rewardedVideoAdServerRewardDidSucceed:(ABURewardedVideoAd *)rewardedVideoAd rewardInfo:(ABUAdapterRewardAdInfo *)rewardInfo verify:(BOOL)verify {
    
}

//视频播放结束(可能因为错误非正常结束)
- (void)rewardedVideoAdDidPlayFinish:(ABURewardedVideoAd *)rewardedVideoAd didFailWithError:(NSError *)error {
    
}

@end
