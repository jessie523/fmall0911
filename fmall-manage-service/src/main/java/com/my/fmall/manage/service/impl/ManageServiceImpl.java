package com.my.fmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.my.fmall.bean.*;
import com.my.fmall.config.RedisUtil;
import com.my.fmall.manage.constant.ManageConst;
import com.my.fmall.manage.mapper.*;
import com.my.fmall0911.service.ManageService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-09-14 9:43
 */
@Service
public class ManageServiceImpl implements ManageService {
    @Autowired
    private BaseCatalog1Mapper baseCatalog1;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;


    /**
     * 获取一级分类
     *
     * @return
     */
    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1.selectAll();
    }

    /**
     * 获取二级分类
     *
     * @param catalog1Id
     * @return
     */
    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return this.baseCatalog2.select(baseCatalog2);
    }

    /**
     * 获取三级分类
     *
     * @param catalog2Id
     * @return
     */
    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return this.baseCatalog3.select(baseCatalog3);
    }

    /**
     * 根据三级分类id，获取商品属性名
     *
     * @param catalog3Id
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
//        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
//        baseAttrInfo.setCatalog3Id(catalog3Id);
//
//        return baseAttrInfoMapper.select(baseAttrInfo);

        return baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);
    }

    /**
     * 保存平台属性
     *
     * @param baseAttrInfo
     */
    @Transactional
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //1、判断是添加 还是 修改
        if (baseAttrInfo != null & baseAttrInfo.getId() != null) {
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        } else {
            baseAttrInfoMapper.insert(baseAttrInfo);
        }
        //2、先删除 所有属性值
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue);

        //3、再重新添加
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue attrValue : attrValueList) {
//            防止主键被赋上一个空字符串
            attrValue.setId(null);
//            前提条件是：必须要能获取到baseAttrInfo的自增主键值
            attrValue.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.insertSelective(attrValue);
        }

    }

    /**
     * 根据属性id，获取属性值的列表
     *
     * @param attrId
     * @return
     */
    @Override
    public List<BaseAttrValue> getAttValueList(String attrId) {
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        return baseAttrValueMapper.select(baseAttrValue);
    }

    /**
     * 获取spu 列表
     *
     * @param catalog3
     * @return
     */
    @Override
    public List<SpuInfo> getSpuList(String catalog3) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3);
        return spuInfoMapper.select(spuInfo);
    }

    /**
     * 查询基本销售属性表
     *
     * @return
     */
    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        List<BaseSaleAttr> list = baseSaleAttrMapper.selectAll();
        return list;
    }

    /**
     * 保存spu
     *
     * @param spu
     * @return
     */
    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spu) {
        //保存spu 操作：spuInfo,spu_sale_attr,spu_sale_attr_value,spu_image

//        判断是 添加 还是 修改
        if (spu.getId() != null) {//修改
            spuInfoMapper.updateByPrimaryKeySelective(spu);
        } else {//添加
            spuInfoMapper.insertSelective(spu);
        }

//        保存spu图片:先删除，再添加
        SpuImage image = new SpuImage();
        image.setSpuId(spu.getId());
        spuImageMapper.delete(image);

        List<SpuImage> spuImageList = spu.getSpuImageList();
        if (spuImageList != null && spuImageList.size() > 0) {
            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spu.getId());
                spuImageMapper.insertSelective(spuImage);
            }
        }


//        保存销售属性：先删除，再添加 (包括销售属性，销售属性值)
        SpuSaleAttr saleAttr = new SpuSaleAttr();
        saleAttr.setSpuId(spu.getId());
        spuSaleAttrMapper.delete(saleAttr);

        SpuSaleAttrValue attrValue = new SpuSaleAttrValue();
        attrValue.setSpuId(spu.getId());
        spuSaleAttrValueMapper.delete(attrValue);

        List<SpuSaleAttr> supSaleAttrList = spu.getSpuSaleAttrList();
        if (supSaleAttrList != null & supSaleAttrList.size() > 0) {


            for (SpuSaleAttr spuSaleAttr : supSaleAttrList) {
                spuSaleAttr.setSpuId(spu.getId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList != null & spuSaleAttrValueList.size() > 0) {

                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spu.getId());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }
                }

            }
        }

    }

    /**
     * 
     * @param spuId
     * @return
     */
    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }

    /**
     * 获取销售属性 （用下拉列表方式显示销售属性值）
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttr(String spuId) {

        //调用mapper
        //设计两张表的关联
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
        return spuSaleAttrList;
    }

    /**
     * 保存sku
     * @param skuInfo
     */
    @Transactional
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        /*
        * 保存的表：skuInfo，
        *           skuImage，
        *           sku_attr_value
        *           sku_sale_attr_value
        * */
