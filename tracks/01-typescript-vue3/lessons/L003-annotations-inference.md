# L003 类型标注与类型推断

## 1. 学习目标

学完本节，你应该能说清楚：

- 类型标注和类型推断分别是什么。
- 为什么不是每一行代码都需要手写类型。
- 哪些位置应该主动写类型标注。
- 哪些位置应该优先交给 TypeScript 自动推断。
- 如何把这个习惯迁移到 Vue 3 的组件状态、函数和接口请求层。

## 2. 前置知识

你需要已经理解：

- L001：TypeScript 的价值是提前检查数据契约。
- L002：`tsc --noEmit` 做类型检查，`tsc` 会输出 JavaScript。
- JavaScript 中变量、对象、函数和数组的基本写法。

## 3. 为什么这个知识点对 Vue 2 前端开发者重要

很多刚开始写 TypeScript 的 Vue 开发者，会进入两个极端：

- 到处写类型，代码变得很啰嗦。
- 几乎不写类型，最后靠 `any` 糊过去。

真正可维护的写法通常在中间：能推断的地方交给 TypeScript，跨边界的地方主动写清楚。

例如在 Vue 业务代码中：

- `const loading = false` 可以让 TypeScript 推断为 `boolean`。
- `function fetchUsers(params)` 的 `params` 应该主动标注，否则调用方不知道该传什么。
- 接口返回值、组件 Props、Emits、Store 状态这类边界，应该主动写类型。
- 临时变量、简单计算结果、明显的字面量，通常不需要重复标注。

这个判断能力很重要。它决定了 TypeScript 是帮你减少心智负担，还是变成一堆噪音。

## 4. 概念解析

### 类型标注

类型标注是你主动告诉 TypeScript：这个值应该是什么类型。

```ts
const pageSize: number = 20
```

冒号后面的 `number` 就是类型标注。

常见标注位置包括：

- 变量：`const keyword: string = 'vue'`
- 函数参数：`function search(keyword: string) {}`
- 函数返回值：`function getTotal(): number {}`
- 对象结构：`type SearchParams = { keyword: string }`
- 接口边界：请求参数、响应数据、组件 Props、Store 状态

### 类型推断

类型推断是 TypeScript 根据右侧值或上下文，自动推导出类型。

```ts
const pageSize = 20
```

这里没有写 `: number`，但 TypeScript 能推断 `pageSize` 是 `number`。

再比如：

```ts
const user = {
  id: 1,
  name: 'Alice'
}
```

TypeScript 会推断出：

```ts
{
  id: number
  name: string
}
```

### 标注和推断不是对立关系

类型标注像“合同”，适合写在边界上。

类型推断像“自动补全”，适合留在局部实现里。

好的 TypeScript 代码不是类型越多越好，而是类型写在关键边界上，局部细节尽量让编译器自己推。

## 5. 心智模型

可以把类型标注和类型推断想成“路牌”和“导航”。

- 类型标注是路牌：在入口、出口、岔路口写清楚方向。
- 类型推断是导航：在一条明确的小路上，自动帮你跟踪当前位置。

如果每一米都立路牌，信息会过载。如果完全没有路牌，团队协作时又容易迷路。

写 TypeScript 的基本节奏是：

```text
业务边界主动标注
  -> 函数内部交给推断
  -> 发现推断不符合预期时再补标注
```

## 6. 与 JavaScript 的差异

JavaScript 中，函数参数没有类型约束：

```js
function createSearchQuery(params) {
  return `keyword=${params.keyword}&page=${params.page}`
}
```

调用方可以传任何东西：

```js
createSearchQuery({ keyword: 'vue', page: '1' })
createSearchQuery(null)
createSearchQuery({ q: 'vue' })
```

这些问题通常要运行后才发现。

TypeScript 中，可以把参数边界写清楚：

```ts
type SearchParams = {
  keyword: string
  page: number
  pageSize: number
}

function createSearchQuery(params: SearchParams): string {
  return `keyword=${params.keyword}&page=${params.page}&pageSize=${params.pageSize}`
}
```

现在调用方必须传入符合 `SearchParams` 的对象。

