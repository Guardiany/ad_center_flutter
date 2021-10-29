//
//  HttpCenter.h
//  ad_center_flutter
//
//  Created by 爱互动 on 2021/10/28.
//

#import <Foundation/Foundation.h>
#import "JSToastDialogs.h"

@interface HttpCenter : NSObject

- (instancetype)initConfig:(NSString*)app_id userId:(NSString*)user_id channel:(NSString*)_channel toast:(JSToastDialogs*)toastInstance;

- (void)getNextAdFromWeb:(void (^)(NSDictionary *result))completionHandler;

- (void)uploadAdResult:(NSString*)source adFlag:(int)adFlag flag:(int)flag jumpFlag:(int)jumpFlag callback:(void (^)(void))callback;

@end
