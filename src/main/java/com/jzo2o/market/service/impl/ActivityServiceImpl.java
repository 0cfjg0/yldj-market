package com.jzo2o.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.common.utils.ObjectUtils;
import com.jzo2o.market.enums.CouponStatusEnum;
import com.jzo2o.market.mapper.ActivityMapper;
import com.jzo2o.market.mapper.CouponMapper;
import com.jzo2o.market.model.domain.Activity;
import com.jzo2o.market.model.domain.Coupon;
import com.jzo2o.market.model.dto.request.ActivityQueryForPageReqDTO;
import com.jzo2o.market.model.dto.request.ActivitySaveReqDTO;
import com.jzo2o.market.model.dto.response.ActivityInfoResDTO;
import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponService;
import com.jzo2o.market.service.ICouponWriteOffService;
import com.jzo2o.mysql.utils.PageHelperUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.jzo2o.market.enums.ActivityStatusEnum.NO_DISTRIBUTE;
import static com.jzo2o.market.enums.ActivityStatusEnum.VOIDED;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements IActivityService {
    private static final int MILLION = 1000000;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ICouponService couponService;

    @Resource
    private ICouponWriteOffService couponWriteOffService;

    @Resource
    ActivityMapper activityMapper;

    @Resource
    CouponMapper couponMapper;


    @Override
    public PageResult<ActivityInfoResDTO> getActivityPages(ActivityQueryForPageReqDTO reqDTO) {
        PageResult<Activity> res = PageHelperUtils.selectPage(reqDTO, () -> {
            return list(Wrappers.<Activity>lambdaQuery()
                    .eq(reqDTO.getStatus() != null, Activity::getStatus, reqDTO.getStatus())
                    .eq(reqDTO.getType() != null, Activity::getType, reqDTO.getType())
                    .eq(reqDTO.getId() != null,Activity::getId,reqDTO.getId()));
        });
        List<ActivityInfoResDTO> resList = res.getList().stream().map(
                item -> BeanUtils.toBean(item, ActivityInfoResDTO.class)
        ).collect(Collectors.toList());
        return new PageResult<ActivityInfoResDTO>(res.getPages(),res.getTotal(),resList);
    }

    @Override
    @Transactional
    public void saveActivity(ActivitySaveReqDTO reqDTO) {
        //参数校验
        if(ObjectUtils.isEmpty(reqDTO)){
            throw new ForbiddenOperationException("参数异常");
        }
        reqDTO.check();
        //保存优惠券
        Activity activity = BeanUtils.toBean(reqDTO, Activity.class);
        activity.setStatus(NO_DISTRIBUTE.getStatus());
        boolean flag = this.saveOrUpdate(activity);
        if(!flag){
            throw new ForbiddenOperationException("保存失败");
        }
    }

    @Override
    public ActivityInfoResDTO getDetailActivity(Long id) {
        if(ObjectUtils.isNull(id)){
            throw new ForbiddenOperationException("参数异常");
        }
        Activity activity = this.getById(id);
        if(ObjectUtils.isEmpty(activity)){
            throw new ForbiddenOperationException("没有这个活动");
        }
        return BeanUtils.toBean(activity,ActivityInfoResDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeActivity(Long id) {
        if(ObjectUtils.isNull(id)){
            throw new ForbiddenOperationException("参数异常");
        }
        Activity activity = this.getById(id);
        if(ObjectUtils.isNull(activity)){
            throw new ForbiddenOperationException("没有这个活动");
        }
        activity.setStatus(VOIDED.getStatus());
        boolean flag = this.updateById(activity);
        if(!flag){
            throw new ForbiddenOperationException("更新失败");
        }
        LambdaQueryWrapper<Coupon> wrapper = Wrappers.<Coupon>lambdaQuery()
                .eq(Coupon::getActivityId, id);
        List<Coupon> coupons = couponMapper.selectList(wrapper);
        coupons.stream().forEach(
                item -> {
                    item.setStatus(CouponStatusEnum.VOIDED.getStatus());
                    Integer tmp = couponMapper.updateById(item);
                    if(tmp == 0){
                        throw new ForbiddenOperationException("更新失败");
                    }
                }
        );
    }
}
