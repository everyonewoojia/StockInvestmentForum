<template>
	<view class="statistics-page">
		<!-- 顶部导航栏 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-back" @tap="goBack">
				<text class="nav-back-icon">‹</text>
			</view>
			<text class="nav-title">服药统计</text>
			<view class="nav-placeholder"></view>
		</view>

		<!-- 周统计 / 月统计 切换 -->
		<view class="period-switch">
			<view
				class="period-btn"
				:class="{ 'period-btn--active': activePeriod === 'week' }"
				@tap="switchPeriod('week')"
			>
				<text class="period-text">周统计</text>
			</view>
			<view
				class="period-btn"
				:class="{ 'period-btn--active': activePeriod === 'month' }"
				@tap="switchPeriod('month')"
			>
				<text class="period-text">月统计</text>
			</view>
		</view>

		<scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
			<!-- 数据卡片区域 -->
			<view class="stats-cards">
				<view class="stat-card">
					<text class="stat-value">{{ currentData.total }}</text>
					<text class="stat-label">总服药次数</text>
				</view>
				<view class="stat-card stat-card--green">
					<text class="stat-value stat-value--green">{{ currentData.completed }}</text>
					<text class="stat-label">已完成次数</text>
				</view>
				<view class="stat-card stat-card--warn">
					<text class="stat-value stat-value--warn">{{ currentData.missed }}</text>
					<text class="stat-label">漏服次数</text>
				</view>
				<view class="stat-card stat-card--rate">
					<text class="stat-value stat-value--rate">{{ currentData.rate }}%</text>
					<text class="stat-label">服药完成率</text>
				</view>
			</view>

			<!-- 图表区域 -->
			<view class="chart-card">
				<text class="chart-title">服药完成率</text>
				<view class="ring-chart-wrap">
					<view
						class="ring-chart"
						:style="{ background: ringChartStyle }"
					>
						<view class="ring-inner">
							<text class="ring-percent">{{ currentData.rate }}%</text>
							<text class="ring-sub">完成率</text>
						</view>
					</view>
					<view class="ring-legend">
						<view class="legend-item">
							<view class="legend-dot legend-dot--done"></view>
							<text class="legend-text">已完成 {{ currentData.completed }}次</text>
						</view>
						<view class="legend-item">
							<view class="legend-dot legend-dot--miss"></view>
							<text class="legend-text">漏服 {{ currentData.missed }}次</text>
						</view>
					</view>
				</view>

				<text class="chart-title chart-title--bar">每日服药情况</text>
				<view class="bar-chart">
					<view
						v-for="(item, index) in currentData.dailyStats"
						:key="index"
						class="bar-item"
					>
						<view class="bar-column">
							<view
								class="bar-fill"
								:style="{ height: item.percent + '%' }"
							></view>
						</view>
						<text class="bar-label">{{ item.label }}</text>
						<text class="bar-num">{{ item.done }}/{{ item.total }}</text>
					</view>
				</view>
			</view>

			<!-- 药品统计列表 -->
			<view class="medicine-stats-card">
				<text class="section-title">药品统计</text>
				<view
					v-for="(item, index) in currentData.medicineStats"
					:key="index"
					class="medicine-stat-row"
				>
					<view class="medicine-stat-info">
						<text class="medicine-stat-name">{{ item.name }}</text>
						<text class="medicine-stat-meta">总次数 {{ item.total }} 次</text>
					</view>
					<view class="rate-badge">
						<text class="rate-badge-text">{{ item.rate }}%</text>
					</view>
				</view>
			</view>

			<view class="scroll-bottom-space"></view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getUserId } from '../utils/auth.js'
import { get } from '../utils/request.js'

const statusBarHeight = ref(20)

/** 当前统计维度：week-周 / month-月，默认周 */
const activePeriod = ref('week')

/** 统计数据 */
const statData = ref({
	total: 0,
	completed: 0,
	missed: 0,
	rate: 0,
	dailyStats: [],
	medicineStats: []
})

/** 当前展示数据 */
const currentData = computed(() => statData.value)

/** 环形图背景（CSS conic-gradient，仅展示用） */
const ringChartStyle = computed(() => {
	const rate = currentData.value.rate
	return `conic-gradient(#52c41a 0% ${rate}%, #e8eef5 ${rate}% 100%)`
})

