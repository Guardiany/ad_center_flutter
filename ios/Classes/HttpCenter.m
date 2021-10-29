//
//  HttpCenter.m
//  ad_center_flutter
//
//  Created by 爱互动 on 2021/10/28.
//

#import <Foundation/Foundation.h>
#import "HttpCenter.h"

@implementation HttpCenter {
    NSString *appId;
    NSString *userId;
    NSString *channel;
    JSToastDialogs *toast;
}

- (instancetype)initConfig:(NSString*)app_id userId:(NSString*)user_id channel:(NSString*)_channel toast:(JSToastDialogs*)toastInstance {
    appId = app_id;
    userId = user_id;
    channel = _channel;
    toast = toastInstance;
    return self;
}

- (void)getNextAdFromWeb: (void (^)(NSDictionary *result))completionHandler {
    NSString *urlStr = [NSString stringWithFormat:@"https://adv.ahd168.com/adv/video/videoNumber?appId=%@&userId=%@&jumpId=0", appId, userId];
    NSURL *url = [NSURL URLWithString:urlStr];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
    [request setHTTPMethod:@"GET"];
    
    NSURLSession *session = [NSURLSession sharedSession];
    NSURLSessionDataTask *task = [session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        NSString *resultStr = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        NSLog(@"服务器返回：%@", resultStr);
//        dispatch_async(dispatch_get_main_queue(), ^{
//            [[JSToastDialogs shareInstance] makeToast:resultStr duration:1.0];
//        });
        NSDictionary *resultDic = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:nil];
        completionHandler(resultDic);
    }];
    [task resume];
}

- (void)uploadAdResult:(NSString*)source adFlag:(int)adFlag flag:(int)flag jumpFlag:(int)jumpFlag callback:(void (^)(void))callback {
    NSString *urlStr = @"https://adv.ahd168.com/adv/video/insert";//http://192.168.101.5:9001/
    NSURL *url = [NSURL URLWithString:urlStr];
    
    NSString *kind = [NSString stringWithFormat:@"%d", flag];
    NSString *listingId = [NSString stringWithFormat:@"%d", adFlag];
    NSString *phoneVersion = [[UIDevice currentDevice] systemVersion];
    NSString *status = @"";
    if (jumpFlag == 1) {
        status = @"1";
    } else {
        status = @"0";
    }
    
    NSMutableDictionary *mulDic = [[NSMutableDictionary alloc] init];
    [mulDic setValue:appId forKey:@"appId"];
    [mulDic setValue:channel forKey:@"channel"];
    [mulDic setValue:userId forKey:@"userId"];
    [mulDic setValue:source forKey:@"source"];
    [mulDic setValue:kind forKey:@"kind"];
    [mulDic setValue:listingId forKey:@"listingId"];
    [mulDic setValue:phoneVersion forKey:@"version"];
    [mulDic setValue:status forKey:@"status"];
    
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:mulDic options:NSJSONWritingPrettyPrinted error:nil];
    
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
    [request setHTTPMethod:@"POST"];
    [request setValue:@"application/json; charset=UTF-8" forHTTPHeaderField:@"Content-Type"];
    [request setHTTPBody:jsonData];
    
    NSURLSession *session = [NSURLSession sharedSession];
    NSURLSessionDataTask *task = [session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        NSString *resultStr = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        NSLog(@"服务器返回：%@", resultStr);
        callback();
    }];
    [task resume];
}

@end
