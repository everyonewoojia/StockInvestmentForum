<template>
	<view class="medicine-list-page">
		<view class="page-header">
			<text class="page-title">我的用药</text>
		</view>

		<view
			v-for="item in medicineList"
			:key="item.medicineId"
			class="medicine-card"
		>
			<view class="card-info">
				<text class="medicine-name">{{ item.medicineName }}</text>
				<text class="medicine-meta">{{ item.dailyTimes }} · {{ item.dose }}</text>
			</view>
			<view class="btn-actions">
				<view class="btn-edit" @tap="goReminderSet(item)">
					<text class="btn-edit-text">编辑提醒</text>
				</view>
				<view class="btn-delete" @tap="handleDelete(item)">
					<text class="btn-delete-text">删除</text>
				</view>
			</view>
		</view>

		<!-- 空状态 -->
		<view v-if="medicineList.length === 0" class="empty-state">
			<text class="empty-icon">💊</text>
			<text class="empty-text">暂无用药记录</text>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getUserId } from '../utils/auth.js'
import { get, del } from '../utils/request.js'

/** 药品列表 */
const medicineList = ref([])

/** 页面显示时加载数据 */
onShow(() => {
	loadMedicineList()
})

/** 加载药品列表 */
async function loadMedicineList() {
	const userId = getUserId()
	if (!userId) {
		uni.showToast({
			title: '用户信息异常，请重新登录',
			icon: 'none'
		})
		return
	}

	try {
		// 调用获取药品列表接口：GET /api/medicineBox/medicine/list
		const [list, reminders] = await Promise.all([
			get('/medicine/list', { userId }, { showLoading: true }),
			get('/reminder/list', { userId }, { showLoading: false })
		])
		const reminderMap = (reminders || []).reduce((map, reminder) => {
			const key = String(reminder.medicineId)
			if (!map[key]) {
				map[key] = reminder
			}
			if (reminder.status === 'enable') {
				map[key] = reminder
			}
			return map
		}, {})

		medicineList.value = (list || []).map(item => ({
			...item,
			reminderId: reminderMap[String(item.medicineId)]?.reminderId || ''
		}))
	} catch (error) {
		console.error('加载药品列表失败:', error)
		// 错误已在request.js中统一处理
	}
}

/** 跳转设置服药提醒页 */
function goReminderSet(item) {
	const name = encodeURIComponent(item.medicineName)
	const reminderId = item.reminderId ? `&reminderId=${item.reminderId}` : ''
	const dailyTimes = item.dailyTimes ? `&dailyTimes=${item.dailyTimes}` : ''
	uni.navigateTo({
		url: `/pages/ReminderSet?medicineName=${name}&medicineId=${item.medicineId}${dailyTimes}${reminderId}&from=medicineList`
	})
}

/** 删除药品 */
function handleDelete(item) {
	uni.showModal({
		title: '提示',
		content: `确定要删除"${item.medicineName}"吗？`,
		success: async (res) => {
			if (res.confirm) {
				await deleteMedicine(item)
			}
		}
	})
}

/** 执行删除操作 */
async function deleteMedicine(item) {
	try {
		// 调用删除药品接口：DELETE /api/medicineBox/medicine/delete
		await del('/medicine/delete', { medicineId: item.medicineId }, { showLoading: true })

		uni.showToast({
			title: '删除成功',
			icon: 'success',
			duration: 1500
		})

		// 刷新列表
		setTimeout(() => {
			loadMedicineList()
		}, 1500)
	} catch (error) {
		console.error('删除药品失败:', error)
		// 错误已在request.js中统一处理
	}
}
</script>

<style scoped>
.medicine-list-page {
	min-height: 100vh;
	background: linear-gradient(180deg, #e8f5fc 0%, #f5fafd 200rpx, #f8fbfd 100%);
	padding: 24rpx 32rpx;
	box-sizing: border-box;
}

.page-header {
	padding: 16rpx 0 32rpx;
}

.page-title {
	font-size: 40rpx;
	font-weight: 700;
	color: #1a4a6e;
}

.medicine-card {
	display: flex;
	align-items: center;
	justify-content: space-between;
	background: #ffffff;
	border-radius: 20rpx;
	padding: 28rpx 32rpx;
	margin-bottom: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(26, 74, 110, 0.08);
}

.card-info {
	flex: 1;
	min-width: 0;
	padding-right: 24rpx;
}

.medicine-name {
	font-size: 32rpx;
	font-weight: 600;
	color: #1a1a1a;
	display: block;
	margin-bottom: 8rpx;
}

.medicine-meta {
	font-size: 26rpx;
	color: #888888;
}

.btn-actions {
	display: flex;
	gap: 12rpx;
	flex-shrink: 0;
}

.btn-edit {
	flex-shrink: 0;
	padding: 12rpx 24rpx;
	background: #eef8fc;
	border-radius: 32rpx;
	border: 2rpx solid #6ec1e8;
}

.btn-edit-text {
	font-size: 26rpx;
	color: #6ec1e8;
	font-weight: 500;
}

.btn-delete {
	flex-shrink: 0;
	padding: 12rpx 24rpx;
	background: #fff1f0;
	border-radius: 32rpx;
	border: 2rpx solid #ff6b6b;
}

.btn-delete-text {
	font-size: 26rpx;
	color: #ff6b6b;
	font-weight: 500;
}

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
</style>