onLoad(() => {
	const sys = uni.getSystemInfoSync()
	statusBarHeight.value = sys.statusBarHeight || 20
})

/** 页面显示时加载数据 */
onShow(() => {
	loadStatData()
})

/** 返回上一页 */
function goBack() {
	const pages = getCurrentPages()
	if (pages.length >= 2) {
		const prevRoute = pages[pages.length - 2].route || ''
		if (prevRoute === 'pages/Index' || prevRoute.endsWith('/Index')) {
			uni.navigateBack({ delta: 1 })
			return
		}
	}
	uni.reLaunch({
		url: '/pages/Index',
		fail: () => {
			uni.redirectTo({ url: '/pages/Index' })
		}
	})
}

/** 切换周/月统计 */
function switchPeriod(type) {
	activePeriod.value = type
	// 重新加载数据
	loadStatData()
}

/** 获取统计日期 */
function getStatDate() {
	const now = new Date()
	const year = now.getFullYear()
	const month = now.getMonth() + 1
	const day = now.getDate()

	if (activePeriod.value === 'week') {
		// 周统计：返回当前日期
		return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
	} else {
		// 月统计：返回当前月份
		return `${year}-${String(month).padStart(2, '0')}`
	}
}

/** 加载统计数据 */
async function loadStatData() {
	const userId = getUserId()
	if (!userId) {
		uni.showToast({
			title: '用户信息异常，请重新登录',
			icon: 'none'
		})
		return
	}

	try {
		// 调用服药统计数据接口：GET /api/medicineBox/record/stat
		const result = await get('/record/stat', {
			userId: userId,
			type: activePeriod.value,
			date: getStatDate()
		}, { showLoading: true })

		if (result) {
			statData.value = {
				total: result.total || 0,
				completed: result.completed || 0,
				missed: result.missed || 0,
				rate: result.rate || 0,
				dailyStats: result.dailyStats || [],
				medicineStats: result.medicineStats || []
			}
		}
	} catch (error) {
		console.error('加载统计数据失败:', error)
		// 错误已在request.js中统一处理
	}
}
</script>

