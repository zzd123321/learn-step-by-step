# L002 TypeScript 编译流程与 tsconfig.json 入门

## 1. 学习目标

学完本节，你应该能说清楚：

- TypeScript 编译器 `tsc` 做了哪几件事。
- `tsc --noEmit` 和 `tsc` 的区别。
- `tsconfig.json` 在项目里负责什么。
- 为什么“类型检查通过”不等于“运行时一定正确”。
- 编译后的 JavaScript 和 TypeScript 源码有什么差异。

## 2. 前置知识

你需要已经理解：

- L001 中的核心结论：TypeScript 主要在运行前检查数据契约。
- JavaScript 文件最终由 Node.js 或浏览器执行。
- 一个前端工程通常会有源码目录、构建产物目录和脚本命令。

## 3. 为什么这个知识点对 Vue 2 前端开发者重要

在 Vue 2 + Webpack 项目里，很多构建细节被脚手架藏起来了。你通常只运行：

```bash
npm run serve
npm run build
```

但切到 Vue 3 + TypeScript 后，你会更频繁看到这些问题：

- 为什么编辑器报类型错误，但页面还能跑？
- 为什么 `npm run build` 会因为类型错误失败？
- 为什么有些 `.ts` 文件没有被检查？
- 为什么编译后的代码里看不到类型？
- 为什么 `tsconfig.json` 改一个选项，整个项目报错数量会变化？

理解 TypeScript 编译流程，可以帮你把“类型检查”“代码转换”“运行代码”这三件事分清楚。后面学习 Vue 3、Pinia、接口类型契约和 Node.js 项目结构时，这个边界会一直出现。

## 4. 概念解析

### `tsc` 是什么

`tsc` 是 TypeScript compiler，也就是 TypeScript 编译器。

它主要做两类事情：

1. 类型检查：检查 `.ts` 源码里的类型使用是否符合规则。
2. 代码输出：把 TypeScript 代码转换成 JavaScript 代码。

简化流程如下：

```text
读取 tsconfig.json
  -> 找到需要检查的 .ts 文件
  -> 做类型检查
  -> 如果允许输出，就生成 JavaScript
  -> Node.js 或浏览器执行 JavaScript
```

### `tsc --noEmit`

`emit` 的意思是“输出文件”。`tsc --noEmit` 表示只做类型检查，不生成 JavaScript 文件。

它常用于提交前检查、持续集成检查或本地快速确认类型是否通过。

```bash
tsc --noEmit
```

如果命令成功且没有输出，通常代表类型检查通过。

### `tsc`

直接运行 `tsc` 时，编译器会根据 `tsconfig.json` 做类型检查，并在允许输出的情况下生成 JavaScript。

```bash
tsc
```

如果 `outDir` 设置为 `dist`，编译产物通常会出现在 `dist/` 下。

### `tsconfig.json`

`tsconfig.json` 是 TypeScript 项目的配置文件。它回答三个核心问题：

- 检查哪些文件？
- 使用哪些类型检查规则？
- 编译产物输出到哪里、输出成什么 JavaScript 版本？

一个最小配置可能长这样：

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "module": "CommonJS",
    "strict": true,
    "rootDir": "src",
    "outDir": "dist"
  },
  "include": ["src/**/*.ts"]
}
```

## 5. 心智模型

可以把 TypeScript 编译流程想成“项目入口处的质检线”。

- `include` 决定哪些源码会进入质检线。
- `compilerOptions` 决定质检标准和输出格式。
- `tsc --noEmit` 只质检，不打包发货。
- `tsc` 质检后把 TypeScript 转成 JavaScript。
- Node.js 或浏览器只认识最后输出的 JavaScript，不认识 TypeScript 类型。

这也是为什么类型系统很有价值，但它不是运行时校验。类型会在编译后被擦除，运行时不会自动检查一个接口返回值是否真的符合你写的类型。

## 6. 与 JavaScript 的差异

JavaScript 文件可以直接运行：

```bash
node src/index.js
```

TypeScript 文件通常需要先编译：

```bash
tsc
node dist/index.js
```

TypeScript 源码里可以写类型：

```ts
type OrderStatus = 'pending' | 'paid' | 'cancelled'

const status: OrderStatus = 'paid'
```

编译后的 JavaScript 不会保留这些类型：

```js
const status = 'paid'
```

类型标注是给编译器和开发者看的，不是给 JavaScript 运行时看的。

## 7. 可运行代码

示例目录：

```text
tracks/01-typescript-vue3/examples/L002-compiler-flow/
```

文件结构：

```text
package.json
tsconfig.json
src/index.ts
```

### `tsconfig.json`

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "module": "CommonJS",
    "strict": true,
    "noEmitOnError": true,
    "rootDir": "src",
    "outDir": "dist"
  },
  "include": ["src/**/*.ts"]
}
```

### `src/index.ts`

```ts
type OrderStatus = 'pending' | 'paid' | 'cancelled'

type Order = {
  id: number
  title: string
  status: OrderStatus
  total: number
}

function formatOrder(order: Order): string {
  const statusText = {
    pending: '待支付',
    paid: '已支付',
    cancelled: '已取消'
  }[order.status]

  return `订单 ${order.id}：${order.title}，${statusText}，金额 ${order.total}`
}

const order: Order = {
  id: 1001,
  title: 'Vue 3 进阶课',
  status: 'paid',
  total: 199
}

console.log(formatOrder(order))
console.log('运行时 typeof order.total =', typeof order.total)
```

