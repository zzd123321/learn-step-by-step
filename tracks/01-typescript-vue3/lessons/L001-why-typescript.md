# L001 TypeScript 到底解决了什么问题：从 JavaScript 的运行时错误到类型系统

## 1. 学习目标

学完本节，你应该能说清楚：

- TypeScript 主要解决的是哪一类 JavaScript 维护问题。
- TypeScript 和 JavaScript 的关系：类型检查发生在运行前，运行时仍然是 JavaScript。
- 为什么“接口数据形状不稳定”会让 Vue 项目越来越难维护。
- 如何通过一个最小示例真实运行并观察 TypeScript 的价值。

## 2. 前置知识

你需要已经理解：

- JavaScript 对象、函数调用和数组渲染。
- Promise、接口返回数据和页面字段展示。
- Vue 2 项目里常见的“字段写错、接口变更、运行后才报错”的问题。

## 3. 为什么这个知识点对 Vue 2 前端开发者重要

在 Vue 2 项目中，很多错误并不是语法错误，而是数据契约错误。

例如接口原来返回：

```js
{
  id: 1,
  name: 'Alice',
  role: 'admin'
}
```

页面代码可能写：

```js
user.role.toUpperCase()
```

如果后端某天把字段改成 `roles`，或者某条数据的 `role` 是 `null`，JavaScript 不会在你写代码时提醒你。它通常会等到浏览器真正运行到这一行时才报错。

TypeScript 的核心价值，是把一部分“运行时才知道”的错误，提前到“写代码和构建时就知道”。

## 4. 概念解析

### JavaScript 的特点

JavaScript 是动态类型语言。动态类型的意思是：变量可以在运行时绑定任何类型的值。

```js
let count = 1
count = '1'
```

这很灵活，但在大型项目中也会带来问题：调用方和被调用方之间缺少稳定契约。

### TypeScript 的特点

TypeScript 是 JavaScript 的超集。超集的意思是：合法的 JavaScript 大多也是合法的 TypeScript，但 TypeScript 额外增加了类型系统。

类型系统不会直接改变浏览器或 Node.js 的运行规则。TypeScript 文件通常会先经过类型检查和编译，最后输出 JavaScript，再交给运行时执行。

一个简化流程是：

```text
写 TypeScript 源码
  -> TypeScript 编译器做类型检查
  -> 输出 JavaScript
  -> 浏览器或 Node.js 执行 JavaScript
```

### 类型系统解决的不是所有错误

TypeScript 能提前发现很多“值的形状不匹配”问题，例如：

- 把字符串当成数字使用。
- 访问不存在的对象属性。
- 忘记处理可能为 `undefined` 的数据。
- 调用函数时传错参数类型。

但它不能保证：

- 接口一定不会 500。
- 用户输入一定合法。
- 数据库一定有数据。
- 业务逻辑一定正确。

所以更准确的理解是：TypeScript 帮你把“代码层面的数据契约”写清楚，并在运行前检查这份契约。

## 5. 心智模型

可以把 TypeScript 想成一份“运行前的合同检查员”。

- JavaScript 负责真正运行代码。
- TypeScript 负责在运行前检查“你说的数据形状”和“你实际使用数据的方式”是否匹配。

它不替你做业务决策，也不替你发请求，但它会在你把 `user.role` 写成 `user.roles`、把 `number` 当成 `string`、或者忘记处理空值时尽早提醒你。

对 Vue 3 和 Node.js 来说，这份合同尤其重要，因为前端组件、接口请求层、状态管理和后端 API 都在传递数据。数据传得越远，契约越重要。

## 6. 与 JavaScript 的差异

JavaScript 代码可以这样写：

```js
function formatUser(user) {
  return `${user.name} (${user.role.toUpperCase()})`
}
```

这段代码假设：

- `user` 一定有 `name`。
- `user` 一定有 `role`。
- `role` 一定是字符串。

但这些假设只存在于开发者脑子里，JavaScript 不会提前检查。

TypeScript 会鼓励你把这些假设写成明确类型：

```ts
type User = {
  id: number
  name: string
  role: 'admin' | 'editor' | 'viewer'
}

function formatUser(user: User): string {
  return `${user.name} (${user.role.toUpperCase()})`
}
```

现在，“用户对象应该长什么样”不再只靠记忆，而是变成了代码中的契约。

## 7. 可运行示例

示例目录：

```text
tracks/01-typescript-vue3/examples/L001-why-typescript/
```

文件结构：

```text
package.json
tsconfig.json
src/runtime-error.js
src/typed-solution.ts
```

### `src/runtime-error.js`

这个文件展示 JavaScript 的典型问题：字段形状不对时，错误会在运行时出现。

```js
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
```

### `src/typed-solution.ts`

这个文件展示 TypeScript 的做法：先声明用户对象契约，再让函数按契约接收数据。

```ts
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
```

`@ts-expect-error` 的意思是：下一行应该出现类型错误。这里保留它，是为了让示例既能通过编译，又能明确展示 TypeScript 本来会拦住的问题。你在练习时可以临时删掉这一行，再运行类型检查，亲自看错误提示。

## 8. 代码逐行说明

### JavaScript 示例

```js
function formatUser(user) {
```

定义一个函数，但没有说明 `user` 必须包含哪些字段。

```js
  return `${user.name} (${user.role.toUpperCase()})`
```

代码直接使用 `user.name` 和 `user.role`，并假设 `role` 一定是字符串。

```js
const apiUser = {
  id: 1,
  name: 'Alice',
  roles: ['admin']
}
```

模拟接口返回值。这里故意使用 `roles`，而不是函数需要的 `role`。

```js
try {
  console.log(formatUser(apiUser))
} catch (error) {
  console.error('JavaScript 运行时错误:', error.message)
}
```

