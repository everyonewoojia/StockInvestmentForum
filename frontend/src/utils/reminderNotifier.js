import { getUserId, isLoggedIn } from './auth.js'
import { get } from './request.js'

const CHECK_INTERVAL = 30 * 1000
const DUE_WINDOW = 5 * 60 * 1000
const STORAGE_KEY = 'medicineReminderNotifiedKeys'

let timer = null
let checking = false
let modalVisible = false

export function startReminderNotifier() {
	if (timer) {
		return
	}
	checkMedicationReminder()
	timer = setInterval(checkMedicationReminder, CHECK_INTERVAL)
}

export function stopReminderNotifier() {
	if (!timer) {
		return
	}
	clearInterval(timer)
	timer = null
}

export async function checkMedicationReminder() {
	if (checking || modalVisible || !isLoggedIn()) {
		return
	}

	const userId = getUserId()
	if (!userId) {
		return
	}

	checking = true
	try {
		const now = new Date()
		const start = new Date(now.getTime() - DUE_WINDOW)
		const records = await get('/record/list', {
			userId,
			startTime: formatDateTime(start),
			endTime: formatDateTime(now)
		}, { showLoading: false })

		const dueRecords = (records || [])
			.filter(item => isDueRecord(item, now))
			.filter(item => !hasNotified(buildNotifyKey(item)))

		if (dueRecords.length) {
			showReminder(dueRecords)
		}
	} catch (error) {
		console.warn('检查服药提醒失败:', error)
	} finally {
		checking = false
	}
}

function isDueRecord(item, now) {
	if (!item || item.status !== 'pending' || !item.planTime) {
		return false
	}
	const planTime = parseDateTime(item.planTime)
	if (!planTime) {
		return false
	}
	const diff = now.getTime() - planTime.getTime()
	return diff >= 0 && diff <= DUE_WINDOW
}

function showReminder(records) {
	const keys = records.map(buildNotifyKey)
	saveNotifiedKeys(keys)

	const names = [...new Set(records.map(item => item.medicineName).filter(Boolean))]
	const content = names.length === 1
		? `现在该服用 ${names[0]} 了`
		: `现在有 ${records.length} 次服药待处理`

	modalVisible = true
	if (typeof uni.vibrateShort === 'function') {
		uni.vibrateShort()
	}

	uni.showModal({
		title: '服药提醒',
		content,
		confirmText: '去记录',
		cancelText: '稍后',
		success: (res) => {
			if (res.confirm) {
				uni.navigateTo({
					url: '/pages/TakeRecord'
				})
			}
		},
		complete: () => {
			modalVisible = false
		}
	})
}

function buildNotifyKey(item) {
	return `${item.medicineId || ''}|${item.planTime || ''}`
}

function hasNotified(key) {
	return getNotifiedKeys().includes(key)
}

function saveNotifiedKeys(keys) {
	const today = formatDate(new Date())
	const oldKeys = getNotifiedKeys().filter(key => key.includes(today))
	const nextKeys = [...new Set([...oldKeys, ...keys])]
	uni.setStorageSync(STORAGE_KEY, nextKeys)
}

function getNotifiedKeys() {
	try {
		const keys = uni.getStorageSync(STORAGE_KEY)
		return Array.isArray(keys) ? keys : []
	} catch (error) {
		return []
	}
}

function parseDateTime(value) {
	if (!value) {
		return null
	}
	const normalized = String(value).replace(/-/g, '/')
	const date = new Date(normalized)
	return Number.isNaN(date.getTime()) ? null : date
}

function formatDateTime(date) {
	return `${formatDate(date)} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function formatDate(date) {
	return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function pad(value) {
	return String(value).padStart(2, '0')
}
