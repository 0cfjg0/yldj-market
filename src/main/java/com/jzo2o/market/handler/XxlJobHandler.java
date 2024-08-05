package com.jzo2o.market.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jzo2o.market.enums.ActivityStatusEnum;
import com.jzo2o.market.enums.CouponStatusEnum;
import com.jzo2o.market.model.domain.Activity;
import com.jzo2o.market.model.domain.Coupon;
import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponService;
import com.jzo2o.redis.sync.SyncManager;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class XxlJobHandler {

    @Resource
    private SyncManager syncManager;

    @Resource
    private IActivityService activityService;

    @Resource
    private ICouponService couponService;

    /**
     * 活动状态修改，
     * 1.活动进行中状态修改
     * 2.活动已失效状态修改
     * 1分钟一次
     * 对于待生效的活动：到达发放开始时间状态改为“进行中”。
     * 对于待生效及进行中的活动：到达发放结束时间状态改为“已失效”
     * 使用xxl-job定义定时任务，每分钟执行一次。
     */
    @XxlJob("updateActivityStatus")
    public void updateActivitySatus(){
        LambdaQueryWrapper<Activity> wrapper = Wrappers.<Activity>lambdaQuery()
                .eq(Activity::getStatus, ActivityStatusEnum.NO_DISTRIBUTE)
                .or()
                .eq(Activity::getStatus, ActivityStatusEnum.DISTRIBUTING);
        List<Activity> list = activityService.list(wrapper);
        list.forEach(
                item -> {
                    //对于待生效的活动：到达发放开始时间状态改为“进行中”。
                    if(item.getStatus().equals(ActivityStatusEnum.NO_DISTRIBUTE.getStatus()) && item.getDistributeStartTime().isAfter(LocalDateTime.now())){
                        item.setStatus(ActivityStatusEnum.DISTRIBUTING.getStatus());
                    //对于待生效及进行中的活动：到达发放结束时间状态改为“已失效”
                    }else if((item.getStatus().equals(ActivityStatusEnum.NO_DISTRIBUTE.getStatus()) || item.getStatus().equals(ActivityStatusEnum.DISTRIBUTING.getStatus())) && item.getDistributeEndTime().isAfter(LocalDateTime.now())){
                        item.setStatus(ActivityStatusEnum.LOSE_EFFICACY.getStatus());
                    }
                    activityService.updateById(item);
                }
        );
    }

    /**
     * 已领取优惠券自动过期任务
     * 用户领取的优惠券如果到达有效期仍然没有使用自动改为“已失效”
     * 使用xxl-job定义定时任务，每小时执行一次。
     */
    @XxlJob("processExpireCoupon")
    public void processExpireCoupon() {
        LambdaQueryWrapper<Coupon> wrapper = Wrappers.<Coupon>lambdaQuery()
                .gt(Coupon::getValidityTime, LocalDateTime.now());
        List<Coupon> list = couponService.list(wrapper);
        list.forEach(item -> {
            item.setStatus(CouponStatusEnum.INVALID.getStatus());
            couponService.updateById(item);
        });
    }


}