<style scoped>
.statistics-page {
	min-height: 100vh;
	display: flex;
	flex-direction: column;
	background: linear-gradient(180deg, #e8f5fc 0%, #f5fafd 160rpx, #f8fbfd 100%);
}

/* 导航栏 */
.nav-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding-left: 24rpx;
	padding-right: 24rpx;
	height: 88rpx;
	background: linear-gradient(180deg, #d8edf9 0%, #e5f4fc 100%);
	box-sizing: content-box;
	flex-shrink: 0;
}

.nav-back {
	width: 64rpx;
	height: 64rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.nav-back-icon {
	font-size: 52rpx;
	color: #333333;
	font-weight: 300;
	line-height: 1;
	margin-top: -8rpx;
}

.nav-title {
	font-size: 34rpx;
	font-weight: 600;
	color: #1a4a6e;
}

.nav-placeholder {
	width: 64rpx;
}

/* 周/月切换 */
.period-switch {
	display: flex;
	margin: 24rpx 32rpx;
	background: #ffffff;
	border-radius: 16rpx;
	padding: 8rpx;
	box-shadow: 0 4rpx 12rpx rgba(26, 74, 110, 0.06);
	flex-shrink: 0;
}

.period-btn {
	flex: 1;
	height: 72rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 12rpx;
}

.period-btn--active {
	background: #6ec1e8;
	box-shadow: 0 4rpx 12rpx rgba(110, 193, 232, 0.35);
}

.period-text {
	font-size: 30rpx;
	color: #2a6a8a;
	font-weight: 500;
}

.period-btn--active .period-text {
	color: #ffffff;
	font-weight: 600;
}

.page-scroll {
	flex: 1;
	height: 0;
	padding: 0 32rpx;
	box-sizing: border-box;
}

/* 数据卡片 2x2 */
.stats-cards {
	display: flex;
	flex-wrap: wrap;
	justify-content: space-between;
	margin-bottom: 24rpx;
}

.stat-card {
	width: 48%;
	background: #ffffff;
	border-radius: 20rpx;
	padding: 28rpx 20rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 4rpx 16rpx rgba(26, 74, 110, 0.08);
	box-sizing: border-box;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.stat-value {
	font-size: 48rpx;
	font-weight: 700;
	color: #1a4a6e;
	line-height: 1.2;
}

.stat-value--green {
	color: #52c41a;
}

.stat-value--warn {
	color: #ff6b6b;
}

.stat-value--rate {
	color: #6ec1e8;
}

.stat-label {
	font-size: 26rpx;
	color: #888888;
	margin-top: 8rpx;
}

.stat-card--green {
	border: 2rpx solid #e8f8ef;
}

.stat-card--warn {
	border: 2rpx solid #fff1f0;
}

.stat-card--rate {
	border: 2rpx solid #eef8fc;
}

/* 图表卡片 */
.chart-card {
	background: #ffffff;
	border-radius: 20rpx;
	padding: 32rpx 28rpx;
	margin-bottom: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(26, 74, 110, 0.08);
}

.chart-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #1a4a6e;
	display: block;
	margin-bottom: 28rpx;
}

.chart-title--bar {
	margin-top: 40rpx;
}

/* 环形图 */
.ring-chart-wrap {
	display: flex;
	align-items: center;
	justify-content: space-around;
	flex-wrap: wrap;
}

.ring-chart {
	width: 240rpx;
	height: 240rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.ring-inner {
	width: 170rpx;
	height: 170rpx;
	border-radius: 50%;
	background: #ffffff;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	box-shadow: inset 0 0 8rpx rgba(26, 74, 110, 0.05);
}

.ring-percent {
	font-size: 44rpx;
	font-weight: 700;
	color: #52c41a;
	line-height: 1.2;
}

.ring-sub {
	font-size: 24rpx;
	color: #888888;
	margin-top: 4rpx;
}

.ring-legend {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.legend-item {
	display: flex;
	align-items: center;
}

.legend-dot {
	width: 20rpx;
	height: 20rpx;
	border-radius: 50%;
	margin-right: 12rpx;
}

.legend-dot--done {
	background: #52c41a;
}

.legend-dot--miss {
	background: #e8eef5;
	border: 2rpx solid #cccccc;
}

.legend-text {
	font-size: 26rpx;
	color: #666666;
}

/* 柱状图 */
.bar-chart {
	display: flex;
	align-items: flex-end;
	justify-content: space-between;
	height: 280rpx;
	padding-top: 16rpx;
}

.bar-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	height: 100%;
}

.bar-column {
	flex: 1;
	width: 36rpx;
	background: #f0f7fb;
	border-radius: 8rpx 8rpx 0 0;
	display: flex;
	align-items: flex-end;
	overflow: hidden;
	margin-bottom: 8rpx;
}

.bar-fill {
	width: 100%;
	background: linear-gradient(180deg, #6ec1e8 0%, #52c41a 100%);
	border-radius: 8rpx 8rpx 0 0;
	min-height: 8rpx;
}

.bar-label {
	font-size: 22rpx;
	color: #888888;
	margin-bottom: 4rpx;
}

.bar-num {
	font-size: 20rpx;
	color: #aaaaaa;
}

/* 药品统计列表 */
.medicine-stats-card {
	background: #ffffff;
	border-radius: 20rpx;
	padding: 32rpx 28rpx;
	margin-bottom: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(26, 74, 110, 0.08);
}

.section-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #1a4a6e;
	display: block;
	margin-bottom: 24rpx;
}

.medicine-stat-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 24rpx 0;
	border-bottom: 2rpx solid #f0f7fb;
}

.medicine-stat-row:last-child {
	border-bottom: none;
	padding-bottom: 0;
}

.medicine-stat-info {
	flex: 1;
	min-width: 0;
	padding-right: 20rpx;
}

.medicine-stat-name {
	font-size: 30rpx;
	font-weight: 600;
	color: #333333;
	display: block;
	margin-bottom: 8rpx;
}

.medicine-stat-meta {
	font-size: 26rpx;
	color: #888888;
}

.rate-badge {
	flex-shrink: 0;
	min-width: 100rpx;
	height: 56rpx;
	background: #e8f8ef;
	border-radius: 28rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border: 2rpx solid #b7eb8f;
}

.rate-badge-text {
	font-size: 28rpx;
	font-weight: 700;
	color: #52c41a;
}

.scroll-bottom-space {
	height: 40rpx;
}
</style>
