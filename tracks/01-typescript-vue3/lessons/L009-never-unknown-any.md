# L009 never、unknown 与 any 的边界

上一节我们学习了类型收窄和类型守卫：外部数据先用 `unknown` 接住，再通过运行时判断变成可信类型。本节继续补上三个“边界类型”：`never`、`unknown` 和 `any`。

它们都很重要，但用途完全不同：

- `unknown`：我不知道它是什么，所以使用前必须先检查。
- `never`：这里不应该有任何值，常用于不可能分支和穷尽检查。
- `any`：跳过类型检查，通常只应该作为旧代码或第三方库迁移时的临时隔离区。

参考：

- https://www.typescriptlang.org/docs/handbook/2/functions.html
- https://www.typescriptlang.org/docs/handbook/2/narrowing.html

## unknown：安全的未知

`unknown` 表示“类型未知”。它和 `any` 最大的区别是：`unknown` 不能直接使用。

```ts
function handle(value: unknown) {
  // value.toUpperCase() // 不允许
}
```

你必须先判断：

```ts
function handle(value: unknown) {
  if (typeof value === 'string') {
    return value.toUpperCase()
  }

  return ''
}
```

所以 `unknown` 很适合放在系统入口：

- 接口返回的原始 JSON。
- 本地缓存读取结果。
- 路由 query。
- 第三方 SDK 回调。

它的意思不是“我永远不知道”，而是“我在检查之前不假装知道”。

## any：关闭类型检查的逃生门

`any` 表示“不要检查这个值”。一旦一个值是 `any`，你可以访问任意属性、调用任意方法、传给任意函数，TypeScript 基本不会拦你。

```ts
function unsafeRead(value: any): string {
  return value.deep.missing.title.toUpperCase()
}
```

这段代码在 TypeScript 看来没问题，但运行时很容易炸。

`any` 不是绝对不能用。真实项目里你可能在这些地方临时遇到它：

- 迁移老 JavaScript 模块。
- 第三方库类型缺失。
- 历史接口封装返回 `any`。

更稳的做法是：把 `any` 限制在很小的适配层里，尽快转换成 `unknown` 或明确业务类型，不让它扩散到组件和业务逻辑。

## never：不可能出现的值

`never` 表示“不可能有值”。常见场景有两个。

第一个是函数永远不会正常返回：

```ts
function fail(message: string): never {
  throw new Error(message)
}
```

第二个是联合类型已经被所有分支处理完，剩下的分支应该不可能发生：

```ts
function assertNever(value: never): never {
  throw new Error(`未处理的值：${JSON.stringify(value)}`)
}
```

配合 `switch`，它可以帮助你在新增联合类型成员时发现漏掉的分支。

## 示例：解析命令队列

示例目录：

```text
tracks/01-typescript-vue3/examples/L009-never-unknown-any/
```

`src/index.ts`：

```ts
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
```

## 关键代码解析

```ts
function isCommand(value: unknown): value is Command {
```

外部数据先是 `unknown`。`value is Command` 表示这是一个类型守卫：返回 `true` 时，TypeScript 可以把 `value` 当成 `Command`。

```ts
if (!isRecord(value) || typeof value.kind !== 'string') {
  return false
}
```

先确认它是对象，再确认 `kind` 是字符串。这样后面才能安全地根据 `kind` 做分支。

```ts
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
```

这里既做运行时校验，也给 TypeScript 提供收窄信息。只有字段结构符合对应命令，才会被当成合法 `Command`。

```ts
function parseCommands(input: unknown): Command[] {
  if (!Array.isArray(input)) {
    return []
  }

  return input.filter(isCommand)
}
```

`parseCommands` 是很典型的接口边界函数。输入是 `unknown`，输出是干净的 `Command[]`。组件或业务逻辑只使用输出后的类型，不直接相信原始数据。

```ts
function assertNever(value: never): never {
  throw new Error(`未处理的命令：${JSON.stringify(value)}`)
}
```

`assertNever` 的参数是 `never`。如果某个分支里还有可能的类型，传给它就会报错。

```ts
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
```

这里三个命令都处理完了，所以 `default` 分支里的 `command` 是 `never`。如果以后给 `Command` 新增一种类型，比如 `'archive'`，但忘记修改 `executeCommand`，`assertNever(command)` 就会提醒你漏了分支。

```ts
function parseLegacyJson(text: string): any {
  return JSON.parse(text)
}
```

`JSON.parse` 在 TypeScript 标准库中返回 `any`。这里故意保留 `any`，用来模拟历史代码或第三方库的情况。

```ts
function unsafeReadTitle(value: any): string {
  return value.payload.title.toUpperCase()
}
```

这段代码展示 `any` 的危险：TypeScript 不会检查 `payload` 是否存在，也不会检查 `title` 是否是字符串。它把风险推迟到了运行时。

## 和 Vue 3 业务代码的联系

请求层应该尽量把未知数据挡在外面：

```ts
async function fetchCommands(): Promise<Command[]> {
  const raw: unknown = await request('/commands')
  return parseCommands(raw)
}
```

组件里拿到的是已经清洗过的类型：

```ts
const commands = ref<Command[]>([])
```

组件渲染或事件处理时，可以使用 `executeCommand` 这类穷尽检查函数，避免新增命令类型后漏改 UI 分支。

旧代码如果返回 `any`，建议先包一层适配函数：

```ts
function normalizeLegacyValue(value: any): unknown {
  return value
}
```

然后在 `unknown` 上做正式校验。不要让 `any` 一路流进组件。

## 常见误区

不要把 `unknown` 当成麻烦。它是在提醒你：外部输入不可信，必须先判断。

不要把 `any` 当成快速修复。它会让类型系统失明，尤其容易在接口层和组件层扩散。

不要只在 `default` 里随便 `throw`，而不使用 `never`。普通 `throw` 不能帮你发现新增联合成员后的漏分支，`assertNever` 可以。

不要误以为 `never` 是给业务数据用的普通类型。它表示“不可能”，通常出现在控制流和类型推导的边缘。

## 运行和已验证结果

进入示例目录：

```bash
cd tracks/01-typescript-vue3/examples/L009-never-unknown-any
```

已真实执行：

```bash
tsc --noEmit
tsc
node dist/index.js
sed -n '1,260p' dist/index.js
```

`tsc --noEmit` 成功且无输出，说明类型检查通过，并且两个 `@ts-expect-error` 标记确实对应类型错误。

`tsc` 成功且无输出，并生成 `dist/index.js`。

`node dist/index.js` 的实际输出：

```text
创建任务：学习 unknown
删除任务：#1001
刷新列表
```

第四条原始数据的 `id` 是字符串，不符合 `DeleteCommand`，所以被过滤掉。

`sed -n '1,260p' dist/index.js` 已确认 `unknown`、`never`、`any` 等类型标注都在编译后被擦除。

## 练习

新增一种命令：

```ts
type ArchiveCommand = {
  kind: 'archive'
  id: number
}
```

然后完成三件事：

- 把 `Command` 扩展为包含 `ArchiveCommand`。
- 修改 `isCommand` 支持解析 `archive`。
- 先不要修改 `executeCommand`，观察 `assertNever(command)` 是否提醒你漏分支，再补上输出 `归档任务：#id`。

运行：

```bash
tsc --noEmit
tsc
node dist/index.js
```

## 下一步

下一节开始进入泛型。我们会先学习泛型函数：如何让类型像参数一样被复用，同时仍然保持调用方和返回值之间的类型关系。
