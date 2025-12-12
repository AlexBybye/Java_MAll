package com.mall.servlet;

import com.google.gson.Gson;
import com.mall.dao.ProductDAO;
import com.mall.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 映射到 /api/product 路径下
//@WebServlet("/product/*")
public class ProductServlet extends HttpServlet {

    private final ProductDAO productDAO = new ProductDAO();
    private final Gson gson = new Gson();

    // 辅助方法：发送 JSON 响应
    private void sendJsonResponse(HttpServletResponse response, int status, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().write(gson.toJson(data));
    }

    // --- 1. 创建商品 (POST /api/product) ---
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, Object> result = new HashMap<>();

        try {
            // 假设我们在这里通过 AuthFilter 验证了用户权限是管理员
            // String userId = (String) request.getAttribute("userId"); // 管理员验证逻辑...

            BufferedReader reader = request.getReader();
            Product product = gson.fromJson(reader, Product.class);

            if (product == null || product.getName() == null || product.getPrice() == null) {
                result.put("success", false);
                result.put("message", "商品名称和价格不能为空。");
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, result);
                return;
            }

            // 避免空指针，给价格设置一个初始值
            if(product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                product.setPrice(BigDecimal.ZERO);
            }
            // 确保库存量非负
            if(product.getStockQuantity() < 0) {
                product.setStockQuantity(0);
            }

            if (productDAO.createProduct(product)) {
                result.put("success", true);
                result.put("message", "商品添加成功。");
                sendJsonResponse(response, HttpServletResponse.SC_CREATED, result); // 201 Created
            } else {
                result.put("success", false);
                result.put("message", "商品添加失败，数据库错误。");
                sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "服务器内部错误。");
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, result);
        }
    }

    // --- 2. 获取商品列表 或 单个商品 (GET /api/product 或 /api/product/{id}) ---
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String pathInfo = request.getPathInfo();
        Map<String, Object> result = new HashMap<>();

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/product - 获取所有商品
            List<Product> products = productDAO.getAllProducts();
            result.put("success", true);
            result.put("data", products);
            sendJsonResponse(response, HttpServletResponse.SC_OK, result);

        } else {
            // GET /api/product/{id} - 获取单个商品
            try {
                // pathInfo 示例：/123。我们截取 / 后面的 ID
                int id = Integer.parseInt(pathInfo.substring(1));
                Product product = productDAO.getProductById(id);

                if (product != null) {
                    result.put("success", true);
                    result.put("data", product);
                    sendJsonResponse(response, HttpServletResponse.SC_OK, result);
                } else {
                    result.put("success", false);
                    result.put("message", "未找到商品。");
                    sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, result); // 404 Not Found
                }
            } catch (NumberFormatException e) {
                result.put("success", false);
                result.put("message", "无效的商品ID格式。");
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, result);
            }
        }
    }

    // --- 3. 更新商品 (PUT /api/product/{id}) ---
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String pathInfo = request.getPathInfo();
        Map<String, Object> result = new HashMap<>();

        if (pathInfo == null || pathInfo.length() <= 1) {
            result.put("success", false);
            result.put("message", "请指定要更新的商品ID。");
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, result);
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            BufferedReader reader = request.getReader();
            Product product = gson.fromJson(reader, Product.class);

            if (product == null) {
                result.put("success", false);
                result.put("message", "请求体不能为空。");
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, result);
                return;
            }

            product.setId(id); // 确保要更新的 ID 是路径中的 ID

            if (productDAO.updateProduct(product)) {
                result.put("success", true);
                result.put("message", "商品更新成功。");
                sendJsonResponse(response, HttpServletResponse.SC_OK, result);
            } else {
                result.put("success", false);
                result.put("message", "商品更新失败，可能商品不存在。");
                sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, result);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "服务器内部错误或ID格式错误。");
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, result);
        }
    }

    // --- 4. 删除商品 (DELETE /api/product/{id}) ---
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String pathInfo = request.getPathInfo();
        Map<String, Object> result = new HashMap<>();

        if (pathInfo == null || pathInfo.length() <= 1) {
            result.put("success", false);
            result.put("message", "请指定要删除的商品ID。");
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, result);
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));

            if (productDAO.deleteProduct(id)) {
                result.put("success", true);
                result.put("message", "商品删除成功。");
                sendJsonResponse(response, HttpServletResponse.SC_OK, result);
            } else {
                result.put("success", false);
                result.put("message", "商品删除失败，可能商品不存在。");
                sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, result);
            }
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的商品ID格式。");
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, result);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "服务器内部错误。");
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, result);
        }
    }
}