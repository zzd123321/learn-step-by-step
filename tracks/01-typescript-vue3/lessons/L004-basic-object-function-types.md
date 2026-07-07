# L004 基础类型、对象类型与函数类型

前面三节已经解释了 TypeScript 的价值、编译流程，以及类型标注和类型推断的分工。本节开始进入最常用的类型写法：基础类型、对象类型和函数类型。

这些语法看起来简单，但它们是后面 Vue 3 组件 Props、接口响应数据、Pinia Store、Node.js API 参数的地基。真实项目里，大多数类型问题并不是来自高级类型，而是来自基础字段和函数边界没有写清楚。

参考：

- https://www.typescriptlang.org/docs/handbook/2/everyday-types.html
- https://www.typescriptlang.org/docs/handbook/2/functions.html

## 从业务数据开始理解类型

后台系统里常见的数据不是孤立的 `string` 或 `number`，而是一条条业务对象。

例如商品列表中的一行数据：

```ts
type Product = {
  id: number
  name: string
  price: number
  inStock: boolean
  tags: string[]
}
```

这段类型里包含了几类基础写法：

- `number`：数字，例如商品 ID、价格、库存数量。
- `string`：字符串，例如名称、状态文案、接口字段。
- `boolean`：布尔值，例如是否上架、是否有库存、是否加载中。
- `string[]`：字符串数组，例如标签列表。
- 对象类型：用 `{ ... }` 描述多个字段组合后的业务实体。

TypeScript 的基础类型不只是“给变量起个类型名”，更重要的是把业务假设写成代码。比如 `price` 是 `number`，意味着格式化价格时可以做数字计算；`tags` 是 `string[]`，意味着可以安全使用 `join`、`map` 等数组方法。

## 示例：格式化商品列表项

示例目录：

```text
tracks/01-typescript-vue3/examples/L004-basic-object-function-types/
```

`src/index.ts`：

```ts
type Product = {
  id: number
  name: string
  price: number
  inStock: boolean
  tags: string[]
}

type ProductCard = {
  title: string
  priceText: string
  stockText: string
  tagText: string
}

function formatPrice(price: number): string {
  return `¥${price.toFixed(2)}`
}

function formatStock(inStock: boolean): string {
  return inStock ? '有库存' : '缺货'
}

function buildProductCard(product: Product): ProductCard {
  const title = `${product.id}. ${product.name}`
  const priceText = formatPrice(product.price)
  const stockText = formatStock(product.inStock)
  const tagText = product.tags.length > 0 ? product.tags.join(' / ') : '暂无标签'

  return {
    title,
    priceText,
    stockText,
    tagText
  }
}

const products: Product[] = [
  {
    id: 1,
    name: 'Vue 3 实战课',
    price: 199,
    inStock: true,
    tags: ['frontend', 'vue']
  },
  {
    id: 2,
    name: 'TypeScript 入门手册',
    price: 69,
    inStock: false,
    tags: []
  }
]

const cards = products.map(buildProductCard)

for (const card of cards) {
  console.log(`${card.title} | ${card.priceText} | ${card.stockText} | ${card.tagText}`)
}

if (false) {
  // @ts-expect-error: price 必须是 number，不能传 string。
  formatPrice('199')

  // @ts-expect-error: Product 缺少 tags 字段。
  buildProductCard({
    id: 3,
    name: 'Node.js 小课',
    price: 99,
    inStock: true
  })
}
```

## 基础类型不是运行时校验

先看这个函数：

```ts
function formatPrice(price: number): string {
  return `¥${price.toFixed(2)}`
}
```

`price: number` 表示调用这个函数时，参数应该是数字。这样函数内部就可以调用 `toFixed(2)`，不用每次先判断 `price` 是否存在、是否是数字。

但注意：这不是运行时校验。如果你从接口拿到价格字段，后端实际返回了字符串 `"199"`，而你直接把它断言成 `number`，TypeScript 不会在运行时替你转换或校验。

正确的边界意识是：

- TypeScript 类型负责约束代码内部如何使用数据。
- 外部输入仍然需要接口联调、运行时校验或数据转换。

## 对象类型描述业务实体

```ts
type Product = {
  id: number
  name: string
  price: number
  inStock: boolean
  tags: string[]
}
```

对象类型的重点是字段契约。

`id` 是数字，适合用于路由跳转、详情查询或列表 key。`name` 是字符串，适合展示。`price` 是数字，适合计算和格式化。`inStock` 是布尔值，适合控制按钮是否可点击。`tags` 是字符串数组，适合渲染多个标签。

如果写成 JavaScript，调用方只能靠约定记住这些字段。写成 TypeScript 后，字段名和字段类型会进入编辑器提示、类型检查和重构流程。

