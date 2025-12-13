<!-- frontend/src/views/AdminProductAddView.vue -->
<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import api from '@/utils/http';
import type { AdminProduct } from '@/types';
import { validateImageUrl, checkImageLoadable } from '@/utils/imageUtils'; // 导入图片处理工具

const router = useRouter();
const authStore = useAuthStore();

// 商品表单数据
const productForm = reactive({
    name: '',
    price: 0,
    stock: 0,
    description: '',
    imageUrl: ''
});

// 加载状态
const isLoading = ref(false);
// 错误信息
const errorMessage = ref('');
// 成功信息
const successMessage = ref('');

// 返回商品管理列表
function goBack() {
    router.push({ name: 'admin-products' });
}

// 提交表单，添加商品
async function submitForm() {
    try {
        isLoading.value = true;
        errorMessage.value = '';
        successMessage.value = '';

        // 表单验证
        if (!productForm.name.trim()) {
            errorMessage.value = '商品名称不能为空';
            return;
        }

        if (productForm.price <= 0) {
            errorMessage.value = '商品价格必须大于0';
            return;
        }

        if (productForm.stock < 0) {
            errorMessage.value = '商品库存不能小于0';
            return;
        }

        // 图片URL验证
        if (productForm.imageUrl) {
            // 验证URL格式
            const validation = validateImageUrl(productForm.imageUrl);
            if (!validation.valid) {
                errorMessage.value = `图片URL无效: ${validation.error}`;
                return;
            }
            
            // 检查图片是否可以加载
            try {
                const isLoadable = await checkImageLoadable(productForm.imageUrl);
                if (!isLoadable) {
                    errorMessage.value = '图片URL无法访问，请检查链接是否有效';
                    return;
                }
            } catch (err) {
                console.warn('图片加载检查失败:', err);
                // 加载检查失败时不阻止表单提交，只记录警告
            }
        }

        // 图片URL长度验证
        if (productForm.imageUrl && productForm.imageUrl.length > 255) {
            errorMessage.value = '商品图片URL长度不能超过255个字符';
            return;
        }

        // 准备提交给后端的数据，将 stock 转换为 stockQuantity
        const productData = {
            ...productForm,
            stockQuantity: productForm.stock
        };

        // 调用API添加商品
        const response = await api.post('/product', productData);

        if (response.data.success) {
            successMessage.value = '商品添加成功！';
            // 重置表单
            Object.assign(productForm, {
                name: '',
                price: 0,
                stock: 0,
                description: '',
                imageUrl: ''
            });
        } else {
            errorMessage.value = response.data.message || '商品添加失败';
        }
    } catch (error: any) {
        errorMessage.value = error.response?.data?.message || '网络错误，请稍后重试';
        console.error('添加商品失败:', error);
    } finally {
        isLoading.value = false;
    }
}
</script>

<template>
    <div class="admin-product-add">
        <header class="management-header">
            <h1>添加商品</h1>
            <div class="header-actions">
                <button @click="goBack" class="back-btn">返回商品管理</button>
            </div>
        </header>

        <div class="product-form-container">
            <!-- 消息提示 -->
            <div v-if="errorMessage" class="message error">
                {{ errorMessage }}
            </div>
            <div v-if="successMessage" class="message success">
                {{ successMessage }}
            </div>

            <form @submit.prevent="submitForm" class="product-form">
                <div class="form-group">
                    <label for="productName">商品名称 *</label>
                    <input id="productName" v-model="productForm.name" type="text" placeholder="请输入商品名称" required />
                </div>

                <div class="form-group">
                    <label for="productPrice">商品价格 *</label>
                    <input id="productPrice" v-model.number="productForm.price" type="number" step="0.01" min="0.01"
                        placeholder="请输入商品价格" required />
                </div>

                <div class="form-group">
                    <label for="productStock">商品库存 *</label>
                    <input id="productStock" v-model.number="productForm.stock" type="number" min="0"
                        placeholder="请输入商品库存" required />
                </div>

                <div class="form-group">
                    <label for="productDescription">商品描述</label>
                    <textarea id="productDescription" v-model="productForm.description" rows="4"
                        placeholder="请输入商品描述"></textarea>
                </div>

                <div class="form-group">
                    <label for="productImage">商品图片URL</label>
                    <input id="productImage" v-model="productForm.imageUrl" type="url" placeholder="请输入商品图片URL" />
                    <small>图片URL长度不能超过255个字符</small>
                </div>

                <div class="form-actions">
                    <button type="button" @click="goBack" class="cancel-btn">
                        取消
                    </button>
                    <button type="submit" class="submit-btn" :disabled="isLoading">
                        {{ isLoading ? '添加中...' : '添加商品' }}
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>

