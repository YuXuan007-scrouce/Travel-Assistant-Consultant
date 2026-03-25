package yuxuan.travelassisant.config;

import yuxuan.travelassisant.interceptor.LoginInterceptor;
import yuxuan.travelassisant.interceptor.RefreshTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Resource;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private RefreshTokenInterceptor refreshTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //需要登录授权的拦截器
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns("/user/code",
                        "/app/login/getCode","/app/login","/app/register",
                        "/user/login","/blog/hot").order(1);
        //token刷新的拦截器
        registry.addInterceptor(refreshTokenInterceptor).order(0);  //默认拦截所有请求
    }
}
