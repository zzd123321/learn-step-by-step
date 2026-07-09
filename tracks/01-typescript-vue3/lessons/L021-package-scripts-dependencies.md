# L021 包管理、脚本命令与依赖边界

前面我们已经写了很多最小 TypeScript 示例。每个示例都有一个 `package.json`，里面通常有这些脚本：

```json
{
  "scripts": {
    "check": "tsc --noEmit",
    "build": "tsc",
    "start": "node dist/index.js"
  }
}
```

这一节不讲复杂包管理器差异，只讲一个工程基本功：

> `package.json` 是项目的运行说明书；scripts 描述怎么操作项目，dependencies 描述运行时需要什么，devDependencies 描述开发时需要什么。

官方参考：

- https://docs.npmjs.com/cli/v10/configuring-npm/package-json/
- https://docs.npmjs.com/cli/v10/using-npm/scripts/

## 为什么 Vue 2 前端开发者要学它

Vue 2 项目里你可能每天都在用：

```bash
npm run dev
npm run build
npm run lint
```

但很多人对这些命令背后的边界不够清楚：

- `npm run build` 到底执行了什么？
- `dependencies` 和 `devDependencies` 怎么区分？
- 为什么不能提交 `node_modules`？
- 为什么 CI 常用 `npm ci` 而不是随手 `npm install`？
- 一个包是运行时依赖，还是只在构建时需要？

这些问题进入 Vue 3、Node.js 和全栈项目后会更重要。前端只要打包能跑还不够，后端服务会直接面对部署环境和运行时依赖。

## `package.json` 是项目入口说明

npm 官方文档说明，`package.json` 必须是实际 JSON。它不是 JavaScript 对象字面量，不能写注释，字符串必须使用双引号。

常见字段可以这样理解：

```json
{
  "name": "my-app",
  "version": "1.0.0",
  "private": true,
  "type": "commonjs",
  "scripts": {},
  "dependencies": {},
  "devDependencies": {}
}
```

`name` 是包名。即使不发布，也建议写清楚，方便脚本、日志和工具识别。

`version` 是版本号。要发布到 npm 时，`name` 和 `version` 很关键。

`private: true` 表示这个项目不应该被发布到 npm。学习项目、业务项目、内部项目通常都应该加上它，避免误发布。

`type` 决定 Node.js 如何解释 `.js` 文件：上一节我们见过 `"commonjs"` 和 `"module"` 的差异。

`scripts` 是命令集合。npm 官方 scripts 文档说明，`npm run <script>` 会执行这里定义的命令。

## scripts：把团队操作固定下来

脚本的价值不是少敲几个字，而是把团队约定固化。

本路线的最小 TypeScript 项目通常用：

```json
{
  "scripts": {
    "check": "tsc --noEmit",
    "build": "tsc",
    "start": "node dist/index.js"
  }
}
```

`check` 只做类型检查，不输出文件。适合提交前、CI、快速验证。

`build` 负责编译，生成 `dist/`。

`start` 运行编译后的 JavaScript。注意它依赖 `build` 已经执行过。

有些项目会加：

```json
{
  "verify": "npm run check && npm run build && npm run start"
}
```

这表示一条命令串联完整验证流程。真实项目中，你可能会有：

- `dev`：启动开发服务器。
- `lint`：代码风格和静态规则检查。
- `test`：运行自动化测试。
- `typecheck`：只跑 TypeScript 类型检查。
- `preview`：预览构建产物。

命令名不必完全一致，但团队要一致。

## dependencies 与 devDependencies

`dependencies` 是运行时依赖。你的应用运行时必须能 `import` 或 `require` 到它。

例子：

```json
{
  "dependencies": {
    "axios": "^1.0.0"
  }
}
```

如果前端运行时代码会请求接口并打包进浏览器产物，`axios` 就是运行时依赖。后端服务运行时要用数据库客户端，那么数据库客户端也是运行时依赖。

`devDependencies` 是开发、构建、测试阶段需要的依赖。

例子：

```json
{
  "devDependencies": {
    "typescript": "^5.0.0",
    "vite": "^5.0.0",
    "vitest": "^1.0.0"
  }
}
```

TypeScript 编译器、Vite、测试框架通常不直接出现在生产运行时代码里，所以它们多半属于开发依赖。

边界判断可以问自己一句：

> 部署后的应用在运行时还需要这个包吗？

需要，放 `dependencies`。只在开发、构建、测试时需要，放 `devDependencies`。

## 可运行示例：用脚本描述项目操作

示例目录：

```text
tracks/01-typescript-vue3/examples/L021-package-scripts-dependencies/
```

`package.json`：

```json
{
  "name": "l021-package-scripts-dependencies",
  "version": "1.0.0",
  "private": true,
  "type": "commonjs",
  "scripts": {
    "check": "tsc --noEmit",
    "build": "tsc",
    "start": "node dist/index.js",
    "verify": "npm run check && npm run build && npm run start"
  },
  "dependencies": {},
  "devDependencies": {}
}
```

`src/index.ts`：

