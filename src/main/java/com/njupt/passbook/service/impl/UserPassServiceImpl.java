package com.njupt.passbook.service.impl;

import com.alibaba.fastjson.JSON;
import com.njupt.passbook.constant.Constants;
import com.njupt.passbook.constant.PassStatus;
import com.njupt.passbook.dao.MerchantsDao;
import com.njupt.passbook.entity.Merchants;
import com.njupt.passbook.mapper.PassRowMapper;
import com.njupt.passbook.service.IUserPassService;
import com.njupt.passbook.vo.Pass;
import com.njupt.passbook.vo.PassInfo;
import com.njupt.passbook.vo.PassTemplate;
import com.njupt.passbook.vo.Response;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.Mergeable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1>用户优惠券相关功能实现</h1>
 */
@Slf4j
@Service
public class UserPassServiceImpl implements IUserPassService {

    /** Hbase 客户端 */
    private final HbaseTemplate hbaseTemplate;

    /** Merchants Dao */
    private final MerchantsDao merchantsDao;

    @Autowired
    public UserPassServiceImpl(HbaseTemplate hbaseTemplate, MerchantsDao merchantsDao) {
        this.hbaseTemplate = hbaseTemplate;
        this.merchantsDao = merchantsDao;
    }

    @Override
    public Response getUserPassInfo(Long userId) throws Exception {
        return getPassInfoByStatus(userId,PassStatus.UNUSED);
    }

    @Override
    public Response getUserUsedPassInfo(Long userId) throws Exception {
        return getPassInfoByStatus(userId,PassStatus.USED);
    }

    @Override
    public Response getUserAllPassInfo(Long userId) throws Exception {
        return getPassInfoByStatus(userId,PassStatus.ALL);
    }

    @Override
    public Response userUsePass(Pass pass) {

        // 根据 userId 构造行键前缀
        byte[] rowPrefix=Bytes.toBytes(new StringBuilder(
                String.valueOf(pass.getUserId())).reverse().toString());
        Scan scan=new Scan();
        List<Filter> filters=new ArrayList<>();
        filters.add(new PrefixFilter(rowPrefix)); //行键过滤器，确定一行
        filters.add(new SingleColumnValueFilter(  //确定Template_ID 相等
                Constants.PassTable.FAMILY_I.getBytes(),
                Constants.PassTable.TEMPLATE_ID.getBytes(),
                CompareFilter.CompareOp.EQUAL,
                Bytes.toBytes(pass.getTemplateId())
        ));
        filters.add(new SingleColumnValueFilter( // 确定Con_Date与-1相等
                Constants.PassTable.FAMILY_I.getBytes(),
                Constants.PassTable.CON_DATE.getBytes(),
                CompareFilter.CompareOp.EQUAL,
                Bytes.toBytes("-1")
        ));

        scan.setFilter(new FilterList(filters));

        List<Pass> passes=hbaseTemplate.find(Constants.PassTable.TABLE_NAME,
                scan,new PassRowMapper());

        if (null==passes || passes.size()!=1){
            log.error("UserUsePass Error:{}", JSON.toJSONString(pass));
            return Response.failure("UserUsePass Error");
        }

        //修改数据库
        byte[] FAMILY_I =Constants.PassTable.FAMILY_I.getBytes();
        byte[] CON_DATE=Constants.PassTable.CON_DATE.getBytes();

        List<Mutation> datas=new ArrayList<>();
        Put put =new Put(passes.get(0).getRowKey().getBytes());
        put.addColumn(FAMILY_I,CON_DATE,
                Bytes.toBytes(DateFormatUtils.ISO_DATE_FORMAT.format(new Date()))
        );

        datas.add(put);

        hbaseTemplate.saveOrUpdates(Constants.PassTable.TABLE_NAME,datas);
        return Response.success();
    }

    /**
     * <h2>根据优惠券状态获取优惠券信息</h2>
     * @param userId 用户id
     * @param status {@link PassStatus}
     * @return {@link Response}
     * */
    private Response getPassInfoByStatus(Long userId, PassStatus status) throws Exception{

        // 根据 userId 构造行键前缀
        byte[] rowPrefix=Bytes.toBytes(new StringBuilder(String.valueOf(userId)).reverse().toString());
        //定义比较器
        CompareFilter.CompareOp compareOp=
                status==PassStatus.UNUSED?CompareFilter.CompareOp.EQUAL:CompareFilter.CompareOp.NOT_EQUAL;
        //定义扫描器扫描HBase中的数据
        Scan scan =new Scan();

        List<Filter> filters=new ArrayList<>();
        //1.行键前缀过滤器，找到特定用户的优惠券
        filters.add(new PrefixFilter(rowPrefix));
        //2.基于列单元值的过滤器，找到未使用的优惠券
        if (status!=PassStatus.ALL){
            filters.add(new SingleColumnValueFilter(
                    Constants.PassTable.FAMILY_I.getBytes(),
                    Constants.PassTable.CON_DATE.getBytes(),compareOp,Bytes.toBytes("-1")));
        }

        scan.setFilter(new FilterList(filters));

        List<Pass> passes =hbaseTemplate.find(Constants.PassTable.TABLE_NAME,scan,new PassRowMapper());
        Map<String,PassTemplate> passTemplateMap = buildPassTemplateMap(passes);
        Map<Integer,Merchants> merchantsMap=buildMerchantsMap(
                new ArrayList<>(passTemplateMap.values()));

        List<PassInfo> result=new ArrayList<>();

        for(Pass pass :passes){
            PassTemplate passTemplate=passTemplateMap.getOrDefault(
                    pass.getTemplateId(),null);

            if (null==passTemplate){
                log.error("PassTemplate Null:{}",pass.getTemplateId());
                continue;
            }

            Merchants merchants=merchantsMap.getOrDefault(passTemplate.getId(),null);
            if(null==merchants){
                log.error("Merchants Null:{}",passTemplate.getId());
                continue;
            }

            result.add(new PassInfo(pass,passTemplate,merchants));

        }
        return new Response(result);
    }

