package com.my.fmall.config;

import com.alibaba.fastjson.JSON;
import com.my.fmall.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 定义拦截器
 * author:zxy
 *
 * @create 2021-09-28 10:48
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter{
    //用户进入拦截器之前
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //用户在登录完成之后，会返回一个url
        //window.location.href=originUrl+"newToken="+data;
        // https://www.jd.com/?newToken=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.XzRrXwDhYywUAFn-ICLJ9t3Xwz7RHo1VVwZZGNdKaaQ
        //获取token
        String token = request.getParameter("newToken");

        //当token不为null时，放入cookie中
        if(token != null){
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }
        // 当用户访问非登录之后的页面，登录之后，继续访问其他业务模块时，url 并没有newToken，
        // 但是后台可能将token 放入了cookie 中！
        if(token == null){
            token = CookieUtil.getCookieValue(request, "token", false);
        }

        //从cookie中获取token，解密token
        if(token != null){
            //开始解密token，获取nickName
            Map map = getUserMapByToken(token);
            String nickName = (String)map.get("nickName");
            //保存到作用域中
            request.setAttribute("nickName",nickName);
        }

        //在拦截器中获取方法上的注解
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 获取方法上的注解LoginRequire
        LoginRequie methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequie.class);
        if(methodAnnotation != null){
            //有注解
            //需要判断用户是否登录？ 调用verify
//       在fmall-passport-web模块上     http://passport.atguigu.com/verify?token=aa&salt=192.168.3.40
          String salt = request.getHeader("X-forwarded-for");
           //利用自定义的HttpClientUtil工具类，专门负责通过restful风格调用接口
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&salt=" + salt);
            if("success".equals(result)){
                //登录，认证成功
                //开始解密token，获取nickName
                Map map = getUserMapByToken(token);
                String userId = (String)map.get("userId");

                //保存到作用域中
                request.setAttribute("userId",userId);
                return true;
            }else{
                // 认证失败！并且 methodAnnotation.autoRedirect()=true; 必须登录
                if(methodAnnotation.autoRedirect()){
                    //必须登录，跳转到页面
//                    http://passport.atguigu.com/index?originUrl=http%3A%2F%2Flocalhost%3A8084%2F33.html
                    //先获取url
                    String requestURL = request.getRequestURL().toString();
                    System.out.println("requestURL:"+requestURL); // http://item.gmall.com/36.html
                    //将url进行转码
                    String encodeURL = URLEncoder.encode(requestURL, "utf-8");
                    System.out.println("encodeURL："+encodeURL);
                    //必须登录
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                    return false;
                }
            }
        }
        return true;
    }

    //解密token，获取map中的数据
    private Map getUserMapByToken(String token) {
//        String token = "eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6Imx1Y3kiLCJ1c2VySWQiOiIxMDAxIn0.KSEsWRt5lXRB-arWsrcur8B9wHgU3ILK6aw9WWYIZUA";
           //获取中间部分
        String tokenUserInfo = StringUtils.substringBetween(token, ".");////截取特定字符串中间部分
        //将tokenUserInfo进行Base64解码
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] decode = base64UrlCodec.decode(tokenUserInfo);
        String jsonStr = null;
        try {
             jsonStr = new String(decode,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //将字符串转换成Map，直接返回
        return JSON.parseObject(jsonStr,Map.class);
    }

    //进入控制器之后，视图渲染之前
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

    //视图渲染之后
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
    }


}
