<!-- frontend\src\views\CartView.vue -->
<template>
  <div class="cart-container">
    <h1>我的购物车</h1>

    <div v-if="cartStore.isLoading">加载中...</div>
    <div v-else-if="cartStore.error" class="error-message">{{ cartStore.error }}</div>

    <div v-else-if="cartStore.cartItems.length === 0" class="empty-cart">
      <p>购物车是空的</p>
      <router-link to="/" class="continue-shopping">继续购物</router-link>
    </div>

    <div v-else class="cart-content">
      <div class="cart-items">
        <div v-for="item in cartStore.cartItems" :key="item.cart_id" class="cart-item">
          <div class="item-info">
            <h3>{{ item.name }}</h3>
            <p class="price">¥{{ item.price.toFixed(2) }}</p>
          </div>

          <div class="item-quantity">
            <button class="quantity-btn" @click="updateQuantity(item.cart_id, item.quantity - 1)"
              :disabled="item.quantity <= 1">
              -
            </button>
            <span class="quantity">{{ item.quantity }}</span>
            <button class="quantity-btn" @click="updateQuantity(item.cart_id, item.quantity + 1)"
              :disabled="item.quantity >= item.stock_quantity">
              +
            </button>
          </div>

          <div class="item-total">
            ¥{{ (item.price * item.quantity).toFixed(2) }}
          </div>

          <button class="remove-btn" @click="removeItem(item.cart_id)">
            删除
          </button>
        </div>
      </div>

      <div class="cart-summary">
        <h2>购物车总计</h2>
        <div class="summary-row">
          <span>商品总数:</span>
          <span>{{ cartStore.totalItems }}</span>
        </div>
        <div class="summary-row total">
          <span>总计:</span>
          <span>¥{{ cartStore.totalPrice.toFixed(2) }}</span>
        </div>
        <button class="checkout-btn" @click="checkout">结算</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useCartStore } from '@/stores/cart';
import { useRouter } from 'vue-router';

const cartStore = useCartStore();
const router = useRouter();

// 页面加载时获取购物车内容
onMounted(() => {
  cartStore.fetchCartItems();
});

// 更新商品数量
async function updateQuantity(cartId: number, quantity: number) {
  await cartStore.updateCartItemQuantity(cartId, quantity);
}

// 删除商品
async function removeItem(cartId: number) {
  if (confirm('确定要从购物车中删除这个商品吗？')) {
    await cartStore.removeCartItem(cartId);
  }
}

// 结算功能
// src/views/CartView.vue
async function checkout() {
  try {
    // 跳转到订单确认页面
    router.push('/order-confirmation');
  } catch (error) {
    console.error('结算失败:', error);
  }
}
</script>

<style scoped>
.cart-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.empty-cart {
  text-align: center;
  padding: 50px 0;
}

.continue-shopping {
  display: inline-block;
  margin-top: 20px;
  padding: 10px 20px;
  background-color: #4CAF50;
  color: white;
  text-decoration: none;
  border-radius: 4px;
}

.cart-content {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}

.cart-items {
  flex: 1;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #ddd;
  gap: 20px;
}

.item-info {
  flex: 1;
}

.item-info h3 {
  margin: 0 0 10px 0;
}

.price {
  color: #e60000;
  font-weight: bold;
}

.item-quantity {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quantity-btn {
  width: 30px;
  height: 30px;
  border: 1px solid #ddd;
  background-color: white;
  cursor: pointer;
  border-radius: 4px;
}

.quantity-btn:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.quantity {
  min-width: 30px;
  text-align: center;
}

.item-total {
  font-weight: bold;
  min-width: 80px;
}

.remove-btn {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.remove-btn:hover {
  background-color: #d32f2f;
}

.cart-summary {
  width: 300px;
  background-color: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.summary-row.total {
  font-size: 1.2em;
  font-weight: bold;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ddd;
}

.checkout-btn {
  width: 100%;
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 15px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  margin-top: 20px;
}

.checkout-btn:hover {
  background-color: #45a049;
}

.error-message {
  color: red;
  padding: 10px;
  border: 1px solid red;
  background-color: #ffebeb;
  margin-bottom: 20px;
}
</style>