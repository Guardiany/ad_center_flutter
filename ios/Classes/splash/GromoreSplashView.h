//
//  GromoreSplashView.h
//  ad_center_flutter
//
//  Created by 爱互动 on 2022/3/16.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>

@interface GromoreSplashView : NSObject<FlutterPlatformView>

-(instancetype)initWithFrame:(CGRect)frame
                  viewIdentifier:(int64_t)viewId
                       arguments:(id _Nullable)args
                 binaryMessenger:(NSObject<FlutterBinaryMessenger>*)messenger;

@end
