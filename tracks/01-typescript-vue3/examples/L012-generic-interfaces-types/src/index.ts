interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

type AsyncState<T> =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'success'; data: T }
  | { status: 'error'; message: string }

type User = {
  id: number
  name: string
  role: 'admin' | 'viewer'
}

function unwrapResponse<T>(response: ApiResponse<T>): T {
  if (response.code !== 0) {
    throw new Error(response.message)
  }

  return response.data
}

function createSuccessState<T>(data: T): AsyncState<T> {
  return {
    status: 'success',
    data
  }
}

function formatUserPageState(state: AsyncState<PageResult<User>>): string {
  switch (state.status) {
    case 'idle':
      return '尚未请求'
    case 'loading':
      return '加载中'
    case 'error':
      return `加载失败：${state.message}`
    case 'success':
      return `共 ${state.data.total} 个用户：${state.data.list.map((user) => user.name).join('、')}`
  }
}

const response: ApiResponse<PageResult<User>> = {
  code: 0,
  message: 'ok',
  data: {
    list: [
      { id: 1, name: 'Alice', role: 'admin' },
      { id: 2, name: 'Bob', role: 'viewer' }
    ],
    total: 2,
    page: 1,
    pageSize: 10
  }
}

const page = unwrapResponse(response)
const state = createSuccessState(page)

console.log(formatUserPageState(state))

if (false) {
  const errorState: AsyncState<PageResult<User>> = {
    status: 'error',
    message: '网络错误'
  }

  // @ts-expect-error: error 状态没有 data 字段。
  console.log(errorState.data)

  const wrongResponse: ApiResponse<User> = {
    code: 0,
    message: 'ok',
    data: {
      // @ts-expect-error: ApiResponse<User> 的 data 必须是 User。
      title: '不是用户'
    }
  }

  console.log(wrongResponse)
}
