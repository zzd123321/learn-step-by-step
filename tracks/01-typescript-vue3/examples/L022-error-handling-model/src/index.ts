type User = {
  id: string
  name: string
}

type BusinessErrorCode = 'USER_NOT_FOUND' | 'USER_DISABLED'

type AppResult<T> =
  | { ok: true; data: T }
  | { ok: false; code: BusinessErrorCode; message: string }

const users: Record<string, User & { enabled: boolean }> = {
  'u-001': { id: 'u-001', name: 'Alice', enabled: true },
  'u-002': { id: 'u-002', name: 'Bob', enabled: false }
}

function findUser(id: string): AppResult<User> {
  const user = users[id]

  if (!user) {
    return {
      ok: false,
      code: 'USER_NOT_FOUND',
      message: '用户不存在'
    }
  }

  if (!user.enabled) {
    return {
      ok: false,
      code: 'USER_DISABLED',
      message: '用户已被禁用'
    }
  }

  return {
    ok: true,
    data: {
      id: user.id,
      name: user.name
    }
  }
}

function parseUserId(raw: string): string {
  const trimmed = raw.trim()

  if (trimmed === '') {
    throw new Error('用户 id 不能为空')
  }

  return trimmed
}

function toErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message
  }

  return '未知错误'
}

function renderUserResult(result: AppResult<User>): string {
  if (result.ok) {
    return `用户：${result.data.name}`
  }

  return `业务失败：${result.code} / ${result.message}`
}

function handleRequest(rawId: string): string {
  try {
    const id = parseUserId(rawId)
    const result = findUser(id)
    return renderUserResult(result)
  } catch (error) {
    return `异常失败：${toErrorMessage(error)}`
  }
}

console.log(handleRequest('u-001'))
console.log(handleRequest('u-002'))
console.log(handleRequest('u-999'))
console.log(handleRequest('   '))

if (false) {
  const result = findUser('u-001')

  // @ts-expect-error: 未收窄到 ok: true 前，不能直接访问 data。
  console.log(result.data.name)
}
