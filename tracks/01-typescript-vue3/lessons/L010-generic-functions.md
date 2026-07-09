# L010 泛型函数入门

前面我们已经能用具体类型描述对象、数组、联合结果和外部输入边界。本节开始学习泛型，先只讲一个最常见、最实用的入口：泛型函数。

泛型的核心不是“写一个尖括号看起来高级”，而是让类型像参数一样被传入，并保持输入和输出之间的类型关系。

参考：

- https://www.typescriptlang.org/docs/handbook/2/generics.html

## 为什么需要泛型函数

先看一个普通函数：

```ts
function firstString(items: string[]): string | undefined {
  return items[0]
}
```

它只能处理字符串数组。

如果你再写数字数组：

```ts
function firstNumber(items: number[]): number | undefined {
  return items[0]
}
```

逻辑一模一样，只是类型不同。你当然可以用 `any`：

```ts
function firstAny(items: any[]): any {
  return items[0]
}
```

但 `any` 会丢掉类型关系。调用 `firstAny(users)` 以后，返回值是 `any`，TypeScript 不再知道它是不是用户对象。

泛型函数解决的正是这个问题：

```ts
function first<T>(items: T[]): T | undefined {
  return items[0]
}
```

`T` 是类型参数。调用时如果传入 `User[]`，返回值就是 `User | undefined`；传入 `number[]`，返回值就是 `number | undefined`。

## 泛型函数的心智模型

普通函数参数传的是值：

```ts
formatPrice(199)
```

泛型参数传的是类型：

```ts
first<User>(users)
```

多数时候不用手写 `<User>`，TypeScript 会根据实参自动推断：

```ts
const user = first(users)
```

重点是：泛型让函数逻辑复用，但不牺牲具体类型。

## 示例：分页数据和列表映射

示例目录：

```text
tracks/01-typescript-vue3/examples/L010-generic-functions/
```

`src/index.ts`：

```ts
type User = {
  id: number
  name: string
  role: 'admin' | 'editor' | 'viewer'
}

type Product = {
  id: number
  title: string
  price: number
}

type PageResult<T> = {
  list: T[]
  total: number
  page: number
  pageSize: number
}

function first<T>(items: T[]): T | undefined {
  return items[0]
}

function mapPage<T, U>(page: PageResult<T>, mapper: (item: T) => U): PageResult<U> {
  return {
    ...page,
    list: page.list.map(mapper)
  }
}

function getPageSummary<T>(page: PageResult<T>): string {
  return `第 ${page.page} 页，每页 ${page.pageSize} 条，共 ${page.total} 条`
}

const userPage: PageResult<User> = {
  list: [
    { id: 1, name: 'Alice', role: 'admin' },
    { id: 2, name: 'Bob', role: 'viewer' }
  ],
  total: 2,
  page: 1,
  pageSize: 10
}

const productPage: PageResult<Product> = {
  list: [
    { id: 101, title: 'TypeScript 小册', price: 69 },
    { id: 102, title: 'Vue 3 实战课', price: 199 }
  ],
  total: 2,
  page: 1,
  pageSize: 10
}

const firstUser = first(userPage.list)
const productTitles = mapPage(productPage, (product) => `${product.title}：¥${product.price}`)

console.log(firstUser ? `${firstUser.name} / ${firstUser.role}` : '没有用户')
console.log(getPageSummary(userPage))
console.log(productTitles.list.join('；'))

if (false) {
  const firstProduct = first(productPage.list)

  // @ts-expect-error: Product 没有 name 字段，泛型保留了具体元素类型。
  console.log(firstProduct?.name)

  const userNames = mapPage(userPage, (user) => user.name)

  // @ts-expect-error: 映射后 list 是 string[]，不是 User[]。
  const users: User[] = userNames.list

  console.log(users)
}
```

## 关键代码解析

```ts
type PageResult<T> = {
  list: T[]
  total: number
  page: number
  pageSize: number
}
```

