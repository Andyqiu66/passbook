package com.njupt.passbook.service;

import com.njupt.passbook.vo.Response;

/**
 * <h1>获取库存信息：只返回用户没有领取的，即优惠券库存功能实现接口定义</h1>
 * */
public interface IInventoryService {

    /**
     * <h2>获取库存信息</h2>
     * @param userId 用户Id
     * @return {@link Response}
     * */
    Response getInventoryInfo(Long userId) throws Exception;//使用useId起到一个过滤的作用，因为每个用户看到的优惠券的库存信息是不一样的


}
