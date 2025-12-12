package com.mall.servlet;

import com.google.gson.Gson;
import com.mall.dao.CustomerDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

// 映射到 /api/customer/ 路径下，所有请求都会先经过 AuthFilter
//@WebServlet("/api/customer/*")
public class CustomerServlet extends HttpServlet {

    private final Gson gson = new Gson();
    // 将CustomerDAO声明为类成员变量
    private final CustomerDAO customerDAO = new CustomerDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();

        // 1. 从 AuthFilter 中获取用户身份信息
        String userId = (String) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");

        // 理论上 Filter 已经保证了 userId 不为空，但保险起见做个检查
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            result.put("success", false);
            result.put("message", "认证信息缺失，请登录。");
            out.print(gson.toJson(result));
            return;
        }

        // 2. 假设我们从数据库或其他服务中获取了更详细的用户信息
        // 提示：你可以在这里调用 DAO 方法根据 userId 查询数据库获取完整的 Customer 对象

        // 3. 返回用户信息
        result.put("success", true);
        result.put("message", "成功获取用户信息。");
        result.put("user_profile", new HashMap<String, Object>(){{
            put("id", userId);
            put("username", username);
            put("info", "这是需要登录才能查看的个人资料。");
        }});

        out.print(gson.toJson(result));
        out.flush();
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();

        // 1. 从AuthFilter获取用户ID
        String userIdStr = (String) request.getAttribute("userId");
        if (userIdStr == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            result.put("success", false);
            result.put("message", "认证信息缺失，请登录。");
            out.print(gson.toJson(result));
            return;
        }

        int userId = Integer.parseInt(userIdStr);

        // 2. 读取请求体中的更新数据
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        Map<String, Object> updateData = gson.fromJson(sb.toString(), Map.class);
        String email = (String) updateData.get("email");
        String phone = (String) updateData.get("phone");

        // 3. 直接使用类成员变量customerDAO，无需每次创建新实例
        boolean success = customerDAO.updateCustomerProfile(userId, email, phone);

        // 4. 返回响应
        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            result.put("success", true);
            result.put("message", "用户资料更新成功。");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("success", false);
            result.put("message", "用户资料更新失败。");
        }

        out.print(gson.toJson(result));
        out.flush();
    }
}