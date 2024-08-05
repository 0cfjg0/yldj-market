package com.jzo2o.market.controller.operation;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.market.model.dto.request.ActivityQueryForPageReqDTO;
import com.jzo2o.market.model.dto.request.ActivitySaveReqDTO;
import com.jzo2o.market.model.dto.response.ActivityInfoResDTO;
import com.jzo2o.market.service.IActivityService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author CFJG
 */
@RestController
@Api("运营管理活动相关接口")
@RequestMapping("/operation/activity")
public class ActivityOperationController {

    @Resource
    IActivityService activityService;

    /**
     * 活动分页查询接口
     * @param reqDTO
     * @return
     */
    @GetMapping("/page")
    public PageResult<ActivityInfoResDTO> getActivityPages(ActivityQueryForPageReqDTO reqDTO){
        return activityService.getActivityPages(reqDTO);
    }

    /**
     * 活动新增接口
     * @param reqDTO
     */
    @PostMapping("/save")
    public void insertActivity(@RequestBody ActivitySaveReqDTO reqDTO){
        activityService.saveActivity(reqDTO);
    }

    /**
     * 活动详情接口
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ActivityInfoResDTO getDetailActivity(@PathVariable Long id){
        return activityService.getDetailActivity(id);
    }

    /**
     * 取消活动
     * @param id
     */
    @PostMapping("/revoke/{id}")
    public void revokeActivity(@PathVariable Long id){
        activityService.revokeActivity(id);
    }
}
