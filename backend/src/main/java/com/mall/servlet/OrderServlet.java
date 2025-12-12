package com.mall.servlet;

import com.google.gson.Gson;
import com.mall.dao.CartDAO;
import com.mall.dao.CustomerDAO; // ⭐ 补充导入 CustomerDAO
import com.mall.dao.OrderDAO;
import com.mall.model.OrderMaster;
import com.mall.model.OrderItem;
import com.mall.util.EmailUtil; // ⭐ 补充导入 EmailUtil
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 映射到 /api/order/ 路径下
//@WebServlet("/order/*")
public class OrderServlet extends HttpServlet {
    private final OrderDAO orderDAO = new OrderDAO();
    private final CartDAO cartDAO = new CartDAO();
    private final CustomerDAO customerDAO = new CustomerDAO(); // ⭐ 实例化 CustomerDAO
    private final Gson gson = new Gson();

    // 辅助方法：发送 JSON 响应
    private void sendJsonResponse(HttpServletResponse response, int status, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().write(gson.toJson(data));
    }

    // --- 创建订单 (POST /api/order) ---
    // OrderServlet.java - doPost 方法 (最终完整修正版)

    // OrderServlet.java - doPost 方法 (最终诊断版本)

    // OrderServlet.java - doPost 方法 (最终追踪版本)

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String userIdStr = (String) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();

        // 外部 try：捕获所有系统级或无法预料的异常
        try {
            System.out.println("[TRACE 1] 进入 OrderServlet.doPost，检查用户ID...");

            if (userIdStr == null) {
                sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, new HashMap<String, String>() {{ put("message", "未登录或Token无效。"); }});
                return;
            }
            int customerId = Integer.parseInt(userIdStr);

            OrderMaster order = null;

            // 内部 try：处理 JSON 解析和 IO 相关的异常
            try (BufferedReader reader = request.getReader()) {
                Map<String, Object> requestData = gson.fromJson(reader, Map.class);
                System.out.println("[TRACE 2] JSON 解析完成，开始验证请求数据...");

                String shippingAddress = (String) requestData.get("shippingAddress");
                List<Double> doubleCartItemIds = (List<Double>) requestData.get("cartItemIds");

                List<Integer> cartItemIds = new ArrayList<>();
                if (doubleCartItemIds != null) {
                    for (Double d : doubleCartItemIds) {
                        cartItemIds.add(d.intValue());
                    }
                }


                if (shippingAddress == null || cartItemIds.isEmpty()) {
                    sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new HashMap<String, String>() {{ put("message", "收货地址和购物车项不能为空。"); }});
                    return;
                }

                // 2. 从数据库获取选中的购物车项信息
                System.out.println("[TRACE 3] 调用 CartDAO.getCartItemsByCustomerId...");
                List<Map<String, Object>> cartItems = cartDAO.getCartItemsByCustomerId(customerId);
                System.out.println("[TRACE 4] CartDAO 调用完成，返回 " + cartItems.size() + " 项. 开始处理数据...");


                BigDecimal totalAmount = BigDecimal.ZERO;
                List<OrderItem> orderItems = new ArrayList<>();

                for (Map<String, Object> item : cartItems) {
                    Object cartIdObj = item.get("cart_id");
                    Object productIdObj = item.get("product_id");
                    Object quantityObj = item.get("quantity");
                    Object priceObj = item.get("price");

                    if (!(cartIdObj instanceof Number) || !(productIdObj instanceof Number) || !(quantityObj instanceof Number) || priceObj == null) {
                        System.err.println("警告：购物车项关键数据缺失或类型错误，跳过此项。ID: " + cartIdObj + ", Price: " + priceObj);
                        continue;
                    }

                    int cartId = ((Number) cartIdObj).intValue();

                    if (cartItemIds.contains(cartId)) {

                        int productId = ((Number) productIdObj).intValue();
                        int quantity = ((Number) quantityObj).intValue();

                        OrderItem orderItem = new OrderItem();
                        orderItem.setProductId(productId);
                        orderItem.setProductName((String) item.get("name"));

                        BigDecimal price;
                        try {
                            price = (BigDecimal) priceObj;
                        } catch (ClassCastException e) {
                            price = new BigDecimal(priceObj.toString());
                        }

                        orderItem.setPriceAtOrder(price);
                        orderItem.setQuantity(quantity);
                        orderItems.add(orderItem);

                        totalAmount = totalAmount.add(price.multiply(new BigDecimal(quantity)));
                    }
                }

