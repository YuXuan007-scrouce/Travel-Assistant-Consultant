package yuxuan.travelassisant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpSession;
import yuxuan.travelassisant.entity.DTO.LoginFormDTO;
import yuxuan.travelassisant.entity.DTO.Result;
import yuxuan.travelassisant.entity.Register;
import yuxuan.travelassisant.entity.User;

public interface LoginService extends IService<User> {
    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginFormDTO, HttpSession session);

    Result register(Register register);
}
