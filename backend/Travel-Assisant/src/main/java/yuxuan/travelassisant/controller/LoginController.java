package yuxuan.travelassisant.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import yuxuan.travelassisant.entity.DTO.LoginFormDTO;
import yuxuan.travelassisant.entity.DTO.Result;
import yuxuan.travelassisant.entity.DTO.UserDTO;
import yuxuan.travelassisant.entity.Register;
import yuxuan.travelassisant.service.LoginService;
import yuxuan.travelassisant.utils.UserHolder;

@RestController
@RequestMapping("/app")
public class LoginController {

    @Resource
    private LoginService loginService;

    //发送验证码
    @GetMapping("/login/getCode")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        return loginService.sendCode(phone,session);
    }

    //登录
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginFormDTO, HttpSession session){
        return loginService.login(loginFormDTO,session);
    }
    //登录后的请求：获取用户基本信息
    @GetMapping("/me")
    public Result me(){
        UserDTO user = UserHolder.getUser();
        System.out.println(user);
        return Result.ok(user);
    }

    @PostMapping("/register")
    public Result register(@RequestBody Register register){
        if (register == null) {
            return Result.fail("请填写注册信息");
        }
        return loginService.register(register);
    }

}
