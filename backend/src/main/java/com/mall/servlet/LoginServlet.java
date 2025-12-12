package com.mall.servlet;

import com.google.gson.Gson;
import com.mall.dao.CustomerDAO;
import com.mall.model.Customer;
import com.mall.util.JWTUtil;
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

//@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 读取请求体并反序列化为 Customer 对象（仅包含 username 和 password）
            BufferedReader reader = request.getReader();
            Customer requestCustomer = gson.fromJson(reader, Customer.class);

            if (requestCustomer == null || requestCustomer.getUsername() == null || requestCustomer.getPassword() == null) {
                result.put("success", false);
                result.put("message", "请输入用户名和密码。");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(result));
                return;
            }

            // 2. 验证用户凭证
            Customer loggedInCustomer = customerDAO.loginCustomer(
                    requestCustomer.getUsername(),
                    requestCustomer.getPassword()
            );

            if (loggedInCustomer != null) {
                // 3. 验证成功：生成 JWT Token
                String token = JWTUtil.generateToken(loggedInCustomer.getId(), loggedInCustomer.getUsername());

                // 4. 构建成功响应
                result.put("success", true);
                result.put("message", "登录成功！");
                result.put("token", token); // 返回给前端，前端需保存并在后续请求中携带
                // 返回部分用户信息 (不含密码)
                result.put("user", new HashMap<String, Object>(){{
                    put("id", loggedInCustomer.getId());
                    put("username", loggedInCustomer.getUsername());
                    put("email", loggedInCustomer.getEmail());
                }});

            } else {
                // 5. 验证失败
                result.put("success", false);
                result.put("message", "用户名或密码错误。");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "服务器内部错误：" + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        out.print(gson.toJson(result));
        out.flush();
    }
}