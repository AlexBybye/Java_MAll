package com.mall.servlet;

import com.google.gson.Gson;
import com.mall.dao.CartDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@WebServlet("/api/cart/*")
public class CartServlet extends HttpServlet {

    private final CartDAO cartDAO = new CartDAO();
    private final Gson gson = new Gson();

    // 辅助方法：发送 JSON 响应
    private void sendJsonResponse(HttpServletResponse response, int status, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().write(gson.toJson(data));
    }

    // --- 获取购物车内容 (GET /cart) ---
    // 路径：/cart
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // 从 AuthFilter 获取当前登录的顾客 ID
        String userIdStr = (String) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();

        if (userIdStr == null) {
            sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    new HashMap<String, String>() {{ put("message", "未登录或Token无效。"); }}
            );
            return;
        }
        int customerId = Integer.parseInt(userIdStr);

        try {
            List<Map<String, Object>> cartItems = cartDAO.getCartItemsByCustomerId(customerId);
            result.put("success", true);
            result.put("data", cartItems);
            sendJsonResponse(response, HttpServletResponse.SC_OK, result);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取购物车失败。");
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, result);
        }
    }

    // --- 添加/更新购物车项 (POST /api/cart) ---
    // 请求体需要 { "productId": 123, "quantity": 1 }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String userIdStr = (String) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();

        if (userIdStr == null) {
            sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    new HashMap<String, String>() {{ put("message", "未登录或Token无效。"); }}
            );
            return;
        }
        int customerId = Integer.parseInt(userIdStr);

        try (BufferedReader reader = request.getReader()) {
            Map<String, Object> requestData = gson.fromJson(reader, Map.class);

            // 简单校验
            if (requestData == null || requestData.get("productId") == null || requestData.get("quantity") == null) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        new HashMap<String, String>() {{ put("message", "请求参数错误。"); }}
                );
                return;
            }

            // Gson 可能会将数字解析为 Double，需要转换
            int productId = ((Double) requestData.get("productId")).intValue();
            int quantity = ((Double) requestData.get("quantity")).intValue();

            if (quantity <= 0) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        new HashMap<String, String>() {{ put("message", "数量必须大于0。"); }}
                );
                return;
            }

            if (cartDAO.addOrUpdateCartItem(customerId, productId, quantity)) {
                result.put("success", true);
                result.put("message", "商品已加入购物车或数量已更新。");
                sendJsonResponse(response, HttpServletResponse.SC_OK, result);
            } else {
                result.put("success", false);
                result.put("message", "操作失败，可能商品或用户不存在。");
                sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "服务器内部错误。");
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, result);
        }
    }

    // --- 更新购物车项数量 (PUT /api/cart/{cartId}) ---
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String userIdStr = (String) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();

        if (userIdStr == null) {
            sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, new HashMap<String, String>() {{ put("message", "未登录。"); }});
            return;
        }

        String pathInfo = request.getPathInfo(); // 期望格式：/cartId

        if (pathInfo == null || pathInfo.length() <= 1) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new HashMap<String, String>() {{ put("message", "请指定购物车项ID。"); }});
            return;
        }

        try (BufferedReader reader = request.getReader()) {
            int cartId = Integer.parseInt(pathInfo.substring(1));
            Map<String, Object> requestData = gson.fromJson(reader, Map.class);

            int newQuantity = ((Double) requestData.get("quantity")).intValue();

            if (newQuantity <= 0) {
                // 数量为0可以视为删除，但更规范的做法是提示BAD_REQUEST
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new HashMap<String, String>() {{ put("message", "数量必须大于0。如需删除请使用DELETE。"); }});
                return;
            }

            if (cartDAO.updateCartQuantity(cartId, newQuantity)) {
                result.put("success", true);
                result.put("message", "购物车项数量更新成功。");
                sendJsonResponse(response, HttpServletResponse.SC_OK, result);
            } else {
                result.put("success", false);
                result.put("message", "更新失败，购物车项不存在。");
                sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new HashMap<String, String>() {{ put("message", "服务器内部错误。"); }});
        }
    }

    // --- 移除购物车项 (DELETE /api/cart/{cartId}) ---
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String userIdStr = (String) request.getAttribute("userId");

        if (userIdStr == null) {
            sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, new HashMap<String, String>() {{ put("message", "未登录。"); }});
            return;
        }

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.length() <= 1) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, new HashMap<String, String>() {{ put("message", "请指定购物车项ID。"); }});
            return;
        }

        try {
            int cartId = Integer.parseInt(pathInfo.substring(1));

            if (cartDAO.removeCartItem(cartId)) {
                sendJsonResponse(response, HttpServletResponse.SC_NO_CONTENT, new HashMap<String, String>() {{ put("message", "购物车项移除成功。"); }}); // 204 No Content
            } else {
                sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, new HashMap<String, String>() {{ put("message", "购物车项移除失败，ID不存在。"); }});
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, new HashMap<String, String>() {{ put("message", "服务器内部错误。"); }});
        }
    }
}