package com.melonecom.service.impl;

import com.melonecom.constant.JwtClaimsConstant;
import com.melonecom.constant.MessageConstant;
import com.melonecom.enumeration.RoleEnum;
import com.melonecom.mapper.AdminMapper;
import com.melonecom.model.dto.AdminDTO;
import com.melonecom.model.entity.Admin;
import com.melonecom.result.Result;
import com.melonecom.service.IAdminService;
import com.melonecom.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 管理员注册
     *
     * @param adminDTO 管理员信息
     * @return 结果
     */
    @Override
    public Result register(AdminDTO adminDTO) {
        Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("username", adminDTO.getUsername()));
        if (admin != null) {
            return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }

        String passwordMD5 = DigestUtils.md5DigestAsHex(adminDTO.getPassword().getBytes());
        Admin adminRegister = new Admin();
        adminRegister.setUsername(adminDTO.getUsername()).setPassword(passwordMD5);

        if (adminMapper.insert(adminRegister) == 0) {
            return Result.error(MessageConstant.REGISTER + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.REGISTER + MessageConstant.SUCCESS);
    }

    /**
     * 管理员登录
     *
     * @param adminDTO 管理员信息
     * @return 结果
     */
    @Override
    public Result login(AdminDTO adminDTO) {
        Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("username", adminDTO.getUsername()));
        if (admin == null) {
            return Result.error(MessageConstant.USERNAME + MessageConstant.ERROR);
        }

        if (DigestUtils.md5DigestAsHex(adminDTO.getPassword().getBytes()).equals(admin.getPassword())) {
            // 登录成功
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.ROLE, RoleEnum.ADMIN.getRole());
            //claims.put(JwtClaimsConstant.ROLE, "ROLE_ADMIN");
            claims.put(JwtClaimsConstant.ADMIN_ID, admin.getAdminId());
            claims.put(JwtClaimsConstant.USERNAME, admin.getUsername());
            String token = JwtUtil.generateToken(claims);

            // 将token存入redis
           String redisKey = "login:token:" + admin.getAdminId();
           stringRedisTemplate.opsForValue().set(redisKey, token, 6, TimeUnit.HOURS);

            return Result.success(MessageConstant.LOGIN + MessageConstant.SUCCESS, token);
        }

        return Result.error(MessageConstant.PASSWORD + MessageConstant.ERROR);
    }

    /**
     * 登出
     *
     * @param token 认证token
     * @return 结果
     */
    @Override
    public Result logout(String token) {

    // 1) 去掉 Bearer 前缀（如果有）
    if (token != null && token.startsWith("Bearer ")) {
        token = token.substring(7);
    }

    // 2) 解析 token 拿 adminId
    Map<String, Object> claims = JwtUtil.parseToken(token);
    Object idObj = claims.get(JwtClaimsConstant.ADMIN_ID);
    if (idObj == null) {
        return Result.error(MessageConstant.LOGOUT + MessageConstant.FAILED);
    }
    Long adminId = (idObj instanceof Number n) ? n.longValue() : Long.valueOf(idObj.toString());

    // 3) 按 login:token:{adminId} 删除
    String redisKey = "login:token:" + adminId;
    Boolean result = stringRedisTemplate.delete(redisKey);

    // 4) 返回结果
    if (Boolean.TRUE.equals(result)) {
        return Result.success(MessageConstant.LOGOUT + MessageConstant.SUCCESS);
    } else {
        return Result.error(MessageConstant.LOGOUT + MessageConstant.FAILED);
    }
}

}
