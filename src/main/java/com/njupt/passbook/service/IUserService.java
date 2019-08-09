package com.njupt.passbook.service;

import com.njupt.passbook.vo.Response;
import com.njupt.passbook.vo.User;

/**
 * <h1>用户服务：创建 User 服务，用户在平台注册，将用户信息写入HBase的过程</h1>
 * */
public interface IUserService {

    /**
     * <h2>创建用户</h2>
     * @param user {@link User}
     * @return {@link Response}
     * */
    Response createUser(User user) throws Exception;

}
