# L008 类型收窄与类型守卫

上一节我们用联合类型表达“提交可能成功，也可能失败”。本节继续往下走：当一个值有多种可能时，TypeScript 怎么知道你在某个分支里处理的是哪一种？

答案是类型收窄和类型守卫。

- 类型收窄，英文是 narrowing，指 TypeScript 根据控制流判断，把一个较宽的类型缩小成更具体的类型。
- 类型守卫，英文是 type guard，指那些能帮助 TypeScript 判断类型的运行时检查，例如 `typeof`、`Array.isArray`、`in`，以及返回 `value is SomeType` 的自定义函数。

参考：

- https://www.typescriptlang.org/docs/handbook/2/narrowing.html

## 为什么前端特别需要类型收窄

Vue 前端项目经常要处理外部数据：

- 接口返回值。
- 路由参数。
- 本地缓存。
- 用户输入。
- 第三方 SDK 回调。

这些数据在进入你的代码之前，并不真的受 TypeScript 保护。即使你在代码里声明了类型，运行时也可能收到错误结构。

所以更稳妥的习惯是：外部输入先用 `unknown` 接住，再通过运行时判断把它收窄成可信类型。

```ts
function parseEvent(value: unknown): ApiEvent | null {
  if (isApiTodo(value)) {
    return value
  }

  if (isApiMessage(value)) {
    return value
  }

  return null
}
```

这里的 `unknown` 表示“我还不知道它是什么”。只有通过 `isApiTodo` 或 `isApiMessage` 检查后，代码才把它当成合法事件使用。

## 常见类型守卫

`typeof` 适合检查 JavaScript 基础类型：

```ts
typeof value === 'string'
typeof value === 'number'
typeof value === 'boolean'
```

`Array.isArray` 适合检查数组：

```ts
Array.isArray(value)
```

`in` 适合检查对象是否有某个属性：

```ts
'kind' in value
```

判别字段适合收窄联合类型：

```ts
if (event.kind === 'todo') {
  // event 是 TodoEvent
}
```

自定义类型守卫适合把复杂判断封装起来：

```ts
function isApiTodo(value: unknown): value is ApiTodo {
  // ...
}
```

`value is ApiTodo` 是类型谓词，意思是：如果这个函数返回 `true`，TypeScript 就可以把 `value` 当成 `ApiTodo`。

## 示例：解析接口事件列表

示例目录：

```text
tracks/01-typescript-vue3/examples/L008-narrowing-type-guards/
```

`src/index.ts`：

```ts
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
```

## 关键代码解析

```ts
function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}
```

`typeof value === 'object'` 可以排除字符串、数字、布尔值等基础类型。`value !== null` 很重要，因为 JavaScript 里 `typeof null` 的结果也是 `'object'`。

`Record<string, unknown>` 可以理解为“一个键是字符串、值未知的普通对象”。它不代表对象已经符合业务类型，只代表现在可以安全地检查属性。

```ts
function isApiTodo(value: unknown): value is ApiTodo {
```

这是自定义类型守卫。返回类型不是普通 `boolean`，而是 `value is ApiTodo`。当它返回 `true` 时，TypeScript 会在对应分支里把 `value` 收窄为 `ApiTodo`。

```ts
isRecord(value) &&
'kind' in value &&
value.kind === 'todo'
```

先确认它是对象，再确认有 `kind` 字段，再判断 `kind` 的具体值。这个顺序很重要：不要在还不知道它是不是对象时就访问属性。

```ts
typeof value.id === 'number' &&
typeof value.title === 'string' &&
typeof value.completed === 'boolean'
```

这些检查把运行时数据结构和 TypeScript 类型对齐。只有字段都满足要求，才把它当成 `ApiTodo`。

```ts
function parseEvents(input: unknown): ApiEvent[] {
  if (!Array.isArray(input)) {
    return []
  }

  return input.map(parseEvent).filter((event): event is ApiEvent => event !== null)
}
```

