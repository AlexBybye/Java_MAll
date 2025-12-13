// src/stores/cart.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import api from '@/utils/http';
import type { CartItem } from '@/types';

export const useCartStore = defineStore('cart', () => {
  // 状态
  const cartItems = ref<CartItem[]>([]);
  const isLoading = ref(false);
  const error = ref<string | null>(null);

  // Getters
  const totalItems = computed(() => {
    return cartItems.value.reduce((total, item) => total + item.quantity, 0);
  });

  const totalPrice = computed(() => {
    return cartItems.value.reduce((total, item) => total + item.price * item.quantity, 0);
  });

  // Actions
  // 获取购物车内容
  async function fetchCartItems() {
    isLoading.value = true;
    error.value = null;
    try {
      const response = await api.get('/cart');  // 移除重复的/api前缀
      if (response.data.success) {
        cartItems.value = response.data.data;
      } else {
        throw new Error(response.data.message || '获取购物车失败');
      }
    } catch (err: any) {
      error.value = err.message || '获取购物车失败';
      console.error('获取购物车失败:', err);
    } finally {
      isLoading.value = false;
    }
  }

  // 添加商品到购物车
  async function addToCart(productId: number, quantity: number = 1) {
    isLoading.value = true;
    error.value = null;
    try {
      const response = await api.post('/cart', { productId, quantity });  // 移除重复的/api前缀
      if (response.data.success) {
        // 重新获取购物车内容以保持同步
        await fetchCartItems();
        return true;
      } else {
        throw new Error(response.data.message || '添加商品到购物车失败');
      }
    } catch (err: any) {
      error.value = err.message || '添加商品到购物车失败';
      console.error('添加商品到购物车失败:', err);
      return false;
    } finally {
      isLoading.value = false;
    }
  }

  // 更新购物车项数量
  async function updateCartItemQuantity(cartId: number, quantity: number) {
    isLoading.value = true;
    error.value = null;
    try {
      const response = await api.put(`/cart/${cartId}`, { quantity });  // 移除重复的/api前缀
      if (response.data.success) {
        // 重新获取购物车内容以保持同步
        await fetchCartItems();
        return true;
      } else {
        throw new Error(response.data.message || '更新购物车项数量失败');
      }
    } catch (err: any) {
      error.value = err.message || '更新购物车项数量失败';
      console.error('更新购物车项数量失败:', err);
      return false;
    } finally {
      isLoading.value = false;
    }
  }

  // 移除购物车项
  async function removeCartItem(cartId: number) {
    isLoading.value = true;
    error.value = null;
    try {
      const response = await api.delete(`/cart/${cartId}`);  // 移除重复的/api前缀
      if (response.data.success) {
        // 重新获取购物车内容以保持同步
        await fetchCartItems();
        return true;
      } else {
        throw new Error(response.data.message || '移除购物车项失败');
      }
    } catch (err: any) {
      error.value = err.message || '移除购物车项失败';
      console.error('移除购物车项失败:', err);
      return false;
    } finally {
      isLoading.value = false;
    }
  }

  // 清空购物车
  async function clearCart() {
    isLoading.value = true;
    error.value = null;
    try {
      // 逐个移除所有购物车项
      for (const item of cartItems.value) {
        await removeCartItem(item.cart_id);
      }
      return true;
    } catch (err: any) {
      error.value = err.message || '清空购物车失败';
      console.error('清空购物车失败:', err);
      return false;
    } finally {
      isLoading.value = false;
    }
  }

  // 创建订单
  async function createOrder(shippingAddress: string) {
    isLoading.value = true;
    error.value = null;
    try {
      // 获取购物车项ID列表
      const cartItemIds = cartItems.value.map(item => item.cart_id);

      // 调用后端API创建订单
      const response = await api.post('/order', { shippingAddress, cartItemIds });

      if (response.data.success) {
        // 订单创建成功后清空购物车
        await clearCart();
        return true;
      } else {
        throw new Error(response.data.message || '创建订单失败');
      }
    } catch (err: any) {
      error.value = err.message || '创建订单失败';
      console.error('创建订单失败:', err);
      return false;
    } finally {
      isLoading.value = false;
    }
  }

  return {
    cartItems,
    isLoading,
    error,
    totalItems,
    totalPrice,
    fetchCartItems,
    addToCart,
    updateCartItemQuantity,
    removeCartItem,
    clearCart,
    createOrder  // 添加到返回对象中
  };
});