package com.njupt.passbook.dao;

import com.njupt.passbook.entity.Merchants;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * <h1>Merchants Dao接口</h1>
 * */
public interface MerchantsDao extends JpaRepository<Merchants,Integer> {

    /**
     * <h2>通过id获取商户对象</h2>
     * @param id 商户id
     * @return {@link Merchants}
     * */
    Merchants findById(Integer id);

    /**
     * <h2>根据商户名称获取商户对象</h2>
     * @param name 商户名称
     * @return {@link Merchants}
     * */
    Merchants findByName(String name);

    /**
     * <h2>根据商户 ids 获取商户对象</h2>
     * @param ids 商户ids
     * @return {@link Merchants}
     * */
    List<Merchants> findByIdIn(List<Integer> ids);





}