## 7. 可运行代码

示例目录：

```text
tracks/01-typescript-vue3/examples/L003-annotations-inference/
```

文件结构：

```text
package.json
tsconfig.json
src/index.ts
```

### `src/index.ts`

```ts
type SearchParams = {
  keyword: string
  page: number
  pageSize: number
}

type SearchResult = {
  query: string
  summary: string
}

function normalizeKeyword(keyword: string) {
  return keyword.trim().toLowerCase()
}

function createSearchQuery(params: SearchParams): string {
  const keyword = normalizeKeyword(params.keyword)
  const safePage = Math.max(params.page, 1)
  const safePageSize = Math.min(Math.max(params.pageSize, 1), 100)

  return `keyword=${keyword}&page=${safePage}&pageSize=${safePageSize}`
}

function buildSearchResult(params: SearchParams): SearchResult {
  const query = createSearchQuery(params)
  const summary = `搜索“${params.keyword}”，第 ${params.page} 页，每页 ${params.pageSize} 条`

  return {
    query,
    summary
  }
}

const defaultPageSize = 20
const params: SearchParams = {
  keyword: ' Vue 3 ',
  page: 1,
  pageSize: defaultPageSize
}

const result = buildSearchResult(params)

console.log(result.query)
console.log(result.summary)

if (false) {
  // @ts-expect-error: page 必须是 number，不能传 string。
  createSearchQuery({ keyword: 'vue', page: '1', pageSize: 20 })

  // @ts-expect-error: 缺少 pageSize，不能当作 SearchParams 使用。
  buildSearchResult({ keyword: 'vue', page: 1 })
}
```

## 8. 代码逐行说明

```ts
type SearchParams = {
  keyword: string
  page: number
  pageSize: number
}
```

定义搜索参数的对象结构。这里是函数入口边界，所以应该主动写类型。

```ts
type SearchResult = {
  query: string
  summary: string
}
```

定义搜索结果的结构。这里是函数出口边界，也适合主动写类型。

```ts
function normalizeKeyword(keyword: string) {
  return keyword.trim().toLowerCase()
}
```

参数 `keyword` 是函数入口，主动标注为 `string`。返回值没有写 `: string`，因为 TypeScript 可以从 `trim().toLowerCase()` 推断出来。

```ts
function createSearchQuery(params: SearchParams): string {
```

`params` 是业务边界，主动标注为 `SearchParams`。返回值写成 `string`，是为了明确这个函数的输出是 URL 查询字符串。

```ts
  const keyword = normalizeKeyword(params.keyword)
```

局部变量 `keyword` 不需要写 `: string`，因为 TypeScript 能从 `normalizeKeyword` 的返回值推断出来。

```ts
  const safePage = Math.max(params.page, 1)
  const safePageSize = Math.min(Math.max(params.pageSize, 1), 100)
```

这两个局部变量都会被推断为 `number`。它们属于函数内部细节，通常不必重复标注。

```ts
  return `keyword=${keyword}&page=${safePage}&pageSize=${safePageSize}`
```

返回查询字符串。如果这里返回数字，TypeScript 会因为函数返回值标注为 `string` 而报错。

```ts
function buildSearchResult(params: SearchParams): SearchResult {
```

这个函数把参数转换成页面可能需要展示的数据。入口和出口都属于边界，所以都写清楚。

```ts
const defaultPageSize = 20
```

这里不需要写 `: number`。右侧字面量足够清楚。

```ts
const params: SearchParams = {
  keyword: ' Vue 3 ',
  page: 1,
  pageSize: defaultPageSize
}
```

这个对象会传入业务函数，所以主动标注为 `SearchParams`。如果漏字段或写错字段类型，类型检查会提醒你。

```ts
const result = buildSearchResult(params)
```

不需要写 `: SearchResult`，因为 TypeScript 能从 `buildSearchResult` 的返回值推断出 `result` 的类型。

```ts
if (false) {
  // @ts-expect-error: page 必须是 number，不能传 string。
  createSearchQuery({ keyword: 'vue', page: '1', pageSize: 20 })

  // @ts-expect-error: 缺少 pageSize，不能当作 SearchParams 使用。
  buildSearchResult({ keyword: 'vue', page: 1 })
}
```

