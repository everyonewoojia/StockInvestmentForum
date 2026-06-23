<template>
	<view class="ocr-page">
		<!-- 顶部返回导航栏 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-back" @tap="goBack">
				<text class="nav-back-icon">‹</text>
			</view>
			<text class="nav-title">处方单识别</text>
			<view class="nav-placeholder"></view>
		</view>

		<scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
			<!-- 第一部分：图片上传区域 -->
			<view class="section upload-section">
				<view class="upload-box" @tap="chooseImage">
					<!-- 未选图：虚线框 + 提示 -->
					<view v-if="!previewImage" class="upload-placeholder">
						<text class="camera-icon">📷</text>
						<text class="upload-tip">点击拍照或从相册选择</text>
						<text class="upload-subtip">上传处方单图片</text>
					</view>
					<!-- 已选图：预览 -->
					<image
						v-else
						class="preview-image"
						:src="previewImage"
						mode="aspectFit"
					/>
				</view>
			</view>

			<!-- 第二部分：橙色识别按钮（仅 UI，无识别逻辑） -->
			<view class="section">
				<view class="btn-recognize" @tap="handleRecognize">
					<text class="btn-recognize-text">识别处方信息</text>
				</view>
			</view>

			<!-- 第三部分：药品信息表单（仅展示） -->
			<view class="section form-section">
				<text class="form-title">药品信息</text>

				<view class="form-item">
					<text class="form-label">药品名称</text>
					<input
						class="form-input"
						type="text"
						v-model="formData.medicineName"
						placeholder="请输入药品名称"
						placeholder-class="input-placeholder"
					/>
				</view>

				<view class="form-item">
					<text class="form-label">每日服用次数</text>
					<input
						class="form-input"
						type="number"
						v-model="formData.dailyTimes"
						placeholder="如：3"
						placeholder-class="input-placeholder"
					/>
				</view>

				<view class="form-item">
					<text class="form-label">每次服用剂量</text>
					<input
						class="form-input"
						type="text"
						v-model="formData.dose"
						placeholder="如：每次1片"
						placeholder-class="input-placeholder"
					/>
				</view>

				<view class="form-item">
					<text class="form-label">服用时段</text>
					<picker
						mode="selector"
						:range="takeTimeOptions"
						:value="takeTimeIndex"
						@change="onTakeTimeChange"
					>
						<view class="form-picker">
							<text class="picker-text">{{ takeTimeOptions[takeTimeIndex] }}</text>
							<text class="picker-arrow">▼</text>
						</view>
					</picker>
				</view>

				<view class="form-item">
					<text class="form-label">服药周期（天）</text>
					<input
						class="form-input"
						type="number"
						v-model="formData.cycle"
						placeholder="如：7"
						placeholder-class="input-placeholder"
					/>
				</view>
			</view>

			<!-- 底部留白，避免被固定按钮遮挡 -->
			<view class="scroll-bottom-space"></view>
		</scroll-view>

		<!-- 第四部分：底部全屏绿色保存按钮 -->
		<view class="footer-bar">
			<view class="btn-save" @tap="handleSave">
				<text class="btn-save-text">保存用药信息</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getUserId } from '../utils/auth.js'
import { BASE_URL, post } from '../utils/request.js'

const statusBarHeight = ref(20)
/** 图片预览路径 */
const previewImage = ref('')
/** 上传后的图片URL（服务器地址） */
const uploadedImageUrl = ref('')

/** 服用时段选项 */
const takeTimeOptions = ['饭前', '饭后']
const takeTimeIndex = ref(1)

/** 表单数据 */
const formData = ref({
	medicineName: '',
	dailyTimes: '',
	dose: '',
	takeTime: '饭后',
	cycle: ''
})

/** 保存的medicineId，用于跳转提醒设置页 */
const savedMedicineId = ref('')

onLoad(() => {
	const sys = uni.getSystemInfoSync()
	statusBarHeight.value = sys.statusBarHeight || 20
})

/** 返回首页 */
function goBack() {
	uni.navigateBack({
		fail: () => {
			uni.redirectTo({ url: '/pages/Index' })
		}
	})
}

/** 选择图片（相册/拍照）并上传 */
function chooseImage() {
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: (res) => {
			previewImage.value = res.tempFilePaths[0]
			// 上传图片到服务器
			uploadImage(res.tempFilePaths[0])
		}
	})
}

