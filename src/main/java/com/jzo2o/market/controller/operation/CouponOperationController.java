package com.jzo2o.market.controller.operation;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.market.model.dto.request.CouponOperationPageQueryReqDTO;
import com.jzo2o.market.model.dto.response.CouponInfoResDTO;
import com.jzo2o.market.service.ICouponService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Api("运营管理优惠券相关接口")
@RequestMapping("/operation/coupon")
public class CouponOperationController {

    @Resource
    ICouponService couponService;

    /**
     * 优惠券分页查询
     */
    @GetMapping("/page")
    public PageResult<CouponInfoResDTO> getCouponPage(CouponOperationPageQueryReqDTO reqDTO){
       return couponService.getCouponPage(reqDTO);
    }

}