`if (false)` 里的代码不会在运行时执行，但 TypeScript 仍会检查它们。`@ts-expect-error` 表示下一行应该出现类型错误，用来证明这类错误确实会被拦住。

## 9. 运行方式

进入示例目录：

```bash
cd tracks/01-typescript-vue3/examples/L003-annotations-inference
```

执行类型检查：

```bash
tsc --noEmit
```

编译 TypeScript：

```bash
tsc
```

运行编译后的 JavaScript：

```bash
node dist/index.js
```

查看编译产物：

```bash
sed -n '1,160p' dist/index.js
```

## 10. 预期结果与真实验证结果

本节已真实执行以下命令。

### `tsc --noEmit`

预期和实际结果都是：命令成功，无输出。

这说明类型检查通过，并且两个 `@ts-expect-error` 标记确实对应了类型错误。

### `tsc`

预期和实际结果都是：命令成功，无输出，并生成 `dist/index.js`。

### `node dist/index.js`

预期和实际结果都是：

```text
keyword=vue 3&page=1&pageSize=20
搜索“ Vue 3 ”，第 1 页，每页 20 条
```

### `sed -n '1,160p' dist/index.js`

实际输出显示：`SearchParams`、`SearchResult`、函数参数类型和返回值类型都已经被擦除，编译产物只保留 JavaScript 逻辑。

## 11. 常见错误

### 误区 1：所有变量都要写类型标注

不需要。像 `const defaultPageSize = 20` 这种值很明确的局部变量，交给类型推断更简洁。

### 误区 2：函数参数可以都不写类型

在 `strict` 模式下，函数参数如果无法推断，会触发隐式 `any` 错误。函数参数是调用边界，通常应该主动标注。

### 误区 3：返回值永远不用写

小函数可以让 TypeScript 推断返回值；但公共函数、请求封装、状态转换函数建议写返回值类型。这样能防止以后改代码时不小心改变输出结构。

### 误区 4：对象标注越具体越好

具体不是目的，契约清晰才是目的。临时对象可以推断；跨函数、跨组件、跨接口传递的对象才更需要命名类型。

### 误区 5：`as` 断言可以代替类型标注

`as` 更像是你告诉编译器“相信我”。如果只是为了压住错误就到处写 `as`，很容易绕过类型系统。初学阶段优先用明确的类型标注。

## 12. 小练习

在 `src/index.ts` 中新增一个函数：

```ts
function buildPageTitle(params: SearchParams): string {
  return `搜索结果：${params.keyword}`
}
```

然后输出：

```ts
console.log(buildPageTitle(params))
```

要求：

- `params` 参数必须主动标注类型。
- 返回值必须主动标注为 `string`。
- 运行 `tsc --noEmit` 确认类型检查通过。
- 运行 `tsc` 和 `node dist/index.js`，确认多输出一行页面标题。

## 13. 贴近真实业务的应用场景

后台管理系统经常有列表页：用户列表、订单列表、文章列表、日志列表。它们通常都需要查询参数：

```ts
type ListQuery = {
  keyword: string
  page: number
  pageSize: number
}
```

在 Vue 3 中，这个类型可能会同时出现在：

- 搜索表单状态。
- 请求函数参数。
- 路由查询参数转换。
- Pinia Store 的列表查询条件。

如果边界没有类型标注，字段名和字段类型很容易在多个文件里漂移。把 `ListQuery` 这种边界类型写清楚，可以让调用方、维护者和编译器都知道数据应该长什么样。

## 14. 本节复盘问题

- 类型标注和类型推断分别是什么？
- 为什么局部变量通常可以少写类型？
- 为什么函数参数更适合主动标注？
- 什么样的类型应该提取成 `type` 或 `interface`？
- `as` 断言为什么不能滥用？

## 15. 下一节预告

下一节学习：基础类型、对象类型与函数类型的第一组核心写法。

我们会开始系统学习 `string`、`number`、`boolean`、对象、数组和函数签名，并继续用接近 Vue 业务代码的小例子来练。
