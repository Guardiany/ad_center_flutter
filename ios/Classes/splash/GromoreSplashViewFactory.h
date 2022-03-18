//
//  GromoreSplashViewFactory.h
//  ad_center_flutter
//
//  Created by 爱互动 on 2022/3/16.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
#import <ABUAdSDK/ABUAdSDK.h>

@interface GromoreSplashViewFactory : NSObject<FlutterPlatformViewFactory>

- (instancetype)initWithMessenger:(NSObject<FlutterBinaryMessenger>*)messager;

@end
