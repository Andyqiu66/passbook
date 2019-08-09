package com.njupt.passbook.service;

import com.njupt.passbook.vo.GainPassTemplateRequest;
import com.njupt.passbook.vo.Response;

/**
 * <h1>用户领取优惠券功能实现</h1>
 * */
public interface IGainPassTemplateService {
    /**
     * <h2>用户领取优惠券</h2>
     * @param request {@link GainPassTemplateRequest}
     * @return {@link Response}
     * */
    Response gainPassTemplate(GainPassTemplateRequest request) throws Exception;
}