//        1、保存skuInfo
        if(skuInfo.getId() != null){

            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }else{
            skuInfo.setId("36");
            skuInfoMapper.insertSelective(skuInfo);
        }

//        2、保存skuImage (先删除)
        SkuImage image = new SkuImage();
        image.setSkuId(skuInfo.getId());
        skuImageMapper.delete(image);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if(skuImageList != null && skuImageList.size() > 0){
            for (SkuImage skuImage : skuImageList) {
//                skuId必须赋值
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(skuImage);
            }
        }

//        3 平台属性sku_attr_value
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue);

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if(skuAttrValueList != null && skuAttrValueList.size() > 0){

            for (SkuAttrValue attrValue : skuAttrValueList) {
                attrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(attrValue);
            }
        }
//        4、sku_sale_attr_value
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if(skuSaleAttrValueList != null && skuSaleAttrValueList.size() > 0){
            for (SkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {
                saleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(saleAttrValue);
            }
        }

    }

    /**
     * 获取skuInfo,用于显示详情页
     * @param skuId
     * 用Jedis 分布式锁结局 缓存击穿
     * @return
     */
    @Override
    public SkuInfo getSkuInfo(String skuId) {
        //获取Jedis
        Jedis jedis = redisUtil.getJedis();
        SkuInfo skuInfo = null;
        //定义key：见名知意：sku:skuId:info
        try {
            String skuKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            //获取数据
            String skuJson = jedis.get(skuKey);
            if(skuJson == null || skuJson.length() == 0){
                //试着加锁
                System.out.println("缓存中没有数据");
                //执行set命令
                //定义上锁的key=sku:skuId:lock
                String skuLockKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKULOCK_SUFFIX;
                String lockKey = jedis.set(skuLockKey,"good","NX","PX",ManageConst.SKULOCK_EXPIRE_PX);
                if("OK".equals(lockKey)){
                    //此时加锁成功
                    skuInfo = getSkuInfoDB(skuId);
                    //将数据放入缓存
                    //将对象转换成字符串
                    String skuRedisStr = JSON.toJSONString(skuInfo);
                    String setex = jedis.setex(skuKey, ManageConst.SKUKEY_TIMEOUT, skuRedisStr);
                    System.out.println("setex:"+setex);
                    //删除锁
                    jedis.del(skuLockKey);
                    return skuInfo;
                }else{
                    //等待
                    Thread.sleep(1000);
                    //调用getSkuInfo();
                    return  getSkuInfo(skuId);
                }
            }else{
                skuInfo = JSON.parseObject(skuJson,SkuInfo.class);
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }

        return getSkuInfoDB(skuId);

    }

    private SkuInfo getSkuInfoDB(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> imageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(imageList);
        
        //查询平台属性
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValues = skuAttrValueMapper.select(skuAttrValue);
        //将平台属性装配到skuInfo
        skuInfo.setSkuAttrValueList(skuAttrValues);
        return skuInfo;
    }

    /**
     * 根据spuId,skuId 获取 spu全部商品销售属性
     * 和 sku的商品属性
     * @param skuInfo
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {

        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getSpuId(),skuInfo.getId());
    }

    /**
     *  获取sku属性值
     * @param spuInfo
     * @return
     */
    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuInfo) {
        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuInfo);
    }

    /**
     * 根据平台属性值ID，获取平台属性列表
     * @param attrValueIdList
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {
         String attrValueIds = StringUtils.join(attrValueIdList.toArray(),",");
        List<BaseAttrInfo> attrInfoList= baseAttrInfoMapper.selectAttrInfoByIds(attrValueIds);
        return attrInfoList;
    }
}