`PageResult<T>` 是泛型类型。`T` 代表列表项类型。`PageResult<User>` 表示用户分页，`PageResult<Product>` 表示商品分页。

这很贴近接口请求层：分页结构通常固定，但 `list` 中的元素会随接口不同而变化。

```ts
function first<T>(items: T[]): T | undefined {
  return items[0]
}
```

`T` 把参数和返回值关联起来。参数是 `T[]`，返回值就是 `T | undefined`。之所以有 `undefined`，是因为数组可能为空。

如果用 `any`，返回值会失去具体类型；用泛型，返回值仍然知道自己来自哪个数组。

```ts
function mapPage<T, U>(page: PageResult<T>, mapper: (item: T) => U): PageResult<U> {
```

这里有两个类型参数：

- `T`：原始列表项类型。
- `U`：映射后的列表项类型。

`mapper` 接收 `T`，返回 `U`。最终函数返回 `PageResult<U>`。

```ts
return {
  ...page,
  list: page.list.map(mapper)
}
```

分页元信息保持不变，只替换 `list`。比如商品分页经过映射后，可以变成字符串分页：`PageResult<string>`。

```ts
function getPageSummary<T>(page: PageResult<T>): string {
```

这个函数不关心 `T` 具体是什么，因为它只读取分页元信息。即使 `T` 没有在返回值中出现，泛型仍然能表达“任何分页结果都可以传进来”。

## 和 Vue 3 业务代码的联系

接口层经常会写成这样：

```ts
type PageResult<T> = {
  list: T[]
  total: number
}

async function fetchPage<T>(url: string): Promise<PageResult<T>> {
  // ...
}
```

组件里可以使用具体类型：

```ts
const userPage = ref<PageResult<User>>({
  list: [],
  total: 0
})
```

Composable 也经常需要泛型：

```ts
function useSelected<T>() {
  const selected = ref<T | null>(null)
  return { selected }
}
```

泛型让“逻辑复用”和“具体类型安全”同时成立。

## 常见误区

不要把泛型写成 `any` 的替代拼写。泛型的价值是保留类型关系，`any` 是放弃类型关系。

不要在不需要类型关系时硬写泛型。如果函数只返回字符串摘要，且不关心列表项类型，可能普通参数类型就够了。

不要给类型参数起太随意的名字。短函数里 `T`、`U` 很常见；业务泛型也可以叫 `TItem`、`TData`，更清楚。

不要忘记空数组。`first<T>` 返回 `T | undefined`，是因为数组可能没有第一项。

## 运行和已验证结果

进入示例目录：

```bash
cd tracks/01-typescript-vue3/examples/L010-generic-functions
```

已真实执行：

```bash
tsc --noEmit
tsc
node dist/index.js
sed -n '1,220p' dist/index.js
```

`tsc --noEmit` 成功且无输出，说明类型检查通过，并且两个 `@ts-expect-error` 标记确实对应类型错误。

`tsc` 成功且无输出，并生成 `dist/index.js`。

`node dist/index.js` 的实际输出：

```text
Alice / admin
第 1 页，每页 10 条，共 2 条
TypeScript 小册：¥69；Vue 3 实战课：¥199
```

`sed -n '1,220p' dist/index.js` 已确认泛型类型参数在编译后被擦除，运行时只剩普通 JavaScript 函数。

## 练习

新增一个泛型函数：

```ts
function last<T>(items: T[]): T | undefined {
  return items[items.length - 1]
}
```

要求：

- 用 `last(userPage.list)` 获取最后一个用户。
- 用 `last(productPage.list)` 获取最后一个商品。
- 分别访问用户的 `name` 和商品的 `title`，确认 TypeScript 能推断出不同返回类型。

运行：

```bash
tsc --noEmit
tsc
node dist/index.js
```

## 下一步

下一节继续学习泛型约束。我们会看如何允许泛型保持灵活，同时要求它至少拥有某些字段，例如 `id`、`name` 或 `length`。
