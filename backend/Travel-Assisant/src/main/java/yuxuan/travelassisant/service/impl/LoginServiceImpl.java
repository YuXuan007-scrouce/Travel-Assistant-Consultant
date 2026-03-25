package yuxuan.travelassisant.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import yuxuan.travelassisant.entity.DTO.LoginFormDTO;
import yuxuan.travelassisant.entity.DTO.Result;
import yuxuan.travelassisant.entity.DTO.UserDTO;
import yuxuan.travelassisant.entity.Register;
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
    @Resource
    private UserMapper userMapper;

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
        log.info("发送验证码短信成功，验证码：{}",vercode);
        return Result.ok();
    }


    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        //1、校验手机号
        if (RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式错误!");
        }
        log.info("执行下一步");
        // 2、登录方式判断
        if(loginForm.getCode()!=null && !loginForm.getCode().equals("")) {
            //校验验证码from Redis
            String code = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
            String vercode01 = loginForm.getCode();
            if (code == null || !code.equals(vercode01)) {
                //不一致报错
                return Result.fail("验证码错误");
            }
        } else if (loginForm.getPassword() != null) {
            //  密码登录
            // 2.1 根据手机号查询用户（需要拿数据库密文进行比对，必须提前查）
            User user = query().eq("phone", phone).one();
            log.info("查询到用户{}",user);
            if (user == null) {
                return Result.fail("手机号未注册");
            }
            if (StrUtil.isBlank(user.getPassword())) {
                return Result.fail("该账号未设置密码，请使用验证码登录");
            }

            // 2.2 BCrypt 验证：BCrypt.checkpw(明文, 数据库密文)
            boolean passwordMatch = BCrypt.checkpw(loginForm.getPassword(), user.getPassword());
            if (!passwordMatch) {
                return Result.fail("密码错误");
            }
        } else {
            return Result.fail("请输入验证码或密码");
        }

        //3、一致，根据手机号查询用户   tb_user 查询的具体的表
        User user = query().eq("phone", phone).one();
        log.info("用户{}",user);
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

    @Override
    public Result register(Register register) {
        User phone = query().eq("phone", register.getPhone()).one();
        if (phone != null) {
            log.info("存在账号");
            return Result.fail("已存在用户请勿重复注册！");
        }
        User user = new User();
        user.setNickName(register.getNickName());
        user.setPhone(register.getPhone());
        String hashpw = BCrypt.hashpw(register.getPassword(), BCrypt.gensalt());
        user.setPassword(hashpw);
        // 写入数据库
        int insert = userMapper.insert(user);
        if (insert != 1) {
            return Result.fail("注册失败");
        }
        return Result.ok();
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