/**
 * 上传图片到服务器
 * @param {string} filePath - 本地图片路径
 */
function uploadImage(filePath) {
	uni.showLoading({
		title: '上传中...',
		mask: true
	})

	const token = uni.getStorageSync('token')

	uni.uploadFile({
		url: `${BASE_URL}/ocr/upload`, // 假设有一个上传图片的接口
		filePath: filePath,
		name: 'file',
		header: {
			'Authorization': `Bearer ${token}`
		},
		success: (uploadRes) => {
			try {
				const result = JSON.parse(uploadRes.data)
				if (result.code === 200) {
					uploadedImageUrl.value = result.data.imageUrl
					uni.showToast({
						title: '上传成功',
						icon: 'success'
					})
				} else {
					uni.showToast({
						title: result.msg || '上传失败',
						icon: 'none'
					})
				}
			} catch (e) {
				console.error('解析上传结果失败:', e)
			}
		},
		fail: (err) => {
			uni.showToast({
				title: '上传失败，请重试',
				icon: 'none'
			})
			console.error('上传失败:', err)
		},
		complete: () => {
			uni.hideLoading()
		}
	})
}

/** 识别按钮：调用OCR识别接口 */
async function handleRecognize() {
	// 检查是否已上传图片
	if (!previewImage.value) {
		uni.showToast({
			title: '请先上传处方单图片',
			icon: 'none'
		})
		return
	}

	// 获取userId
	const userId = getUserId()
	if (!userId) {
		uni.showToast({
			title: '用户信息异常，请重新登录',
			icon: 'none'
		})
		return
	}

	try {
		// 调用OCR识别接口：POST /api/medicineBox/ocr/recognize
		const result = await post('/ocr/recognize', {
			userId: userId,
			imageUrl: uploadedImageUrl.value || previewImage.value
		}, { showLoading: true })

		// 识别成功，回填表单数据
		if (result) {
			formData.value.medicineName = result.medicineName || ''
			formData.value.dailyTimes = result.dailyTimes || ''
			formData.value.dose = result.dose || ''
			formData.value.takeTime = result.takeTime || '饭后'
			formData.value.cycle = result.cycle || ''

			// 更新服用时段索引
			const index = takeTimeOptions.indexOf(formData.value.takeTime)
			if (index !== -1) {
				takeTimeIndex.value = index
			}

			uni.showToast({
				title: '识别成功',
				icon: 'success'
			})
		}
	} catch (error) {
		console.error('OCR识别失败:', error)
		// 错误已在request.js中统一处理
	}
}

/** 服用时段改变 */
function onTakeTimeChange(e) {
	takeTimeIndex.value = e.detail.value
	formData.value.takeTime = takeTimeOptions[takeTimeIndex.value]
}

/** 保存用药信息 */
async function handleSave() {
	// 表单验证
	if (!formData.value.medicineName) {
		uni.showToast({
			title: '请输入药品名称',
			icon: 'none'
		})
		return
	}

	if (!formData.value.dailyTimes) {
		uni.showToast({
			title: '请输入每日服用次数',
			icon: 'none'
		})
		return
	}

	if (!formData.value.dose) {
		uni.showToast({
			title: '请输入每次服用剂量',
			icon: 'none'
		})
		return
	}

	if (!formData.value.cycle) {
		uni.showToast({
			title: '请输入服药周期',
			icon: 'none'
		})
		return
	}

	// 获取userId
	const userId = getUserId()
	if (!userId) {
		uni.showToast({
			title: '用户信息异常，请重新登录',
			icon: 'none'
		})
		return
	}

	try {
		// 调用保存用药信息接口：POST /api/medicineBox/ocr/save
		const result = await post('/ocr/save', {
			userId: userId,
			medicineName: formData.value.medicineName,
			dailyTimes: Number(formData.value.dailyTimes),
			dose: formData.value.dose,
			takeTime: formData.value.takeTime,
			cycle: Number(formData.value.cycle)
		}, { showLoading: true })

		// 保存成功，接收返回的medicineId
		if (result && result.medicineId) {
			savedMedicineId.value = result.medicineId

			uni.showToast({
				title: '保存成功',
				icon: 'success',
				duration: 1500
			})

			setTimeout(() => {
				// 携带药品信息跳转设置服药提醒页面
				const medicineName = encodeURIComponent(formData.value.medicineName)
				const dailyTimes = Number(formData.value.dailyTimes) || 1
				uni.navigateTo({
					url: `/pages/ReminderSet?medicineId=${result.medicineId}&medicineName=${medicineName}&dailyTimes=${dailyTimes}&from=ocr`
				})
			}, 1500)
		}
	} catch (error) {
		console.error('保存用药信息失败:', error)
		// 错误已在request.js中统一处理
	}
}
</script>