```ts
type ScriptName = 'check' | 'build' | 'start' | 'verify'

type ProjectScript = {
  name: ScriptName
  command: string
  purpose: string
}

const scripts: ProjectScript[] = [
  {
    name: 'check',
    command: 'tsc --noEmit',
    purpose: '只做类型检查，不输出文件'
  },
  {
    name: 'build',
    command: 'tsc',
    purpose: '编译 TypeScript 到 dist'
  },
  {
    name: 'start',
    command: 'node dist/index.js',
    purpose: '运行编译后的 JavaScript'
  },
  {
    name: 'verify',
    command: 'npm run check && npm run build && npm run start',
    purpose: '串联本项目的基础验证流程'
  }
]

function formatScript(script: ProjectScript): string {
  return `${script.name}: ${script.command} -> ${script.purpose}`
}

console.log('脚本清单')
console.log(scripts.map(formatScript).join('\n'))

if (false) {
  const wrongScript: ProjectScript = {
    // @ts-expect-error: ScriptName 只允许项目已定义的脚本名。
    name: 'deploy',
    command: 'node deploy.js',
    purpose: '部署'
  }

  console.log(wrongScript)
}
```

## 代码解析

```ts
type ScriptName = 'check' | 'build' | 'start' | 'verify'
```

这里用字面量联合表示项目允许的脚本名。真实项目里，脚本名来自 `package.json`；示例里用类型把它们建模出来，帮助你理解“脚本是一组明确约定”。

```ts
type ProjectScript = {
  name: ScriptName
  command: string
  purpose: string
}
```

`ProjectScript` 描述一条脚本说明。`name` 不能随便写，`command` 是实际执行命令，`purpose` 是脚本用途。

```ts
const scripts: ProjectScript[] = [
```

这组数据对应 `package.json` 中的 scripts。区别是：`package.json` 给 npm 执行，`src/index.ts` 用来展示这些脚本的含义。

```ts
function formatScript(script: ProjectScript): string {
```

这个函数把脚本信息格式化成可读文本。它让示例可以真实运行，而不只是静态配置文件。

```ts
const wrongScript: ProjectScript = {
  name: 'deploy',
```

`deploy` 不在 `ScriptName` 联合类型中，因此 TypeScript 会报错。`@ts-expect-error` 验证这个错误确实存在。

## 运行与真实验证

在示例目录执行：

```bash
npm run check
npm run build
npm run start
npm run verify
sed -n '1,220p' package.json
```

实际运行输出的核心内容：

```text
脚本清单
check: tsc --noEmit -> 只做类型检查，不输出文件
build: tsc -> 编译 TypeScript 到 dist
start: node dist/index.js -> 运行编译后的 JavaScript
verify: npm run check && npm run build && npm run start -> 串联本项目的基础验证流程
```

本示例没有安装任何依赖，因此不会产生 `node_modules` 或锁文件。`.gitignore` 仍然写了 `node_modules/` 和 `dist/`，因为真实项目中这两个目录都不应该提交。

## 常见误区

第一个误区：把 `node_modules` 提交到仓库。

依赖应该通过 `package.json` 和锁文件恢复，而不是把安装产物提交进去。本仓库规则也明确禁止提交 `node_modules`。

第二个误区：所有包都放 `dependencies`。

构建工具、测试工具、类型工具通常应该放 `devDependencies`。否则部署环境会安装不必要的包，增加体积和风险。

第三个误区：脚本名随意变化。

如果一个项目叫 `typecheck`，另一个叫 `check-types`，第三个叫 `tsc-check`，团队和 CI 都会很痛苦。脚本命名最好有稳定约定。

第四个误区：以为 `npm run start` 会自动 build。

除非你在脚本里明确写：

```json
{
  "start": "npm run build && node dist/index.js"
}
```

否则 `start` 只执行它自己配置的命令。本节示例的 `start` 假设 `dist/index.js` 已经存在。

## 一个小练习

在本节示例中新增一个脚本名：

```ts
type ScriptName = 'check' | 'build' | 'start' | 'verify' | 'clean'
```

然后在 `scripts` 数组中新增：

```ts
{
  name: 'clean',
  command: '删除 dist 目录',
  purpose: '清理构建产物'
}
```

注意：练习里只更新 TypeScript 示例数据，不要真的执行删除命令。当前仓库规则禁止随意执行破坏性清理命令。

## 真实业务场景

一个 Vue 3 + Node.js 全栈项目可能会有：

```json
{
  "scripts": {
    "dev": "启动前后端开发环境",
    "build": "构建生产产物",
    "typecheck": "只做类型检查",
    "test": "运行测试",
    "lint": "检查代码质量",
    "preview": "预览前端构建结果"
  }
}
```

这些脚本是团队协作入口。新人进入项目时，第一眼看的通常就是 README 和 `package.json`。

依赖边界也会影响部署。后端项目如果把运行时依赖错放到 `devDependencies`，生产环境只安装生产依赖时就可能启动失败。前端项目如果把构建工具放错位置，通常不会立刻炸，但会让依赖关系变混乱。

## 本节复盘

你可以用下面几个问题检查自己：

1. `package.json` 为什么必须是 JSON，而不是 JavaScript 对象？
2. `scripts` 的价值只是少敲命令吗？
3. `dependencies` 和 `devDependencies` 的判断标准是什么？
4. 为什么 `node_modules` 不应该提交？
5. `start` 脚本会不会自动执行 `build`？

下一节建议学习 L022：错误处理模型。我们会从 `try/catch` 开始，区分未知异常、可预期业务错误和接口错误结构。
