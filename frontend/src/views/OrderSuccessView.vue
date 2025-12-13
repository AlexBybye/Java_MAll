<!-- d:\JavaMall\frontend\src\views\OrderSuccessView.vue -->
<template>
    <div class="order-success">
        <h1>订单创建成功！</h1>
        <p>您的订单已成功创建，我们将尽快处理。</p>
        <div class="actions">
            <router-link to="/" class="btn">继续购物</router-link>
            <router-link to="/orders" class="btn">查看我的订单</router-link> <!-- 修改这里 -->
        </div>
    </div>
</template>

<script setup lang="ts">
// 简单的订单成功页面
</script>

<style scoped>
.order-success {
    max-width: 600px;
    margin: 0 auto;
    padding: 50px;
    text-align: center;
    background-color: #f5f5f5;
    border-radius: 8px;
    margin-top: 50px;
}

.actions {
    margin-top: 30px;
}

.btn {
    display: inline-block;
    margin: 0 10px;
    padding: 10px 20px;
    background-color: #4CAF50;
    color: white;
    text-decoration: none;
    border-radius: 4px;
}

.btn:hover {
    background-color: #45a049;
}
</style>