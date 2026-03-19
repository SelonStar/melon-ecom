package com.melonecom.interceptor;

import com.melonecom.config.RolePermissionManager;
import com.melonecom.constant.JwtClaimsConstant;
import com.melonecom.constant.MessageConstant;
import com.melonecom.util.JwtUtil;
import com.melonecom.util.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RolePermissionManager rolePermissionManager;

    private final PathMatcher pathMatcher = new AntPathMatcher();

    // ✅ 白名单：不需要登录/鉴权
    private static final List<String> ALLOWED_PATHS = Arrays.asList(
            "/doc.html",
            "/favicon.ico",
            "/webjars/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/auth/**",
            // ===== 电商游客可访问接口（新增） =====
            "/category/**",   // 类目树 / 类目列表
            "/product/**",    // 商品列表 / 商品详情 / SKU
            "/products/**",
            "/banner/**",     // 首页轮播 / 活动位
            "/home/**",       // 首页导航卡等首页公共配置
            "/search/**",     // 搜索

            // ===== 静态资源 / 文件访问（按你实际接口选一个或多个） =====
            "/file/**",
            "/oss/**",
            "/minio/**",

            // ===== Spring 内部端点 =====
            "/error",

            // ===== 登录 / 注册 / 登出（无需 token） =====
            "/admin/login",
            "/admin/register",
            "/admin/logout",
            "/user/login",
            "/user/register",
            "/user/logout",
            "/user/resetUserPassword",
            "/user/sendVerificationCode"
    );

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(message);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // ✅ 0) 允许 CORS 预检请求直接通过
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        // ✅ 1) 先白名单放行（无论有没有 token）
        String path = request.getRequestURI();
        boolean isAllowedPath = ALLOWED_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (isAllowedPath) {
            return true;
        }

        // ✅ 2) 再取 token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
        }

        if (token == null || token.isBlank()) {
            sendErrorResponse(response, 401, MessageConstant.NOT_LOGIN);
            return false;
        }

        try {
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();

            // 3) 解析 token
            Map<String, Object> claims = JwtUtil.parseToken(token);

            // 4) 取登录主体ID：优先 adminId，其次 userId
            Object idObj = claims.get(JwtClaimsConstant.ADMIN_ID);
            if (idObj == null) {
                idObj = claims.get(JwtClaimsConstant.USER_ID);
            }
            if (idObj == null) {
                sendErrorResponse(response, 401, MessageConstant.SESSION_EXPIRED);
                return false;
            }

            Long loginId;
            if (idObj instanceof Number n) {
                loginId = n.longValue();
            } else {
                loginId = Long.valueOf(idObj.toString());
            }

            // 5) 查 Redis 校验 token
            String redisKey = "login:token:" + loginId;
            String redisToken = operations.get(redisKey);

            if (redisToken == null || !token.equals(redisToken)) {
                sendErrorResponse(response, 401, MessageConstant.SESSION_EXPIRED);
                return false;
            }

            // 6) 权限校验
            String role = (String) claims.get(JwtClaimsConstant.ROLE);
            String requestURI = request.getRequestURI();

            System.out.println("==== LOGIN INTERCEPTOR ====");
System.out.println("role = " + role);
System.out.println("requestURI = " + requestURI);
System.out.println("hasPermission = " + rolePermissionManager.hasPermission(role, requestURI));

if (!rolePermissionManager.hasPermission(role, requestURI)) {
    sendErrorResponse(response, 403, MessageConstant.NO_PERMISSION);
    return false;
}
            if (!rolePermissionManager.hasPermission(role, requestURI)) {
                sendErrorResponse(response, 403, MessageConstant.NO_PERMISSION);
                return false;
            }

            // 7) 放入 ThreadLocal
            ThreadLocalUtil.set(claims);
            return true;

        } catch (Exception e) {
            sendErrorResponse(response, 401, MessageConstant.SESSION_EXPIRED);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ThreadLocalUtil.remove();
    }
    
}
