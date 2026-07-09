type UserRole = 'admin' | 'operator' | 'viewer'

type ApiUser = {
  id: string
  name: string
  email: string
  role: UserRole
  enabled: boolean
  createdAt: string
  updatedAt?: string
}

type CreateUserPayload = Omit<ApiUser, 'id' | 'createdAt' | 'updatedAt'>
type UpdateUserPayload = Partial<Pick<ApiUser, 'name' | 'email' | 'role' | 'enabled'>>
type UserListItem = Pick<ApiUser, 'id' | 'name' | 'role' | 'enabled'>
type CompleteApiUser = Required<ApiUser>

const roleLabels: Record<UserRole, string> = {
  admin: '管理员',
  operator: '运营',
  viewer: '访客'
}

function createUser(payload: CreateUserPayload): ApiUser {
  return {
    id: 'u-001',
    ...payload,
    createdAt: '2026-07-09T10:00:00.000Z'
  }
}

function updateUser(user: Readonly<ApiUser>, patch: UpdateUserPayload): ApiUser {
  return {
    ...user,
    ...patch,
    updatedAt: '2026-07-09T10:30:00.000Z'
  }
}

function normalizeUser(user: ApiUser): CompleteApiUser {
  return {
    ...user,
    updatedAt: user.updatedAt ?? user.createdAt
  }
}

function toListItem(user: ApiUser): UserListItem {
  return {
    id: user.id,
    name: user.name,
    role: user.role,
    enabled: user.enabled
  }
}

function buildAuditLog(action: 'create' | 'update', user: UserListItem) {
  return {
    action,
    targetId: user.id,
    text: `${user.name}（${roleLabels[user.role]}）已${action === 'create' ? '创建' : '更新'}`
  }
}

type AuditLog = ReturnType<typeof buildAuditLog>
type UpdateUserArgs = Parameters<typeof updateUser>

const createdUser = createUser({
  name: 'Alice',
  email: 'alice@example.com',
  role: 'operator',
  enabled: true
})

const updateArgs: UpdateUserArgs = [createdUser, { enabled: false }]
const updatedUser = updateUser(...updateArgs)
const completeUser = normalizeUser(updatedUser)
const listItem = toListItem(completeUser)
const auditLog: AuditLog = buildAuditLog('update', listItem)

console.log(`列表项：${listItem.name} / ${roleLabels[listItem.role]} / ${listItem.enabled ? '启用' : '停用'}`)
console.log(`更新时间：${completeUser.updatedAt}`)
console.log(`审计日志：${auditLog.text}`)

if (false) {
  const readonlyUser: Readonly<ApiUser> = createdUser

  // @ts-expect-error: Readonly<ApiUser> 的属性不能重新赋值。
  readonlyUser.name = 'Bob'

  const badCreatePayload: CreateUserPayload = {
    name: 'Bob',
    email: 'bob@example.com',
    role: 'viewer',
    enabled: true,
    // @ts-expect-error: 创建参数不能包含后端生成的 id。
    id: 'u-999'
  }

  // @ts-expect-error: Record<UserRole, string> 必须覆盖 viewer。
  const badRoleLabels: Record<UserRole, string> = {
    admin: '管理员',
    operator: '运营'
  }

  console.log(badCreatePayload, badRoleLabels)
}
