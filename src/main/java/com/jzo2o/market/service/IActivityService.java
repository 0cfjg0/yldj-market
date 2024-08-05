package com.jzo2o.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.market.model.domain.Activity;
import com.jzo2o.market.model.dto.request.ActivityQueryForPageReqDTO;
import com.jzo2o.market.model.dto.request.ActivitySaveReqDTO;
import com.jzo2o.market.model.dto.response.ActivityInfoResDTO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author itcast
 * @since 2023-09-16
 */
public interface IActivityService extends IService<Activity> {

    PageResult<ActivityInfoResDTO> getActivityPages(ActivityQueryForPageReqDTO reqDTO);

    void saveActivity(ActivitySaveReqDTO reqDTO);

    ActivityInfoResDTO getDetailActivity(Long id);

    void revokeActivity(Long id);
}
