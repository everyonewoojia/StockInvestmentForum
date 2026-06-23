<template>
	<view class="record-page">
		<!-- 顶部导航栏 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-back" @tap="goBack">
				<text class="nav-back-icon">‹</text>
			</view>
			<text class="nav-title">服药记录</text>
			<view class="nav-placeholder"></view>
		</view>

		<!-- 时间筛选栏 -->
		<view class="filter-bar">
			<view
				v-for="tab in filterTabs"
				:key="tab.key"
				class="filter-item"
				:class="{ 'filter-item--active': activeTab === tab.key }"
				@tap="switchTab(tab.key)"
			>
				<text class="filter-text">{{ tab.label }}</text>
				<view v-if="activeTab === tab.key" class="filter-line"></view>
			</view>
		</view>

		<scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
			<!-- 有记录：列表 -->
			<view v-if="displayList.length > 0" class="record-list">
				<view
					v-for="item in displayList"
					:key="item.id"
					class="record-card"
				>
					<!-- 左侧状态图标 -->
					<view class="status-icon" :class="'status-icon--' + item.status">
						<text class="status-symbol">{{ statusIconMap[item.status] }}</text>
					</view>

					<!-- 中间信息 -->
					<view class="record-info">
						<text class="medicine-name">{{ item.medicineName }}</text>
						<text class="time-row">计划服用：{{ item.planTime }}</text>
						<text v-if="item.status === 'taken'" class="time-row time-row--actual">
							实际服用：{{ item.actualTime }}
						</text>
						<text v-else-if="item.status === 'missed'" class="time-row time-row--missed">
							未按时服用
						</text>
						<text v-else class="time-row time-row--pending">等待服用</text>
					</view>

					<!-- 待服用：标记按钮 -->
					<view
						v-if="item.status === 'pending'"
						class="btn-mark"
						hover-class="btn-mark--hover"
						@tap="markAsTaken(item)"
					>
						<text class="btn-mark-text">标记已服用</text>
					</view>
				</view>
			</view>

			<!-- 无记录：空状态 -->
			<view v-else class="empty-state">
				<text class="empty-icon">📋</text>
				<text class="empty-text">{{ emptyTipText }}</text>
			</view>

			<view class="scroll-bottom-space"></view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getUserId } from '../utils/auth.js'
import { get, post } from '../utils/request.js'

const statusBarHeight = ref(20)

/** 筛选选项 */
const filterTabs = [
	{ key: 'today', label: '今日' },
	{ key: 'week', label: '本周' },
	{ key: 'month', label: '本月' }
]

/** 当前选中筛选，默认今日 */
const activeTab = ref('today')

/** 状态图标映射 */
const statusIconMap = {
	taken: '✓',
	pending: '🕐',
	missed: '!'
}

/** 记录数据 */
const recordList = ref([])

/** 当前展示列表（根据activeTab过滤） */
const displayList = computed(() => {
	return recordList.value.filter(item => {
		if (activeTab.value === 'today') {
			return item.dateType === 'today'
		} else if (activeTab.value === 'week') {
			return item.dateType === 'week'
		} else if (activeTab.value === 'month') {
			return item.dateType === 'month'
		}
		return true
	})
})

/** 空状态提示文案 */
const emptyTipText = computed(() => {
	const map = {
		today: '今日暂无服药记录',
		week: '本周暂无服药记录',
		month: '本月暂无服药记录'
	}
	return map[activeTab.value] || '暂无服药记录'
})

onLoad(() => {
	const sys = uni.getSystemInfoSync()
	statusBarHeight.value = sys.statusBarHeight || 20
})

/** 页面显示时加载数据 */
onShow(() => {
	loadRecordList()
})

/** 返回首页 Index */
function goBack() {
	const pages = getCurrentPages()
	// 上一页是首页时，直接返回
	if (pages.length >= 2) {
		const prevRoute = pages[pages.length - 2].route || ''
		if (prevRoute === 'pages/Index' || prevRoute.endsWith('/Index')) {
			uni.navigateBack({ delta: 1 })
			return
		}
	}
	// 无历史栈或上一页不是首页时，重新打开首页（H5/直接打开本页时适用）
	uni.reLaunch({
		url: '/pages/Index',
		fail: () => {
			uni.redirectTo({ url: '/pages/Index' })
		}
	})
}

/** 切换筛选栏 */
function switchTab(key) {
	activeTab.value = key
}

/** 获取时间范围 */
function getDateRange(type) {
	const now = new Date()
	const year = now.getFullYear()
	const month = now.getMonth() + 1
	const day = now.getDate()
	const weekDay = now.getDay() // 0-6，0表示周日

	let startTime, endTime

	if (type === 'today') {
		// 今日
		startTime = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} 00:00:00`
		endTime = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} 23:59:59`
	} else if (type === 'week') {
		// 本周：从周一到周日
		const mondayOffset = weekDay === 0 ? -6 : 1 - weekDay
		const monday = new Date(now)
		monday.setDate(day + mondayOffset)
		const sunday = new Date(monday)
		sunday.setDate(monday.getDate() + 6)

		startTime = `${monday.getFullYear()}-${String(monday.getMonth() + 1).padStart(2, '0')}-${String(monday.getDate()).padStart(2, '0')} 00:00:00`
		endTime = `${sunday.getFullYear()}-${String(sunday.getMonth() + 1).padStart(2, '0')}-${String(sunday.getDate()).padStart(2, '0')} 23:59:59`
	} else if (type === 'month') {
		// 本月
		startTime = `${year}-${String(month).padStart(2, '0')}-01 00:00:00`
		const lastDay = new Date(year, month, 0).getDate()
		endTime = `${year}-${String(month).padStart(2, '0')}-${String(lastDay).padStart(2, '0')} 23:59:59`
	}

	return { startTime, endTime }
}

