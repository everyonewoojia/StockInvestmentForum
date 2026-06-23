<template>
	<view class="forum-shell">
		<view class="topbar">
			<view>
				<text class="brand">股票基金投资论坛</text>
				<text class="tagline">市场讨论、公司研究、基金策略和合规运营一体化平台</text>
			</view>
			<view class="top-actions">
				<input v-model="keyword" class="search" placeholder="搜索关键词、用户、股票代码" confirm-type="search" @confirm="runSearch" />
				<button class="ghost-btn" @tap="runSearch">搜索</button>
				<button v-if="!me" class="primary-mini" @tap="goLogin">登录</button>
				<button v-else class="ghost-btn" @tap="logout">退出</button>
			</view>
		</view>

		<view class="layout">
			<view class="sidebar">
				<view
					v-for="item in navItems"
					:key="item.key"
					class="nav-item"
					:class="{ active: activeView === item.key }"
					@tap="switchView(item.key)"
				>
					<text class="nav-label">{{ item.label }}</text>
					<text v-if="item.badge" class="nav-badge">{{ item.badge }}</text>
				</view>
			</view>

			<scroll-view scroll-y class="main">
				<view v-if="activeView === 'feed'" class="content-grid">
					<view class="primary-column">
						<view class="section-bar">
							<view>
								<text class="section-title">推荐 Feed</text>
								<text class="section-subtitle">编辑精选、热榜和最新发布内容</text>
							</view>
							<picker :range="boardPickerLabels" :value="selectedBoardIndex" @change="onBoardChange">
								<view class="picker">{{ activeBoardName }}</view>
							</picker>
						</view>

						<view class="composer">
							<view class="composer-head">
								<text class="panel-title">发布观点</text>
								<text class="review-pill">提交后进入审核队列</text>
							</view>
							<input v-model="postForm.title" class="input" placeholder="标题，例如：新能源板块估值修复怎么看" />
							<textarea v-model="postForm.content" class="textarea" placeholder="写下分析逻辑、数据依据和风险提示。请避免承诺收益或直接荐股。" />
							<view class="form-row">
								<picker :range="postTypeLabels" :value="postTypeIndex" @change="onPostTypeChange">
									<view class="picker small">{{ postTypeLabels[postTypeIndex] }}</view>
								</picker>
								<input v-model="postForm.stockCodesText" class="input compact" placeholder="股票/基金代码，逗号分隔" />
								<button class="primary-mini" @tap="submitPost">发布</button>
							</view>
						</view>

						<view v-if="searchResult" class="search-panel">
							<view class="section-bar">
								<text class="panel-title">搜索结果</text>
								<text class="muted">引擎：{{ searchResult.engine }}</text>
							</view>
							<view class="chips">
								<view v-for="symbol in searchResult.symbols" :key="symbol.code" class="chip">{{ symbol.market }} {{ symbol.code }} {{ symbol.name }}</view>
							</view>
							<view class="chips">
								<view v-for="user in searchResult.users" :key="user.id" class="chip subtle">{{ user.nickName }}</view>
							</view>
						</view>

						<view v-for="post in posts" :key="post.id" class="post-card" @tap="openPost(post.id)">
							<view class="post-head">
								<view class="avatar">{{ initials(post.authorName) }}</view>
								<view class="post-meta">
									<view class="name-row">
										<text class="author">{{ post.authorName }}</text>
										<text v-if="post.authorBadge" class="v-badge">V</text>
										<text class="board">{{ post.boardName }}</text>
										<text v-if="post.digest" class="digest">精华</text>
									</view>
									<text class="muted">{{ post.publishedAt || post.createdAt }}</text>
								</view>
							</view>
							<text class="post-title">{{ post.title }}</text>
							<text class="post-summary">{{ post.summary || post.content }}</text>
							<view class="post-actions">
								<text>浏览 {{ post.viewCount || 0 }}</text>
								<text>赞 {{ post.likeCount || 0 }}</text>
								<text>收藏 {{ post.favoriteCount || 0 }}</text>
								<text>评论 {{ post.commentCount || 0 }}</text>
							</view>
						</view>
					</view>

					<view class="side-column">
						<view class="panel">
							<text class="panel-title">账号状态</text>
							<view v-if="me" class="profile-mini">
								<view class="avatar big">{{ initials(me.nickName) }}</view>
								<view>
									<view class="name-row">
										<text class="author">{{ me.nickName }}</text>
										<text v-if="me.professionalBadge" class="v-badge">V</text>
									</view>
									<text class="muted">{{ me.role }} · {{ me.verificationLevel }} · {{ me.riskPreference }}</text>
								</view>
							</view>
							<view v-else>
								<text class="muted block">登录后可发帖、评论、关注、私信和进入管理端。</text>
								<button class="primary-wide" @tap="goLogin">登录 / 注册</button>
							</view>
						</view>

						<view class="panel">
							<text class="panel-title">热榜</text>
							<view v-for="hot in hotPosts" :key="hot.id" class="hot-item" @tap="openPost(hot.id)">
								<text class="hot-title">{{ hot.title }}</text>
								<text class="muted">{{ hot.boardName }} · {{ hot.viewCount || 0 }} 浏览</text>
							</view>
						</view>

						<view class="panel">
							<text class="panel-title">板块</text>
							<view v-for="board in boards.slice(0, 8)" :key="board.id" class="board-row" @tap="filterBoard(board.id)">
								<text>{{ board.name }}</text>
								<text class="muted">{{ board.category }}</text>
							</view>
						</view>
					</view>
				</view>

				<view v-else-if="activeView === 'boards'" class="board-grid">
					<view v-for="board in boards" :key="board.id" class="board-card" @tap="filterBoard(board.id)">
						<text class="board-title">{{ board.name }}</text>
						<text class="board-category">{{ board.category }}</text>
						<text class="board-desc">{{ board.description }}</text>
					</view>
				</view>

				<view v-else-if="activeView === 'following'" class="primary-column wide">
					<view class="section-bar">
						<text class="section-title">关注动态</text>
						<button class="ghost-btn" @tap="loadFollowingFeed">刷新</button>
					</view>
					<view v-for="post in followingPosts" :key="post.id" class="post-card" @tap="openPost(post.id)">
						<text class="post-title">{{ post.title }}</text>
						<text class="post-summary">{{ post.authorName }} · {{ post.boardName }} · {{ post.summary || post.content }}</text>
					</view>
					<view v-if="followingPosts.length === 0" class="empty">暂无关注动态，可以在帖子详情里关注作者。</view>
				</view>

				<view v-else-if="activeView === 'groups'" class="primary-column wide">
					<view class="section-bar">
						<text class="section-title">投资主题群组</text>
						<button class="primary-mini" @tap="createGroup">创建群组</button>
					</view>
					<view class="composer">
						<input v-model="groupForm.name" class="input" placeholder="群组名称" />
						<input v-model="groupForm.description" class="input" placeholder="群组简介" />
					</view>
					<view v-for="group in groups" :key="group.id" class="board-card horizontal">
						<view>
							<text class="board-title">{{ group.name }}</text>
							<text class="board-desc">{{ group.description || '公开投资主题群组' }}</text>
							<text class="muted">{{ group.ownerName }} · {{ group.memberCount }} 人</text>
						</view>
						<button class="ghost-btn" @tap="joinGroup(group.id)">加入</button>
					</view>
				</view>

				<view v-else-if="activeView === 'messages'" class="primary-column wide">
					<view class="section-bar">
						<text class="section-title">私信</text>
						<text class="section-subtitle">支持文本和图片链接，当前展示点对点会话</text>
					</view>
					<view class="composer">
						<input v-model="messageForm.receiverId" class="input" placeholder="接收用户 ID" />
						<textarea v-model="messageForm.content" class="textarea compact-area" placeholder="输入私信内容" />
						<button class="primary-mini" @tap="sendMessage">发送私信</button>
					</view>
					<view v-for="message in messages" :key="message.id" class="message-row">
						<text class="author">{{ message.senderName }}</text>
						<text>{{ message.content }}</text>
						<text class="muted">{{ message.createdAt }}</text>
					</view>
				</view>

				<view v-else-if="activeView === 'profile'" class="primary-column wide">
					<view class="section-bar">
						<text class="section-title">个人资料与认证</text>
						<button class="primary-mini" @tap="saveProfile">保存资料</button>
					</view>
					<view class="composer">
						<input v-model="profileForm.nickName" class="input" placeholder="昵称" />
						<input v-model="profileForm.bio" class="input" placeholder="个人简介" />
						<input v-model="profileForm.marketsText" class="input" placeholder="关注领域，如 A股,基金,美股" />
						<input v-model="profileForm.experienceTagsText" class="input" placeholder="投资经验标签" />
						<view class="form-row">
							<button class="ghost-btn" @tap="submitBasicVerification">基础认证</button>
							<button class="ghost-btn" @tap="submitRealNameVerification">实名/人脸申请</button>
							<button class="ghost-btn" @tap="submitProfessionalVerification">专业认证申请</button>
							<button class="ghost-btn" @tap="completeRisk">完成风险评估</button>
						</view>
					</view>
				</view>

				<view v-else-if="activeView === 'admin'" class="admin-grid">
					<view class="panel stat-panel">
						<text class="panel-title">运营仪表盘</text>
						<view class="stats">
							<view class="stat"><text class="stat-value">{{ adminDashboard.users || 0 }}</text><text>用户</text></view>
							<view class="stat"><text class="stat-value">{{ adminDashboard.posts || 0 }}</text><text>帖子</text></view>
							<view class="stat"><text class="stat-value">{{ adminDashboard.pendingPosts || 0 }}</text><text>待审</text></view>
							<view class="stat"><text class="stat-value">{{ adminDashboard.openReports || 0 }}</text><text>举报</text></view>
						</view>
					</view>

					<view class="panel">
						<view class="section-bar">
							<text class="panel-title">内容审核</text>
							<button class="ghost-btn" @tap="loadAdmin">刷新</button>
						</view>
						<view v-for="post in pendingPosts" :key="post.id" class="review-item">
							<text class="review-title">{{ post.title }}</text>
							<text class="muted">{{ post.authorName }} · {{ post.boardName }} · {{ post.reviewReason || '常规人工审核' }}</text>
							<view class="form-row">
								<button class="primary-mini" @tap="reviewPost(post.id, 'APPROVE')">通过</button>
								<button class="danger-btn" @tap="reviewPost(post.id, 'REJECT')">拒绝</button>
							</view>
						</view>
						<view v-if="pendingPosts.length === 0" class="empty">暂无待审核内容。</view>
					</view>

					<view class="panel">
						<text class="panel-title">举报处理</text>
						<view v-for="report in reports" :key="report.id" class="review-item">
							<text class="review-title">{{ report.targetType }} #{{ report.targetId }}</text>
							<text class="muted">{{ report.reporterName }} · {{ report.reason }} · {{ report.status }}</text>
							<button class="ghost-btn" @tap="resolveReport(report.id)">标记已处理</button>
						</view>
					</view>

					<view class="panel">
						<text class="panel-title">板块管理</text>
						<view class="form-row">
							<input v-model="boardForm.name" class="input compact" placeholder="板块名" />
							<input v-model="boardForm.slug" class="input compact" placeholder="slug" />
							<button class="primary-mini" @tap="createBoard">新增</button>
						</view>
						<view v-for="board in boards" :key="board.id" class="board-row">
							<text>{{ board.name }}</text>
							<text class="muted">{{ board.enabled ? '启用' : '停用' }}</text>
						</view>
					</view>
				</view>
			</scroll-view>

			<view v-if="selectedPost" class="detail">
				<view class="detail-head">
					<text class="panel-title">帖子详情</text>
					<text class="close" @tap="closePost">返回列表</text>
				</view>
				<text class="post-title detail-title">{{ selectedPost.title }}</text>
				<view class="name-row">
					<text class="author">{{ selectedPost.authorName }}</text>
					<text v-if="selectedPost.authorBadge" class="v-badge">V</text>
					<text class="board">{{ selectedPost.boardName }}</text>
				</view>
				<text class="detail-content">{{ selectedPost.content }}</text>
				<view class="post-actions detail-actions">
					<button class="ghost-btn" @tap="interact('LIKE')">点赞 {{ selectedPost.likeCount || 0 }}</button>
					<button class="ghost-btn" @tap="interact('FAVORITE')">收藏 {{ selectedPost.favoriteCount || 0 }}</button>
					<view class="detail-count">评论 {{ selectedPost.commentCount || comments.length || 0 }}</view>
					<button class="ghost-btn" @tap="followAuthor">关注作者</button>
					<button class="ghost-btn" @tap="reportPost">举报</button>
				</view>
				<view class="comment-box">
					<textarea v-model="commentText" class="textarea compact-area" placeholder="参与讨论，支持楼中楼回复" />
					<button class="primary-mini" @tap="submitComment">评论</button>
				</view>
				<view v-for="comment in comments" :key="comment.id" class="comment">
					<text class="author">{{ comment.authorName }}</text>
					<text>{{ comment.content }}</text>
					<text class="muted">{{ comment.createdAt }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { get, post, put, del } from '../utils/request.js'
import { clearUserInfo, getUserInfo, isAdminRole, saveUserInfo } from '../utils/auth.js'

const activeView = ref('feed')
const previousView = ref('feed')
const keyword = ref('')
const me = ref(null)
const boards = ref([])
const posts = ref([])
const followingPosts = ref([])
const groups = ref([])
const messages = ref([])
const selectedPost = ref(null)
const comments = ref([])
const searchResult = ref(null)
const adminDashboard = ref({})
const pendingPosts = ref([])
const reports = ref([])
const selectedBoardId = ref(null)
const selectedBoardIndex = ref(0)
const postTypeIndex = ref(0)
const commentText = ref('')

const postTypeLabels = ['普通帖子', '长文分析', '投票调研', '实时讨论']
const postTypes = ['NORMAL', 'LONG_ARTICLE', 'POLL', 'SHORT']

const postForm = ref({
	boardId: null,
	type: 'NORMAL',
	title: '',
	content: '',
	stockCodesText: ''
})

const groupForm = ref({
	name: '',
	description: ''
})

const messageForm = ref({
	receiverId: '',
	content: ''
})

const profileForm = ref({
	nickName: '',
	bio: '',
	marketsText: '',
	experienceTagsText: ''
})

const boardForm = ref({
	name: '',
	slug: ''
})

const navItems = computed(() => {
	const items = [
		{ key: 'feed', label: '首页 Feed' },
		{ key: 'boards', label: '板块' },
		{ key: 'following', label: '关注动态' },
		{ key: 'groups', label: '群组' },
		{ key: 'messages', label: '私信' },
		{ key: 'profile', label: '资料认证' }
	]
	if (me.value && isAdminRole(me.value.role)) {
		items.push({ key: 'admin', label: '运营后台', badge: pendingPosts.value.length || '' })
	}
	return items
})

const boardPickerLabels = computed(() => ['全部板块', ...boards.value.map(item => item.name)])
const activeBoardName = computed(() => selectedBoardIndex.value === 0 ? '全部板块' : boardPickerLabels.value[selectedBoardIndex.value])
const hotPosts = computed(() => posts.value.slice(0, 5).sort((a, b) => score(b) - score(a)))

onLoad(async () => {
	me.value = getUserInfo()
	await Promise.all([loadMe(), loadBoards(), loadPosts(), loadGroups()])
	if (me.value && isAdminRole(me.value.role)) {
		await loadAdmin()
	}
})

async function loadMe() {
	if (!getUserInfo()) return
	try {
		const profile = await get('/auth/me', {}, { showLoading: false })
		me.value = profile
		saveUserInfo({
			userId: profile.id,
			nickName: profile.nickName,
			avatarUrl: profile.avatarUrl,
			role: profile.role,
			verificationLevel: profile.verificationLevel,
			professionalBadge: profile.professionalBadge,
			token: getUserInfo().token
		})
		profileForm.value.nickName = profile.nickName || ''
		profileForm.value.bio = profile.bio || ''
	} catch (error) {
		me.value = null
	}
}

async function loadBoards() {
	boards.value = await get('/boards', {}, { needToken: false, showLoading: false })
	if (!postForm.value.boardId && boards.value.length) {
		postForm.value.boardId = boards.value[0].id
	}
}

async function loadPosts() {
	const params = { page: 1, size: 20 }
	if (selectedBoardId.value) params.boardId = selectedBoardId.value
	if (keyword.value) params.keyword = keyword.value
	const page = await get('/posts', params, { needToken: false, showLoading: false })
	posts.value = page.records || []
}

async function loadFollowingFeed() {
	ensureLogin()
	const page = await get('/social/following-feed', { page: 1, size: 20 }, { showLoading: true })
	followingPosts.value = page.records || []
}

async function loadGroups() {
	groups.value = await get('/groups', {}, { needToken: false, showLoading: false })
}

async function loadAdmin() {
	if (!me.value || !isAdminRole(me.value.role)) return
	const [dashboard, reviewPage, reportPage] = await Promise.all([
		get('/admin/dashboard', {}, { showLoading: false }),
		get('/admin/review/posts', { status: 'PENDING_REVIEW', page: 1, size: 20 }, { showLoading: false }),
		get('/admin/reports', { status: 'OPEN', page: 1, size: 20 }, { showLoading: false })
	])
	adminDashboard.value = dashboard || {}
	pendingPosts.value = reviewPage.records || []
	reports.value = reportPage.records || []
}

function switchView(view) {
	activeView.value = view
	if (view === 'following') loadFollowingFeed()
	if (view === 'admin') loadAdmin()
}

function onBoardChange(event) {
	selectedBoardIndex.value = Number(event.detail.value)
	selectedBoardId.value = selectedBoardIndex.value === 0 ? null : boards.value[selectedBoardIndex.value - 1].id
	loadPosts()
}

function filterBoard(boardId) {
	const index = boards.value.findIndex(item => item.id === boardId)
	selectedBoardIndex.value = index >= 0 ? index + 1 : 0
	selectedBoardId.value = boardId
	activeView.value = 'feed'
	loadPosts()
}

function onPostTypeChange(event) {
	postTypeIndex.value = Number(event.detail.value)
	postForm.value.type = postTypes[postTypeIndex.value]
}

async function submitPost() {
	ensureLogin()
	const payload = {
		boardId: postForm.value.boardId || (boards.value[0] && boards.value[0].id),
		type: postForm.value.type,
		title: postForm.value.title,
		content: postForm.value.content,
		summary: postForm.value.content.slice(0, 120),
		stockCodes: splitText(postForm.value.stockCodesText)
	}
	await post('/posts', payload, { showLoading: true })
	postForm.value.title = ''
	postForm.value.content = ''
	postForm.value.stockCodesText = ''
	uni.showToast({ title: '已提交审核', icon: 'success' })
	if (me.value && isAdminRole(me.value.role)) loadAdmin()
}

async function openPost(postId) {
	previousView.value = activeView.value === 'postDetail' ? previousView.value : activeView.value
	selectedPost.value = await get(`/posts/${postId}`, {}, { needToken: !!getUserInfo(), showLoading: true })
	comments.value = selectedPost.value.comments || []
	activeView.value = 'postDetail'
}

function closePost() {
	selectedPost.value = null
	comments.value = []
	activeView.value = previousView.value || 'feed'
}

async function interact(action) {
	ensureLogin()
	selectedPost.value = await post(`/posts/${selectedPost.value.id}/interactions`, { action, active: true }, { showLoading: false })
}

async function submitComment() {
	ensureLogin()
	if (!commentText.value.trim()) return
	await post(`/posts/${selectedPost.value.id}/comments`, { content: commentText.value }, { showLoading: true })
	commentText.value = ''
	await openPost(selectedPost.value.id)
}

async function followAuthor() {
	ensureLogin()
	await post(`/social/follow/${selectedPost.value.authorId}`, {}, { showLoading: true })
	uni.showToast({ title: '已关注作者', icon: 'success' })
}

async function reportPost() {
	ensureLogin()
	await post('/reports', {
		targetType: 'POST',
		targetId: selectedPost.value.id,
		reason: '内容可能存在合规风险',
		detail: '用户从帖子详情页提交举报'
	}, { showLoading: true })
	uni.showToast({ title: '举报已提交', icon: 'success' })
}

async function runSearch() {
	searchResult.value = await get('/search', { keyword: keyword.value, page: 1, size: 10 }, { needToken: false, showLoading: true })
	posts.value = (searchResult.value.posts && searchResult.value.posts.records) || []
	activeView.value = 'feed'
}

async function createGroup() {
	ensureLogin()
	await post('/groups', {
		name: groupForm.value.name,
		description: groupForm.value.description,
		visibility: 'PUBLIC',
		joinPolicy: 'OPEN'
	}, { showLoading: true })
	groupForm.value.name = ''
	groupForm.value.description = ''
	await loadGroups()
}

async function joinGroup(groupId) {
	ensureLogin()
	await post(`/groups/${groupId}/join`, {}, { showLoading: true })
	uni.showToast({ title: '已加入群组', icon: 'success' })
	await loadGroups()
}

async function sendMessage() {
	ensureLogin()
	const result = await post('/messages', {
		receiverId: Number(messageForm.value.receiverId),
		content: messageForm.value.content
	}, { showLoading: true })
	messages.value = [result, ...messages.value]
	messageForm.value.content = ''
}

async function saveProfile() {
	ensureLogin()
	const profile = await put('/users/me', {
		nickName: profileForm.value.nickName,
		bio: profileForm.value.bio,
		markets: splitText(profileForm.value.marketsText),
		experienceTags: splitText(profileForm.value.experienceTagsText),
		riskPreference: me.value.riskPreference || 'BALANCED',
		privacyProfile: 'PUBLIC'
	}, { showLoading: true })
	me.value = profile
	uni.showToast({ title: '资料已保存', icon: 'success' })
}

async function submitBasicVerification() {
	ensureLogin()
	me.value = await post('/users/me/verifications', { type: 'BASIC' }, { showLoading: true })
	uni.showToast({ title: '基础认证完成', icon: 'success' })
}

async function submitRealNameVerification() {
	ensureLogin()
	await post('/users/me/verifications', {
		type: 'REAL_NAME',
		realName: me.value.nickName,
		provider: 'tencent-faceid'
	}, { showLoading: true })
	uni.showToast({ title: '实名申请已提交', icon: 'success' })
}

async function submitProfessionalVerification() {
	ensureLogin()
	await post('/users/me/verifications', {
		type: 'PROFESSIONAL',
		materials: ['qualification-placeholder.pdf']
	}, { showLoading: true })
	uni.showToast({ title: '专业认证已提交', icon: 'success' })
}

async function completeRisk() {
	ensureLogin()
	me.value = await post('/users/me/risk-assessment', {
		score: 72,
		riskLevel: 'BALANCED',
		answers: ['长期投资', '可承受中等波动', '了解基金和股票基础风险']
	}, { showLoading: true })
	uni.showToast({ title: '评估已完成', icon: 'success' })
}

async function reviewPost(postId, decision) {
	await post(`/admin/review/posts/${postId}`, { decision, reason: decision === 'APPROVE' ? '内容合规' : '不符合社区规范' }, { showLoading: true })
	await Promise.all([loadAdmin(), loadPosts()])
}

async function resolveReport(reportId) {
	await post(`/admin/reports/${reportId}/resolve`, { reason: '已处理' }, { showLoading: true })
	await loadAdmin()
}

async function createBoard() {
	await post('/admin/boards', {
		name: boardForm.value.name,
		slug: boardForm.value.slug,
		category: '主题专区',
		description: '运营新增板块',
		sortOrder: 120,
		enabled: true
	}, { showLoading: true })
	boardForm.value.name = ''
	boardForm.value.slug = ''
	await loadBoards()
}

function goLogin() {
	uni.navigateTo({ url: '/pages/Login' })
}

function logout() {
	clearUserInfo()
	me.value = null
	uni.showToast({ title: '已退出', icon: 'success' })
}

function ensureLogin() {
	if (!getUserInfo()) {
		uni.navigateTo({ url: '/pages/Login' })
		throw new Error('login required')
	}
}

function initials(name) {
	const text = name || '投'
	return text.slice(0, 1)
}

function splitText(value) {
	return String(value || '')
		.split(/[,，\s]+/)
		.map(item => item.trim())
		.filter(Boolean)
}

function score(post) {
	return (post.likeCount || 0) * 3 + (post.commentCount || 0) * 4 + (post.viewCount || 0)
}
</script>

<style scoped>
.forum-shell {
	min-height: 100vh;
	background: #f4f7f8;
	color: #17212b;
}

.topbar {
	height: 76px;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 28px 0 72px;
	background: #ffffff;
	border-bottom: 1px solid #dbe4e8;
	box-sizing: border-box;
	position: sticky;
	top: 0;
	z-index: 5;
}

.brand,
.tagline,
.section-title,
.section-subtitle,
.panel-title,
.post-title,
.post-summary,
.muted,
.author,
.board,
.digest,
.v-badge,
.board-title,
.board-category,
.board-desc,
.hot-title,
.review-title,
.detail-content {
	display: block;
}

.brand {
	font-size: 22px;
	font-weight: 800;
	color: #0f3f3b;
}

.tagline {
	margin-top: 4px;
	font-size: 12px;
	color: #6b7b85;
}

.top-actions,
.form-row,
.post-actions,
.name-row,
.section-bar,
.composer-head,
.detail-head,
.profile-mini,
.chips,
.stats {
	display: flex;
	align-items: center;
	gap: 10px;
}

.top-actions {
	justify-content: flex-end;
}

.search {
	width: 320px;
	height: 38px;
	border: 1px solid #d7e0e5;
	border-radius: 8px;
	padding: 0 12px;
	box-sizing: border-box;
	background: #f8fbfc;
}

.layout {
	display: block;
	position: relative;
	min-height: calc(100vh - 76px);
}

.sidebar {
	position: fixed;
	top: 76px;
	left: 0;
	bottom: 0;
	width: 220px;
	background: #ffffff;
	border-right: 1px solid #dbe4e8;
	padding: 18px;
	box-sizing: border-box;
	z-index: 10;
	transform: translateX(-176px);
	transition: transform 0.18s ease, box-shadow 0.18s ease;
	box-shadow: 8px 0 24px rgba(15, 23, 42, 0);
}

.sidebar:hover {
	transform: translateX(0);
	box-shadow: 8px 0 24px rgba(15, 23, 42, 0.12);
}

.sidebar::after {
	content: "MENU";
	position: absolute;
	top: 18px;
	right: 7px;
	writing-mode: vertical-rl;
	font-size: 11px;
	font-weight: 800;
	color: #0f766e;
	letter-spacing: 0;
	pointer-events: none;
}

.nav-item {
	min-height: 42px;
	border-radius: 8px;
	padding: 0 12px;
	display: flex;
	align-items: center;
	justify-content: space-between;
	color: #50616b;
	font-size: 14px;
	margin-bottom: 6px;
}

.nav-item.active {
	background: #e7f5f1;
	color: #0f766e;
	font-weight: 700;
}

.nav-badge {
	min-width: 22px;
	height: 22px;
	line-height: 22px;
	text-align: center;
	background: #dc2626;
	color: #ffffff;
	border-radius: 999px;
	font-size: 12px;
}

.main {
	height: calc(100vh - 76px);
	width: 100%;
	padding-left: 56px;
	box-sizing: border-box;
}

.content-grid {
	display: grid;
	grid-template-columns: minmax(0, 1fr) 360px;
	gap: 18px;
	padding: 22px 28px 22px 24px;
	box-sizing: border-box;
}

.primary-column,
.side-column,
.admin-grid {
	display: flex;
	flex-direction: column;
	gap: 16px;
}

.primary-column.wide {
	padding: 22px 28px 22px 24px;
	max-width: none;
}

.section-bar {
	justify-content: space-between;
}

.section-title {
	font-size: 24px;
	font-weight: 800;
}

.section-subtitle,
.muted {
	font-size: 12px;
	color: #6b7b85;
	line-height: 1.5;
}

.muted.block {
	margin-bottom: 12px;
}

.composer,
.post-card,
.panel,
.board-card,
.search-panel,
.detail {
	background: #ffffff;
	border: 1px solid #dbe4e8;
	border-radius: 8px;
	padding: 16px;
	box-sizing: border-box;
}

.composer-head,
.detail-head {
	justify-content: space-between;
	margin-bottom: 12px;
}

.panel-title {
	font-size: 16px;
	font-weight: 800;
	color: #1f2d38;
}

.review-pill,
.digest,
.v-badge {
	font-size: 12px;
	border-radius: 999px;
	padding: 3px 8px;
	background: #e7f5f1;
	color: #0f766e;
}

.input,
.textarea,
.picker {
	width: 100%;
	border: 1px solid #d7e0e5;
	border-radius: 8px;
	background: #fbfdfe;
	box-sizing: border-box;
	font-size: 14px;
}

.input,
.picker {
	height: 40px;
	line-height: 40px;
	padding: 0 12px;
}

.input.compact {
	min-width: 180px;
}

.textarea {
	min-height: 118px;
	padding: 10px 12px;
	line-height: 1.6;
	margin: 10px 0;
}

.compact-area {
	min-height: 78px;
}

.picker.small {
	width: 128px;
}

button {
	margin: 0;
}

button::after {
	border: 0;
}

.primary-mini,
.ghost-btn,
.danger-btn,
.primary-wide {
	height: 38px;
	line-height: 38px;
	border-radius: 8px;
	font-size: 14px;
	padding: 0 14px;
	white-space: nowrap;
}

.primary-mini,
.primary-wide {
	background: #0f766e;
	color: #ffffff;
}

.primary-wide {
	width: 100%;
}

.ghost-btn {
	background: #eef4f5;
	color: #26434d;
}

.danger-btn {
	background: #fee2e2;
	color: #b91c1c;
}

.post-card {
	cursor: pointer;
}

.post-head {
	display: flex;
	gap: 12px;
	margin-bottom: 12px;
}

.avatar {
	width: 38px;
	height: 38px;
	border-radius: 50%;
	background: #0f766e;
	color: #ffffff;
	display: flex;
	align-items: center;
	justify-content: center;
	font-weight: 800;
	flex-shrink: 0;
}

.avatar.big {
	width: 52px;
	height: 52px;
}

.post-meta {
	min-width: 0;
}

.author {
	font-size: 14px;
	font-weight: 700;
	color: #1f2d38;
}

.board {
	font-size: 12px;
	color: #0f766e;
}

.post-title {
	font-size: 19px;
	font-weight: 800;
	margin-bottom: 8px;
	color: #17212b;
}

.post-summary {
	font-size: 14px;
	line-height: 1.7;
	color: #42515b;
	overflow: hidden;
	display: -webkit-box;
	-webkit-line-clamp: 3;
	-webkit-box-orient: vertical;
}

.post-actions {
	margin-top: 14px;
	color: #6b7b85;
	font-size: 13px;
	flex-wrap: wrap;
}

.profile-mini {
	align-items: center;
	margin-top: 14px;
}

.hot-item,
.board-row,
.review-item,
.message-row,
.comment {
	padding: 12px 0;
	border-top: 1px solid #edf2f4;
}

.hot-title {
	font-size: 14px;
	font-weight: 700;
	line-height: 1.5;
}

.board-row {
	display: flex;
	justify-content: space-between;
	gap: 12px;
}

.board-grid {
	display: grid;
	grid-template-columns: repeat(3, minmax(0, 1fr));
	gap: 16px;
	padding: 22px 28px 22px 24px;
}

.board-card {
	min-height: 138px;
}

.board-card.horizontal {
	min-height: auto;
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.board-title {
	font-size: 18px;
	font-weight: 800;
	margin-bottom: 8px;
}

.board-category {
	font-size: 12px;
	color: #0f766e;
	margin-bottom: 12px;
}

.board-desc {
	font-size: 14px;
	color: #52636d;
	line-height: 1.6;
}

.chips {
	flex-wrap: wrap;
	margin-top: 10px;
}

.chip {
	padding: 6px 10px;
	background: #e7f5f1;
	color: #0f766e;
	border-radius: 999px;
	font-size: 12px;
}

.chip.subtle {
	background: #eef4f5;
	color: #455966;
}

.admin-grid {
	padding: 22px 28px 22px 24px;
}

.stats {
	display: grid;
	grid-template-columns: repeat(4, minmax(0, 1fr));
}

.stat {
	background: #f7fafb;
	border-radius: 8px;
	padding: 14px;
}

.stat-value {
	display: block;
	font-size: 28px;
	font-weight: 800;
	color: #0f766e;
}

.review-title {
	font-size: 15px;
	font-weight: 800;
	margin-bottom: 6px;
}

.detail {
	position: fixed;
	top: 76px;
	left: 0;
	right: 0;
	bottom: 0;
	z-index: 20;
	background: #f4f7f8;
	border: 0;
	border-radius: 0;
	height: auto;
	padding: 24px 48px 80px;
	overflow-y: auto;
	box-shadow: none;
}

.close {
	color: #0f766e;
	font-size: 14px;
	font-weight: 700;
	cursor: pointer;
}

.detail > .detail-head,
.detail > .detail-title,
.detail > .name-row,
.detail > .detail-content,
.detail > .detail-actions,
.detail > .comment-box,
.detail > .comment {
	max-width: 940px;
	margin-left: auto;
	margin-right: auto;
	box-sizing: border-box;
}

.detail-head {
	position: sticky;
	top: -24px;
	z-index: 2;
	background: rgba(244, 247, 248, 0.96);
	padding: 12px 0;
	backdrop-filter: blur(12px);
}

.detail-title {
	font-size: 34px;
	line-height: 1.28;
	margin-top: 26px;
	margin-bottom: 16px;
	color: #111827;
}

.detail-content {
	margin-top: 22px;
	font-size: 17px;
	line-height: 1.95;
	color: #2b3a45;
	white-space: pre-wrap;
	background: #ffffff;
	border: 1px solid #dbe4e8;
	border-radius: 8px;
	padding: 28px;
}

.detail-actions {
	align-items: center;
	margin-top: 18px;
	margin-bottom: 18px;
	padding: 14px 0;
	border-top: 1px solid #dbe4e8;
	border-bottom: 1px solid #dbe4e8;
	flex-wrap: wrap;
}

.detail-count {
	height: 38px;
	line-height: 38px;
	padding: 0 14px;
	border-radius: 8px;
	background: #ffffff;
	border: 1px solid #dbe4e8;
	color: #52636d;
	font-size: 14px;
}

.comment-box {
	background: #ffffff;
	border: 1px solid #dbe4e8;
	border-radius: 8px;
	padding: 16px;
	margin-top: 18px;
}

.comment {
	background: #ffffff;
	border: 1px solid #edf2f4;
	border-radius: 8px;
	padding: 14px 16px;
	margin-top: 12px;
}

.empty {
	background: #ffffff;
	border: 1px dashed #cfdbe1;
	border-radius: 8px;
	padding: 28px;
	color: #6b7b85;
	text-align: center;
}

@media (max-width: 760px) {
	.topbar {
		height: auto;
		min-height: 76px;
		display: block;
		padding: 16px;
	}

	.top-actions {
		margin-top: 12px;
		justify-content: flex-start;
		flex-wrap: wrap;
	}

	.search {
		width: 100%;
	}

	.layout {
		display: block;
	}

	.sidebar {
		display: flex;
		overflow-x: auto;
		border-right: 0;
		border-bottom: 1px solid #dbe4e8;
	}

	.nav-item {
		flex: 0 0 auto;
	}

	.content-grid,
	.board-grid {
		grid-template-columns: 1fr;
	}

	.detail {
		height: auto;
		border-left: 0;
	}
}
</style>
