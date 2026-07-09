# L011 泛型约束

上一节我们学习了泛型函数：用类型参数保留输入和输出之间的类型关系。本节继续学习泛型约束。

泛型默认很自由。自由是好事，但函数内部往往需要使用某些字段或能力。比如你想读取 `item.id`，那就不能允许任何类型都传进来；你需要告诉 TypeScript：这个泛型可以灵活，但至少要有 `id`。

这就是泛型约束，英文是 generic constraints。

参考：

- https://www.typescriptlang.org/docs/handbook/2/generics.html

## 为什么需要泛型约束

先看一个泛型函数：

```ts
function first<T>(items: T[]): T | undefined {
  return items[0]
}
```

这个函数不关心 `T` 有哪些字段，所以无需约束。

但如果你想写一个“按 id 建索引”的函数：

```ts
function indexById<T>(items: T[]) {
  // item.id ?
}
```

这里就有问题：任意 `T` 不一定有 `id`。TypeScript 不允许你直接访问 `item.id`，因为这对没有 `id` 的类型不安全。

应该写成：

```ts
function indexById<T extends { id: number }>(items: T[]): Record<number, T> {
  // ...
}
```

`T extends { id: number }` 的意思是：`T` 可以是更具体的类型，但必须至少包含数字类型的 `id` 字段。

## 约束不是替换类型

这一点很关键：约束不是把 `T` 变成 `{ id: number }`。

如果传入的是：

```ts
type User = {
  id: number
  name: string
  role: 'admin' | 'viewer'
}
```

那么 `indexById<User>` 返回的仍然是 `Record<number, User>`，不是 `Record<number, { id: number }>`。

泛型约束只规定最低要求，具体类型信息仍然会被保留下来。

## 示例：列表工具函数

示例目录：

```text
tracks/01-typescript-vue3/examples/L011-generic-constraints/
```

`src/index.ts`：

```ts
type User = {
  id: number
  name: string
  role: 'admin' | 'viewer'
}

type Product = {
  id: number
  title: string
  price: number
}

function indexById<T extends { id: number }>(items: T[]): Record<number, T> {
  const result: Record<number, T> = {}

  for (const item of items) {
    result[item.id] = item
  }

  return result
}

function pickField<T, K extends keyof T>(item: T, key: K): T[K] {
  return item[key]
}

function describeLength<T extends { length: number }>(value: T): string {
  return `长度是 ${value.length}`
}

const users: User[] = [
  { id: 1, name: 'Alice', role: 'admin' },
  { id: 2, name: 'Bob', role: 'viewer' }
]

const products: Product[] = [
  { id: 101, title: 'TypeScript 小册', price: 69 },
  { id: 102, title: 'Vue 3 实战课', price: 199 }
]

const usersById = indexById(users)
const productsById = indexById(products)

const firstUserName = pickField(users[0], 'name')
const firstProductPrice = pickField(products[0], 'price')

console.log(usersById[1].name)
console.log(productsById[102].title)
console.log(`${firstUserName} / ¥${firstProductPrice}`)
console.log(describeLength('TypeScript'))
console.log(describeLength(products))

if (false) {
  const tags = ['vue', 'typescript']

  // @ts-expect-error: string[] 没有 number 类型的 id 字段，不能用于 indexById。
  indexById(tags)

  // @ts-expect-error: User 没有 email 字段。
  pickField(users[0], 'email')

  // @ts-expect-error: number 没有 length 字段。
  describeLength(123)
}
```

## 关键代码解析

```ts
function indexById<T extends { id: number }>(items: T[]): Record<number, T> {
```

`T extends { id: number }` 是泛型约束。它允许传入 `User`、`Product` 这类更具体的类型，但要求它们至少有 `id: number`。

`Record<number, T>` 是 TypeScript 内置工具类型，意思是“键是数字，值是 T 的对象”。后面会专门讲常用 Utility Types，这里先把它当作索引表理解。

```ts
const result: Record<number, T> = {}

for (const item of items) {
  result[item.id] = item
}
```

因为 `T` 被约束为至少有 `id`，所以这里可以安全访问 `item.id`。同时，`item` 的完整类型仍然保留：用户索引里的值还是 `User`，商品索引里的值还是 `Product`。

```ts
function pickField<T, K extends keyof T>(item: T, key: K): T[K] {
  return item[key]
}
```

这是另一个常见约束。

`keyof T` 表示 `T` 的所有字段名组成的联合类型。如果 `T` 是 `User`，那么 `keyof T` 大致是 `'id' | 'name' | 'role'`。

`K extends keyof T` 表示 `key` 必须是 `item` 上真实存在的字段名。

`T[K]` 表示这个字段对应的值类型。比如：

- `pickField(user, 'name')` 返回 `string`。
- `pickField(product, 'price')` 返回 `number`。

这比返回 `unknown` 或 `any` 精确得多。

```ts
function describeLength<T extends { length: number }>(value: T): string {
  return `长度是 ${value.length}`
}
```

这个函数只要求传入值有 `length`。字符串、数组都可以；数字不行。

这类约束适合“我不关心你具体是什么，只关心你有某个能力”的场景。

## 和 Vue 3 业务代码的联系

列表页常常需要按 ID 建索引：

```ts
const usersById = computed(() => indexById(users.value))
```

表格组件、筛选器、详情抽屉也经常需要按字段名取值：

```ts
const value = pickField(row, column.key)
```

如果 `column.key` 被约束为 `keyof Row`，就能避免写出不存在的列字段。

Composable 里也会出现能力约束：

```ts
function useSelection<T extends { id: number }>() {
  // ...
}
```

这表示这个选择逻辑能复用在用户、商品、订单上，但前提是它们都有 `id`。

## 常见误区

不要用 `T extends any`。这没有实际约束，和没写差不多。

不要为了访问字段直接写 `as any`。如果函数确实需要某个字段，就用泛型约束把要求写出来。

不要把约束写得过宽。`T extends object` 只能保证是对象，不能保证有 `id`、`name` 或 `length`。

不要把约束写得过窄。如果函数只需要 `id`，就不要要求传入完整 `User`，否则复用性会下降。

## 运行和已验证结果

进入示例目录：

```bash
cd tracks/01-typescript-vue3/examples/L011-generic-constraints
```

已真实执行：

```bash
tsc --noEmit
tsc
node dist/index.js
sed -n '1,220p' dist/index.js
```

`tsc --noEmit` 成功且无输出，说明类型检查通过，并且三个 `@ts-expect-error` 标记确实对应类型错误。

`tsc` 成功且无输出，并生成 `dist/index.js`。

`node dist/index.js` 的实际输出：

```text
Alice
Vue 3 实战课
Alice / ¥69
长度是 10
长度是 2
```

`sed -n '1,220p' dist/index.js` 已确认泛型约束在编译后被擦除，运行时只剩普通 JavaScript 逻辑。

## 练习

新增一个函数：

```ts
function getDisplayName<T extends { name?: string; title?: string }>(item: T): string {
  return item.name ?? item.title ?? '未命名'
}
```

然后分别传入 `users[0]` 和 `products[0]`。

思考：这个约束为什么允许 `name` 和 `title` 都是可选的？如果业务要求必须至少有一个字段，这个类型还够不够精确？

## 下一步

下一节学习泛型接口与泛型类型别名。我们会把泛型从函数扩展到更稳定的数据结构，例如接口响应、表单状态和异步请求状态。
