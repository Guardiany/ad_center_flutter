//
//  PangolinNativeAdView.h
//  ad_center_flutter
//
//  Created by 爱互动 on 2021/12/27.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
#import <BUAdSDK/BUAdSDK.h>

@interface PangolinNativeAdView : NSObject<FlutterPlatformView>

-(instancetype _Nullable )initWithFrame:(CGRect)frame
                  viewIdentifier:(int64_t)viewId
                       arguments:(id _Nullable)args
                        binaryMessenger:(NSObject<FlutterPluginRegistrar>*_Nullable)messenger;

@end
