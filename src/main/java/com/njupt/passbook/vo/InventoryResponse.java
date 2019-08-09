package com.njupt.passbook.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <h1>库存请求响应，商户在平台上投放的可使用的优惠券（没有过期的）</h1>
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    /** 用户id  标识不同的用户能够看到不同的优惠券信息*/
    private Long userId;

    /** 优惠券模板信息 用户没有领取且没有过期的优惠券  */
    private List<PassTemplateInfo> passTemplateInfos;




}
