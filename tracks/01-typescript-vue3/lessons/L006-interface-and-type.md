# L006 interface 与 type

到目前为止，我们已经用 `type` 写过对象、数组、元组和字面量联合类型。本节开始区分 TypeScript 中两个非常常见的类型声明工具：`interface` 和 `type`。

它们有重叠能力：都能描述对象结构。区别在于侧重点不同：

- `interface` 更像“对象契约”，适合描述可扩展的对象结构，尤其是组件 Props、接口响应对象、类要实现的形状。
- `type` 更像“类型表达式的名字”，除了对象，还能给联合类型、元组、函数类型、交叉组合等起名。

参考：

- https://www.typescriptlang.org/docs/handbook/2/everyday-types.html
- https://www.typescriptlang.org/docs/handbook/2/objects.html

## interface：描述对象应该长什么样

`interface` 主要用于描述对象结构。

```ts
interface StaffUser {
  id: number
  name: string
  role: UserRole
  department: string
}
```

这段代码的意思是：一个 `StaffUser` 对象必须有 `id`、`name`、`role` 和 `department`。

在 Vue 3 项目里，`interface` 很适合这些场景：

- 描述接口返回的对象结构。
- 描述组件 Props。
- 描述 Store 中比较稳定的对象状态。
- 描述一个类或模块需要满足的对象能力。

`interface` 还支持扩展：

```ts
interface BaseUser {
  id: number
  name: string
}

interface StaffUser extends BaseUser {
  role: UserRole
  department: string
}
```

这里 `StaffUser` 继承了 `BaseUser` 的字段，并额外增加 `role` 和 `department`。这很像真实业务里的“基础用户字段 + 后台员工字段”。

## type：给任意类型表达式起名

`type` 能做对象类型，也能做 `interface` 做不了或不适合做的事情。

字面量联合类型：

```ts
type UserRole = 'admin' | 'editor' | 'viewer'
```

元组：

```ts
type MenuEntry = [string, string]
```

对象组合：

```ts
type UserSummary = StaffUser & {
  label: string
  canEdit: boolean
}
```

这里的 `&` 是交叉类型，中文可以理解为“同时拥有”。`UserSummary` 既有 `StaffUser` 的字段，也有 `label` 和 `canEdit`。

你不用急着掌握交叉类型的所有细节；本节只需要先建立一个判断：当你要给“联合、元组、组合后的类型表达式”起名时，`type` 通常更自然。

## 示例：从员工对象生成页面摘要

示例目录：

```text
tracks/01-typescript-vue3/examples/L006-interface-and-type/
```

`src/index.ts`：

```ts
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
```

## 关键代码解析

```ts
type UserRole = 'admin' | 'editor' | 'viewer'
```

角色是有限集合，所以用 `type` 声明字面量联合类型。`interface` 不能直接表达这种“几个值里选一个”的类型。

```ts
interface BaseUser {
  id: number
  name: string
}

interface StaffUser extends BaseUser {
  role: UserRole
  department: string
}
```

这两段展示 `interface` 的扩展能力。`BaseUser` 是基础用户对象，`StaffUser` 在它的基础上增加后台系统需要的字段。

这在真实项目里很常见：列表项、详情对象、当前登录用户，往往共享一些基础字段，但又各自扩展。

```ts
type UserSummary = StaffUser & {
  label: string
  canEdit: boolean
}
```

`UserSummary` 是一个组合类型。它保留原始员工字段，同时增加页面展示字段 `label` 和权限字段 `canEdit`。

这类类型常用于“接口数据到视图数据”的转换。后端返回的是业务对象，前端页面往往还需要追加展示文案、按钮权限、格式化后的金额或状态。

```ts
type RoleOption = [UserRole, string]
```

这里用 `type` 给元组起名。第 1 项是角色值，第 2 项是展示文案。元组适合这种轻量配置对。

```ts
function getRoleLabel(role: UserRole): string {
  const option = roleOptions.find(([value]) => value === role)
  return option ? option[1] : '未知角色'
}
```

函数入口用 `UserRole`，所以调用方不能传入非法角色。`find` 可能找不到结果，因此返回值要处理 `undefined` 的情况。

```ts
function buildUserSummary(user: StaffUser): UserSummary {
```

输入是接口定义的对象契约 `StaffUser`，输出是 `type` 定义的组合结果 `UserSummary`。这个函数很好地展示了 `interface` 和 `type` 的分工：对象实体用 `interface`，组合后的视图结果用 `type`。

```ts
return {
  ...user,
  label: `${user.name}（${roleLabel} / ${user.department}）`,
  canEdit: canEditUser(user)
}
```

`...user` 把原始员工字段复制到返回对象中，再补充页面需要的字段。因为返回值标注为 `UserSummary`，如果漏掉 `label` 或 `canEdit`，类型检查会失败。

## 如何选择 interface 还是 type

可以先用一条实用规则：

```text
描述稳定对象结构：优先 interface
描述联合类型、元组、函数类型、组合类型：优先 type
```

这不是绝对规则，而是团队代码更容易保持一致的起点。

例如：

```ts
interface UserProfile {
  id: number
  name: string
}

type UserRole = 'admin' | 'editor' | 'viewer'
type Point = [number, number]
type ClickHandler = (id: number) => void
```

如果团队已有明确规范，优先跟随团队规范。TypeScript 允许两者在对象类型上有重叠能力，真正重要的是让同一个项目里风格一致。

## 常见误区

不要认为 `interface` 比 `type` 更高级。它们是不同工具，不是上下级关系。

不要为了统一而只用其中一个。只用 `interface` 会让联合类型、元组等表达很别扭；只用 `type` 也会弱化对象契约的扩展语义。

不要滥用扩展。`extends` 适合表达真实的对象层级关系，不适合为了少写几个字段而强行继承。

不要忽略命名语义。`StaffUser`、`UserSummary` 这类名字应该告诉读者它在业务里代表什么，而不是只叫 `Data`、`Info`。

## 运行和已验证结果

进入示例目录：

```bash
cd tracks/01-typescript-vue3/examples/L006-interface-and-type
```

已真实执行：

```bash
tsc --noEmit
tsc
node dist/index.js
sed -n '1,180p' dist/index.js
```

`tsc --noEmit` 成功且无输出，说明类型检查通过，并且两个 `@ts-expect-error` 标记确实对应类型错误。

`tsc` 成功且无输出，并生成 `dist/index.js`。

`node dist/index.js` 的实际输出：

```text
Alice（管理员 / 平台） => 可编辑
Bob（访客 / 运营） => 只读
```

`sed -n '1,180p' dist/index.js` 已确认 `interface`、`type`、函数参数类型和返回值类型都在编译后被擦除。

## 练习

给 `StaffUser` 增加字段：

```ts
active: boolean
```

然后完成三件事：

- 给 `users` 中每个用户补上 `active`。
- 在 `UserSummary` 中增加 `statusText: string`。
- 在 `buildUserSummary` 中根据 `active` 返回 `启用` 或 `停用`，并把它输出到控制台。

运行：

```bash
tsc --noEmit
tsc
node dist/index.js
```

观察：如果只修改了 `StaffUser`，但忘记补 `users` 数组里的字段，TypeScript 会在哪里提醒你？

## 下一步

下一节学习联合类型、交叉类型、可选属性和只读属性。你会开始更系统地描述“一个值可能有几种形态”“对象可以由多个能力组合而来”“字段可能不存在或不允许修改”。
