type UserRole = 'admin' | 'editor' | 'viewer'

type User = {
  id: number
  name: string
  role: UserRole
}

function formatUser(user: User): string {
  return `${user.name} (${user.role.toUpperCase()})`
}

const apiUser: User = {
  id: 1,
  name: 'Alice',
  role: 'admin'
}

console.log(formatUser(apiUser))

const wrongApiUser = {
  id: 2,
  name: 'Bob',
  roles: ['editor']
}

if (false) {
  // @ts-expect-error: wrongApiUser 缺少 role 字段，不能当作 User 使用。
  formatUser(wrongApiUser)
}
