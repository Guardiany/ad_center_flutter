//
//  GromoreSplashViewFactory.m
//  ad_center_flutter
//
//  Created by 爱互动 on 2022/3/16.
//

#import <Foundation/Foundation.h>
#import "GromoreSplashViewFactory.h"
#import "GromoreSplashView.h"

@implementation GromoreSplashViewFactory {
    NSObject<FlutterBinaryMessenger> *_messenger;
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

- (nonnull NSObject<FlutterPlatformView> *)createWithFrame:(CGRect)frame viewIdentifier:(int64_t)viewId arguments:(id _Nullable)args {
    GromoreSplashView *view = [[GromoreSplashView alloc] initWithFrame:frame viewIdentifier:viewId arguments:args binaryMessenger:_messenger];
    return view;
}

@end
