# L019 ESM 与 CommonJS 的差异

从这一节开始，我们进入模块系统。你在 Vue 2 + Webpack 项目里大多写的是：

```ts
import { createApp } from 'vue'
export function request() {}
```

但到了 Node.js 和 TypeScript 工程里，光会写 `import/export` 还不够。你还要知道最终运行时用的是 ESM（ECMAScript Modules）还是 CommonJS。

本节只讲一个核心问题：

> TypeScript 的 `import/export` 是源码写法；Node.js 真正运行的是编译后的模块格式。

官方参考：

- https://nodejs.org/api/esm.html
- https://nodejs.org/api/modules.html
- https://www.typescriptlang.org/tsconfig/module.html

## CommonJS 和 ESM 的基本区别

CommonJS 是 Node.js 早期的模块系统。它的运行时代码通常长这样：

```js
const math = require('./math')
module.exports = {}
```

ESM 是 JavaScript 标准模块系统。它的运行时代码通常长这样：

```js
import { formatTotal } from './math.js'
export function formatTotal() {}
```

在 TypeScript 源码里，你可能两边都写 `import/export`。真正的差异来自 `tsconfig.json` 的 `compilerOptions.module` 和 `package.json` 的 `"type"`。

## 两个开关：TypeScript 编译目标和 Node 运行规则

第一个开关是 TypeScript：

```json
{
  "compilerOptions": {
    "module": "CommonJS"
  }
}
```

这会把 `import/export` 编译成 `require` 和 `exports`。

如果写：

```json
{
  "compilerOptions": {
    "module": "NodeNext",
    "moduleResolution": "NodeNext"
  }
}
```

TypeScript 会按 Node.js 的 ESM/CommonJS 规则处理模块。

第二个开关是 Node.js：

```json
{
  "type": "commonjs"
}
```

这表示 `.js` 默认按 CommonJS 执行。

```json
{
  "type": "module"
}
```

这表示 `.js` 默认按 ESM 执行。

两个开关必须配合。如果 TypeScript 输出 ESM，但 Node 按 CommonJS 执行，就会出现 `Cannot use import statement outside a module` 这类错误。反过来，如果 TypeScript 输出 CommonJS，但 Node 按 ESM 执行，也会遇到 `exports is not defined` 之类的问题。

## 可运行示例：同一逻辑，两种模块产物

示例目录：

```text
tracks/01-typescript-vue3/examples/L019-esm-commonjs/
```

本节有两个子项目：

```text
commonjs/
esm/
```

两个项目的业务逻辑一样，都是计算数字总和。

### CommonJS 项目

`commonjs/package.json`：

```json
{
  "type": "commonjs"
}
```

`commonjs/tsconfig.json`：

```json
{
  "compilerOptions": {
    "module": "CommonJS"
  }
}
```

`commonjs/src/math.ts`：

```ts
export function formatTotal(items: number[]): string {
  const total = items.reduce((sum, item) => sum + item, 0)
  return `total=${total}`
}
```

`commonjs/src/index.ts`：

```ts
import { formatTotal } from './math'

console.log(`CommonJS 输出：${formatTotal([10, 20, 30])}`)
```

编译后，`dist/index.js` 的核心会变成：

```js
const math_1 = require("./math");
```

这说明 TypeScript 把源码里的 `import` 转成了 CommonJS 的 `require`。

### ESM 项目

`esm/package.json`：

```json
{
  "type": "module"
}
```

`esm/tsconfig.json`：

```json
{
  "compilerOptions": {
    "module": "NodeNext",
    "moduleResolution": "NodeNext"
  }
}
```

`esm/src/math.ts`：

```ts
export function formatTotal(items: number[]): string {
  const total = items.reduce((sum, item) => sum + item, 0)
  return `total=${total}`
}
```

`esm/src/index.ts`：

```ts
import { formatTotal } from './math.js'

console.log(`ESM 输出：${formatTotal([10, 20, 30])}`)
```

这里最容易让前端同学疑惑的是 `./math.js`。源文件明明叫 `math.ts`，为什么导入要写 `.js`？