<style scoped>
.ocr-page {
	min-height: 100vh;
	display: flex;
	flex-direction: column;
	background: linear-gradient(180deg, #e8f5fc 0%, #f5fafd 200rpx, #f8fbfd 100%);
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

/* ========== 滚动内容区 ========== */
.page-scroll {
	flex: 1;
	height: 0;
	padding: 24rpx 32rpx 0;
	box-sizing: border-box;
}

.section {
	margin-bottom: 32rpx;
}

/* ========== 图片上传区域 ========== */
.upload-box {
	width: 100%;
	min-height: 360rpx;
	border: 4rpx dashed #9ed4eb;
	border-radius: 20rpx;
	background: #ffffff;
	overflow: hidden;
	display: flex;
	align-items: center;
	justify-content: center;
	box-sizing: border-box;
}

.upload-placeholder {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 48rpx 32rpx;
}

.camera-icon {
	font-size: 80rpx;
	line-height: 1;
	margin-bottom: 24rpx;
}

.upload-tip {
	font-size: 32rpx;
	font-weight: 600;
	color: #2a6a8a;
	margin-bottom: 12rpx;
}

.upload-subtip {
	font-size: 26rpx;
	color: #888888;
}

.preview-image {
	width: 100%;
	min-height: 360rpx;
	max-height: 480rpx;
}

/* ========== 橙色识别按钮 ========== */
.btn-recognize {
	width: 100%;
	height: 96rpx;
	background: linear-gradient(180deg, #ff9f43 0%, #f57c00 100%);
	border-radius: 16rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8rpx 20rpx rgba(245, 124, 0, 0.25);
}

.btn-recognize-text {
	font-size: 34rpx;
	font-weight: 600;
	color: #ffffff;
}

/* ========== 表单区域 ========== */
.form-section {
	background: #ffffff;
	border-radius: 20rpx;
	padding: 32rpx 28rpx 16rpx;
	box-shadow: 0 4rpx 16rpx rgba(26, 74, 110, 0.06);
}

.form-title {
	font-size: 34rpx;
	font-weight: 700;
	color: #1a4a6e;
	margin-bottom: 28rpx;
	display: block;
}

.form-item {
	margin-bottom: 28rpx;
}

.form-label {
	font-size: 30rpx;
	font-weight: 500;
	color: #333333;
	margin-bottom: 16rpx;
	display: block;
}

.form-input {
	width: 100%;
	height: 88rpx;
	padding: 0 24rpx;
	font-size: 30rpx;
	color: #333333;
	background: #f5fafd;
	border-radius: 12rpx;
	border: 2rpx solid #e0eef5;
	box-sizing: border-box;
}

.input-placeholder {
	color: #aaaaaa;
	font-size: 28rpx;
}

.form-picker {
	display: flex;
	align-items: center;
	justify-content: space-between;
	height: 88rpx;
	padding: 0 24rpx;
	background: #f5fafd;
	border-radius: 12rpx;
	border: 2rpx solid #e0eef5;
	box-sizing: border-box;
}

.picker-text {
	font-size: 30rpx;
	color: #333333;
}

.picker-arrow {
	font-size: 24rpx;
	color: #6ec1e8;
}

.scroll-bottom-space {
	height: 160rpx;
}

/* ========== 底部绿色保存按钮 ========== */
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
	background: linear-gradient(180deg, #52c41a 0%, #389e0d 100%);
	border-radius: 16rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8rpx 20rpx rgba(56, 158, 13, 0.25);
}

.btn-save-text {
	font-size: 34rpx;
	font-weight: 600;
	color: #ffffff;
}
</style>
