<template>
	<view class="login-page">
		<view class="hero">
			<view>
				<text class="eyebrow">Investment Forum</text>
				<text class="title">股票基金投资论坛</text>
				<text class="subtitle">围绕市场、基金、公司研究和宏观策略的合规交流社区</text>
			</view>
			<view class="hero-metrics">
				<view class="metric">
					<text class="metric-value">10</text>
					<text class="metric-label">默认板块</text>
				</view>
				<view class="metric">
					<text class="metric-value">3</text>
					<text class="metric-label">认证等级</text>
				</view>
				<view class="metric">
					<text class="metric-value">24h</text>
					<text class="metric-label">审核流</text>
				</view>
			</view>
		</view>

		<view class="auth-card">
			<view class="tabs">
				<view class="tab" :class="{ active: mode === 'login' }" @tap="mode = 'login'">登录</view>
				<view class="tab" :class="{ active: mode === 'register' }" @tap="mode = 'register'">注册</view>
			</view>

			<view v-if="mode === 'login'" class="form">
				<input v-model="loginForm.account" class="input" placeholder="用户名 / 手机号 / 邮箱" />
				<input v-model="loginForm.password" class="input" password placeholder="密码" />
				<button class="primary-btn" :loading="loading" @tap="handleLogin">进入论坛</button>
				<view class="third-party">
					<text class="third-party-title">第三方账号登录</text>
					<view class="provider-row">
						<view class="provider provider-wechat" title="微信登录" @tap="handleThirdLogin('微信')">微</view>
						<view class="provider provider-weibo" title="微博登录" @tap="handleThirdLogin('微博')">博</view>
						<view class="provider provider-qq" title="QQ 登录" @tap="handleThirdLogin('QQ')">Q</view>
						<view class="provider provider-alipay" title="支付宝登录" @tap="handleThirdLogin('支付宝')">支</view>
					</view>
				</view>
			</view>

			<view v-else class="form">
				<input v-model="registerForm.username" class="input" placeholder="用户名" />
				<input v-model="registerForm.phone" class="input" placeholder="手机号，可选" />
				<input v-model="registerForm.email" class="input" placeholder="邮箱，可选" />
				<input v-model="registerForm.nickName" class="input" placeholder="昵称" />
				<input v-model="registerForm.password" class="input" password placeholder="密码，至少 6 位" />
				<button class="primary-btn" :loading="loading" @tap="handleRegister">创建账号</button>
				<text class="hint">注册后默认完成基础认证；实名、人脸和专业认证在个人设置中提交。</text>
			</view>

			<view class="secondary" @tap="goHome">先浏览公开内容</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { post } from '../utils/request.js'
import { isLoggedIn, saveUserInfo } from '../utils/auth.js'

const mode = ref('login')
const loading = ref(false)

const loginForm = ref({
	account: 'admin',
	password: 'forum-admin-2026'
})

const registerForm = ref({
	username: '',
	phone: '',
	email: '',
	nickName: '',
	password: ''
})

onLoad(() => {
	if (isLoggedIn()) {
		uni.reLaunch({ url: '/pages/Index' })
	}
})

async function handleLogin() {
	if (loading.value) return
	loading.value = true
	try {
		const result = await post('/auth/login', loginForm.value, { needToken: false, showLoading: true })
		saveUserInfo(result)
		uni.showToast({ title: '登录成功', icon: 'success' })
		setTimeout(() => uni.reLaunch({ url: '/pages/Index' }), 500)
	} finally {
		loading.value = false
	}
}

async function handleRegister() {
	if (loading.value) return
	loading.value = true
	try {
		const result = await post('/auth/register', registerForm.value, { needToken: false, showLoading: true })
		saveUserInfo(result)
		uni.showToast({ title: '注册成功', icon: 'success' })
		setTimeout(() => uni.reLaunch({ url: '/pages/Index' }), 500)
	} finally {
		loading.value = false
	}
}

function goHome() {
	uni.reLaunch({ url: '/pages/Index' })
}

function handleThirdLogin(provider) {
	uni.showToast({
		title: `${provider} 登录待接入`,
		icon: 'none'
	})
}
</script>

