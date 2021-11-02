//
//  PangolinBannerViewFactory.m
//  ad_center_flutter
//
//  Created by 爱互动 on 2021/11/1.
//

#import <Foundation/Foundation.h>
#import "PangolinBannerViewFactory.h"
#import "PangolinBannerView.h"

@implementation PangolinBannerViewFactory{
    NSObject<FlutterPluginRegistrar> *_messenger;
    BUNativeExpressSplashView *splashView;
}

- (instancetype)initWithMessenger:(NSObject<FlutterPluginRegistrar> *)messager {
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

- (nonnull NSObject<FlutterPlatformView> *)createWithFrame:(CGRect)frame viewIdentifier:(int64_t)viewId arguments:(id _Nullable)args {
    PangolinBannerView *bnView = [[PangolinBannerView alloc] initWithFrame:frame viewIdentifier:viewId arguments:args binaryMessenger:_messenger];
    return bnView;
}

@end