原因是 Node.js ESM 运行时看的是编译后的 JavaScript 文件。编译后会有 `dist/math.js`，所以源码里就要写运行时能找到的扩展名。`NodeNext` 会按这个规则检查。

## 代码解析

```ts
import { formatTotal } from './math'
```

CommonJS 示例里可以这样写，因为 TypeScript 会输出：

```js
const math_1 = require("./math");
```

CommonJS 的 `require` 可以按 Node 的 CommonJS 规则查找 `./math.js`。

```ts
import { formatTotal } from './math.js'
```

ESM 示例必须写清楚 `.js` 扩展名。Node.js 的 ESM 对相对路径更严格，不能默认帮你补扩展名。

```json
"module": "CommonJS"
```

这决定 TypeScript 输出 CommonJS 代码。

```json
"module": "NodeNext"
```

这让 TypeScript 按 Node 的当前模块规则工作。配合 `"type": "module"` 时，`.ts` 源文件会被当作 ESM，输出保留 `import/export`。

```json
"type": "module"
```

这是 Node.js 的运行时判断。它不管你的 TypeScript 源码怎么写，只决定 `.js` 文件最终按哪种模块格式执行。

## 运行与真实验证

在 CommonJS 子项目执行：

```bash
tsc --noEmit
tsc
node dist/index.js
sed -n '1,160p' dist/index.js
```

实际运行输出：

```text
CommonJS 输出：total=60
```

在 ESM 子项目执行：

```bash
tsc --noEmit
tsc
node dist/index.js
sed -n '1,160p' dist/index.js
```

实际运行输出：

```text
ESM 输出：total=60
```

## 常见误区

第一个误区：以为源码写了 `import/export` 就一定是 ESM。

不一定。TypeScript 可以把 `import/export` 编译成 CommonJS。

第二个误区：只改 `tsconfig.module`，不改 `package.json.type`。

TypeScript 负责编译，Node.js 负责运行。两边不匹配时，编译可能成功，但运行失败。

第三个误区：ESM 相对导入不写扩展名。

```ts
import { formatTotal } from './math'
```

在很多打包器里这没问题，但 Node.js 原生 ESM 要求更明确。用 `NodeNext` 时，TypeScript 会帮助你尽早发现这个问题。

第四个误区：在 ESM 中直接使用 CommonJS 全局变量。

Node.js 官方文档明确列出 ESM 与 CommonJS 的差异：ESM 中没有 `require`、`exports`、`module.exports`、`__filename`、`__dirname` 这些 CommonJS 变量。要用对应能力，需要换成 ESM 的方式。

## 一个小练习

在两个子项目中各新增一个 `src/discount.ts`：

```ts
export function applyDiscount(total: number, rate: number): number {
  return total * (1 - rate)
}
```

然后在 `index.ts` 中导入并输出折扣后金额。

注意：

- CommonJS 子项目可以写 `./discount`。
- ESM 子项目应写 `./discount.js`。

练习重点：同样的 TypeScript 语法，在不同模块目标下要服务于不同的运行时规则。

## 真实业务场景

前端 Vue 3 项目通常由 Vite 处理 ESM，很多路径细节被打包器照顾了。Node.js 后端不一定有打包器，尤其是轻量服务、脚本工具、CLI 项目，运行时会直接面对 Node 的模块规则。

当你看到一个库的文档分别写：

```js
const pkg = require('pkg')
```

和：

```js
import pkg from 'pkg'
```

它们不是简单的两种写法，而是在兼容两种模块系统。理解这一点，后面学包管理、模块解析、Node.js 后端时会少踩很多坑。

## 本节复盘

你可以用下面几个问题检查自己：

1. TypeScript 的 `module` 选项决定什么？
2. Node.js 的 `"type": "module"` 决定什么？
3. 为什么 ESM 示例里 TypeScript 源码要写 `./math.js`？
4. CommonJS 产物里 `import` 会变成什么？
5. ESM 里为什么不能直接使用 `require` 和 `__dirname`？

下一节建议学习 L020：TypeScript 中的模块解析。我们会继续看 TypeScript 如何根据路径、扩展名、`package.json` 和 `tsconfig` 找到一个模块。
