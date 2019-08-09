package com.njupt.passbook.service;

import com.njupt.passbook.vo.Pass;
import com.njupt.passbook.vo.Response;

/**
 * <h1>获取用户个人优惠券信息</h1>
 * */
public interface IUserPassService {

    /**
     * <h2>获取用户个人优惠券信息，即我的优惠券功能实现</h2>
     * @param userId 用户 id
     * @return {@link Response}
     * */
    Response getUserPassInfo(Long userId) throws Exception;

    /**
     * <h2>获取用户已经消费了的优惠券，即已使用优惠券功能实现</h2>
     * @param userId 用户id
     * @return {@link Response}
     * */
    Response getUserUsedPassInfo(Long userId) throws Exception;

    /**
     * <h2>获取用户所有的优惠券</h2>
     * @param userId 用户Id
     * @return {@link Response}
     * */
    Response getUserAllPassInfo(Long userId) throws Exception;//代表用户对哪些优惠券感兴趣

    /**
     * <h2>用户使用优惠券</h2>
     * @param pass {@link Pass}
     * @return {@link Response}
     * */
    Response userUsePass(Pass pass);
}