外部输入 `input` 先是 `unknown`。通过 `Array.isArray` 后，TypeScript 知道它是数组。

`map(parseEvent)` 会得到 `(ApiEvent | null)[]`。因为每一项可能解析失败，所以需要过滤掉 `null`。

`(event): event is ApiEvent => event !== null` 是另一个自定义类型守卫。它告诉 TypeScript：过滤后数组里只剩 `ApiEvent`。

```ts
function formatEvent(event: ApiEvent): string {
  if (event.kind === 'todo') {
    const state = event.completed ? '已完成' : '未完成'
    return `任务 #${event.id}: ${event.title}（${state}）`
  }

  return `消息 #${event.id}: ${event.from} 说「${event.text}」`
}
```

`ApiEvent` 是 `ApiTodo | ApiMessage`。进入 `event.kind === 'todo'` 分支后，TypeScript 知道 `event` 是 `ApiTodo`，所以可以访问 `title` 和 `completed`。

离开这个分支后，只剩 `ApiMessage`，所以可以访问 `from` 和 `text`。

## 和 Vue 3 业务代码的联系

接口请求层很适合使用这种模式：

```ts
async function fetchEvents(): Promise<ApiEvent[]> {
  const data: unknown = await request('/events')
  return parseEvents(data)
}
```

组件里拿到的就是已经被收窄过的数据：

```ts
const events = ref<ApiEvent[]>([])
```

模板或计算属性里再根据 `kind` 分支渲染：

```ts
const labels = computed(() => events.value.map(formatEvent))
```

这样接口边界负责处理未知数据，组件内部就能使用更明确的类型。

## 常见误区

不要用 `any` 接外部数据。`any` 会绕过检查，`unknown` 会迫使你先判断。

不要只检查 `kind`，就假设其他字段都正确。外部数据可能伪造出 `kind: 'todo'`，但 `id`、`title`、`completed` 仍然是错的。

不要在判断对象前访问属性。先确认 `typeof value === 'object' && value !== null`，再检查字段。

不要忘记数组过滤后的类型。普通 `filter(Boolean)` 在很多情况下不能表达业务类型；自定义类型谓词更明确。

## 运行和已验证结果

进入示例目录：

```bash
cd tracks/01-typescript-vue3/examples/L008-narrowing-type-guards
```

已真实执行：

```bash
tsc --noEmit
tsc
node dist/index.js
sed -n '1,240p' dist/index.js
```

`tsc --noEmit` 成功且无输出，说明类型检查通过，并且两个 `@ts-expect-error` 标记确实对应类型错误。

`tsc` 成功且无输出，并生成 `dist/index.js`。

`node dist/index.js` 的实际输出：

```text
任务 #1: 学习类型收窄（未完成）
消息 #2: 系统 说「欢迎继续学习 TypeScript」
```

第三条原始数据的 `id` 是字符串，不符合 `ApiTodo`，所以被过滤掉。

`sed -n '1,240p' dist/index.js` 已确认类型谓词、联合类型和参数类型都在编译后被擦除，只留下运行时判断逻辑。

## 练习

新增一种事件：

```ts
type ApiWarning = {
  kind: 'warning'
  id: number
  message: string
}
```

然后完成三件事：

- 把 `ApiEvent` 扩展为 `ApiTodo | ApiMessage | ApiWarning`。
- 新增 `isApiWarning`。
- 修改 `parseEvent` 和 `formatEvent`，让 warning 能被解析并输出。

运行：

```bash
tsc --noEmit
tsc
node dist/index.js
```

观察：如果只扩展了 `ApiEvent`，但忘记修改 `formatEvent`，TypeScript 会不会立刻提醒？这个问题会引出下一节的 `never` 和穷尽检查。

## 下一步

下一节学习 `never`、`unknown`、`any` 的边界。我们会把“未知输入如何进入系统”“哪些分支不应该发生”“什么时候不得不用 any”讲清楚。
