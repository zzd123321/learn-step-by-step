type UserRole = 'admin' | 'editor' | 'viewer'

interface BaseUser {
  id: number
  name: string
}

interface StaffUser extends BaseUser {
  role: UserRole
  department: string
}

type UserSummary = StaffUser & {
  label: string
  canEdit: boolean
}

type RoleOption = [UserRole, string]

const roleOptions: RoleOption[] = [
  ['admin', '管理员'],
  ['editor', '编辑'],
  ['viewer', '访客']
]

const users: StaffUser[] = [
  {
    id: 1,
    name: 'Alice',
    role: 'admin',
    department: '平台'
  },
  {
    id: 2,
    name: 'Bob',
    role: 'viewer',
    department: '运营'
  }
]

function getRoleLabel(role: UserRole): string {
  const option = roleOptions.find(([value]) => value === role)
  return option ? option[1] : '未知角色'
}

function canEditUser(user: StaffUser): boolean {
  return user.role === 'admin' || user.role === 'editor'
}

function buildUserSummary(user: StaffUser): UserSummary {
  const roleLabel = getRoleLabel(user.role)

  return {
    ...user,
    label: `${user.name}（${roleLabel} / ${user.department}）`,
    canEdit: canEditUser(user)
  }
}

const summaries = users.map(buildUserSummary)

for (const summary of summaries) {
  console.log(`${summary.label} => ${summary.canEdit ? '可编辑' : '只读'}`)
}

if (false) {
  // @ts-expect-error: owner 不是合法的 UserRole。
  buildUserSummary({ id: 3, name: 'Cindy', role: 'owner', department: '销售' })

  // @ts-expect-error: StaffUser 必须包含 department。
  const missingDepartment: StaffUser = { id: 4, name: 'David', role: 'viewer' }

  console.log(missingDepartment)
}
