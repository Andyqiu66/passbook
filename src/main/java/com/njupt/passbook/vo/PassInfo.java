package com.njupt.passbook.vo;

import com.njupt.passbook.entity.Merchants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1>用户领取的优惠券信息</h1>
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassInfo {

    /** 优惠券的详细信息  用户领取优惠券包含的所有信息 */
    private Pass pass;

    /** 优惠券模板 Pass优惠券对应的模板  商户投放时的原始信息*/
    private PassTemplate passTemplate;

    /** 优惠券对应的商户 这张优惠券所属的属主信息 */
    private Merchants merchants;
}
