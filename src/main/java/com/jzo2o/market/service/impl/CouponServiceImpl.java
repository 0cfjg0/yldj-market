package com.jzo2o.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.model.CurrentUserInfo;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.common.utils.ObjectUtils;
import com.jzo2o.market.mapper.CouponMapper;
import com.jzo2o.market.model.domain.Coupon;
import com.jzo2o.market.model.dto.request.CouponOperationPageQueryReqDTO;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;
import com.jzo2o.market.service.IActivityService;
import com.jzo2o.market.service.ICouponService;
import com.jzo2o.market.service.ICouponUseBackService;
import com.jzo2o.market.service.ICouponWriteOffService;
import com.jzo2o.mvc.utils.UserContext;
import com.jzo2o.mysql.utils.PageHelperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
@Service
@Slf4j
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements ICouponService {

    @Resource(name = "seizeCouponScript")
    private DefaultRedisScript<String> seizeCouponScript;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private IActivityService activityService;

    @Resource
    private ICouponUseBackService couponUseBackService;

    @Resource
    private ICouponWriteOffService couponWriteOffService;


    @Override
    public PageResult<CouponInfoResDTO> getCouponPage(CouponOperationPageQueryReqDTO reqDTO) {
        PageResult<Coupon> res = PageHelperUtils.selectPage(reqDTO, () -> {
            return list(Wrappers.<Coupon>lambdaQuery()
                    .eq(reqDTO.getActivityId() != null,Coupon::getActivityId,reqDTO.getActivityId()));
        });
        List<CouponInfoResDTO> resList = res.getList().stream().map(
                item -> BeanUtils.toBean(item, CouponInfoResDTO.class)
        ).collect(Collectors.toList());
        return new PageResult<CouponInfoResDTO>(res.getPages(),res.getTotal(),resList);
    }

    @Override
    public List<CouponInfoResDTO> getMyCoupon(Integer status, Integer lastId) {
        //获取当前用户
        CurrentUserInfo currentUserInfo = UserContext.currentUser();
        LambdaQueryWrapper<Coupon> wrapper = Wrappers.<Coupon>lambdaQuery()
                .eq(Coupon::getStatus, status)
                .eq(Coupon::getUserId, currentUserInfo.getId())
                .gt(lastId != null, Coupon::getId, lastId);
        //查询优惠券
        List<Coupon> list = this.list(wrapper);
        if(ObjectUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        return list.stream().map(item -> BeanUtils.toBean(item,CouponInfoResDTO.class)).collect(Collectors.toList());
    }
}
