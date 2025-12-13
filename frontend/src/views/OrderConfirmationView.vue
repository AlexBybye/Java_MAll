<!-- src/views/OrderConfirmationView.vue -->
<template>
    <div class="order-confirmation">
        <h1>订单确认</h1>

        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="error" class="error">{{ error }}</div>
        <div v-else>
            <div class="order-summary">
                <h2>订单摘要</h2>
                <div class="order-items">
                    <div v-for="item in cartItems" :key="item.cart_id" class="order-item">
                        <div class="item-info">
                            <h3>{{ item.name }}</h3>
                            <p>数量: {{ item.quantity }}</p>
                        </div>
                        <div class="item-price">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
                    </div>
                </div>
                <div class="total">
                    <span>总计:</span>
                    <span>¥{{ totalPrice.toFixed(2) }}</span>
                </div>
            </div>

            <div class="shipping-info">
                <h2>配送信息</h2>
                <form @submit.prevent="createOrder">
                    <div class="form-group">
                        <label for="shippingAddress">配送地址:</label>
                        <textarea id="shippingAddress" v-model="shippingAddress" required rows="3"></textarea>
                    </div>
                    <button type="submit" class="confirm-btn">确认订单</button>
                </form>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useCartStore } from '@/stores/cart';
import { api } from '@/services/api';

const router = useRouter();
const cartStore = useCartStore();
const loading = ref(false);
const error = ref('');
const shippingAddress = ref('');

const cartItems = computed(() => cartStore.cartItems);
const totalPrice = computed(() => cartStore.totalPrice);

onMounted(async () => {
    if (cartItems.value.length === 0) {
        await cartStore.fetchCartItems();
        if (cartItems.value.length === 0) {
            router.push('/cart');
        }
    }
});

async function createOrder() {
    if (!shippingAddress.value.trim()) {
        error.value = '请填写配送地址';
        return;
    }

    loading.value = true;
    error.value = '';

    try {
        const cartItemIds = cartItems.value.map(item => item.cart_id);
        // 在OrderConfirmationView.vue中
        await cartStore.createOrder(shippingAddress.value.trim());

        // 清空购物车
        await cartStore.clearCart();

        // 跳转到订单成功页面
        router.push('/order-success');
    } catch (err: any) {
        error.value = err.message || '创建订单失败';
    } finally {
        loading.value = false;
    }
}
</script>

<style scoped>
.order-confirmation {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.order-summary {
    background-color: #f5f5f5;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
}

.order-items {
    margin-bottom: 20px;
}

.order-item {
    display: flex;
    justify-content: space-between;
    padding: 10px 0;
    border-bottom: 1px solid #ddd;
}

.total {
    display: flex;
    justify-content: space-between;
    font-weight: bold;
    font-size: 1.2em;
}

.shipping-info {
    background-color: #f5f5f5;
    padding: 20px;
    border-radius: 8px;
}

.form-group {
    margin-bottom: 20px;
}

textarea {
    width: 100%;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 4px;
    resize: vertical;
}

.confirm-btn {
    background-color: #4CAF50;
    color: white;
    border: none;
    padding: 10px 20px;
    border-radius: 4px;
    cursor: pointer;
}

.confirm-btn:hover {
    background-color: #45a049;
}
</style>