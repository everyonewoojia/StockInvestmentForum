<template>
	<view class="reminder-page">
		<!-- 顶部导航栏 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-back" @tap="goBack">
				<text class="nav-back-icon">‹</text>
			</view>
			<text class="nav-title">设置服药提醒</text>
			<view class="nav-placeholder"></view>
		</view>

		<scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
			<!-- 当前药品 -->
			<view class="card medicine-card">
				<text class="card-label">当前药品</text>
				<text class="medicine-name">{{ medicineName }}</text>
			</view>

			<!-- 服药时间列表 -->
			<view class="card">
				<view class="card-header">
					<text class="card-title">服药时间</text>
					<view class="btn-add-time" @tap="addRemindTime">
						<text class="btn-add-text">+ 添加时间</text>
					</view>
				</view>

				<view
					v-for="(time, index) in remindTimes"
					:key="index"
					class="time-row"
				>
					<picker mode="time" :value="time" @change="(e) => onTimeChange(index, e)">
						<view class="time-picker">
							<text class="time-icon">🕐</text>
							<text class="time-value">{{ time }}</text>
							<text class="time-arrow">▼</text>
						</view>
					</picker>
					<view
						v-if="remindTimes.length > 1"
						class="btn-remove"
						@tap="removeRemindTime(index)"
					>
						<text class="btn-remove-text">删除</text>
					</view>
				</view>
			</view>

			<!-- 重复频率 -->
			<view class="card">
				<text class="card-title">重复频率</text>
				<view class="repeat-options">
					<view
						class="repeat-item"
						:class="{ 'repeat-item--active': repeatType === 'daily' }"
						@tap="repeatType = 'daily'"
					>
						<text class="repeat-text">每天</text>
					</view>
					<view
						class="repeat-item"
						:class="{ 'repeat-item--active': repeatType === 'weekday' }"
						@tap="repeatType = 'weekday'"
					>
						<text class="repeat-text">工作日</text>
					</view>
				</view>
			</view>

			<view class="scroll-bottom-space"></view>
		</scroll-view>

		<!-- 底部保存按钮 -->
		<view class="footer-bar">
			<view class="btn-save" @tap="handleSave">
				<text class="btn-save-text">保存提醒</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getUserId } from '../utils/auth.js'
import { post, get } from '../utils/request.js'

const statusBarHeight = ref(20)
/** 药品ID（由上一页传入） */
const medicineId = ref('')
/** 药品名称（由上一页传入，无则使用默认展示） */
const medicineName = ref('未选择药品')
/** 每日服用次数（由上一页传入，用于生成默认提醒时间） */
const dailyTimes = ref(1)
/** 服药时间列表 */
const remindTimes = ref(buildDefaultTimes(1))
/** 重复类型：daily-每天 / weekday-工作日 */
const repeatType = ref('daily')
/** 提醒ID（编辑时使用） */
const reminderId = ref('')
/** 来源页面：ocr/medicineList */
const fromPage = ref('ocr')

onLoad((options) => {
	const sys = uni.getSystemInfoSync()
	statusBarHeight.value = sys.statusBarHeight || 20

	// 获取传递的参数
	if (options.medicineName) {
		medicineName.value = decodeURIComponent(options.medicineName)
	}
	if (options.medicineId) {
		medicineId.value = options.medicineId
	}
	if (options.dailyTimes) {
		dailyTimes.value = normalizeDailyTimes(options.dailyTimes)
		remindTimes.value = buildDefaultTimes(dailyTimes.value)
	}
	if (options.from) {
		fromPage.value = options.from
	}
	if (options.reminderId) {
		reminderId.value = options.reminderId
		// 如果有reminderId，说明是编辑模式，需要加载提醒详情
		loadReminderDetail()
	}
})

/** 返回上一页 */
function goBack() {
	uni.navigateBack({
		fail: () => {
			uni.redirectTo({ url: '/pages/Index' })
		}
	})
}

/** 添加服药时间 */
function addRemindTime() {
	const nextTimes = buildDefaultTimes(remindTimes.value.length + 1)
	remindTimes.value.push(nextTimes[nextTimes.length - 1] || '08:00')
}

/** 删除服药时间 */
function removeRemindTime(index) {
	if (remindTimes.value.length > 1) {
		remindTimes.value.splice(index, 1)
	}
}

/** 修改某个时间点 */
function onTimeChange(index, e) {
	remindTimes.value[index] = e.detail.value
}

/** 加载提醒详情（编辑模式） */
async function loadReminderDetail() {
	try {
		// 获取用户所有提醒列表，找到对应的提醒
		const userId = getUserId()
		if (!userId) {
			uni.showToast({
				title: '用户信息异常',
				icon: 'none'
			})
			return
		}

		const list = await get('/reminder/list', { userId }, { showLoading: true })
		
		// 找到对应的提醒
		const reminder = list.find(item => item.reminderId === reminderId.value)
		if (reminder) {
			remindTimes.value = reminder.remindTimes && reminder.remindTimes.length
				? reminder.remindTimes
				: buildDefaultTimes(dailyTimes.value)
			repeatType.value = reminder.repeatType || 'daily'
			medicineName.value = reminder.medicineName || medicineName.value
		}
	} catch (error) {
		console.error('加载提醒详情失败:', error)
	}
}

