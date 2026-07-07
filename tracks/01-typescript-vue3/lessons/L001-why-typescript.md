# L001 TypeScript 到底解决了什么问题

本节先不急着学语法。我们先回答一个更重要的问题：你已经会写 JavaScript，也有 Vue 2 项目经验，为什么还要引入 TypeScript？

TypeScript 的核心价值不是“让代码看起来更高级”，而是把一部分原本要等到运行时才暴露的问题，提前到编写代码、编辑器提示和构建检查阶段发现。

官方文档把 TypeScript 定位为 JavaScript 的静态类型检查器，也强调 TypeScript 会保留 JavaScript 的运行时行为，并在编译后擦除类型。也就是说，TypeScript 不会发明一套新的运行时规则；它是在 JavaScript 运行之前帮你检查数据使用方式是否合理。

参考：

- https://www.typescriptlang.org/docs/handbook/typescript-from-scratch.html
- https://www.typescriptlang.org/docs/handbook/2/everyday-types.html

## 从 Vue 2 项目里的真实问题说起

在后台管理系统里，接口字段写错、后端字段变更、空值没有处理，是很常见的运行时错误来源。

假设页面原来拿到的用户数据长这样：

```js
{
  id: 1,
  name: 'Alice',
  role: 'admin'
}
```

页面展示时写了：

```js
user.role.toUpperCase()
```

这段代码隐含了三个假设：

- `user` 一定是对象。
- `user.role` 一定存在。
- `user.role` 一定是字符串。

JavaScript 不会在你写代码时检查这些假设。如果某次接口返回的是 `roles: ['admin']`，或者 `role: null`，错误会等到代码真正运行到这一行时才爆出来。

TypeScript 要解决的就是这类“数据契约没有写清楚”的问题。

## TypeScript 和 JavaScript 的关系

TypeScript 是 JavaScript 的超集。简单说，绝大多数合法 JavaScript 代码也可以作为 TypeScript 代码，但 TypeScript 额外增加了类型系统。

一个简化流程是：

```text
编写 TypeScript 源码
  -> TypeScript 编译器检查类型
  -> 编译输出 JavaScript
  -> Node.js 或浏览器执行 JavaScript
```

这里有两个关键边界：

- 类型检查发生在运行前。
- 真正运行的仍然是 JavaScript。

所以 TypeScript 可以提前发现“访问不存在的属性”“把字符串当数字用”“函数参数传错类型”等问题，但它不能保证接口一定成功、网络一定稳定、用户输入一定符合业务规则。

## 最小示例：同一个字段错误，JavaScript 和 TypeScript 怎么表现

示例目录：

```text
tracks/01-typescript-vue3/examples/L001-why-typescript/
```

JavaScript 示例故意模拟接口字段不匹配：

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

`formatUser` 期望 `user.role`，但 `apiUser` 实际提供的是 `roles`。JavaScript 允许这段代码写出来，也允许它运行；直到执行 `user.role.toUpperCase()` 时，才发现 `user.role` 是 `undefined`。

TypeScript 示例把用户对象的形状写成明确契约：

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

这段 TypeScript 代码里，`User` 是用户对象的契约。`formatUser` 声明自己只接收 `User`，于是 `wrongApiUser` 这种缺少 `role` 的对象不能被当成合法参数。

`if (false)` 里的代码不会在运行时执行，但 TypeScript 仍然会检查它。`@ts-expect-error` 表示下一行应该出现类型错误；如果那一行没有错误，类型检查反而会失败。这里用它来保留一个可验证的反例。

## 关键语句拆解

```ts
type UserRole = 'admin' | 'editor' | 'viewer'
```

`UserRole` 把角色限制在三个固定字符串中。这比普通 `string` 更贴近业务，因为后台权限系统里的角色通常不是任意文本。

```ts
type User = {
  id: number
  name: string
  role: UserRole
}
```

`User` 描述对象结构：必须有 `id`、`name` 和 `role`。这类类型很适合放在接口数据、页面状态、组件 Props 或 Store 状态的边界上。

```ts
function formatUser(user: User): string {
```

`user: User` 约束调用方传入的数据形状，`: string` 约束函数返回值。以后如果有人把返回值改成对象或数字，类型检查会提醒。

```ts
return `${user.name} (${user.role.toUpperCase()})`
```

因为 `role` 已经是字符串字面量联合类型，所以可以调用 `toUpperCase()`。这不是 TypeScript 在运行时保护了你，而是它在运行前确认了 `role` 的类型。

## 运行和已验证结果

进入示例目录：

```bash
cd tracks/01-typescript-vue3/examples/L001-why-typescript
```

已真实执行：

```bash
node src/runtime-error.js
tsc --noEmit
tsc
node dist/typed-solution.js
```

`node src/runtime-error.js` 的实际输出：

```text
JavaScript 运行时错误: Cannot read properties of undefined (reading 'toUpperCase')
```

`tsc --noEmit` 和 `tsc` 均成功且无输出。

`node dist/typed-solution.js` 的实际输出：

```text
Alice (ADMIN)
```

## 重要边界

TypeScript 能提前检查代码里写出的类型契约，但不能替你验证外部世界。

例如你写了：

```ts
const apiUser: User = await fetchUser()
```

这只是告诉 TypeScript“我相信 `fetchUser()` 返回 `User`”。如果后端真实返回了错误结构，而你没有做运行时校验，运行时仍然可能出错。

所以真实项目里要分清两层：

- TypeScript 类型：约束代码内部如何使用数据。
- 运行时校验：确认接口、用户输入、本地缓存等外部数据真的符合预期。

## 常见误区

`any` 不是类型安全。`any` 表示跳过类型检查，适合迁移旧代码时少量过渡，但不应该成为默认选择。

类型不会留在运行时。TypeScript 编译后会输出 JavaScript，类型标注会被擦除。

类型声明不等于接口真实数据。类型能帮助前端代码保持一致，但不能让后端自动按这个类型返回。

## 练习

在 `src/typed-solution.ts` 中新增：

```ts
const viewerUser: User = {
  id: 3,
  name: 'Cindy',
  role: 'viewer'
}

console.log(formatUser(viewerUser))
```

然后依次运行：

```bash
tsc --noEmit
tsc
node dist/typed-solution.js
```

观察输出是否多了：

```text
Cindy (VIEWER)
```

再把 `role` 改成 `'owner'`，看看 TypeScript 会在什么阶段提醒你。

## 下一步

下一节学习 TypeScript 编译流程与 `tsconfig.json`。你会看到 `tsc --noEmit`、`tsc`、`strict`、`include`、`outDir` 分别控制什么，以及为什么编译后的 JavaScript 里没有类型。