## 8. 代码逐行说明

```ts
type OrderStatus = 'pending' | 'paid' | 'cancelled'
```

定义订单状态只能是三个固定字符串之一。这个类型只在编译前存在，编译后不会出现在 JavaScript 文件里。

```ts
type Order = {
  id: number
  title: string
  status: OrderStatus
  total: number
}
```

定义订单对象的结构。它约束 `formatOrder` 和 `order` 之间的数据契约。

```ts
function formatOrder(order: Order): string {
```

声明 `formatOrder` 的参数必须符合 `Order` 类型，返回值必须是字符串。

```ts
  const statusText = {
    pending: '待支付',
    paid: '已支付',
    cancelled: '已取消'
  }[order.status]
```

根据订单状态映射中文文案。因为 `order.status` 被限制在三个状态内，所以不会访问到未定义的状态键。

```ts
  return `订单 ${order.id}：${order.title}，${statusText}，金额 ${order.total}`
```

返回页面上可能会展示的订单摘要字符串。

```ts
const order: Order = {
  id: 1001,
  title: 'Vue 3 进阶课',
  status: 'paid',
  total: 199
}
```

创建一个符合 `Order` 契约的对象。这里如果把 `status` 改成 `'finished'`，`tsc --noEmit` 会报错。

```ts
console.log(formatOrder(order))
console.log('运行时 typeof order.total =', typeof order.total)
```

第一行输出业务结果。第二行提醒你：运行时只知道 JavaScript 的基础类型，不知道 `Order` 这种 TypeScript 类型。

## 9. 运行方式

进入示例目录：

```bash
cd tracks/01-typescript-vue3/examples/L002-compiler-flow
```

只做类型检查：

```bash
tsc --noEmit
```

编译并查看输出文件：

```bash
tsc --listEmittedFiles --pretty false
```

运行编译后的 JavaScript：

```bash
node dist/index.js
```

查看编译后的 JavaScript：

```bash
sed -n '1,120p' dist/index.js
```

## 10. 预期结果与真实验证结果

本节已真实执行以下命令。

### `tsc --noEmit`

预期和实际结果都是：命令成功，无输出。

这代表 TypeScript 完成了类型检查，但没有生成 `dist/` 文件。

### `tsc --listEmittedFiles --pretty false`

预期和实际结果都是：命令成功，并输出生成的 JavaScript 文件路径。

实际输出：

```text
TSFILE: /Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/01-typescript-vue3/examples/L002-compiler-flow/dist/index.js
```

### `node dist/index.js`

预期和实际结果都是：

```text
订单 1001：Vue 3 进阶课，已支付，金额 199
运行时 typeof order.total = number
```

### `sed -n '1,120p' dist/index.js`

实际输出显示：`type OrderStatus`、`type Order`、`: Order`、`: string` 都已经不在编译产物中。编译后的文件只剩可执行的 JavaScript。

## 11. 常见错误

### 误区 1：`tsc --noEmit` 会生成 JavaScript

不会。`--noEmit` 的意思就是不输出文件，只做类型检查。

### 误区 2：类型检查通过，业务就一定正确

不一定。类型检查只能证明代码符合静态类型规则，不能证明接口一定成功、库存一定充足、权限一定正确。

### 误区 3：编译后的 JavaScript 还会保留类型

不会。TypeScript 的类型会被擦除。运行时执行的是普通 JavaScript。

### 误区 4：`include` 不重要

很重要。没有被 `include` 覆盖的 `.ts` 文件，可能不会进入类型检查。真实项目里这会导致“某些文件明明有错，但构建没发现”。

### 误区 5：`strict` 太严格，应该先关掉

迁移旧项目时可以阶段性放宽规则，但新项目建议默认开启 `strict`。它能尽早暴露空值、隐式 `any`、函数参数等问题。

## 12. 小练习

在 `src/index.ts` 中做一个小实验：

1. 把 `order.status` 从 `'paid'` 改成 `'finished'`。
2. 执行 `tsc --noEmit`。
3. 观察 TypeScript 报错。
4. 再把它改回 `'paid'`，确认 `tsc --noEmit` 重新通过。

思考：这个错误如果放在 JavaScript 项目里，通常会在什么时候被发现？

## 13. 贴近真实业务的应用场景

假设你在后台管理系统里做订单列表。订单状态可能来自后端接口，也会影响：

- 列表状态标签。
- 操作按钮是否展示。
- 详情页的流程节点。
- 退款、支付、取消等接口是否可点击。

如果订单状态在项目中没有明确类型，`'paid'`、`'payed'`、`'finished'` 这类拼写差异会散落在多个页面里。通过 `OrderStatus` 这样的类型，编译器可以帮你在运行前发现不合法状态。

同时，`tsconfig.json` 决定哪些文件会进入这套检查流程。它不是“可有可无的配置”，而是整个 TypeScript 项目的检查边界。

## 14. 本节复盘问题

- `tsc --noEmit` 和 `tsc` 的区别是什么？
- `tsconfig.json` 主要回答哪三个问题？
- 为什么编译后的 JavaScript 里没有 `type Order`？
- 类型检查通过后，为什么接口数据仍然可能出错？

## 15. 下一节预告

下一节学习：类型标注与类型推断。

我们会用更贴近 Vue 业务代码的变量、对象和函数示例，理解什么时候应该主动写类型，什么时候应该让 TypeScript 自动推断。