<!-- 在AdminProductAddView.vue和AdminProductEditView.vue的样式部分添加以下代码 -->
<style scoped>
/* 表单容器 */
.product-form {
  background-color: white;
  border-radius: 12px;
  padding: 2.5rem;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.product-form:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

/* 浮动标签效果 */
.input-container {
  position: relative;
  margin-bottom: 1.5rem;
}

.floating-label {
  position: absolute;
  top: 50%;
  left: 1rem;
  transform: translateY(-50%);
  color: #7f8c8d;
  font-size: 1rem;
  pointer-events: none;
  transition: all 0.3s ease;
  background-color: white;
  padding: 0 0.5rem;
}

.form-input {
  width: 100%;
  padding: 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background-color: white;
}

.form-input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.form-input:focus + .floating-label,
.form-input:not(:placeholder-shown) + .floating-label {
  top: 0;
  left: 0.75rem;
  font-size: 0.8rem;
  color: #3498db;
  transform: translateY(-100%);
}

/* 按钮样式 */
.btn {
  padding: 1rem 2rem;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-primary {
  background-color: #3498db;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #2980b9;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(52, 152, 219, 0.3);
}

.btn-secondary {
  background-color: #95a5a6;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #7f8c8d;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(149, 165, 166, 0.3);
}

.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.admin-product-add {
    min-height: 100vh;
    background-color: #f5f5f5;
    padding: 2rem;
}

.management-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    background: white;
    padding: 1.5rem;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.management-header h1 {
    margin: 0;
    color: #2c3e50;
}

.header-actions {
    display: flex;
    gap: 1rem;
}

.back-btn {
    padding: 0.5rem 1rem;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 1rem;
    background-color: #95a5a6;
    color: white;
}

.back-btn:hover {
    background-color: #7f8c8d;
}

.product-form-container {
    background: white;
    padding: 2rem;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    max-width: 600px;
    margin: 0 auto;
}

.message {
    padding: 1rem;
    margin-bottom: 1.5rem;
    border-radius: 4px;
    font-weight: bold;
}

.message.error {
    background-color: #f8d7da;
    color: #721c24;
    border: 1px solid #f5c6cb;
}

.message.success {
    background-color: #d4edda;
    color: #155724;
    border: 1px solid #c3e6cb;
}

.product-form {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.form-group label {
    font-weight: bold;
    color: #495057;
}

.form-group input,
.form-group textarea,
.form-group select {
    padding: 0.75rem;
    border: 1px solid #ced4da;
    border-radius: 4px;
    font-size: 1rem;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
    outline: none;
    border-color: #3498db;
    box-shadow: 0 0 0 0.2rem rgba(52, 152, 219, 0.25);
}

.form-group small {
    color: #6c757d;
    font-size: 0.875rem;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
    margin-top: 2rem;
}

.cancel-btn,
.submit-btn {
    padding: 0.75rem 1.5rem;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 1rem;
    font-weight: bold;
}

.cancel-btn {
    background-color: #95a5a6;
    color: white;
}

.cancel-btn:hover {
    background-color: #7f8c8d;
}

.submit-btn {
    background-color: #27ae60;
    color: white;
}

.submit-btn:hover {
    background-color: #229954;
}

.submit-btn:disabled {
    background-color: #95a5a6;
    cursor: not-allowed;
}
</style>