package com.my.fmall.cart.Service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.my.fmall.bean.CartInfo;
import com.my.fmall.bean.SkuInfo;
import com.my.fmall.cart.constant.CartConst;
import com.my.fmall.cart.mapper.CartInfoMapper;
import com.my.fmall.config.RedisUtil;
import com.my.fmall0911.service.CartService;
import com.my.fmall0911.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * author:zxy
 *
 * @create 2021-09-30 9:06
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Reference
    private ManageService manageService;

    @Autowired
    private RedisUtil redisUtil;
    /**
     * 登录还是未登录，在控制器判断
     *
     * 此方法：用户已登录的情况
     * @param skuId
     * @param userId
     * @param skuNum
     */
    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
            /*
            * 1、先查询一下购物车中是否有相同的商品，如果有则数量增加，更新缓存
            * 2、如果没有，直接添加到数据库，并插入缓存
            * */
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);

        CartInfo cart = cartInfoMapper.selectOne(cartInfo);//userId和skuId 确定唯一记录
        if(cart != null){//数据库中存在该商品，则数量增加
            cart.setSkuNum(cart.getSkuNum() + skuNum);
            cart.setSkuPrice(cart.getCartPrice());
            cartInfoMapper.updateByPrimaryKeySelective(cart); //更新数据库
            //更新缓存
        }else{
            //如果不存在保存，保存购物车
            //cartInfo数据来源于商品详情页，也就是来源于skuInfo
            //根据skuId，查询skuInfo
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);

            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());

            cartInfoMapper.insert(cartInfo);
            cart = cartInfo;
            //插入缓存
        }

        //更新缓存
        Jedis jedis = redisUtil.getJedis();
        //构建key：user:userId:cart
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        String value = JSON.toJSONString(cart);
        jedis.hset(cartKey,skuId,value);

        // 缓存的过期时间！
        // 购物车需要过期时间么？ 不去设置失效时间
        // 设置失效时间？ 与用户的过期时间一致！
        // 获取一用户的过期时间
        // 得到用户的key key=user:userId:info
        String userKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;

        //如何获取userKey的过期时间
        Long ttl = jedis.ttl(userKey);

        //给购物车设置过期时间
        jedis.expire(cartKey,ttl.intValue());
        //关闭redis
        jedis.close();

    }

    /**
     * 已登录 用户，显示购物车列表
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> getCartList(String userId) {

        /*
        * 先从缓存中取值，如果没有 从数据库取
        * */

        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        //  jedis.hgetAll(cartKey); 返回map key = field value = cartInfo 字符串

        //Hvals 命令返回哈希表所有字段的值。
        List<String> stringList = jedis.hvals(cartKey); //返回List集合 String = cartInfo字符串
        List<CartInfo> cartInfoLst = new ArrayList<>();

        if(stringList != null && stringList.size() > 0){
                //解析json字符串
                //1、从redis中取出进行反序列化
                //2、redis的hash结构是无序的，要进行排序（按时间戳或者主键id，倒序排序）
            for (String cartInfoStr : stringList) {
                //cartInfoStr 转换为对象CartInfo
                CartInfo cartInfo = JSON.parseObject(cartInfoStr,CartInfo.class);
                cartInfoLst.add(cartInfo);
            }
            //查看的时候应该做排序，真实项目应该按照时间戳排序（模拟项目：按照id进行排序）
            cartInfoLst.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    //定义比较规则
                    // compareTo 比较规则： s1 = abc s2=abcd
                    return o1.getId().compareTo(o2.getId());
                }
            });
        }else{
            //查询数据库 order by,并添加到缓存中


            cartInfoLst = loadCartCache(userId);
        }


        return cartInfoLst;
    }

    /**
     * 根据userId从数据中查询 购物车
     *
     * @param userId
     * @return
     */
    private List<CartInfo> loadCartCache(String userId) {
        //查询数据库时，要查询实时的最新价格，而不是购物车的价格
            List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);

            if(cartInfoList == null || cartInfoList.size() == 0){
                return null;
            }

            //放入redis中
        Jedis jedis = redisUtil.getJedis();

        HashMap<String, String> map = new HashMap<>();

        for (CartInfo cartInfo : cartInfoList) {
            map.put(cartInfo.getSkuId(),JSON.toJSONString(map));
        }

        //一次行放入多条记录
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        jedis.hmset(cartKey,map);//一次放入多个
        //hset():将单个 field-value 对设置到哈希表key中。

        jedis.close();
        return cartInfoList;
    }
}
