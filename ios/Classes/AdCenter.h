//
//  AdCenter.h
//  ad_center_flutter
//
//  Created by 爱互动 on 2021/10/28.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>

@interface AdCenter : NSObject

- (void)initAppName:(NSString*)app_name appId:(NSString*)app_id pangolinAppId:(NSString*)pangolin_appId pangolinRewardId:(NSString*)pangolin_rewardId tencentAppId:(NSString*)tencent_appId tencentRewardId:(NSString*)tencent_rewardId ksAppId:(NSString*)ks_appId ksRewardId:(NSString*)ks_rewardId channel:(NSString*)_channel userId:(NSString*)user_id result:(FlutterResult)result;

- (void)displayAd:(NSString*)source result:(FlutterResult)result;

- (void)setUserId:(NSString*)uId;

@end