代码只有在运行到 `formatUser(apiUser)` 时才发现 `role` 是 `undefined`，于是 `undefined.toUpperCase()` 报错。

### TypeScript 示例

```ts
type UserRole = 'admin' | 'editor' | 'viewer'
```

定义用户角色只能是三个固定字符串之一。这个叫字面量联合类型，后续会单独学习。

```ts
type User = {
  id: number
  name: string
  role: UserRole
}
```

定义用户对象的形状：必须有数字类型的 `id`、字符串类型的 `name` 和指定范围内的 `role`。

```ts
function formatUser(user: User): string {
```

声明函数参数必须是 `User`，返回值必须是字符串。

```ts
  return `${user.name} (${user.role.toUpperCase()})`
```

因为 `role` 已经被声明为字符串字面量联合类型，所以可以安全调用 `toUpperCase()`。

```ts
const apiUser: User = {
  id: 1,
  name: 'Alice',
  role: 'admin'
}
```

声明一个符合 `User` 契约的对象。

```ts
console.log(formatUser(apiUser))
```

传入符合契约的数据，类型检查通过，运行也正常。

```ts
const wrongApiUser = {
  id: 2,
  name: 'Bob',
  roles: ['editor']
}
```

模拟字段写错的接口数据。

```ts
if (false) {
  // @ts-expect-error: wrongApiUser 缺少 role 字段，不能当作 User 使用。
  formatUser(wrongApiUser)
}
```

`if (false)` 让这段代码不会在运行时执行，但 TypeScript 仍然会检查里面的代码。TypeScript 会发现 `wrongApiUser` 没有 `role`，不能传给 `formatUser`。这一类问题在 JavaScript 中往往要等到用户点开页面才暴露。

## 9. 运行方式

进入示例目录：

```bash
cd tracks/01-typescript-vue3/examples/L001-why-typescript
```

运行 JavaScript 示例：

```bash
node src/runtime-error.js
```

执行 TypeScript 类型检查：

```bash
tsc --noEmit
```

编译 TypeScript：

```bash
tsc
```

运行编译后的 JavaScript：

```bash
node dist/typed-solution.js
```

## 10. 预期结果与真实验证结果

本节已真实执行以下命令。

### `node src/runtime-error.js`

预期和实际结果都是：

```text
JavaScript 运行时错误: Cannot read properties of undefined (reading 'toUpperCase')
```

### `tsc --noEmit`

预期和实际结果都是：命令成功，无输出。

这里无输出代表类型检查通过。示例中的错误行被 `@ts-expect-error` 标记为“预期存在错误”，所以编译器会确认这行确实有错误，但不会让命令失败。

### `tsc`

预期和实际结果都是：命令成功，无输出，并生成 `dist/typed-solution.js`。

### `node dist/typed-solution.js`

预期和实际结果都是：

```text
Alice (ADMIN)
```

## 11. 常见错误

### 误区 1：TypeScript 会让运行时变得更安全

不准确。TypeScript 的类型检查主要发生在运行前。代码真正运行时，执行的仍然是 JavaScript。

如果接口返回了不符合类型声明的数据，而你没有做运行时校验，运行时仍然可能出错。

### 误区 2：所有地方都应该马上写满类型标注

不需要。TypeScript 有类型推断能力。能从右侧值清楚推断出来的类型，可以先让编译器推断。

例如：

```ts
const count = 1
```

这里通常不必写成：

```ts
const count: number = 1
```

### 误区 3：用了 `any` 就等于用了 TypeScript

`any` 的意思是“跳过类型检查”。它适合用在迁移旧代码、临时接入未知数据时，但不应该变成默认选择。

```ts
function formatUser(user: any) {
  return user.role.toUpperCase()
}
```

这段代码形式上是 TypeScript，实际又回到了 JavaScript 的不确定状态。

### 误区 4：接口类型等于接口真实数据

类型声明只是你对数据的描述，不是运行时验证。后端真的返回什么，仍然要通过联调、测试和必要的运行时校验确认。

## 12. 小练习

在 `src/typed-solution.ts` 中新增一个用户：

```ts
const viewerUser: User = {
  id: 3,
  name: 'Cindy',
  role: 'viewer'
}
```

然后调用：

```ts
console.log(formatUser(viewerUser))
```

要求：

- 运行 `tsc --noEmit`，确认类型检查通过。
- 运行 `tsc`，再运行 `node dist/typed-solution.js`。
- 观察输出中是否多了一行 `Cindy (VIEWER)`。

进阶思考：如果把 `role` 改成 `'owner'`，TypeScript 会在什么时候提醒你？

## 13. 贴近真实业务的应用场景

假设你正在维护一个后台管理系统，有一个用户列表接口：

```ts
type UserListItem = {
  id: number
  name: string
  role: 'admin' | 'editor' | 'viewer'
}
```

当列表组件、详情抽屉、权限按钮和编辑弹窗都依赖 `role` 时，类型契约可以帮你统一字段名和字段取值范围。

如果后端字段改名，或者前端某处误写成 `roles`，类型检查可以在提交前提醒你，而不是等测试或用户打开页面才发现。

## 14. 本节复盘问题

- TypeScript 的类型检查发生在运行前还是运行时？
- 为什么说 TypeScript 最擅长提前发现“数据契约不匹配”的问题？
- `any` 为什么会削弱 TypeScript 的价值？
- 类型声明能不能保证接口真实返回的数据一定正确？

## 15. 下一节预告

下一节学习：TypeScript 编译流程与 `tsconfig.json` 入门。

我们会拆开看 `tsc` 做了什么、`noEmit` 是什么、为什么类型检查通过不等于业务一定正确，以及一个项目如何通过 `tsconfig.json` 管理类型检查规则。
