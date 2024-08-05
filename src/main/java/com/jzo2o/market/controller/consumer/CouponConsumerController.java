package com.jzo2o.market.controller.consumer;

import com.jzo2o.market.model.dto.response.CouponInfoResDTO;
import com.jzo2o.market.service.ICouponService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Api("用户端活动相关接口")
@RequestMapping("/consumer/coupon")
public class CouponConsumerController {

    @Resource
    ICouponService couponService;

    @GetMapping("/my")
    public List<CouponInfoResDTO> getMyCoupon(@RequestParam("status") Integer status, @RequestParam(value = "lastId",required = false) Integer lastId){
        return couponService.getMyCoupon(status,lastId);
    }

}