/** 保存提醒 */
async function handleSave() {
	// 验证数据
	if (!medicineId.value) {
		uni.showToast({
			title: '药品信息异常',
			icon: 'none'
		})
		return
	}

	const userId = getUserId()
	if (!userId) {
		uni.showToast({
			title: '用户信息异常，请重新登录',
			icon: 'none'
		})
		return
	}

	if (remindTimes.value.length === 0) {
		uni.showToast({
			title: '请至少添加一个服药时间',
			icon: 'none'
		})
		return
	}

	try {
		// 判断是新增还是修改
		if (reminderId.value) {
			// 修改提醒：POST /api/medicineBox/reminder/update
			await post('/reminder/update', {
				reminderId: reminderId.value,
				remindTimes: remindTimes.value,
				repeatType: repeatType.value,
				status: 'enable'
			}, { showLoading: true })

			uni.showToast({
				title: '修改成功',
				icon: 'success',
				duration: 1500
			})
		} else {
			// 新增提醒：POST /api/medicineBox/reminder/add
			await post('/reminder/add', {
				userId: userId,
				medicineId: medicineId.value,
				remindTimes: remindTimes.value,
				repeatType: repeatType.value,
				status: 'enable'
			}, { showLoading: true })

			uni.showToast({
				title: '保存成功',
				icon: 'success',
				duration: 1500
			})
		}

		setTimeout(() => {
			// 跳转回我的用药页面
			uni.navigateBack()
		}, 1500)
	} catch (error) {
		console.error('保存提醒失败:', error)
		// 错误已在request.js中统一处理
	}
}

function normalizeDailyTimes(value) {
	const times = Number(value)
	if (!Number.isFinite(times) || times < 1) {
		return 1
	}
	return Math.min(Math.floor(times), 8)
}

function buildDefaultTimes(times) {
	const count = normalizeDailyTimes(times)
	const presets = {
		1: ['08:00'],
		2: ['08:00', '20:00'],
		3: ['08:00', '12:00', '20:00'],
		4: ['08:00', '12:00', '18:00', '22:00']
	}
	if (presets[count]) {
		return [...presets[count]]
	}

	const result = []
	const startMinutes = 8 * 60
	const endMinutes = 22 * 60
	const step = (endMinutes - startMinutes) / (count - 1)
	for (let index = 0; index < count; index += 1) {
		const totalMinutes = Math.round((startMinutes + step * index) / 5) * 5
		const hour = Math.floor(totalMinutes / 60)
		const minute = totalMinutes % 60
		result.push(`${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`)
	}
	return result
}
</script>

<style scoped>
.reminder-page {
	min-height: 100vh;
	display: flex;
	flex-direction: column;
	background: linear-gradient(180deg, #e8f5fc 0%, #f5fafd 200rpx, #f8fbfd 100%);
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

.page-scroll {
	flex: 1;
	height: 0;
	padding: 24rpx 32rpx 0;
	box-sizing: border-box;
}

/* 卡片通用 */
.card {
	background: #ffffff;
	border-radius: 20rpx;
	padding: 32rpx 28rpx;
	margin-bottom: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(26, 74, 110, 0.08);
}

.medicine-card {
	padding: 36rpx 28rpx;
}

.card-label {
	font-size: 26rpx;
	color: #888888;
	display: block;
	margin-bottom: 12rpx;
}

.medicine-name {
	font-size: 36rpx;
	font-weight: 700;
	color: #1a4a6e;
	line-height: 1.4;
}

.card-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 24rpx;
}

.card-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #1a4a6e;
}

.btn-add-time {
	padding: 8rpx 20rpx;
	background: #eef8fc;
	border-radius: 32rpx;
	border: 2rpx solid #6ec1e8;
}

.btn-add-text {
	font-size: 26rpx;
	color: #6ec1e8;
	font-weight: 500;
}

.time-row {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.time-row:last-child {
	margin-bottom: 0;
}

.time-picker {
	flex: 1;
	display: flex;
	align-items: center;
	height: 88rpx;
	padding: 0 24rpx;
	background: #f5fafd;
	border-radius: 12rpx;
	border: 2rpx solid #e0eef5;
	box-sizing: border-box;
}

.time-icon {
	font-size: 32rpx;
	margin-right: 16rpx;
}

.time-value {
	flex: 1;
	font-size: 32rpx;
	font-weight: 500;
	color: #333333;
}

.time-arrow {
	font-size: 22rpx;
	color: #6ec1e8;
}

.btn-remove {
	margin-left: 16rpx;
	padding: 16rpx 20rpx;
	flex-shrink: 0;
}

.btn-remove-text {
	font-size: 26rpx;
	color: #ff6b6b;
}

/* 重复频率 */
.repeat-options {
	display: flex;
	gap: 24rpx;
	margin-top: 24rpx;
}

.repeat-item {
	flex: 1;
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: #f5fafd;
	border-radius: 12rpx;
	border: 2rpx solid #e0eef5;
}

.repeat-item--active {
	background: #6ec1e8;
	border-color: #6ec1e8;
	box-shadow: 0 4rpx 12rpx rgba(110, 193, 232, 0.3);
}

.repeat-text {
	font-size: 30rpx;
	color: #2a6a8a;
	font-weight: 500;
}

.repeat-item--active .repeat-text {
	color: #ffffff;
	font-weight: 600;
}

.scroll-bottom-space {
	height: 160rpx;
}

/* 底部保存 */
.footer-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	padding: 20rpx 32rpx;
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
	background: #ffffff;
	box-shadow: 0 -4rpx 16rpx rgba(26, 74, 110, 0.06);
	z-index: 10;
}

.btn-save {
	width: 100%;
	height: 96rpx;
	background: linear-gradient(180deg, #6ec1e8 0%, #4a9fd4 100%);
	border-radius: 16rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8rpx 20rpx rgba(110, 193, 232, 0.35);
}

.btn-save-text {
	font-size: 34rpx;
	font-weight: 600;
	color: #ffffff;
}
</style>
