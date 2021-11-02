//
//  PangolinBannerViewFactory.h
//  ad_center_flutter
//
//  Created by 爱互动 on 2021/11/1.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
#import <BUAdSDK/BUAdSDK.h>

@interface PangolinBannerViewFactory : NSObject<FlutterPlatformViewFactory>

- (instancetype)initWithMessenger:(NSObject<FlutterPluginRegistrar>*)messager;

@end