<style scoped>
.login-page {
	min-height: 100vh;
	display: grid;
	grid-template-columns: minmax(0, 1.1fr) 420px;
	gap: 36px;
	padding: 56px;
	box-sizing: border-box;
	background:
		linear-gradient(120deg, rgba(14, 116, 144, 0.12), rgba(22, 163, 74, 0.08)),
		#f4f7f8;
}

.hero {
	min-height: 560px;
	border-radius: 8px;
	background:
		linear-gradient(rgba(5, 20, 32, 0.18), rgba(5, 20, 32, 0.56)),
		url("https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?auto=format&fit=crop&w=1400&q=80");
	background-size: cover;
	background-position: center;
	color: #ffffff;
	padding: 48px;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	box-sizing: border-box;
}

.eyebrow {
	font-size: 14px;
	text-transform: uppercase;
	letter-spacing: 0;
	opacity: 0.82;
	display: block;
	margin-bottom: 12px;
}

.title {
	display: block;
	font-size: 46px;
	font-weight: 800;
	line-height: 1.12;
	margin-bottom: 18px;
}

.subtitle {
	display: block;
	max-width: 620px;
	font-size: 18px;
	line-height: 1.7;
	color: rgba(255, 255, 255, 0.88);
}

.hero-metrics {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 14px;
}

.metric {
	padding: 18px;
	border: 1px solid rgba(255, 255, 255, 0.22);
	background: rgba(255, 255, 255, 0.12);
	backdrop-filter: blur(12px);
	border-radius: 8px;
}

.metric-value,
.metric-label {
	display: block;
}

.metric-value {
	font-size: 28px;
	font-weight: 800;
}

.metric-label {
	margin-top: 4px;
	font-size: 13px;
	opacity: 0.84;
}

.auth-card {
	align-self: center;
	background: #ffffff;
	border: 1px solid #dbe4e8;
	border-radius: 8px;
	padding: 26px;
	box-shadow: 0 18px 50px rgba(14, 36, 48, 0.12);
}

.tabs {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	background: #eef3f5;
	border-radius: 8px;
	padding: 4px;
	margin-bottom: 22px;
}

.tab {
	text-align: center;
	padding: 11px;
	border-radius: 6px;
	font-size: 15px;
	color: #50616b;
}

.tab.active {
	background: #ffffff;
	color: #0f766e;
	font-weight: 700;
	box-shadow: 0 1px 6px rgba(15, 23, 42, 0.08);
}

.form {
	display: flex;
	flex-direction: column;
	gap: 14px;
}

.input {
	height: 46px;
	border: 1px solid #d6e0e5;
	border-radius: 8px;
	padding: 0 14px;
	box-sizing: border-box;
	background: #fbfdfe;
	font-size: 15px;
}

.primary-btn {
	height: 48px;
	line-height: 48px;
	border-radius: 8px;
	background: #0f766e;
	color: #ffffff;
	font-weight: 700;
	border: 0;
}

.primary-btn::after {
	border: 0;
}

.third-party,
.third-party-title,
.hint,
.secondary {
	display: block;
}

.third-party {
	margin-top: 8px;
	padding-top: 16px;
	border-top: 1px solid #edf2f4;
}

.third-party-title {
	font-size: 13px;
	font-weight: 700;
	color: #50616b;
	text-align: center;
	margin-bottom: 14px;
}

.hint {
	font-size: 13px;
	line-height: 1.7;
	color: #5a6b75;
}

.provider-row {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 14px;
}

.provider {
	width: 44px;
	height: 44px;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 17px;
	font-weight: 800;
	color: #ffffff;
	cursor: pointer;
	box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
	transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.provider:hover {
	transform: translateY(-2px);
	box-shadow: 0 12px 22px rgba(15, 23, 42, 0.16);
}

.provider-wechat {
	background: #1aad19;
}

.provider-weibo {
	background: #e6162d;
}

.provider-qq {
	background: #12b7f5;
}

.provider-alipay {
	background: #1677ff;
}

.secondary {
	margin-top: 18px;
	text-align: center;
	color: #0f766e;
	font-size: 14px;
}

@media (max-width: 900px) {
	.login-page {
		display: block;
		padding: 18px;
	}

	.hero {
		min-height: 340px;
		padding: 28px;
		margin-bottom: 18px;
	}

	.title {
		font-size: 34px;
	}

	.hero-metrics {
		grid-template-columns: 1fr;
	}
}
</style>
