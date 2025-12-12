package com.mall.servlet;

import com.google.gson.Gson;
import com.mall.dao.StatsDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售统计Servlet (Stats Servlet)
 * 提供销售统计数据的API接口
 */
// 映射到 /api/stats 路径下
//@WebServlet("/stats/*")
public class StatsServlet extends HttpServlet {

    private final StatsDAO statsDAO = new StatsDAO();
    private final Gson gson = new Gson();

    // 辅助方法：发送 JSON 响应
    private void sendJsonResponse(HttpServletResponse response, int status, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().write(gson.toJson(data));
    }

    // --- 处理统计数据请求 (GET /api/stats/{type}) ---
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userIdStr = (String) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证用户登录状态
            if (userIdStr == null) {
                Map<String, String> errorMsg = new HashMap<>();
                errorMsg.put("message", "未登录或Token无效。");
                sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, errorMsg);
                return;
            }

            // 获取请求路径，确定统计类型
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/stats - 返回所有统计数据
                Map<String, Object> allStats = new HashMap<>();

                // 默认获取最近30天的每日销售数据
                String endDate = java.time.LocalDate.now().toString();
                String startDate = java.time.LocalDate.now().minusDays(29).toString();
                allStats.put("dailySales", statsDAO.getDailySales(startDate, endDate));

                // 获取当前年份的月度销售数据
                String currentYear = String.valueOf(java.time.Year.now().getValue());
                allStats.put("monthlySales", statsDAO.getMonthlySales(currentYear));

                // 获取前10名热销商品
                allStats.put("topSellingProducts", statsDAO.getTopSellingProducts(10));

                // 获取订单状态统计
                allStats.put("orderStatusStats", statsDAO.getOrderStatusStats());

                result.put("success", true);
                result.put("data", allStats);
                sendJsonResponse(response, HttpServletResponse.SC_OK, result);

            } else {
                // GET /api/stats/{type} - 返回特定类型的统计数据
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2 && !pathParts[1].isEmpty()) {
                    String statsType = pathParts[1];

                    switch (statsType.toLowerCase()) {
                        case "daily":
                            // 每日销售额统计 - 支持日期范围参数
                            String startDate = request.getParameter("startDate");
                            String endDate = request.getParameter("endDate");

                            // 如果没有提供日期参数，默认获取最近7天的数据
                            if (startDate == null || endDate == null) {
                                endDate = java.time.LocalDate.now().toString();
                                startDate = java.time.LocalDate.now().minusDays(6).toString();
                            }

                            List<Map<String, Object>> dailySales = statsDAO.getDailySales(startDate, endDate);
                            result.put("success", true);
                            result.put("data", dailySales);
                            result.put("startDate", startDate);
                            result.put("endDate", endDate);
                            sendJsonResponse(response, HttpServletResponse.SC_OK, result);
                            break;

                        case "monthly":
                            // 月度销售额统计 - 支持年份参数
                            String year = request.getParameter("year");

                            // 如果没有提供年份参数，默认获取当前年份的数据
                            if (year == null) {
                                year = String.valueOf(java.time.Year.now().getValue());
                            }

                            List<Map<String, Object>> monthlySales = statsDAO.getMonthlySales(year);
                            result.put("success", true);
                            result.put("data", monthlySales);
                            result.put("year", year);
                            sendJsonResponse(response, HttpServletResponse.SC_OK, result);
                            break;

                        case "top":
                            // 热销商品统计 - 支持数量参数
                            String limitStr = request.getParameter("limit");
                            int limit = 10; // 默认获取前10名

                            if (limitStr != null) {
                                try {
                                    limit = Integer.parseInt(limitStr);
                                    if (limit <= 0) limit = 10;
                                    if (limit > 100) limit = 100; // 最多返回100个
                                } catch (NumberFormatException e) {
                                    // 忽略无效的limit参数，使用默认值
                                }
                            }

                            List<Map<String, Object>> topProducts = statsDAO.getTopSellingProducts(limit);
                            result.put("success", true);
                            result.put("data", topProducts);
                            result.put("limit", limit);
                            sendJsonResponse(response, HttpServletResponse.SC_OK, result);
                            break;

                        case "status":
                            // 订单状态统计
                            List<Map<String, Object>> statusStats = statsDAO.getOrderStatusStats();
                            result.put("success", true);
                            result.put("data", statusStats);
                            sendJsonResponse(response, HttpServletResponse.SC_OK, result);
                            break;

                        default:
                            // 无效的统计类型
                            Map<String, String> errorMsg = new HashMap<>();
                            errorMsg.put("message", "无效的统计类型。支持的类型：daily, monthly, top, status");
                            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, errorMsg);
                    }

                } else {
                    // 无效的请求路径格式
                    Map<String, String> errorMsg = new HashMap<>();
                    errorMsg.put("message", "错误的请求路径格式。");
                    sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, errorMsg);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorMsg = new HashMap<>();
            errorMsg.put("message", "服务器内部错误：" + e.getMessage());
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMsg);
        }
    }
}