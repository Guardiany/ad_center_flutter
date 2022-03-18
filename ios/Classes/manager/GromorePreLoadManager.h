//
//  GromorePreLoadManager.h
//  ad_center_flutter
//
//  Created by 爱互动 on 2022/3/16.
//

#import <Foundation/Foundation.h>
#import <ABUAdSDK/ABUAdSDK.h>

typedef void(^LoadSuccess)(void);
typedef void(^LoadError)(void);

@interface GromorePreLoadManager : NSObject

+ (GromorePreLoadManager *)instance;

- (void)preLoadSplash:(NSString*)appid codeId:(NSString*)codeId loadSuccess:(LoadSuccess)didLoad loadError:(LoadError)loadError;

- (ABUSplashAd*)getSplashView;

@end

