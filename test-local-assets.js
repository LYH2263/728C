const fs = require('fs')
const path = require('path')
const assert = require('assert')

const projectRoot = __dirname
const sqlPath = path.join(projectRoot, 'database', 'init.sql')
const publicRoot = path.join(projectRoot, 'frontend', 'public')
const sqlContent = fs.readFileSync(sqlPath, 'utf8')

function extractQuotedValues(block) {
  const matches = block.match(/'((?:\\'|[^'])*)'/g) || []
  return matches.map((item) => item.slice(1, -1))
}

function extractLocalPathsFromJsonArray(raw) {
  if (!raw.startsWith('[')) return []
  const values = JSON.parse(raw)
  return values.filter((item) => typeof item === 'string')
}

function fileExistsForPublicPath(publicPath) {
  const fullPath = path.join(publicRoot, publicPath.replace(/^\//, ''))
  return fs.existsSync(fullPath)
}

const assetPaths = []

const userInsertMatch = sqlContent.match(/INSERT INTO `users`[\s\S]*?;/)
assert(userInsertMatch, '未找到 users 初始化数据')
for (const value of extractQuotedValues(userInsertMatch[0])) {
  if (value.startsWith('/avatars/') || value.startsWith('http')) {
    assetPaths.push(value)
  }
}

const gameInsertMatch = sqlContent.match(/INSERT INTO `games`[\s\S]*?;/)
assert(gameInsertMatch, '未找到 games 初始化数据')
for (const value of extractQuotedValues(gameInsertMatch[0])) {
  if (value.startsWith('/')) {
    assetPaths.push(value)
    continue
  }

  if (value.startsWith('http')) {
    assetPaths.push(value)
    continue
  }

  if (value.startsWith('[')) {
    assetPaths.push(...extractLocalPathsFromJsonArray(value))
  }
}

const remoteAssets = assetPaths.filter((item) => /^https?:\/\//.test(item))
assert.strictEqual(
  remoteAssets.length,
  0,
  `仍存在外链图片:\n${remoteAssets.join('\n')}`
)

const missingLocalAssets = [...new Set(assetPaths)]
  .filter((item) => item.startsWith('/'))
  .filter((item) => !fileExistsForPublicPath(item))

assert.strictEqual(
  missingLocalAssets.length,
  0,
  `以下本地资源不存在:\n${missingLocalAssets.join('\n')}`
)

console.log(`校验通过，共检查 ${assetPaths.length} 个资源引用`)
