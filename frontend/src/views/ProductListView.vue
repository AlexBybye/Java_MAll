<!-- frontend\src\views\ProductListView.vue -->
<template>
  <div class="product-list-container">
    <div class="page-header">
      <h1 class="page-title">商品列表</h1>
      <router-link to="/cart" class="cart-link">
        <i class="cart-icon">🛒</i>
        购物车 ({{ cartStore.totalItems }})
      </router-link>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>正在加载商品...</p>
    </div>

    <!-- 错误信息 -->
    <div v-else-if="error" class="error-container">
      <i class="error-icon">⚠️</i>
      <p class="error-message">{{ error }}</p>
      <button class="retry-btn" @click="fetchProducts">重新加载</button>
    </div>

    <!-- 商品列表容器 -->
    <div v-else class="products-container">
      <!-- 有商品时显示商品列表 -->
      <div v-if="products.length > 0" class="products-grid">
        <div v-for="product in products" :key="product.id" class="product-card">
          <!-- 在ProductListView.vue的商品卡片中 -->
          <router-link :to="'/product/' + product.id" class="product-card-link">
            <!-- 商品图片 -->
            <div class="product-image-container">
              <img 
                v-if="product.imageUrl" 
                :src="processImageUrl(product.imageUrl)" 
                :alt="product.name" 
                class="product-image" 
                loading="lazy" 
                @error="handleImageError($event, product)" 
                @load="handleImageLoad($event, product)"
              />
              <div v-else class="no-image">
                <i class="image-placeholder">📷</i>
                <span>暂无图片</span>
              </div>
            </div>

            <!-- 图片加载状态指示器 -->
            <div v-if="imageLoadingStates[product.id]" class="image-loading-indicator">
              <div class="loading-spinner-small"></div>
            </div>
            
            <!-- 商品信息 -->
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <!-- 其他信息代码 -->
            </div>
          </router-link>

          <!-- 商品URL -->
          <div v-if="product.imageUrl" class="product-url">
            <a :href="product.imageUrl" target="_blank" rel="noopener noreferrer" class="url-link">
              <i class="url-icon">🔗</i>
              查看图片
            </a>
          </div>

          <div class="product-meta">
            <span class="product-price">¥{{ product.price.toFixed(2) }}</span>
            <span class="product-stock" :class="{ 'out-of-stock': product.stockQuantity <= 0 }">
              {{ product.stockQuantity <= 0 ? '已售罄' : `库存: ${product.stockQuantity}` }} </span>
          </div>

          <button class="add-to-cart-btn" @click="addToCart(product.id)" :disabled="product.stockQuantity <= 0 || getCartItemQuantity(product.id) >= product.stockQuantity">
            {{ product.stockQuantity <= 0 ? '已售罄' : '加入购物车' }}
          </button>
        </div>
      </div>

      <!-- 空状态：当没有商品时显示 -->
      <div v-else class="empty-state">
        <i class="empty-icon">📦</i>
        <p>暂无商品</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import api from '@/utils/http';
import type { Product } from '@/types';
import { useCartStore } from '@/stores/cart';
import { validateImageUrl, processImageUrl, checkImageLoadable, getFallbackImage } from '@/utils/imageUtils'; // 导入图片处理工具

// 定义API响应类型
interface ApiResponse {
  success: boolean;
  data: Product[];
  message?: string;
}