    /**
     * <h2>通过获取的 Passes 对象构造 Map</h2>
     * @param passes {@link Pass}
     * @return Map {@link PassTemplate}
     * */
    private
    Map<String, PassTemplate> buildPassTemplateMap(List<Pass> passes) throws Exception{

        String[] patterns = new String[]{"yyyy-MM-dd"};

        byte[] FAMILY_B= Bytes.toBytes(Constants.PassTemplateTable.FAMILY_B);
        byte[] ID = Bytes.toBytes(Constants.PassTemplateTable.ID);
        byte[] TITLE=Bytes.toBytes(Constants.PassTemplateTable.TITLE);
        byte[] SUMMARY=Bytes.toBytes(Constants.PassTemplateTable.SUMMARY);
        byte[] DESC=Bytes.toBytes(Constants.PassTemplateTable.DESC);
        byte[] HAS_TOKEN=Bytes.toBytes(Constants.PassTemplateTable.HAS_TOKEN);
        byte[] BACKGROUND=Bytes.toBytes(Constants.PassTemplateTable.BACKGROUND);

        byte[] FAMILY_C=Bytes.toBytes(Constants.PassTemplateTable.FAMILY_C);
        byte[] LIMIT=Bytes.toBytes(Constants.PassTemplateTable.LIMIT);
        byte[] START=Bytes.toBytes(Constants.PassTemplateTable.START);
        byte[] END=Bytes.toBytes(Constants.PassTemplateTable.END);

        List<String> templateIds = passes.stream().map(
                Pass::getTemplateId
        ).collect(Collectors.toList());

        List<Get> templateGets=new ArrayList<>(templateIds.size());
        templateIds.forEach(t->templateGets.add(new Get(Bytes.toBytes(t))));

        Result[] templateResults=hbaseTemplate.getConnection()
                .getTable(TableName.valueOf(Constants.PassTemplateTable.TABLE_NAME))
                .get(templateGets);

        //构造 PassTemplateId -> PassTemplate Object 的 Map,用于构造 PassInfo
        Map<String,PassTemplate> templateId2Object=new HashMap<>();
        for(Result item:templateResults){
            PassTemplate passTemplate=new PassTemplate();

            passTemplate.setId(Bytes.toInt(item.getValue(FAMILY_B,ID)));
            passTemplate.setTitle(Bytes.toString(item.getValue(FAMILY_B,TITLE)));
            passTemplate.setSummary(Bytes.toString(item.getValue(FAMILY_B,SUMMARY)));
            passTemplate.setDesc(Bytes.toString(item.getValue(FAMILY_B,DESC)));
            passTemplate.setHasToken(Bytes.toBoolean(item.getValue(FAMILY_B,HAS_TOKEN)));
            passTemplate.setBackground(Bytes.toInt(item.getValue(FAMILY_B,BACKGROUND)));

            passTemplate.setLimit(Bytes.toLong(item.getValue(FAMILY_C,LIMIT)));
            passTemplate.setStart(DateUtils.parseDate(
                Bytes.toString(item.getValue(FAMILY_C,START)),patterns));
            passTemplate.setEnd(DateUtils.parseDate(
                    Bytes.toString(item.getValue(FAMILY_C,END)),patterns));

            templateId2Object.put(Bytes.toString(item.getRow()),passTemplate);
        }

        return templateId2Object;
    }

    /**
     * <h2>通过获取的 PassTemplate 对象构造 Merchants Map</h2>
     * @param passTemplates {@link PassTemplate}
     * @return {@link Merchants}
     * */
    private
    Map<Integer, Merchants> buildMerchantsMap(List<PassTemplate> passTemplates){

        Map<Integer,Merchants> merchantsMap =new HashMap<>();
        List<Integer> merchantsIds=passTemplates.stream().map(
                PassTemplate::getId
        ).collect(Collectors.toList());
        List<Merchants> merchants =merchantsDao.findByIdIn(merchantsIds);

        merchants.forEach(m->merchantsMap.put(m.getId(),m));

        return merchantsMap;
    }
}
