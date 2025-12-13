// src/services/api.ts

const API_BASE_URL = '/api'; // 假设配置了代理或同源

async function request(method: string, url: string, data?: any, requiresAuth: boolean = false) {
    const headers: HeadersInit = {
        'Content-Type': 'application/json',
    };
    
    if (requiresAuth) {
        const token = localStorage.getItem('token'); // 从 localStorage 获取 token
        if (!token) {
            // 在实际应用中，这里应该重定向到登录页
            console.error('Authentication required, but no token found.');
            throw new Error('Unauthorized');
        }
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config: RequestInit = {
        method: method,
        headers: headers,
    };

    let fullUrl = API_BASE_URL + url;

    if (method === 'GET' && data) {
        // 对于 GET 请求，将数据转换为查询参数
        const query = new URLSearchParams(data).toString();
        fullUrl += `?${query}`;
    } else if (data) {
        config.body = JSON.stringify(data);
    }
    
    try {
        const response = await fetch(fullUrl, config);
        const jsonResponse = await response.json();

        if (!response.ok) {
            // 统一处理非 2xx 状态码
            const errorMessage = jsonResponse.message || `请求失败，状态码: ${response.status}`;
            throw new Error(errorMessage);
        }

        return jsonResponse;
    } catch (error) {
        console.error('API Request Error:', error);
        // 抛出错误，以便组件可以捕获和显示
        throw error; 
    }
}

// 导出一个包含所有API方法的对象
export const api = {
    // 1. 认证模块
    register: (data: any) => request('POST', '/register', data),
    login: (data: any) => request('POST', '/login', data),

    // 2. 用户管理
    getProfile: () => request('GET', '/customer/profile', null, true),
    updateProfile: (data: any) => request('PUT', '/customer/profile', data, true),
    
    // 3. 产品管理 (普通用户)
    getProducts: () => request('GET', '/product'),
    getProductDetail: (id: number) => request('GET', `/product/${id}`),

    // 3. 产品管理 (管理员)
    addProduct: (data: any) => request('POST', '/product', data, true),
    updateProduct: (id: number, data: any) => request('PUT', `/product/${id}`, data, true),
    deleteProduct: (id: number) => request('DELETE', `/product/${id}`, null, true),
    
    // 4. 购物车管理
    getCart: () => request('GET', '/cart', null, true),
    addItemToCart: (productId: number, quantity: number) => request('POST', '/cart', { productId, quantity }, true),
    updateCartItemQuantity: (cartId: number, quantity: number) => request('PUT', `/cart/${cartId}`, { quantity }, true),
    deleteCartItem: (cartId: number) => request('DELETE', `/cart/${cartId}`, null, true),

    // 5. 订单模块
    createOrder: (shippingAddress: string, cartItemIds: number[]) => request('POST', '/order', { shippingAddress, cartItemIds }, true),
    getUserOrders: () => request('GET', '/order', null, true),
    getOrderDetail: (id: number) => request('GET', `/order/${id}`, null, true),
    // 订单管理 (管理员)
    getAllOrders: () => request('GET', '/order/all', null, true),
    updateOrderStatus: (id: number, status: string) => request('PUT', `/order/${id}/status`, { status }, true),
    deleteOrder: (id: number) => request('DELETE', `/order/${id}`, null, true),
    
    // 6. 统计模块 (管理员)
    getStats: () => request('GET', '/stats', null, true),
    getDailySales: (params: { startDate: string, endDate: string }) => request('GET', '/stats/daily', params, true),
    getMonthlySales: (params: { year: string }) => request('GET', '/stats/monthly', params, true),
    getTopProducts: (params: { limit: number }) => request('GET', '/stats/top-products', params, true),
    getOrderStatusStats: () => request('GET', '/stats/status', null, true),
};