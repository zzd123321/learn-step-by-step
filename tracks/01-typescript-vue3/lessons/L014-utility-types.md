# L014 常用 Utility Types

上一节我们学习了条件类型、映射类型和 `infer`。TypeScript 内置的 Utility Types（工具类型）可以理解为官方提前写好的常用类型转换函数。它们不会生成运行时代码，只在编译期帮助你从已有类型派生出新类型。

本节只建立一条主线：

> 不要复制粘贴一堆相似类型，而是用 Utility Types 表达“这个类型来自哪里、改了什么”。

官方参考：

- https://www.typescriptlang.org/docs/handbook/utility-types.html

## 为什么 Vue 2 前端开发者要学它

在 Vue 2 后台项目里，你大概率写过这些结构：

```js
// 用户详情
const user = {
  id: 'u-001',
  name: 'Alice',
  email: 'alice@example.com',
  role: 'operator',
  enabled: true
}

// 创建用户参数：没有 id
const createPayload = {
  name: 'Alice',
  email: 'alice@example.com',
  role: 'operator',
  enabled: true
}

// 更新用户参数：只传变化字段
const updatePayload = {
  enabled: false
}
```

这些对象之间不是互不相关的。创建参数来自用户模型，但去掉了后端生成字段；更新参数来自用户模型，但所有字段都可选；列表项来自用户模型，但只展示少数字段。

如果每个类型都手写一份，字段一多就会失去同步。Utility Types 的作用就是把这种关系直接写出来。

## 先看一组高频工具

### `Pick<T, K>`：只挑出部分字段

```ts
type UserListItem = Pick<ApiUser, 'id' | 'name' | 'role' | 'enabled'>
```

`Pick` 适合做列表项、卡片摘要、弹窗里只需要的部分数据。

它表达的是：

> 从 `ApiUser` 中挑出 `id`、`name`、`role`、`enabled` 这些字段。

### `Omit<T, K>`：移除部分字段

```ts
type CreateUserPayload = Omit<ApiUser, 'id' | 'createdAt' | 'updatedAt'>
```

`Omit` 常用于创建参数。创建用户时，`id`、`createdAt`、`updatedAt` 通常由后端生成，前端不应该提交。

它表达的是：

> 使用 `ApiUser` 的大部分字段，但去掉后端生成字段。

### `Partial<T>`：所有字段变成可选

```ts
type UpdateUserPayload = Partial<Pick<ApiUser, 'name' | 'email' | 'role' | 'enabled'>>
```

更新接口通常只提交变化字段，所以字段应该是可选的。

这里先用 `Pick` 限制“允许更新哪些字段”，再用 `Partial` 表示“这些字段都可以不传”。这比直接写 `Partial<ApiUser>` 更安全，因为你不会不小心允许更新 `id` 或 `createdAt`。

### `Readonly<T>`：禁止重新赋值属性

```ts
function updateUser(user: Readonly<ApiUser>, patch: UpdateUserPayload): ApiUser {
  return {
    ...user,
    ...patch
  }
}
```

`Readonly` 表示函数内部不应该修改传入对象，而是返回新对象。它适合保护 Props、Store 快照、请求缓存数据等不该被随手改掉的数据。

注意：`Readonly` 是 TypeScript 编译期约束，不等于运行时深冻结。对象在 JavaScript 运行时仍然是普通对象。

### `Record<K, V>`：固定 key 的字典

```ts
const roleLabels: Record<UserRole, string> = {
  admin: '管理员',
  operator: '运营',
  viewer: '访客'
}
```

`Record` 适合写状态文案、权限映射、枚举配置。只要 `UserRole` 新增一种角色，TypeScript 就会要求你补上对应文案。

### `Required<T>`：所有字段变成必填

```ts
type CompleteApiUser = Required<ApiUser>
```

`Required` 是 `Partial` 的反方向。它常用于“经过归一化处理之后，某些原本可选的字段已经被补齐”的数据。

### `ReturnType<T>` 与 `Parameters<T>`：复用函数签名

```ts
type AuditLog = ReturnType<typeof buildAuditLog>
type UpdateUserArgs = Parameters<typeof updateUser>
```

`ReturnType` 提取函数返回值类型，`Parameters` 提取函数参数元组类型。

它们适合在 Composable、请求封装、测试辅助函数里复用已有函数签名，避免手写一份“看起来一样但其实会漂移”的类型。

## 可运行示例：用户管理里的类型派生

示例目录：

```text
tracks/01-typescript-vue3/examples/L014-utility-types/
```

`src/index.ts`：

```ts
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
```

## 代码解析

```ts
type CreateUserPayload = Omit<ApiUser, 'id' | 'createdAt' | 'updatedAt'>
```

这行没有重新描述创建参数的所有字段，而是明确说：创建参数来自 `ApiUser`，但不包含后端生成的三个字段。

这比手写下面这种类型更稳：

```ts
type CreateUserPayload = {
  name: string
  email: string
  role: UserRole
  enabled: boolean
}
```

手写类型的问题是：如果将来 `ApiUser` 新增 `phone`，你需要自己记得同步创建参数。`Omit` 至少让“创建参数和用户模型有关”这件事清楚地留在类型里。

```ts
type UpdateUserPayload = Partial<Pick<ApiUser, 'name' | 'email' | 'role' | 'enabled'>>
```

这行组合了两个工具类型：

1. `Pick<ApiUser, ...>` 先限定允许更新的字段。
2. `Partial<...>` 再把这些字段都变成可选。

它适合 PATCH 更新接口。`{ enabled: false }` 合法，`{ name: 'Bob', role: 'viewer' }` 也合法，但 `{ id: 'x' }` 不合法。

