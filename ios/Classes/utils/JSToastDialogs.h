//
//  JSToastDialogs.h
//  ad_center_flutter
//
//  Created by 爱互动 on 2021/10/28.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

//声明定义一个DialogsLabel对象
@interface DialogsLabel : UILabel

- (void)setMessageText:(NSString *)text;

@end

@interface JSToastDialogs : NSObject {
    DialogsLabel *dialogsLabel;
    NSTimer *countTimer;
}

//创建声明单例方法
+ (instancetype)shareInstance;

- (void)makeToast:(NSString *)message duration:(CGFloat)duration;

@end
