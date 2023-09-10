package com.hmdp.service.impl;

import ch.qos.logback.core.net.SyslogConstants;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
// TODO 重构code enum
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1. 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.不符合 返回错误信息
            return Result.fail("手机号格式错误");
        }
        // 3.符合 生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4.保存验证码
        session.setAttribute(SystemConstants.SEND_CODE, code);
        // 5. TODO 发送验证码 第三方平台
        log.debug("发送短信验证码成功，验证码：{}", code);
        // 返回
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1.校验手机号
        if (RegexUtils.isPhoneInvalid(loginForm.getPhone())) {
            return Result.fail("手机号格式错误");
        }
        // 2.校验验证码
        Object cacheCode = session.getAttribute(SystemConstants.SEND_CODE);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.toString().equals(code)) {
            // 3.不一致 报错
            Result.fail("验证码错误");
        }
        // 4.一致 根据手机号查找用户
        User user = query().eq("phone", loginForm.getPhone()).one();
        if (user == null) {
            // 5.不存在 注册用户
            user = createuserWithPhone(loginForm.getPhone());
        }
        // 6.用户存在 直接保存到session
        session.setAttribute(SystemConstants.SESSION_USER, user);
        return Result.ok();
    }

    private User createuserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