/** 加载服药记录 */
async function loadRecordList() {
	const userId = getUserId()
	if (!userId) {
		uni.showToast({
			title: '用户信息异常，请重新登录',
			icon: 'none'
		})
		return
	}

	try {
		// 获取当前选择的时间范围
		const { startTime, endTime } = getDateRange(activeTab.value)

		// 调用获取服药记录接口：GET /api/medicineBox/record/list
		const list = await get('/record/list', {
			userId: userId,
			startTime: startTime,
			endTime: endTime
		}, { showLoading: true })

		// 处理返回数据，添加dateType标识用于筛选
		recordList.value = (list || []).map(item => ({
			...item,
			dateType: activeTab.value
		}))
	} catch (error) {
		console.error('加载服药记录失败:', error)
		// 错误已在request.js中统一处理
	}
}

/** 获取当前时间字符串 */
function getNowTimeStr() {
	const d = new Date()
	const pad = (n) => String(n).padStart(2, '0')
	return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

/** 标记已服用 */
async function markAsTaken(item) {
	const userId = getUserId()
	if (!userId) {
		uni.showToast({
			title: '用户信息异常',
			icon: 'none'
		})
		return
	}

	try {
		// 调用标记服药接口：POST /api/medicineBox/record/add
		await post('/record/add', {
			userId: userId,
			medicineId: item.medicineId,
			planTime: item.planTime,
			actualTime: getNowTimeStr()
		}, { showLoading: true })

		uni.showToast({
			title: '已标记服用',
			icon: 'success'
		})

		// 刷新列表
		setTimeout(() => {
			loadRecordList()
		}, 1500)
	} catch (error) {
		console.error('标记服用失败:', error)
		// 错误已在request.js中统一处理
	}
}
</script>

<style scoped>
.record-page {
	min-height: 100vh;
	display: flex;
	flex-direction: column;
	background: linear-gradient(180deg, #e8f5fc 0%, #f5fafd 160rpx, #f8fbfd 100%);
}

/* ========== 导航栏 ========== */
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

/* ========== 筛选栏 ========== */
.filter-bar {
	display: flex;
	align-items: center;
	justify-content: space-around;
	background: #ffffff;
	padding: 0 32rpx;
	box-shadow: 0 2rpx 8rpx rgba(26, 74, 110, 0.06);
	flex-shrink: 0;
}

.filter-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 28rpx 0 20rpx;
	position: relative;
}

.filter-text {
	font-size: 30rpx;
	color: #888888;
	font-weight: 500;
}

.filter-item--active .filter-text {
	color: #1a4a6e;
	font-weight: 700;
	font-size: 32rpx;
}

.filter-line {
	width: 48rpx;
	height: 6rpx;
	background: #6ec1e8;
	border-radius: 3rpx;
	margin-top: 12rpx;
}

/* ========== 列表区域 ========== */
.page-scroll {
	flex: 1;
	height: 0;
	padding: 24rpx 32rpx 0;
	box-sizing: border-box;
}

.record-list {
	padding-bottom: 16rpx;
}

.record-card {
	display: flex;
	align-items: flex-start;
	background: #ffffff;
	border-radius: 20rpx;
	padding: 28rpx 24rpx;
	margin-bottom: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(26, 74, 110, 0.08);
}

/* 状态图标 */
.status-icon {
	width: 72rpx;
	height: 72rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
	margin-right: 20rpx;
}

.status-icon--taken {
	background: #e8f8ef;
	border: 2rpx solid #52c41a;
}

.status-icon--taken .status-symbol {
	color: #52c41a;
	font-size: 36rpx;
	font-weight: 700;
}

.status-icon--pending {
	background: #f5f5f5;
	border: 2rpx solid #cccccc;
}

.status-icon--pending .status-symbol {
	font-size: 32rpx;
}

.status-icon--missed {
	background: #fff1f0;
	border: 2rpx solid #ff4d4f;
}

.status-icon--missed .status-symbol {
	color: #ff4d4f;
	font-size: 36rpx;
	font-weight: 700;
}

.record-info {
	flex: 1;
	min-width: 0;
	padding-top: 4rpx;
}

.medicine-name {
	font-size: 32rpx;
	font-weight: 600;
	color: #1a1a1a;
	display: block;
	margin-bottom: 12rpx;
}

.time-row {
	font-size: 26rpx;
	color: #888888;
	line-height: 1.6;
	display: block;
}

.time-row--actual {
	color: #52c41a;
}

.time-row--missed {
	color: #ff4d4f;
}

.time-row--pending {
	color: #999999;
}

/* 标记已服用按钮 */
.btn-mark {
	flex-shrink: 0;
	margin-left: 12rpx;
	padding: 14rpx 20rpx;
	background: #6ec1e8;
	border-radius: 32rpx;
	align-self: center;
}

.btn-mark--hover {
	opacity: 0.85;
	transform: scale(0.96);
}

.btn-mark-text {
	font-size: 24rpx;
	color: #ffffff;
	font-weight: 500;
	white-space: nowrap;
}

/* ========== 空状态 ========== */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 120rpx 32rpx;
}

.empty-icon {
	font-size: 96rpx;
	margin-bottom: 24rpx;
	opacity: 0.5;
}

.empty-text {
	font-size: 30rpx;
	color: #999999;
}

.scroll-bottom-space {
	height: 40rpx;
}
</style>
