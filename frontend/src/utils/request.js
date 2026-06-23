let BASE_URL = '/api/forum'

// #ifdef MP-WEIXIN
BASE_URL = 'http://127.0.0.1:8080/api/forum'
// #endif

export { BASE_URL }

export function request(options) {
	const {
		url,
		method = 'GET',
		data = {},
		needToken = true,
		showLoading = false
	} = options

	if (showLoading) {
		uni.showLoading({ title: '处理中...', mask: true })
	}

	return new Promise((resolve, reject) => {
		const header = { 'Content-Type': 'application/json' }
		if (needToken) {
			const token = uni.getStorageSync('token')
			if (token) {
				header.Authorization = `Bearer ${token}`
			}
		}

		uni.request({
			url: BASE_URL + url,
			method,
			data,
			header,
			success: (res) => {
				if (showLoading) uni.hideLoading()
				const body = res.data || {}
				if (body.code === 200) {
					resolve(body.data)
					return
				}
				if (body.code === 401) {
					clearLoginInfo()
				}
				const message = body.msg || '请求失败'
				uni.showToast({ title: message, icon: 'none' })
				reject(new Error(message))
			},
			fail: (err) => {
				if (showLoading) uni.hideLoading()
				uni.showToast({ title: '网络连接失败', icon: 'none' })
				reject(err)
			}
		})
	})
}

function clearLoginInfo() {
	uni.removeStorageSync('userId')
	uni.removeStorageSync('nickName')
	uni.removeStorageSync('avatarUrl')
	uni.removeStorageSync('role')
	uni.removeStorageSync('verificationLevel')
	uni.removeStorageSync('token')
}

export function get(url, data = {}, options = {}) {
	return request({ url, method: 'GET', data, ...options })
}

export function post(url, data = {}, options = {}) {
	return request({ url, method: 'POST', data, ...options })
}

export function put(url, data = {}, options = {}) {
	return request({ url, method: 'PUT', data, ...options })
}

export function del(url, data = {}, options = {}) {
	return request({ url, method: 'DELETE', data, ...options })
}
