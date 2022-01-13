//
//  PangolinSplashView.h
//  ad_center_flutter
//
//  Created by 爱互动 on 2021/11/1.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
#import <BUAdSDK/BUAdSDK.h>

@interface PangolinSplashView : NSObject<FlutterPlatformView>

-(instancetype)initWithFrame:(CGRect)frame
                  viewIdentifier:(int64_t)viewId
                       arguments:(id _Nullable)args
                 binaryMessenger:(NSObject<FlutterBinaryMessenger>*)messenger;

@end
