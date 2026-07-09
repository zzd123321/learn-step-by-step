# L022 错误处理模型

前面我们学习过 `unknown`、联合类型、判别联合和 `never`。这一节把它们放到真实业务里：错误处理。

先给结论：

> 可预期的业务失败应该进入明确的结果类型；真正异常才用 `throw` 和 `catch` 处理。`catch` 到的错误要先当作 `unknown`，再收窄。

这会直接影响 Vue 3 页面请求、Node.js API、表单提交和接口错误展示。

## 为什么 Vue 2 前端开发者要学它

Vue 2 项目里常见写法是：

```js
try {
  const res = await getUser(id)
  this.user = res.data
} catch (error) {
  this.$message.error(error.message)
}
```

这段代码表面简单，但有几个隐患：

- `error` 不一定是 `Error` 实例。
- 用户不存在、用户被禁用这类业务失败，不一定应该当成程序异常。
- UI 不知道失败有哪些稳定类型，只能展示字符串。
- 接口层、页面层、日志层可能各自处理一遍错误。

TypeScript 的价值不是让 `catch` 多写几个类型，而是帮你把失败分成不同层次。

## 三类失败

第一类是成功：

```ts
{ ok: true, data: user }
```

第二类是可预期业务失败：

```ts
{ ok: false, code: 'USER_NOT_FOUND', message: '用户不存在' }
```

例如用户不存在、余额不足、权限不够、表单校验失败。这类失败是业务流程的一部分，不一定需要抛异常。

第三类是真正异常：

```ts
throw new Error('用户 id 不能为空')
```

例如不合法输入进入底层函数、JSON 解析失败、第三方库抛错、网络层出现未知异常。这类问题适合用 `try/catch` 兜住。

## 用判别联合表达结果

本节使用一个结果类型：

```ts
type AppResult<T> =
  | { ok: true; data: T }
  | { ok: false; code: BusinessErrorCode; message: string }
```

`ok` 是判别字段。

当 `ok === true` 时，结果有 `data`。

当 `ok === false` 时，结果有 `code` 和 `message`。

这比“成功返回数据，失败返回 null 或抛异常”更清楚。调用者必须处理两种情况，TypeScript 也会帮你检查有没有直接访问不存在的字段。

## 可运行示例：用户查询的失败分层

示例目录：

```text
tracks/01-typescript-vue3/examples/L022-error-handling-model/
```

`src/index.ts`：

```ts
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
```

## 代码解析

```ts
type BusinessErrorCode = 'USER_NOT_FOUND' | 'USER_DISABLED'
```

业务错误码用字面量联合表示。这样 UI 或接口层可以稳定判断错误类型，而不是解析错误文案。

```ts
type AppResult<T> =
  | { ok: true; data: T }
  | { ok: false; code: BusinessErrorCode; message: string }
```

这是泛型判别联合。成功时有 `data`，失败时有 `code` 和 `message`。

泛型 `T` 让这个结果类型可以复用：`AppResult<User>`、`AppResult<Order>`、`AppResult<Product[]>` 都可以。

```ts
function findUser(id: string): AppResult<User> {
```

`findUser` 不抛出“用户不存在”或“用户禁用”的异常，而是把它们作为业务失败返回。这样调用者能在类型层面看见失败分支。

```ts
if (!user) {
  return {
    ok: false,
    code: 'USER_NOT_FOUND',
    message: '用户不存在'
  }
}
```

这是可预期业务失败。用户不存在不是程序崩了，而是一个稳定业务状态。

```ts
function parseUserId(raw: string): string {
```

`parseUserId` 负责把外部输入变成内部可用 ID。空字符串进入这里，说明输入不合法，所以它抛出异常。

```ts
throw new Error('用户 id 不能为空')
```

这里使用 `throw`，因为这个函数无法返回一个合法 ID。真实项目中，表单层也可以提前校验，避免异常走到更深层。

```ts
function toErrorMessage(error: unknown): string {
```

`catch` 到的错误应该先按 `unknown` 处理。因为 JavaScript 允许抛出任何值：

```ts
throw 'bad'
throw { message: 'bad' }
```

