import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { describe, it } from 'node:test'
import { fileURLToPath } from 'node:url'

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..')

function source(path) {
  return readFileSync(resolve(root, path), 'utf8')
}

describe('login page contract', () => {
  const login = source('src/pages/Login.vue')

  it('renders third-party login providers', () => {
    assert.match(login, /provider-wechat/)
    assert.match(login, /provider-weibo/)
    assert.match(login, /provider-qq/)
    assert.match(login, /provider-alipay/)
    assert.match(login, /handleThirdLogin/)
  })

  it('keeps login, register and browse entry points', () => {
    assert.match(login, /handleLogin/)
    assert.match(login, /handleRegister/)
    assert.match(login, /goHome/)
    assert.match(login, /\/pages\/Index/)
  })
})

describe('forum index page contract', () => {
  const index = source('src/pages/Index.vue')

  it('contains the publishing workflow controls', () => {
    assert.match(index, /postForm/)
    assert.match(index, /submitPost/)
    assert.match(index, /postTypes/)
    assert.match(index, /stockCodesText/)
  })

  it('contains the post detail workflow and interaction counters', () => {
    assert.match(index, /selectedPost/)
    assert.match(index, /class="detail"/)
    assert.match(index, /detail-count/)
    assert.match(index, /interact\('LIKE'\)/)
    assert.match(index, /interact\('FAVORITE'\)/)
    assert.match(index, /submitComment/)
    assert.match(index, /reportPost/)
  })

  it('contains auth guarded social workflows', () => {
    assert.match(index, /ensureLogin/)
    assert.match(index, /loadFollowingFeed/)
    assert.match(index, /followAuthor/)
    assert.match(index, /sendMessage/)
    assert.match(index, /createGroup/)
    assert.match(index, /joinGroup/)
  })

  it('contains admin review and operation entry points', () => {
    assert.match(index, /isAdminRole/)
    assert.match(index, /loadAdmin/)
    assert.match(index, /reviewPost/)
    assert.match(index, /resolveReport/)
    assert.match(index, /createBoard/)
    assert.match(index, /pendingPosts/)
  })
})
