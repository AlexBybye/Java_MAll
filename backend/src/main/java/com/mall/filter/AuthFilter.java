package com.mall.filter;

import com.google.gson.Gson;
import com.mall.util.JWTUtil;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthFilter implements Filter {

    private final Gson gson = new Gson();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 新增：验证Filter是否被Tomcat加载
        System.out.println("=== AuthFilter 初始化成功 ===");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // ⭐ 1. 【新增】处理 CORS 预检请求 (OPTIONS) 和设置 CORS 头部
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173"); // 允许你的前端地址
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization"); // 允许 JWT Token 头部
        resp.setHeader("Access-Control-Allow-Credentials", "true"); // 允许携带 Cookie/Session

        // 如果请求是 OPTIONS，这说明是预检请求，直接返回 200/204 状态码，不进入过滤器链。
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_OK); // 返回 200 OK
            System.out.println("--- AuthFilter 拦截并处理 OPTIONS 预检请求 ---");
            return; // 结束处理，不继续执行 Filter Chain
        }

        // 1. 设置响应格式
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

// ⭐ 修正路径获取方式：使用 getServletPath() 更精确地匹配
        String requestPath = req.getServletPath();
// 如果你的项目没有配置 Servlet Path，也可以继续使用 requestURI，但我们用更精确的。

        System.out.println("--- AuthFilter 拦截请求: " + requestPath);

// 放行登录/注册
// ⭐ 修正后的放行逻辑
        if (requestPath.endsWith("/login") || requestPath.endsWith("/register")) {
            System.out.println("--- AuthFilter 放行（登录/注册）: " + requestPath);
            chain.doFilter(request, response);
            return;
        }

        // 2. 获取Token
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.err.println("--- AuthFilter 失败: 无Token或格式错误");
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "请求被拒绝，请携带有效的认证Token。");
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("--- AuthFilter 提取Token: " + token);

        // 3. 验证Token（关键：捕获所有异常）
        try {
            DecodedJWT jwt = JWTUtil.verifyToken(token);
            if (jwt == null) {
                throw new JWTVerificationException("Token验证返回空");
            }

            // Token有效，存入请求属性
            System.out.println("--- AuthFilter 成功: User ID: " + jwt.getSubject());
            req.setAttribute("userId", jwt.getSubject());
            req.setAttribute("username", jwt.getClaim("username").asString());
            chain.doFilter(request, response);

        } catch (JWTVerificationException e) {
            // 捕获JWT所有验证异常（过期、签名错误、格式错误等）
            System.err.println("--- AuthFilter 失败: JWT验证异常: " + e.getMessage());
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Token无效或已过期，请重新登录。");
        } catch (Exception e) {
            // 捕获其他未知异常
            System.err.println("--- AuthFilter 失败: 未知异常: " + e.getMessage());
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器内部错误，请稍后重试。");
        }
    }

    private void sendErrorResponse(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        // 1. 兜底：确保message不为null
        String finalMsg = "JWT_FAILURE_" + (message == null ? "未知错误" : message);
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", finalMsg);

        try {
            // 2. 序列化JSON时捕获异常
            String jsonStr = gson.toJson(error);
            System.out.println("--- 响应JSON: " + jsonStr); // 打印序列化结果
            resp.getWriter().write(jsonStr);
            resp.getWriter().flush();
        } catch (Exception e) {
            // 3. 序列化失败时直接写字符串兜底
            System.err.println("--- JSON序列化失败: " + e.getMessage());
            resp.getWriter().write("{\"success\":false,\"message\":\"" + finalMsg.replace("\"", "\\\"") + "\"}");
            resp.getWriter().flush();
        }
    }

    @Override
    public void destroy() {
        System.out.println("=== AuthFilter 销毁 ===");
    }
}