## 函数类型保护输入和输出

```ts
function buildProductCard(product: Product): ProductCard {
```

这行代码同时定义了两个边界：

- 输入边界：`product` 必须符合 `Product`。
- 输出边界：函数必须返回 `ProductCard`。

在真实 Vue 项目中，这种函数常出现在接口数据到视图数据的转换层。后端给你的可能是 DTO，也就是 Data Transfer Object，中文可以理解为“数据传输对象”；页面真正渲染的可能是 VO，也就是 View Object，中文可以理解为“视图对象”。

`buildProductCard` 就是在做一个很小的 DTO 到 VO 的转换：

```ts
const title = `${product.id}. ${product.name}`
const priceText = formatPrice(product.price)
const stockText = formatStock(product.inStock)
const tagText = product.tags.length > 0 ? product.tags.join(' / ') : '暂无标签'
```

这些局部变量没有手写类型，因为 TypeScript 能从表达式推断：

- `title` 是字符串。
- `priceText` 是 `formatPrice` 的返回值，也就是字符串。
- `stockText` 是 `formatStock` 的返回值，也就是字符串。
- `tagText` 两个分支都是字符串，所以结果是字符串。

这延续了 L003 的规则：边界主动标注，内部尽量推断。

## 数组类型和回调推断

```ts
const products: Product[] = [
  // ...
]
```

`Product[]` 表示这是一个由 `Product` 组成的数组。也可以写成 `Array<Product>`，两种写法在这里等价。初学阶段优先使用 `Product[]`，更短，也更接近日常业务代码。

```ts
const cards = products.map(buildProductCard)
```

`products` 是 `Product[]`，`buildProductCard` 接收 `Product` 并返回 `ProductCard`，所以 TypeScript 可以推断 `cards` 是 `ProductCard[]`。

这就是上下文推断的价值：你不需要给 `cards` 再写一遍类型，编译器能顺着数组和函数签名推出来。

## 反例为什么放在 `if (false)` 里

示例末尾有两段反例：

```ts
if (false) {
  // @ts-expect-error: price 必须是 number，不能传 string。
  formatPrice('199')

  // @ts-expect-error: Product 缺少 tags 字段。
  buildProductCard({
    id: 3,
    name: 'Node.js 小课',
    price: 99,
    inStock: true
  })
}
```

`if (false)` 让代码不会在运行时执行，但 TypeScript 仍然会检查里面的代码。`@ts-expect-error` 表示下一行应该有类型错误。如果未来这行不再报错，类型检查会失败。

这是一种教学示例里很实用的写法：既能保留错误案例，又不影响正常运行。

## 常见误区

不要把 `Number`、`String`、`Boolean` 当成日常类型使用。业务代码里通常用小写的 `number`、`string`、`boolean`。大写版本是 JavaScript 包装对象类型，和基础类型不是同一个概念。

不要过早把所有字段写成可选。比如 `tags?: string[]` 会让每次使用 `product.tags` 时都要处理 `undefined`。如果业务上列表项一定有标签数组，哪怕为空数组，也应该写成 `tags: string[]`。

不要用 `object` 代替明确对象结构。`object` 只能表示“不是基础类型的对象值”，但不能告诉你里面有哪些字段。业务实体应该写出具体字段。

不要把函数返回值全都省略。小工具函数可以推断，但负责数据转换、接口封装、组件输出的函数，显式返回值能防止重构时悄悄改变结构。

## 运行和已验证结果

进入示例目录：

```bash
cd tracks/01-typescript-vue3/examples/L004-basic-object-function-types
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
1. Vue 3 实战课 | ¥199.00 | 有库存 | frontend / vue
2. TypeScript 入门手册 | ¥69.00 | 缺货 | 暂无标签
```

`sed -n '1,180p' dist/index.js` 已确认 `Product`、`ProductCard`、函数参数类型和返回值类型都在编译后被擦除。

## 练习

给 `Product` 增加一个字段：

```ts
rating: number
```

然后完成三件事：

- 给 `products` 里的每个商品补上 `rating`。
- 在 `ProductCard` 中新增 `ratingText: string`。
- 在 `buildProductCard` 中返回类似 `评分 4.8` 的文本，并输出到控制台。

运行：

```bash
tsc --noEmit
tsc
node dist/index.js
```

观察：如果你只改了 `Product`，但忘记更新数组里的商品对象，TypeScript 会在哪一步提醒你？

## 下一步

下一节继续围绕数据建模，学习数组、元组和字面量类型。它们会帮助你描述列表数据、固定结构数据，以及“状态只能是几个固定值之一”的业务规则。
