//
//  JSToastDialogs.m
//  ad_center_flutter
//
//  Created by 爱互动 on 2021/10/28.
//

#import <Foundation/Foundation.h>
#import "JSToastDialogs.h"

#define SCREEN_HEIGHT [[UIScreen mainScreen] bounds].size.height
#define SCREEN_WIDTH [[UIScreen mainScreen] bounds].size.width

static int changeCount;
@implementation JSToastDialogs
// 实现声明单例方法 GCD
+ (instancetype)shareInstance {
    static JSToastDialogs *singleton = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        singleton = [[JSToastDialogs alloc] init];
    });
    return singleton;
}
// 初始化方法
- (instancetype)init {
    self = [super init];
    if (self) {
        dialogsLabel = [[DialogsLabel alloc]init];
        countTimer = [NSTimer scheduledTimerWithTimeInterval:1.0 target:self selector:@selector(changeTime) userInfo:nil repeats:YES];
        countTimer.fireDate = [NSDate distantFuture];//关闭定时器
    }
    return self;
}
/**
 *  弹出并显示JSToastDialogs
 *  @param message  显示的文本内容
 *  @param duration 显示时间
 */
- (void)makeToast:(NSString *)message duration:(CGFloat)duration{
    if ([message length] == 0) {
        return;
    }
    [dialogsLabel setMessageText:message];
    [[[UIApplication sharedApplication] keyWindow] addSubview:dialogsLabel];
    dialogsLabel.alpha = 0.8;
    countTimer.fireDate = [NSDate distantPast];//开启定时器
    changeCount = duration;
}
//定时器回调方法
- (void)changeTime {
    if(changeCount-- <= 0){
        countTimer.fireDate = [NSDate distantFuture]; //关闭定时器
        [UIView animateWithDuration:0.2f animations:^{
            self->dialogsLabel.alpha = 0;
        } completion:^(BOOL finished) {
            [self->dialogsLabel removeFromSuperview];
        }];
    }
}
@end
#pragma mark - DialogsLabel的方法
@implementation DialogsLabel
//DialogsLabel初始化，为label设置各种属性
- (instancetype)init {
    self = [super init];
    if (self) {
        self.layer.cornerRadius = 8;
        self.layer.masksToBounds = YES;
        self.backgroundColor = [UIColor blackColor];
        self.numberOfLines = 0;
        self.textAlignment = NSTextAlignmentCenter;
        self.textColor = [UIColor whiteColor];
        self.font = [UIFont systemFontOfSize:15];
    }
    return self;
}
//设置显示的文字
- (void)setMessageText:(NSString *)text {
    [self setText:text];
 CGRect rect = [self.text boundingRectWithSize:CGSizeMake(SCREEN_WIDTH - 20, MAXFLOAT) options:NSStringDrawingUsesLineFragmentOrigin|NSStringDrawingUsesFontLeading attributes:@{NSFontAttributeName:self.font} context:nil];
    CGFloat width = rect.size.width + 20;
    CGFloat height = rect.size.height + 20;
    CGFloat x = (SCREEN_WIDTH-width)/2;
    CGFloat y = SCREEN_HEIGHT-height - 59;
    self.frame = CGRectMake(x, y, width, height);
}
@end
