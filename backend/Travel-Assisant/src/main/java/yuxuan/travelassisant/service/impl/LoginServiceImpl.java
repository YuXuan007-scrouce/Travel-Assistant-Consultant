package yuxuan.travelassisant.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import yuxuan.travelassisant.entity.DTO.LoginFormDTO;
import yuxuan.travelassisant.entity.DTO.Result;
import yuxuan.travelassisant.entity.DTO.UserDTO;
import yuxuan.travelassisant.entity.User;
import yuxuan.travelassisant.mapper.UserMapper;
import yuxuan.travelassisant.service.LoginService;
import yuxuan.travelassisant.utils.RegexUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static yuxuan.travelassisant.utils.RedisConstants.*;
import static yuxuan.travelassisant.utils.SystemConstants.USER_NICK_NAME_PREFIX;

@Slf4j
@Service
public class LoginServiceImpl extends ServiceImpl<UserMapper, User> implements LoginService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1、校验手机号
        if (RegexUtils.isPhoneInvalid(phone)){
            //2、如果不符合，返回错误信息
            return Result.fail("手机号格式错误!");
        }
        //3、符号，生成一个验证码
        String vercode = RandomUtil.randomNumbers(6);

        //4、保存验证码到Redis    //set key value ex:120
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, vercode,LOGIN_CODE_TTL, TimeUnit.MINUTES);

        //5、发送验证码
        log.debug("发送验证码短信成功，验证码：{}",vercode);
        return Result.ok();
    }


    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        //1、校验手机号
        if (RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式错误!");
        }
        //TOOD 2、校验验证码from Redis
        String code = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String vercode01 = loginForm.getCode();
        if (code == null || ! code.equals(vercode01)){
            //不一致报错
            return Result.fail("验证码错误");
        }

        //4、一致，根据手机号查询用户   tb_user 查询的具体的表
        User user = query().eq("phone", phone).one();

        //5、判断用户存在
        if (user == null){
            //6、不存在，创建新用户
            user = createUserWithPhone(phone);
        }
        //7、保存用户信息到session中
        // 7.1 随机生成token,作为令牌
        String token = UUID.randomUUID().toString(true);
        // 7.2  将UserDTO对象转为 Hash 存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO,new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fielName,fieldValue) -> fieldValue.toString())); //所有字段都转字符串
        //7、3 存储
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY+token,userMap);
        //7.4 设置token 有效期    每次用户登录都会更新Redis中的token有效期
        stringRedisTemplate.expire(LOGIN_USER_KEY+token,LOGIN_USER_TTL, TimeUnit.MINUTES);

        //8.返回token
        return Result.ok(token);
    }

    /**
     * 创建新用户
     */
    private User createUserWithPhone(String phone) {
        // 创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX+RandomUtil.randomNumbers(10));
        // 保存用户
        save(user);
        return user;
    }
}
