type ApiTodo = {
  kind: 'todo'
  id: number
  title: string
  completed: boolean
}

type ApiMessage = {
  kind: 'message'
  id: number
  from: string
  text: string
}

type ApiEvent = ApiTodo | ApiMessage

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function isApiTodo(value: unknown): value is ApiTodo {
  return (
    isRecord(value) &&
    'kind' in value &&
    value.kind === 'todo' &&
    typeof value.id === 'number' &&
    typeof value.title === 'string' &&
    typeof value.completed === 'boolean'
  )
}

function isApiMessage(value: unknown): value is ApiMessage {
  return (
    isRecord(value) &&
    'kind' in value &&
    value.kind === 'message' &&
    typeof value.id === 'number' &&
    typeof value.from === 'string' &&
    typeof value.text === 'string'
  )
}

function parseEvent(value: unknown): ApiEvent | null {
  if (isApiTodo(value)) {
    return value
  }

  if (isApiMessage(value)) {
    return value
  }

  return null
}

function parseEvents(input: unknown): ApiEvent[] {
  if (!Array.isArray(input)) {
    return []
  }

  return input.map(parseEvent).filter((event): event is ApiEvent => event !== null)
}

function formatEvent(event: ApiEvent): string {
  if (event.kind === 'todo') {
    const state = event.completed ? '已完成' : '未完成'
    return `任务 #${event.id}: ${event.title}（${state}）`
  }

  return `消息 #${event.id}: ${event.from} 说「${event.text}」`
}

const rawEvents: unknown = [
  {
    kind: 'todo',
    id: 1,
    title: '学习类型收窄',
    completed: false
  },
  {
    kind: 'message',
    id: 2,
    from: '系统',
    text: '欢迎继续学习 TypeScript'
  },
  {
    kind: 'todo',
    id: 'wrong-id',
    title: '这条数据会被过滤',
    completed: false
  }
]

const events = parseEvents(rawEvents)

for (const event of events) {
  console.log(formatEvent(event))
}

if (false) {
  const rawValue: unknown = { kind: 'todo', id: 3 }

  // @ts-expect-error: unknown 不能直接当作 ApiEvent 使用。
  formatEvent(rawValue)

  const message: ApiMessage = {
    kind: 'message',
    id: 4,
    from: 'Alice',
    text: 'hello'
  }

  // @ts-expect-error: ApiMessage 没有 title 字段。
  console.log(message.title)
}
