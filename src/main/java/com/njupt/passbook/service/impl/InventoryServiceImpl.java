package com.njupt.passbook.service.impl;

import com.njupt.passbook.constant.Constants;
import com.njupt.passbook.dao.MerchantsDao;
import com.njupt.passbook.entity.Merchants;
import com.njupt.passbook.mapper.PassTemplateRowMapper;
import com.njupt.passbook.service.IInventoryService;
import com.njupt.passbook.service.IUserPassService;
import com.njupt.passbook.utils.RowKeyGenUtil;
import com.njupt.passbook.vo.*;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.LongComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1>获取库存信息，只返回用户没有领取的</h1>
 * */
@Slf4j
@Service
public class InventoryServiceImpl implements IInventoryService {

    /** Hbase 客户端 */
    private final HbaseTemplate hbaseTemplate;

    /** Merchants Dao 接口 */
    private final MerchantsDao merchantsDao;

    /**  */
    private final IUserPassService userPassService;

    @Autowired
    public InventoryServiceImpl(HbaseTemplate hbaseTemplate,
                                MerchantsDao merchantsDao,
                                IUserPassService userPassService) {
        this.hbaseTemplate = hbaseTemplate;
        this.merchantsDao = merchantsDao;
        this.userPassService = userPassService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response getInventoryInfo(Long userId) throws Exception {

        Response allUserPass=userPassService.getUserAllPassInfo(userId);//获取用户所有的优惠券信息
        List<PassInfo> passInfos=(List<PassInfo>)allUserPass.getData();
        //库存信息需要排除掉用户已经领取的
        List<PassTemplate> excludeObject=passInfos.stream().map(PassInfo::getPassTemplate)
                .collect(Collectors.toList());
        List<String> excludeIds=new ArrayList<>();

        excludeObject.forEach(e->excludeIds.add(
                RowKeyGenUtil.genPassTemplateRowKey(e)));

        return new Response(new InventoryResponse(userId,
                buildPassTemplateInfo(getAvailblePassTemplate(excludeIds))));
    }

    /**
     * <h2>获取系统中可用的优惠券</h2>
     * @param excludeIds 需要排除的优惠券 ids 已经领取了的和过期的都不展现
     * @return {@link PassTemplate}
     */
    private List<PassTemplate> getAvailblePassTemplate(List<String> excludeIds){
        FilterList filterList=new FilterList(FilterList.Operator.MUST_PASS_ONE);

        filterList.addFilter(
                new SingleColumnValueFilter(
                        Bytes.toBytes(Constants.PassTemplateTable.FAMILY_C),
                        Bytes.toBytes(Constants.PassTemplateTable.LIMIT),
                        CompareFilter.CompareOp.GREATER,
                        new LongComparator(0L)
                )
        );
        filterList.addFilter(
                new SingleColumnValueFilter(
                        Bytes.toBytes(Constants.PassTemplateTable.FAMILY_C),
                        Bytes.toBytes(Constants.PassTemplateTable.LIMIT),
                        CompareFilter.CompareOp.EQUAL,
                       Bytes.toBytes("-1")
                )
        );

        Scan scan =new Scan();
        scan.setFilter(filterList);

        List<PassTemplate> vaildTemplates =hbaseTemplate.find(
                Constants.PassTemplateTable.TABLE_NAME,scan,new PassTemplateRowMapper());
        List<PassTemplate> availablePassTemplates=new ArrayList<>();

        Date cur= new Date();

        for (PassTemplate vaildTemplate:vaildTemplates){
            if (excludeIds.contains(RowKeyGenUtil.genPassTemplateRowKey(vaildTemplate))){
                continue;
            }

            if (cur.getTime()>=vaildTemplate.getStart().getTime()
            &&cur.getTime()<=vaildTemplate.getEnd().getTime()){
                availablePassTemplates.add(vaildTemplate);
            }
        }

        return availablePassTemplates;
    }

    /**
     * <h2>构造优惠券的信息</h2>
     * @param passTemplates {@link PassTemplate}
     * @return {@link PassTemplateInfo}
     * */
    private
    List<PassTemplateInfo> buildPassTemplateInfo(List<PassTemplate> passTemplates){

        Map<Integer, Merchants> merchantsMap=new HashMap<>();
        List<Integer> merchantsIds=passTemplates.stream().map(
                PassTemplate::getId
        ).collect(Collectors.toList());
        List<Merchants> merchants=merchantsDao.findByIdIn(merchantsIds);
        merchants.forEach(m->merchantsMap.put(m.getId(),m));

        List<PassTemplateInfo> result=new ArrayList<>(passTemplates.size());

        for(PassTemplate passTemplate:passTemplates){
            Merchants mc=merchantsMap.getOrDefault(passTemplate.getId(),null);
            if (null==mc){
                log.error("Merchants Error:{}",passTemplate.getId());
                continue;
            }

            result.add(new PassTemplateInfo(passTemplate,mc));
        }

        return result;
    }


}