const products = ref<Product[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

// 图片加载状态管理
const imageLoadingStates = reactive<Record<number, boolean>>({});
const imageStatuses = reactive<Record<number, 'success' | 'error' | ''>>({});
const imageStatusText = reactive<Record<number, string>>({});

// 获取购物车store
const cartStore = useCartStore();

async function fetchProducts() {
  isLoading.value = true;
  error.value = null;
  try {
    const response = await api.get<ApiResponse>('/product');
    
    if (response.data.success) {
      products.value = response.data.data;
      
      // 验证并检查所有图片URL
      validateAllImages(products.value);
    } else {
      throw new Error(response.data.message || '获取商品列表失败');
    }
  } catch (err: any) {
    console.error('获取商品列表失败:', err);
    error.value = '加载商品失败，请检查服务器连接。';
  } finally {
    isLoading.value = false;
  }
}

// 验证所有商品图片
async function validateAllImages(products: Product[]) {
  for (const product of products) {
    if (product.imageUrl) {
      const validation = validateImageUrl(product.imageUrl);
      if (!validation.valid) {
        console.warn(`商品ID ${product.id} 图片URL无效: ${validation.error}`);
        imageStatuses[product.id] = 'error';
        imageStatusText[product.id] = `URL无效: ${validation.error}`;
      } else {
        // 检查图片是否可以加载
        try {
          const isLoadable = await checkImageLoadable(product.imageUrl);
          if (!isLoadable) {
            console.warn(`商品ID ${product.id} 图片无法加载: ${product.imageUrl}`);
            imageStatuses[product.id] = 'error';
            imageStatusText[product.id] = '图片无法加载';
          } else {
            imageStatuses[product.id] = 'success';
            imageStatusText[product.id] = '图片正常';
          }
        } catch (err) {
          console.warn(`商品ID ${product.id} 图片检查失败:`, err);
        }
      }
    }
  }
}

// 图片加载处理
function handleImageLoad(event: Event, product: Product) {
  const imgElement = event.target as HTMLImageElement;
  imageLoadingStates[product.id] = false;
  imageStatuses[product.id] = 'success';
  imageStatusText[product.id] = '图片加载成功';
  console.log(`商品ID ${product.id} 图片加载成功`);
}

// 图片加载错误处理
function handleImageError(event: Event, product: Product) {
  const imgElement = event.target as HTMLImageElement;
  imageLoadingStates[product.id] = false;
  imageStatuses[product.id] = 'error';
  imageStatusText[product.id] = '图片加载失败';
  
  // 设置备用图片
  imgElement.src = getFallbackImage(200, 200, '图片加载失败');
  imgElement.onerror = null; // 防止递归调用
  
  console.error(`商品ID ${product.id} 图片加载失败:`, product.imageUrl);
  showNotification(`商品 "${product.name}" 的图片加载失败`, 'error');
}

// 添加商品到购物车
async function addToCart(productId: number) {
  const product = products.value.find(p => p.id === productId);
  if (!product) return;
  
  const currentCartQuantity = getCartItemQuantity(productId);
  if (currentCartQuantity >= product.stockQuantity) {
    showNotification('购物车中该商品数量已达库存上限！', 'error');
    return;
  }
  
  try {
    await cartStore.addToCart(productId, 1);
    showNotification('商品已成功添加到购物车！', 'success');
  } catch (err: any) {
    showNotification('添加商品到购物车失败：' + (err.message || '未知错误'), 'error');
  }
}

// 简单的通知函数
function showNotification(message: string, type: 'success' | 'error') {
  const notification = document.createElement('div');
  notification.className = `notification ${type}`;
  notification.textContent = message;
  document.body.appendChild(notification);
  
  setTimeout(() => notification.classList.add('show'), 10);
  
  setTimeout(() => {
    notification.classList.remove('show');
    setTimeout(() => document.body.removeChild(notification), 300);
  }, 3000);
}

onMounted(() => {
  fetchProducts();
});
// 获取购物车中某商品的当前数量
function getCartItemQuantity(productId: number): number {
  const cartItem = cartStore.cartItems.find(item => item.product_id === productId);
  return cartItem ? cartItem.quantity : 0;
}

</script>

<!-- 在ProductListView.vue的样式部分添加以下代码 -->
<style scoped>
/* 商品卡片悬停效果 */
.product-card {
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
}

/* 商品图片悬停效果 */
.product-image-container {
  overflow: hidden;
  border-radius: 12px 12px 0 0;
}

.product-image {
  transition: transform 0.3s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

/* 按钮悬停效果 */
.add-to-cart-btn {
  transition: all 0.3s ease;
}

.add-to-cart-btn:hover {
  background-color: #27ae60;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(46, 204, 113, 0.3);
}

.add-to-cart-btn:active {
  transform: translateY(0);
}

/* 购物车链接悬停效果 */
.cart-link {
  transition: all 0.3s ease;
  padding: 0.75rem 1.5rem;
  border-radius: 25px;
}

.cart-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

/* 全局样式 */
.product-list-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.cart-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-decoration: none;
  padding: 0.75rem 1.5rem;
  border-radius: 50px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.cart-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.cart-icon {
  font-size: 1.2rem;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 错误信息 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
  background-color: #fff5f5;
  border-radius: 12px;
  border: 1px solid #ffebee;
}

.error-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.error-message {
  color: #c62828;
  font-size: 1.1rem;
  margin-bottom: 1.5rem;
}

.retry-btn {
  background-color: #667eea;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  background-color: #5568d3;
  transform: translateY(-1px);
}

/* 商品网格 */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
}

/* 商品卡片 */
.product-card {
  background-color: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
}

/* 商品图片 */
.product-image-container {
  width: 100%;
  height: 200px;
  background-color: #f8f9fa;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
  /* 添加图片平滑过渡效果 */
  opacity: 1;
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.no-image {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
}

.image-placeholder {
  font-size: 3rem;
  margin-bottom: 0.5rem;
}

/* 商品信息 */
.product-info {
  padding: 1.5rem;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.product-name {
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 商品URL */
.product-url {
  margin-top: -0.5rem;
}

.url-link {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  color: #667eea;
  text-decoration: none;
  font-size: 0.875rem;
  transition: color 0.3s ease;
}

.url-link:hover {
  color: #5568d3;
  text-decoration: underline;
}

.url-icon {
  font-size: 0.8rem;
}

/* 商品元信息 */
.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.product-price {
  font-size: 1.5rem;
  font-weight: 700;
  color: #e63946;
}

.product-stock {
  font-size: 0.875rem;
  color: #4caf50;
  font-weight: 500;
}

.product-stock.out-of-stock {
  color: #f44336;
}

/* 添加到购物车按钮 */
.add-to-cart-btn {
  background: linear-gradient(135deg, #4caf50 0%, #45a049 100%);
  color: white;
  border: none;
  padding: 0.875rem;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 1rem;
}

.add-to-cart-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #45a049 0%, #3d8b40 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.add-to-cart-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
  opacity: 0.6;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 6rem 2rem;
  text-align: center;
  background-color: #f8f9fa;
  border-radius: 16px;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  color: #bdbdbd;
}

.empty-state p {
  color: #757575;
  font-size: 1.2rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-list-container {
    padding: 1rem;
  }

  .page-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .page-title {
    font-size: 1.5rem;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 1.5rem;
  }

  .product-info {
    padding: 1rem;
  }

  .product-price {
    font-size: 1.25rem;
  }
}

@media (max-width: 480px) {
  .products-grid {
    grid-template-columns: 1fr;
  }

  .product-image-container {
    height: 180px;
  }
}

/* 通知样式 */
.notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  transform: translateX(100%);
  transition: transform 0.3s ease;
  z-index: 1000;
}

.notification.success {
  background-color: #4caf50;
}

.notification.error {
  background-color: #f44336;
}

.notification.show {
  transform: translateX(0);
}

.product-card-link {
  text-decoration: none;
  color: inherit;
  display: block;
  height: 100%;
}
</style>