```ts
type UserListItem = Pick<ApiUser, 'id' | 'name' | 'role' | 'enabled'>
```

列表页不需要完整用户详情，只需要展示字段。`Pick` 能清楚表达“列表项是用户模型的一部分”。

```ts
type CompleteApiUser = Required<ApiUser>
```

`ApiUser` 中 `updatedAt` 是可选的，说明接口返回的原始数据可能没有这个字段。经过 `normalizeUser` 处理后，我们用 `Required<ApiUser>` 表示：现在所有字段都已经补齐。

```ts
const roleLabels: Record<UserRole, string> = {
```

`Record<UserRole, string>` 表示这个对象必须以所有 `UserRole` 作为 key，value 都是字符串。

如果 `UserRole` 增加 `'auditor'`，这个对象没有补 `auditor` 文案时，TypeScript 会报错。这对业务状态文案尤其有用。

```ts
function updateUser(user: Readonly<ApiUser>, patch: UpdateUserPayload): ApiUser {
```

第一个参数是 `Readonly<ApiUser>`，表示函数内部不应该直接修改 `user.name`、`user.enabled` 等属性。

函数返回新对象：

```ts
return {
  ...user,
  ...patch,
  updatedAt: '2026-07-09T10:30:00.000Z'
}
```

这和 Vue、Pinia、请求缓存里的“不要随手改输入对象”是同一个方向。虽然 Vue 3 的响应式系统允许修改状态，但对函数参数、接口返回缓存、组件 Props 来说，减少隐式修改会让代码更可预测。

```ts
type AuditLog = ReturnType<typeof buildAuditLog>
```

`buildAuditLog` 的返回值由函数实现决定。`ReturnType` 可以直接提取这个返回值类型。

好处是：以后 `buildAuditLog` 多返回一个 `createdAt` 字段，`AuditLog` 会自动更新。

```ts
type UpdateUserArgs = Parameters<typeof updateUser>
```

`Parameters` 提取的是函数参数元组。这里得到的类型类似：

```ts
type UpdateUserArgs = [user: Readonly<ApiUser>, patch: UpdateUserPayload]
```

所以这行是类型安全的：

```ts
const updateArgs: UpdateUserArgs = [createdUser, { enabled: false }]
```

最后，`if (false)` 里的代码不会执行，但 TypeScript 仍会检查类型。`@ts-expect-error` 要求下一行必须真的报错，这让反例也能参与验证。

## 运行与真实验证

在示例目录执行：

```bash
tsc --noEmit
tsc
node dist/index.js
sed -n '1,220p' dist/index.js
```

实际运行输出：

```text
列表项：Alice / 运营 / 停用
更新时间：2026-07-09T10:30:00.000Z
审计日志：Alice（运营）已更新
```

## 常见误区

第一个误区：直接对整个模型使用 `Partial`。

```ts
type UpdateUserPayload = Partial<ApiUser>
```

这样会允许传入 `id`、`createdAt` 等不该由前端更新的字段。更稳的写法是先 `Pick` 允许更新的字段，再 `Partial`。

第二个误区：以为 `Readonly` 会在运行时阻止修改。

```ts
type ReadonlyUser = Readonly<ApiUser>
```

这只是编译期约束。TypeScript 编译后不会自动调用 `Object.freeze`。如果你需要运行时冻结对象，要显式使用运行时 API。

第三个误区：用 `Record<string, string>` 代替有限枚举映射。

```ts
const labels: Record<string, string> = {}
```

这太宽了。它无法提醒你遗漏某个业务状态。业务状态已知时，应优先使用 `Record<UserRole, string>` 这类有限 key。

第四个误区：为了少写代码，过度堆叠工具类型。

```ts
type X = Partial<Omit<Readonly<Pick<ApiUser, 'id' | 'name'>>, 'id'>>
```

类型可以组合，但可读性也很重要。当组合变得难懂时，拆成几个有业务含义的类型别名更好。

## 一个小练习

基于本节示例新增一个 `ApiProduct`：

```ts
type ApiProduct = {
  id: string
  title: string
  price: number
  visible: boolean
  createdAt: string
}
```

然后完成：

1. 用 `Omit` 定义 `CreateProductPayload`，去掉 `id` 和 `createdAt`。
2. 用 `Partial<Pick<...>>` 定义 `UpdateProductPayload`，只允许更新 `title`、`price`、`visible`。
3. 用 `Pick` 定义 `ProductListItem`，只保留 `id`、`title`、`visible`。
4. 故意在更新参数里写入 `createdAt`，用 `@ts-expect-error` 验证它不能被更新。

## 真实业务场景

在一个 Vue 3 用户管理页面中，常见类型关系可以这样表达：

```ts
type UserCreateForm = CreateUserPayload
type UserEditForm = UpdateUserPayload
type UserTableRow = UserListItem
type UserRoleLabels = Record<UserRole, string>
```

这比“每个页面各写一份差不多的类型”更容易维护。接口模型变化时，你能更快看到哪些创建、更新、列表、映射类型会受到影响。

## 本节复盘

你可以用下面几个问题检查自己：

1. `Pick` 和 `Omit` 分别适合什么场景？
2. 为什么更新接口不建议直接写 `Partial<ApiUser>`？
3. `Record<UserRole, string>` 比 `Record<string, string>` 多提供了什么保护？
4. `Readonly<T>` 是编译期约束还是运行时冻结？
5. `ReturnType<typeof fn>` 和手写返回值类型相比有什么维护优势？

下一节建议学习 L015：类、构造函数与实例类型。我们会进入 TypeScript 的面向对象部分，重点看类在工程代码里的使用边界，而不是把它当作必须使用的默认风格。
