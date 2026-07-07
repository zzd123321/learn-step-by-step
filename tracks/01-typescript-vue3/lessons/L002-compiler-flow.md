# L002 TypeScript 编译流程与 tsconfig.json 入门

上一节我们知道了：TypeScript 在运行前检查类型，最后运行的仍然是 JavaScript。本节把这条链路拆开，看清 `tsc` 和 `tsconfig.json` 各自负责什么。

官方 TSConfig Reference 说明：某个目录中的 `tsconfig.json` 表示这个目录是一个 TypeScript 或 JavaScript 项目的根目录。它不仅是“配置文件”，也是项目类型检查边界的一部分。

参考：

- https://www.typescriptlang.org/tsconfig/
- https://www.typescriptlang.org/docs/handbook/typescript-from-scratch.html

## 编译流程到底发生了什么

`tsc` 是 TypeScript compiler，也就是 TypeScript 编译器。

它主要做两件事：

- 类型检查：检查 `.ts` 源码里的类型使用是否符合规则。
- 代码输出：把 TypeScript 转成 JavaScript。

简化流程如下：

```text
读取 tsconfig.json
  -> 根据 include / files 找到源文件
  -> 根据 compilerOptions 做类型检查
  -> 如果允许输出，生成 JavaScript
  -> Node.js 或浏览器执行 JavaScript
```

这也是 Vue 3 + TypeScript 项目里经常要分清的三件事：

- 编辑器报错：通常来自 TypeScript 语言服务。
- 类型检查失败：通常来自 `tsc --noEmit` 或构建工具集成的类型检查。
- 页面运行报错：来自浏览器或 Node.js 执行 JavaScript 时的运行时错误。

它们有关联，但不是同一件事。

## `tsc --noEmit` 和 `tsc`

`emit` 是“输出文件”的意思。

```bash
tsc --noEmit
```

这条命令只做类型检查，不生成 JavaScript。它适合放在提交前检查或 CI 流程里，用来快速回答“当前代码的类型是否通过”。

```bash
tsc
```

这条命令会读取 `tsconfig.json`，做类型检查，并在允许输出时生成 JavaScript。输出位置通常由 `outDir` 控制。

本节示例还使用了：

```bash
tsc --listEmittedFiles --pretty false
```

`--listEmittedFiles` 会列出实际输出了哪些文件，`--pretty false` 让输出更适合复制到文档或日志中。

## 示例项目的 tsconfig.json

示例目录：

```text
tracks/01-typescript-vue3/examples/L002-compiler-flow/
```

配置文件：

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

逐项理解：

- `target: "ES2022"`：输出的 JavaScript 语法目标。目标越新，编译器需要转换的语法通常越少。
- `module: "CommonJS"`：输出 CommonJS 模块格式，适合本节直接用 Node.js 运行。
- `strict: true`：开启一组更严格的类型检查规则。新项目建议默认开启。
- `noEmitOnError: true`：如果出现类型错误，不输出编译产物，避免带着错误继续运行旧产物。
- `rootDir: "src"`：告诉编译器源码根目录在哪里。
- `outDir: "dist"`：告诉编译器把输出文件放到哪里。
- `include: ["src/**/*.ts"]`：只把 `src` 下的 TypeScript 文件纳入这个项目。

`include` 很关键。没有进入 `include` 范围的文件，可能不会参与类型检查。真实项目里如果某个目录没有被覆盖，就可能出现“文件明明有错，但构建没发现”的情况。

## 示例代码：订单状态格式化

`src/index.ts`：

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

这段代码选择订单状态，是因为它非常贴近后台业务：状态字段会影响列表标签、详情流程、按钮权限和接口操作。

## 关键代码解析

```ts
type OrderStatus = 'pending' | 'paid' | 'cancelled'
```

这行定义了订单状态的取值范围。`OrderStatus` 只存在于 TypeScript 编译前，编译后的 JavaScript 里不会有这个名字。

```ts
type Order = {
  id: number
  title: string
  status: OrderStatus
  total: number
}
```

`Order` 描述订单对象结构。它让 `formatOrder` 的调用方必须提供完整字段，并保证 `status` 只能是合法状态。

```ts
function formatOrder(order: Order): string {
```

这里同时标注了参数和返回值。参数标注保护函数入口，返回值标注保护函数出口。以后如果 `formatOrder` 被改成返回对象，类型检查会提醒调用方和维护者。

```ts
const statusText = {
  pending: '待支付',
  paid: '已支付',
  cancelled: '已取消'
}[order.status]
```

`order.status` 只能是三个合法值之一，所以这里一定能取到对应文案。如果你把 `order.status` 改成普通 `string`，这个表达式的安全性就会下降，因为任意字符串都可能访问不到文案。

```ts
console.log('运行时 typeof order.total =', typeof order.total)
```

这一行故意观察运行时。运行时只知道 `order.total` 是 JavaScript 的 `number`，不知道 `Order` 类型，也不知道 `OrderStatus`。

## 编译后的 JavaScript 会变成什么

运行：

```bash
tsc
```

编译后的 `dist/index.js` 中，类型都消失了，只剩 JavaScript 逻辑：

```js
"use strict";
function formatOrder(order) {
    const statusText = {
        pending: '待支付',
        paid: '已支付',
        cancelled: '已取消'
    }[order.status];
    return `订单 ${order.id}：${order.title}，${statusText}，金额 ${order.total}`;
}
const order = {
    id: 1001,
    title: 'Vue 3 进阶课',
    status: 'paid',
    total: 199
};
console.log(formatOrder(order));
console.log('运行时 typeof order.total =', typeof order.total);
```

这正是 TypeScript 的重要边界：类型帮你在编译前检查，但不改变 JavaScript 的运行方式。

## 运行和已验证结果

进入示例目录：

```bash
cd tracks/01-typescript-vue3/examples/L002-compiler-flow
```

已真实执行：

```bash
tsc --noEmit
tsc --listEmittedFiles --pretty false
node dist/index.js
sed -n '1,120p' dist/index.js
```

`tsc --noEmit` 成功且无输出，代表只做类型检查，没有生成文件。

`tsc --listEmittedFiles --pretty false` 的实际输出：

```text
TSFILE: /Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/01-typescript-vue3/examples/L002-compiler-flow/dist/index.js
```

`node dist/index.js` 的实际输出：

```text
订单 1001：Vue 3 进阶课，已支付，金额 199
运行时 typeof order.total = number
```

`sed -n '1,120p' dist/index.js` 已确认编译产物中没有 `type OrderStatus`、`type Order`、`: Order` 或 `: string`。

## 真实项目里的判断

在 Vue 3 项目里，你会遇到三类脚本：

- 开发服务器脚本：启动页面，提供热更新。
- 类型检查脚本：通常类似 `vue-tsc --noEmit` 或 `tsc --noEmit`。
- 构建脚本：生成生产环境产物。

不要把“页面能跑”当成“类型一定没问题”。开发服务器为了速度，可能不会完整执行类型检查。反过来，类型检查通过也不代表接口一定成功、权限一定正确、业务流程一定完整。

## 练习

把示例中的：

```ts
status: 'paid'
```

改成：

```ts
status: 'finished'
```

然后运行：

```bash
tsc --noEmit
```

观察 TypeScript 报错。再改回 `'paid'`，确认类型检查恢复通过。

## 下一步

下一节学习类型标注与类型推断。重点不是“哪里都写类型”，而是学会判断：哪些边界必须写清楚，哪些局部细节应该交给编译器自动推断。
