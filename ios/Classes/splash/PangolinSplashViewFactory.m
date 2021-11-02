//
//  PangolinSplashViewFactory.m
//  ad_center_flutter
//
//  Created by 爱互动 on 2021/11/1.
//

#import <Foundation/Foundation.h>
#import "PangolinSplashViewFactory.h"

@implementation PangolinSplashViewFactory{
    NSObject<FlutterBinaryMessenger> *_messenger;
    PangolinSplashView *splashView;
}

- (instancetype)initWithMessenger:(NSObject<FlutterBinaryMessenger> *)messager {
    self = [super init];
    if (self) {
        _messenger = messager;
    }
    return self;
}

//设置参数的编码方式
- (NSObject<FlutterMessageCodec>*)createArgsCodec{
    return [FlutterStandardMessageCodec sharedInstance];
}

- (void)setSplashView:(PangolinSplashView*)splash_view {
    splashView = splash_view;
}

- (nonnull NSObject<FlutterPlatformView> *)createWithFrame:(CGRect)frame viewIdentifier:(int64_t)viewId arguments:(id _Nullable)args {
    PangolinSplashView *spView = nil;
    if (splashView) {
        spView = [splashView initWithFrame:frame viewIdentifier:viewId arguments:args binaryMessenger:_messenger splashView:[splashView getSplashView]];
    } else {
        spView = [[PangolinSplashView alloc] initWithFrame:frame viewIdentifier:viewId arguments:args binaryMessenger:_messenger splashView:nil];
    }
    return spView;
}

@end
