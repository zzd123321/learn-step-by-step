function formatUser(user) {
  return `${user.name} (${user.role.toUpperCase()})`
}

const apiUser = {
  id: 1,
  name: 'Alice',
  roles: ['admin']
}

try {
  console.log(formatUser(apiUser))
} catch (error) {
  console.error('JavaScript 运行时错误:', error.message)
}
