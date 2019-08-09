package com.njupt.passbook.controller;

import com.njupt.passbook.log.LogConstants;
import com.njupt.passbook.log.LogGenerator;
import com.njupt.passbook.service.IUserService;
import com.njupt.passbook.vo.Response;
import com.njupt.passbook.vo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * <h1>创建用户服务</h1>
 * */
@Slf4j
@RestController
@RequestMapping("/passbook")
public class CreateUserController {
    /** 创建用户服务*/
    private final IUserService userService;

    /** HttpServletRequest  */
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public CreateUserController(IUserService userService,
                                HttpServletRequest httpServletRequest) {
        this.userService = userService;
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * <h2>创建用户</h2>
     * @param user {@link User}
     * @return {@link Response}
     * */
    @ResponseBody
    @PostMapping("/createuser")
    Response createUser(@RequestBody User user) throws Exception{
        LogGenerator.genLog(
                httpServletRequest,
                -1L,
                LogConstants.ActionName.CREATE_USER,
                user
        );
        return userService.createUser(user);
    }

}
