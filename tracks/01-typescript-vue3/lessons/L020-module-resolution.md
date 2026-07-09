# L020 TypeScript 中的模块解析

上一节我们看了 ESM 与 CommonJS 的差异。这一节继续往下走：当你写下一行代码时，TypeScript 到底怎么知道它要找哪个文件？

```ts
import { payOrder } from './services/order-service'
```

这个过程叫 module resolution（模块解析）。

本节核心结论：

> 模块解析是 TypeScript 根据导入路径、当前文件位置、`tsconfig.json`、`package.json` 和文件扩展名，找到真实模块和类型信息的过程。

官方参考：

- https://www.typescriptlang.org/docs/handbook/modules/reference.html
- https://www.typescriptlang.org/tsconfig/moduleResolution.html

## 为什么 Vue 2 前端开发者要学它

在 Vue 2 + Webpack 项目里，你可能很习惯这样写：

```ts
import UserList from '@/views/user/UserList.vue'
import request from '@/utils/request'
```

`@/` 看起来像 JavaScript 原生能力，但它其实通常来自打包器配置。Webpack、Vite、TypeScript、Node.js 都可能各有一套“如何理解路径”的规则。

如果这些规则没有对齐，就会出现很典型的问题：

- 编辑器不报错，但运行时报 `Cannot find module`。
- TypeScript 能编译，Node.js 不能运行。
- Vite 能运行，测试工具找不到别名。
- `import type` 被擦除后运行没问题，但普通 `import` 会留下运行时路径。

所以模块解析不是“路径写法小细节”，它直接决定工程是否稳定。

## 相对路径：从当前文件出发

最基础的模块解析是相对路径：

```ts
import { createOrder } from '../domain/order'
```

它从当前文件所在目录出发：

```text
src/services/order-service.ts
```

`../domain/order` 表示：

```text
src/domain/order.ts
```

TypeScript 会尝试根据配置和模块解析策略查找匹配文件。对于这个 CommonJS 示例，`moduleResolution` 使用 `Node10`，所以它会按 Node 经典 CommonJS 风格查找 `.ts`、`.tsx`、`.d.ts` 等类型源文件，并在编译后输出 CommonJS `require`。

## `moduleResolution`：选择哪套解析规则

`moduleResolution` 决定 TypeScript 使用哪套规则解析模块。

常见值可以这样理解：

- `Node10`：传统 Node.js CommonJS 风格解析。
- `Node16` / `NodeNext`：匹配现代 Node.js 的 ESM + CommonJS 双模块系统。
- `Bundler`：面向 Vite、Webpack、Rollup 这类打包器，更贴近前端构建工具的解析方式。

上一节 ESM 示例使用 `NodeNext`，因为 Node.js ESM 对扩展名和 package 边界更严格。

本节先用 `Node10`，因为我们只演示最基础、最直观的相对路径解析。

## 可运行示例：订单服务的相对导入

示例目录：

```text
tracks/01-typescript-vue3/examples/L020-module-resolution/
```

目录结构：

```text
src/
  domain/
    order.ts
  services/
    order-service.ts
  index.ts
```

`src/domain/order.ts`：

```ts
export type OrderStatus = 'draft' | 'paid'

export type Order = {
  id: string
  amount: number
  status: OrderStatus
}

export function createOrder(id: string, amount: number): Order {
  if (id.trim() === '') {
    throw new Error('订单 id 不能为空')
  }

  if (amount <= 0) {
    throw new Error('订单金额必须大于 0')
  }

  return {
    id,
    amount,
    status: 'draft'
  }
}
```

`src/services/order-service.ts`：

```ts
import { createOrder, type Order } from '../domain/order'

export function payOrder(id: string, amount: number): Order {
  const order = createOrder(id, amount)

  return {
    ...order,
    status: 'paid'
  }
}
```

`src/index.ts`：

```ts
import { payOrder } from './services/order-service'

const order = payOrder('order-1001', 99)

console.log(`订单：${order.id} / ${order.status} / ${order.amount}`)

if (false) {
  // @ts-expect-error: payOrder 的 amount 必须是 number。
  payOrder('order-1002', '99')
}
```

## 代码解析

```ts
import { createOrder, type Order } from '../domain/order'
```

这行有两个导入：

- `createOrder` 是运行时函数，编译后必须保留。
- `type Order` 是类型导入，只用于类型检查，编译后会被擦除。

这来自 TypeScript 官方模块文档中的规则：`import type` 或带 `type` 的导入说明这个名字只用于类型位置，输出 JavaScript 时不会保留它。

```ts
export function payOrder(id: string, amount: number): Order {
```

函数返回值使用 `Order` 类型，但运行时并不存在名为 `Order` 的值。TypeScript 会在编译期检查返回对象是否符合 `Order`，然后把类型擦除。