                System.out.println("[TRACE 5] 购物车循环处理完成。待插入订单项数量: " + orderItems.size());


                if (orderItems.isEmpty()) {
                    sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new HashMap<String, String>() {{ put("message", "选中的购物车项无效或已过期。"); }});
                    return;
                }

                // 3. 构建 OrderMaster
                order = new OrderMaster();
                System.out.println("[TRACE 5.1] New OrderMaster created."); // 追踪点

                order.setCustomerId(customerId);
                System.out.println("[TRACE 5.2] Customer ID set."); // 追踪点

                order.setShippingAddress(shippingAddress);
                System.out.println("[TRACE 5.3] Shipping address set."); // 追踪点

                order.setTotalAmount(totalAmount);
                System.out.println("[TRACE 5.4] Total amount set: " + totalAmount); // 追踪点

                // 4. 执行事务创建订单
                System.out.println("[TRACE 6] 调用 OrderDAO.createOrder..."); // 追踪点
                int orderId = orderDAO.createOrder(order, orderItems, cartItemIds);
                System.out.println("[TRACE 7] OrderDAO 调用结束，返回 orderId: " + orderId);

                if (orderId > 0) {
                    result.put("success", true);
                    result.put("orderId", orderId);
                    result.put("message", "订单创建成功（已隔离辅助功能）。");
                    sendJsonResponse(response, HttpServletResponse.SC_CREATED, result); // 201 Created

                } else {
                    result.put("success", false);
                    result.put("message", "订单创建失败：库存不足或数据库操作错误。");
                    sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, result);
                }

            } catch (Exception e) {
                System.err.println("❌ OrderServlet 内部异常！");
                e.printStackTrace();
                sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new HashMap<String, String>() {{ put("message", "服务器内部错误：(JSON解析或IO错误) " + e.getMessage()); }});
            }

        } catch (Exception e) {
            System.err.println("❌ OrderServlet 外部系统级异常！");
            e.printStackTrace();
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new HashMap<String, String>() {{ put("message", "服务器内部错误：(系统级错误) " + e.getMessage()); }});
        }
    }

    // --- 订单列表/详情 (GET /api/order 或 /api/order/{id}) ---
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userIdStr = (String) request.getAttribute("userId");
        if (userIdStr == null) {
            sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    new HashMap<String, String>() {{ put("message", "未登录或Token无效。"); }});
            return;
        }
        int customerId = Integer.parseInt(userIdStr);

        String pathInfo = request.getPathInfo();
        Map<String, Object> result = new HashMap<>();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // 情况 1: GET /api/order/ - 获取订单列表
                List<Map<String, Object>> orders = orderDAO.getOrdersByCustomerId(customerId);
                result.put("success", true);
                result.put("orders", orders);
                sendJsonResponse(response, HttpServletResponse.SC_OK, result);

            } else {
                // 情况 2: GET /api/order/{id} - 获取单个订单详情
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2 && !pathParts[1].isEmpty()) {
                    int orderId = Integer.parseInt(pathParts[1]);

                    Map<String, Object> orderDetails = orderDAO.getOrderById(orderId);

                    if (orderDetails == null) {
                        sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND,
                                new HashMap<String, String>() {{ put("message", "订单未找到。"); }});
                        return;
                    }

                    // 安全性检查：确保该订单属于当前用户
                    if (!orderDetails.get("customerId").equals(customerId)) {
                        sendJsonResponse(response, HttpServletResponse.SC_FORBIDDEN,
                                new HashMap<String, String>() {{ put("message", "无权访问此订单。"); }});
                        return;
                    }

                    result.put("success", true);
                    result.put("order", orderDetails);
                    sendJsonResponse(response, HttpServletResponse.SC_OK, result);

                } else {
                    sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                            new HashMap<String, String>() {{ put("message", "错误的请求路径格式。"); }});
                }
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    new HashMap<String, String>() {{ put("message", "订单ID格式错误。"); }});
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new HashMap<String, String>() {{ put("message", "服务器内部错误：" + e.getMessage()); }});
        }
    }
}