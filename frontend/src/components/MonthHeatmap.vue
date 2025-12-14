<!-- src/components/MonthHeatmap.vue -->
<script setup lang="ts">
import { computed } from 'vue'

// 定义组件属性
interface Props {
  // 月度数据，key为月份缩写(JAN, FEB等)，value为数值
  monthData?: Record<string, number>
  // 最大值，用于计算颜色渐变
  maxValue?: number
}

const props = withDefaults(defineProps<Props>(), {
  monthData: () => ({}),
  maxValue: 100
})

// 月份列表
const months = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC']

// 计算颜色函数
const getColor = (value: number) => {
  if (value === 0) return '#e0f7fa' // 冷色调
  
  // 计算颜色渐变 (从蓝色到红色)
  const ratio = Math.min(value / props.maxValue, 1)
  const r = Math.round(100 + ratio * 155) // 100 -> 255
  const g = Math.round(200 - ratio * 155) // 200 -> 45
  const b = Math.round(255 - ratio * 255) // 255 -> 0
  
  return `rgb(${r}, ${g}, ${b})`
}

// 计算每个月的值
const getMonthValue = (month: string) => {
  return props.monthData[month] || 0
}
</script>

<template>
  <div class="month-heatmap">
    <v-card>
      <v-card-title class="text-center">月度销售热力图</v-card-title>
      <v-card-text>
        <div class="heatmap-grid">
          <div 
            v-for="month in months" 
            :key="month"
            class="month-cell"
            :style="{
              backgroundColor: getColor(getMonthValue(month)),
              border: '1px solid rgba(0, 0, 0, 0.1)'
            }"
          >
            <div class="month-name">{{ month }}</div>
            <div class="month-value">{{ getMonthValue(month) }}</div>
          </div>
        </div>
      </v-card-text>
    </v-card>
  </div>
</template>

<style scoped>
.month-heatmap {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.heatmap-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  padding: 20px;
}

.month-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
  cursor: pointer;
  min-height: 100px;
}

.month-cell:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.month-name {
  font-weight: bold;
  font-size: 1.2rem;
  margin-bottom: 5px;
  color: rgba(0, 0, 0, 0.87);
}

.month-value {
  font-size: 1.5rem;
  font-weight: bold;
  color: rgba(0, 0, 0, 0.87);
}
</style>