```ts
import { payOrder } from './services/order-service'
```

这是相对导入。`index.ts` 和 `services/` 都在 `src/` 下，所以路径从 `src/index.ts` 出发，找到：

```text
src/services/order-service.ts
```

```json
"rootDir": "src",
"outDir": "dist"
```

这两个配置决定输入和输出目录关系。编译后结构会保持类似：

```text
dist/
  domain/
    order.js
  services/
    order-service.js
  index.js
```

因此编译后的 `require("./services/order-service")` 仍然能找到对应文件。

## 运行与真实验证

在示例目录执行：

```bash
tsc --noEmit
tsc
node dist/index.js
sed -n '1,200p' dist/services/order-service.js
sed -n '1,120p' dist/index.js
```

实际运行输出：

```text
订单：order-1001 / paid / 99
```

`dist/services/order-service.js` 中可以看到：

```js
const order_1 = require("../domain/order");
```

但不会看到 `Order` 类型导入，因为它在编译时已经被擦除了。

## 路径别名的边界

前端项目常见的 `@/` 通常来自 `tsconfig.json` 的 `paths` 配置和打包器别名配置：

```json
{
  "compilerOptions": {
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  }
}
```

这能让 TypeScript 理解：

```ts
import { payOrder } from '@/services/order-service'
```

但要注意：`paths` 主要告诉 TypeScript 如何做类型检查，不会自动改写编译后的 JavaScript 导入路径。

如果直接用 `tsc` 编译并交给 Node.js 运行，Node.js 默认不认识 `@/services/order-service`。前端项目能运行，是因为 Vite 或 Webpack 也配置了同样的别名，并在打包时处理了它。

所以别名要成套配置：

- TypeScript：让编辑器和类型检查认识。
- Vite/Webpack：让构建工具认识。
- 测试工具：让测试运行时认识。
- Node.js 直跑项目：要么避免别名，要么使用额外运行时解析方案。

本节示例刻意使用相对路径，就是为了避免让 TypeScript “能检查”但 Node “不能运行”的错觉。

## 常见误区

第一个误区：以为 `paths` 会改写输出路径。

`paths` 帮助 TypeScript 解析模块和类型，但不会自动把 `@/x` 编译成 `../../x`。如果运行时不认识别名，仍然会失败。

第二个误区：忽略 `import type`。

如果只需要类型，尽量显式写 `type`：

```ts
import { createOrder, type Order } from '../domain/order'
```

这样读代码的人能立即看出 `Order` 不会出现在运行时，也能减少一些不必要的运行时依赖。

第三个误区：把前端打包器规则误认为 Node.js 规则。

Vite 能解析的路径，Node.js 原生不一定能解析。Node.js ESM、CommonJS、打包器和 TypeScript 都有各自规则。

第四个误区：改了目录结构，只改运行代码，不看类型导入。

TypeScript 会检查类型导入路径。如果重构目录后路径没同步，类型检查能帮你尽早发现问题。

## 一个小练习

在本节示例里新增：

```text
src/formatters/order-formatter.ts
```

导出：

```ts
import type { Order } from '../domain/order'

export function formatOrder(order: Order): string {
  return `${order.id}:${order.status}:${order.amount}`
}
```

然后在 `src/index.ts` 中导入 `formatOrder` 并输出它的结果。

练习重点：

- 从 `formatters/order-formatter.ts` 到 `domain/order.ts` 的相对路径应该怎么写？
- `Order` 为什么应该用 `import type`？
- 编译后 `dist/formatters/order-formatter.js` 里会不会出现 `require("../domain/order")`？

## 真实业务场景

在 Vue 3 项目中，你可能会这样分层：

```text
src/
  api/
  components/
  composables/
  domain/
  pages/
  stores/
```

模块解析决定这些层之间如何引用。好的路径规则会让依赖关系清楚；混乱的别名和相对路径会让重构变得痛苦。

在 Node.js 服务端里，模块解析更直接影响运行时。没有打包器时，TypeScript 检查通过不代表 Node.js 一定能找到模块。后面做轻量后端时，我们会更谨慎地选择 `module`、`moduleResolution` 和导入路径风格。

## 本节复盘

你可以用下面几个问题检查自己：

1. 模块解析解决的是什么问题？
2. `module` 和 `moduleResolution` 分别关注什么？
3. `import { createOrder, type Order }` 中，哪个名字会出现在运行时代码里？
4. 为什么 `paths` 不等于运行时别名？
5. 为什么本节示例不用 `@/`，而是使用相对路径？

下一节建议学习 L021：包管理、脚本命令与依赖边界。我们会把 `package.json`、`dependencies`、`devDependencies`、脚本命令和项目依赖卫生放到一起看。
