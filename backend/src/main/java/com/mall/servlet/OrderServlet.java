package com.mall.servlet;

import com.google.gson.Gson;
import com.mall.dao.CartDAO;
import com.mall.dao.CustomerDAO;
import com.mall.dao.OrderDAO;
import com.mall.model.OrderMaster;
import com.mall.model.OrderItem;
import com.mall.util.EmailUtil;
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
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final Gson gson = new Gson();

    // 辅助方法：发送 JSON 响应
    private void sendJsonResponse(HttpServletResponse response, int status, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().write(gson.toJson(data));
    }

    // --- 创建订单 (POST /api/order) ---
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
                    result.put("message", "订单创建成功。");
                    sendJsonResponse(response, HttpServletResponse.SC_CREATED, result); // 201 Created

                    // 5. 发送订单确认邮件（使用单独线程，避免影响响应时间）
                    sendOrderConfirmationEmail(customerId, orderId, totalAmount, shippingAddress, orderItems);

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
        Boolean isAdmin = (Boolean) request.getAttribute("isAdmin"); // 获取管理员标识
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
            } else if (pathInfo.equals("/all")) {
                // 情况 2: GET /api/order/all - 获取所有订单（仅管理员）
                if (isAdmin == null || !isAdmin) {
                    sendJsonResponse(response, HttpServletResponse.SC_FORBIDDEN,
                            new HashMap<String, String>() {{ put("message", "无权访问此资源。"); }});
                    return;
                }
                List<Map<String, Object>> allOrders = orderDAO.getAllOrders();
                result.put("success", true);
                result.put("orders", allOrders);
                sendJsonResponse(response, HttpServletResponse.SC_OK, result);
            } else {
                // 情况 3: GET /api/order/{id} - 获取单个订单详情
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2 && !pathParts[1].isEmpty()) {
                    int orderId = Integer.parseInt(pathParts[1]);

                    Map<String, Object> orderDetails = orderDAO.getOrderById(orderId);

                    if (orderDetails == null) {
                        sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND,
                                new HashMap<String, String>() {{ put("message", "订单未找到。"); }});
                        return;
                    }

                    // 修复越权问题：显式转换类型确保比较准确性
                    int orderCustomerId = (Integer) orderDetails.get("customerId");
                    if (orderCustomerId != customerId && (isAdmin == null || !isAdmin)) {
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

    // --- 删除订单 (DELETE /api/order/{id}) ---
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userIdStr = (String) request.getAttribute("userId");
        Boolean isAdmin = (Boolean) request.getAttribute("isAdmin"); // 获取管理员标识

        if (userIdStr == null) {
            sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    new HashMap<String, String>() {{ put("message", "未登录或Token无效。"); }});
            return;
        }

        int customerId = Integer.parseInt(userIdStr);
        String pathInfo = request.getPathInfo();
        Map<String, Object> result = new HashMap<>();

        try {
            // 解析请求路径获取订单ID
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length == 2 && !pathParts[1].isEmpty()) {
                int orderId = Integer.parseInt(pathParts[1]);

                // 验证订单权限
                Map<String, Object> orderDetails = orderDAO.getOrderById(orderId);
                if (orderDetails == null) {
                    sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND,
                            new HashMap<String, String>() {{ put("message", "订单未找到。"); }});
                    return;
                }

                int orderCustomerId = (Integer) orderDetails.get("customerId");
                if (orderCustomerId != customerId && (isAdmin == null || !isAdmin)) {
                    sendJsonResponse(response, HttpServletResponse.SC_FORBIDDEN,
                            new HashMap<String, String>() {{ put("message", "无权删除此订单。"); }});
                    return;
                }

                // 执行删除操作
                boolean success = orderDAO.deleteOrder(orderId, customerId, isAdmin != null && isAdmin);

                if (success) {
                    result.put("success", true);
                    result.put("message", "订单删除成功。");
                    sendJsonResponse(response, HttpServletResponse.SC_OK, result);
                } else {
                    result.put("success", false);
                    result.put("message", "订单删除失败。");
                    sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, result);
                }

            } else {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        new HashMap<String, String>() {{ put("message", "错误的请求路径格式，应为 /api/order/{id}。"); }});
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

    // --- 更新订单状态 (PUT /api/order/{id}/status) ---
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userIdStr = (String) request.getAttribute("userId");
        Boolean isAdmin = (Boolean) request.getAttribute("isAdmin"); // 获取管理员标识
        
        // 验证用户是否登录
        if (userIdStr == null) {
            sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    new HashMap<String, String>() {{ put("message", "未登录或Token无效。"); }});
            return;
        }
        
        // 验证用户是否是管理员
        if (isAdmin == null || !isAdmin) {
            sendJsonResponse(response, HttpServletResponse.SC_FORBIDDEN,
                    new HashMap<String, String>() {{ put("message", "仅管理员有权更新订单状态。"); }});
            return;
        }

        String pathInfo = request.getPathInfo();
        Map<String, Object> result = new HashMap<>();

        try {
            // 解析请求路径获取订单ID
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length == 3 && pathParts[2].equals("status") && !pathParts[1].isEmpty()) {
                int orderId = Integer.parseInt(pathParts[1]);

                // 解析请求体获取新的订单状态
                try (BufferedReader reader = request.getReader()) {
                    Map<String, Object> requestData = gson.fromJson(reader, Map.class);
                    // 同时支持"orderStatus"和"status"字段名
                    String newStatus = (String) requestData.get("orderStatus");
                    if (newStatus == null) {
                        newStatus = (String) requestData.get("status");
                    }

                    if (newStatus == null || newStatus.isEmpty()) {
                        sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                new HashMap<String, String>() {{ put("message", "订单状态不能为空。"); }});
                        return;
                    }

                    // 更新订单状态
                    boolean success = orderDAO.updateOrderStatus(orderId, newStatus);

                    if (success) {
                        result.put("success", true);
                        result.put("message", "订单状态更新成功。");
                        sendJsonResponse(response, HttpServletResponse.SC_OK, result);
                    } else {
                        result.put("success", false);
                        result.put("message", "订单状态更新失败，订单可能不存在。");
                        sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, result);
                    }
                }

            } else {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        new HashMap<String, String>() {{ put("message", "错误的请求路径格式，应为 /api/order/{id}/status。"); }});
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

    // 发送订单确认邮件
    private void sendOrderConfirmationEmail(int customerId, int orderId, BigDecimal totalAmount, String shippingAddress, List<OrderItem> orderItems) {
        new Thread(() -> {
            try {
                // 获取用户邮箱
                String userEmail = customerDAO.getEmailById(customerId);
                if (userEmail == null || userEmail.isEmpty()) {
                    System.err.println("[ERROR] 无法获取用户邮箱，跳过邮件发送。");
                    return;
                }

                // 构建邮件内容
                StringBuilder emailContent = new StringBuilder();
                emailContent.append("亲爱的顾客：\n\n");
                emailContent.append("感谢您的订单！以下是您的订单详情：\n\n");
                emailContent.append("订单号：").append(orderId).append("\n");
                emailContent.append("总金额：￥").append(totalAmount).append("\n");
                emailContent.append("收货地址：").append(shippingAddress).append("\n\n");
                emailContent.append("订单商品：\n");
                emailContent.append("----------------------------------------\n");
                emailContent.append("商品名称\t\t数量\t单价\t小计\n");
                emailContent.append("----------------------------------------\n");

                for (OrderItem item : orderItems) {
                    BigDecimal itemTotal = item.getPriceAtOrder().multiply(new BigDecimal(item.getQuantity()));
                    emailContent.append(item.getProductName())
                            .append("\t\t")
                            .append(item.getQuantity())
                            .append("\t")
                            .append("￥")
                            .append(item.getPriceAtOrder())
                            .append("\t")
                            .append("￥")
                            .append(itemTotal)
                            .append("\n");
                }

                emailContent.append("----------------------------------------\n");
                emailContent.append("总计：￥").append(totalAmount).append("\n\n");
                emailContent.append("我们将尽快为您处理订单。如有任何问题，请随时联系我们。\n\n");
                emailContent.append("此致\n");
                emailContent.append("商城团队\n");

                // 发送邮件
                String subject = "订单确认 - 订单号：" + orderId;
                EmailUtil.sendEmail(userEmail, subject, emailContent.toString());
                System.out.println("[INFO] 订单确认邮件已发送至：" + userEmail);

            } catch (Exception e) {
                System.err.println("[ERROR] 发送订单确认邮件失败：");
                e.printStackTrace();
            }
        }).start();
    }
}