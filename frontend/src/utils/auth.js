export function isLoggedIn() {
	return !!getToken()
}

export function saveUserInfo(loginData) {
	uni.setStorageSync('userId', loginData.userId || '')
	uni.setStorageSync('nickName', loginData.nickName || '投资者')
	uni.setStorageSync('avatarUrl', loginData.avatarUrl || '')
	uni.setStorageSync('role', loginData.role || 'USER')
	uni.setStorageSync('verificationLevel', loginData.verificationLevel || 'BASIC')
	uni.setStorageSync('professionalBadge', !!loginData.professionalBadge)
	uni.setStorageSync('token', loginData.token || '')
}

export function getUserInfo() {
	const token = getToken()
	if (!token) return null
	return {
		userId: uni.getStorageSync('userId') || '',
		nickName: uni.getStorageSync('nickName') || '投资者',
		avatarUrl: uni.getStorageSync('avatarUrl') || '',
		role: uni.getStorageSync('role') || 'USER',
		verificationLevel: uni.getStorageSync('verificationLevel') || 'BASIC',
		professionalBadge: !!uni.getStorageSync('professionalBadge'),
		token
	}
}

export function getToken() {
	return uni.getStorageSync('token') || ''
}

export function clearUserInfo() {
	uni.removeStorageSync('userId')
	uni.removeStorageSync('nickName')
	uni.removeStorageSync('avatarUrl')
	uni.removeStorageSync('role')
	uni.removeStorageSync('verificationLevel')
	uni.removeStorageSync('professionalBadge')
	uni.removeStorageSync('token')
}

export function isAdminRole(role) {
	return role === 'ADMIN' || role === 'MODERATOR'
}

export function checkLoginAndRedirect() {
	return true
}
