type CreateCommand = {
  kind: 'create'
  title: string
}

type DeleteCommand = {
  kind: 'delete'
  id: number
}

type RefreshCommand = {
  kind: 'refresh'
}

type Command = CreateCommand | DeleteCommand | RefreshCommand

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function isCommand(value: unknown): value is Command {
  if (!isRecord(value) || typeof value.kind !== 'string') {
    return false
  }

  switch (value.kind) {
    case 'create':
      return typeof value.title === 'string'
    case 'delete':
      return typeof value.id === 'number'
    case 'refresh':
      return true
    default:
      return false
  }
}

function parseCommands(input: unknown): Command[] {
  if (!Array.isArray(input)) {
    return []
  }

  return input.filter(isCommand)
}

function assertNever(value: never): never {
  throw new Error(`未处理的命令：${JSON.stringify(value)}`)
}

function executeCommand(command: Command): string {
  switch (command.kind) {
    case 'create':
      return `创建任务：${command.title}`
    case 'delete':
      return `删除任务：#${command.id}`
    case 'refresh':
      return '刷新列表'
    default:
      return assertNever(command)
  }
}

function parseLegacyJson(text: string): any {
  return JSON.parse(text)
}

function unsafeReadTitle(value: any): string {
  return value.payload.title.toUpperCase()
}

const rawCommands: unknown = [
  { kind: 'create', title: '学习 unknown' },
  { kind: 'delete', id: 1001 },
  { kind: 'refresh' },
  { kind: 'delete', id: 'wrong-id' }
]

const commands = parseCommands(rawCommands)

for (const command of commands) {
  console.log(executeCommand(command))
}

if (false) {
  const rawValue: unknown = { kind: 'create', title: 'demo' }

  // @ts-expect-error: unknown 不能直接当作 Command 使用。
  executeCommand(rawValue)

  const legacyValue = parseLegacyJson('{ "payload": null }')

  // TypeScript 不会阻止这行，但运行时会有风险，这就是 any 的问题。
  unsafeReadTitle(legacyValue)

  function executeWithoutRefresh(command: Command): string {
    switch (command.kind) {
      case 'create':
        return command.title
      case 'delete':
        return String(command.id)
      default:
        // @ts-expect-error: refresh 分支没有处理完，所以 command 还不是 never。
        return assertNever(command)
    }
  }

  console.log(executeWithoutRefresh)
}
