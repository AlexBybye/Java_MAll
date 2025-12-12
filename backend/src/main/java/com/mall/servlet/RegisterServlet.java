package com.mall.servlet;

import com.google.gson.Gson;
import com.mall.dao.CustomerDAO;
import com.mall.model.Customer;
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

// 使用 @WebServlet 注解映射请求路径，这会替换 web.xml 中的配置
// 路径应与我们 Tomcat 配置的 Application Context (/api) 结合起来： /api/register
//@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final Gson gson = new Gson();

    // 核心方法：处理来自前端的 POST 请求 (注册通常是 POST)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. 设置响应格式为 JSON，并设置字符编码
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // 用于存放响应结果的 Map
        Map<String, Object> result = new HashMap<>();

        try {
            // 2. 从请求体中读取 JSON 数据
            // 由于是前后端分离，数据在请求体中，需要手动读取
            BufferedReader reader = request.getReader();

            // 3. 使用 Gson 将 JSON 字符串反序列化为 Customer 对象
            Customer customer = gson.fromJson(reader, Customer.class);

            if (customer == null || customer.getUsername() == null || customer.getPassword() == null) {
                result.put("success", false);
                result.put("message", "用户名和密码不能为空。");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 错误
                out.print(gson.toJson(result));
                return;
            }

            // 4. 业务逻辑校验：检查用户名是否已存在
            if (customerDAO.isUsernameExist(customer.getUsername())) {
                result.put("success", false);
                result.put("message", "用户名已存在，请更换。");
            } else {
                // 5. 调用 DAO 层进行注册
                boolean isSuccess = customerDAO.registerCustomer(customer);

                if (isSuccess) {
                    result.put("success", true);
                    result.put("message", "注册成功！请登录。");
                } else {
                    result.put("success", false);
                    result.put("message", "注册失败，数据库操作错误。");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "服务器内部错误：" + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 错误
        }

        // 6. 将结果 Map 转换为 JSON 格式并发送给前端
        out.print(gson.toJson(result));
        out.flush();
    }
}