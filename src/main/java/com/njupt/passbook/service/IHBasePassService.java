package com.njupt.passbook.service;

import com.njupt.passbook.vo.PassTemplate;

/**
 * <h1>Pass HBase 服务</h1>
 * */
public interface IHBasePassService {

    /**
     * <h2>将 PassTemplate 写入HBase </h2>
     * @param passTemplate {@link PassTemplate}
     * @return true/false
     * */
    boolean dropPassTemplateToHBase(PassTemplate passTemplate);


}
