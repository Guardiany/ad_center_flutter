//
//  PangolinNativeAdFactory.m
//  ad_center_flutter
//
//  Created by 爱互动 on 2021/12/27.
//

#import <Foundation/Foundation.h>
#import "PangolinNativeAdFactory.h"
#import "PangolinNativeAdView.h"

@implementation PangolinNativeAdFactory{
    NSObject<FlutterPluginRegistrar> *_messenger;
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
    PangolinNativeAdView *nativeAdView = [[PangolinNativeAdView alloc] initWithFrame:frame viewIdentifier:viewId arguments:args binaryMessenger:_messenger];
    return nativeAdView;
}

@end
