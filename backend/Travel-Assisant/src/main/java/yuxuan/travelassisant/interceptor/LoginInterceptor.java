package yuxuan.travelassisant.interceptor;


import yuxuan.travelassisant.entity.DTO.UserDTO;
import yuxuan.travelassisant.utils.UserHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserDTO user = UserHolder.getUser();
        System.out.println("LoginInterceptor - 当前线程用户: " + user);
        System.out.println("LoginInterceptor - 请求URI: " + request.getRequestURI());

      // 1、判断是否需要拦截(ThreadLocal中是否有用户)
        if (user == null) {
            System.out.println("LoginInterceptor - 用户为空，返回401");
            // 没有，需要拦截，设置状态码
            response.setStatus(401);
            // 拦截
            return false;
        }
    // 2、有用户，放行
        return true;
    }
}