所以不能直接假设 `error.message` 一定存在。

```ts
if (error instanceof Error) {
  return error.message
}
```

这是类型收窄。只有确认它是 `Error` 实例后，才能安全读取 `message`。

```ts
function renderUserResult(result: AppResult<User>): string {
  if (result.ok) {
    return `用户：${result.data.name}`
  }

  return `业务失败：${result.code} / ${result.message}`
}
```

`if (result.ok)` 会把 `result` 收窄到成功分支，因此可以访问 `data`。

离开成功分支后，TypeScript 知道剩下的是失败分支，因此可以访问 `code` 和 `message`。

```ts
try {
  const id = parseUserId(rawId)
  const result = findUser(id)
  return renderUserResult(result)
} catch (error) {
```

`try/catch` 包住的是可能抛异常的输入解析和后续流程。但业务失败仍然通过 `AppResult` 正常返回，不会落到 `catch`。

最后的 `if (false)` 中，`result` 还没有经过 `ok` 判断，不能直接访问 `result.data`。`@ts-expect-error` 验证 TypeScript 确实会阻止这个危险访问。

## 运行与真实验证

在示例目录执行：

```bash
tsc --noEmit
tsc
node dist/index.js
sed -n '1,240p' dist/index.js
```

实际运行输出：

```text
用户：Alice
业务失败：USER_DISABLED / 用户已被禁用
业务失败：USER_NOT_FOUND / 用户不存在
异常失败：用户 id 不能为空
```

## 常见误区

第一个误区：所有失败都用异常。

用户不存在、余额不足、权限不够通常是业务状态。它们可以用结果类型表达，让调用方明确处理。

第二个误区：`catch (error)` 里直接读 `error.message`。

在严格配置下，`catch` 变量应该按 `unknown` 处理。先判断 `error instanceof Error`，再读取 `message`。

第三个误区：用 `null` 表示所有失败。

```ts
function findUser(id: string): User | null
```

这种写法只能告诉你“没拿到用户”，但无法表达为什么失败。业务错误码能让 UI 和日志更稳定。

第四个误区：把错误文案当成业务判断依据。

不要写：

```ts
if (message === '用户不存在') {}
```

文案会变，语言会变。业务判断应该依赖稳定的 `code`。

## 一个小练习

给示例新增一个错误码：

```ts
type BusinessErrorCode = 'USER_NOT_FOUND' | 'USER_DISABLED' | 'INVALID_AMOUNT'
```

然后新增一个 `pay(userId: string, amount: number): AppResult<{ receiptId: string }>`：

1. 当 `amount <= 0` 时返回 `INVALID_AMOUNT`。
2. 先调用 `findUser(userId)`。
3. 如果用户查询失败，直接返回同样的业务失败。
4. 成功时返回 `{ receiptId: 'r-001' }`。

练习重点：让业务失败沿着类型安全的结果对象传递，而不是到处抛异常。

## 真实业务场景

在 Vue 3 页面里，接口请求通常会有三层失败：

- HTTP 或网络异常：请求失败、超时、服务器不可达。
- 接口业务失败：后端返回错误码和消息。
- 前端输入异常：表单数据不合法、调用参数不合法。

你可以让请求层把后端错误统一转换成类似 `AppResult<T>` 的结构。页面层拿到结果后，根据 `ok` 渲染成功态或失败态。真正未知的异常再走统一日志和兜底提示。

在 Node.js 后端里，也可以用类似模型区分：

- 可预期业务错误：返回 400、403、404。
- 未知异常：记录日志，返回 500。

## 本节复盘

你可以用下面几个问题检查自己：

1. 可预期业务失败和真正异常有什么区别？
2. `AppResult<T>` 为什么要用 `ok` 作为判别字段？
3. 为什么 `catch` 到的错误应该先当作 `unknown`？
4. 为什么不建议用错误文案做业务判断？
5. `result.ok` 判断后，TypeScript 如何帮助你访问正确字段？

下一节建议学习 L023：基础测试。我们会讲清楚类型检查和自动化测试各自负责什么，以及如何用最小 Node.js 示例写一个不用额外依赖的测试。
