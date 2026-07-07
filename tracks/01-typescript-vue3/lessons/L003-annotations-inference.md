# L003 类型标注与类型推断

上一节我们看到了 `tsc` 会检查类型并输出 JavaScript。这一节解决一个日常写 TypeScript 时最容易摇摆的问题：到底什么时候该写类型，什么时候该让 TypeScript 自己推断？

结论先放前面：边界处主动标注，局部实现尽量推断。

官方 Everyday Types 文档也给了类似方向：变量类型标注通常是可选的，TypeScript 会尽量根据初始化值自动推断；函数参数和函数返回值则是传递数据时最重要的类型位置。

参考：

- https://www.typescriptlang.org/docs/handbook/2/everyday-types.html
- https://www.typescriptlang.org/docs/handbook/type-inference.html

## 类型标注是什么

类型标注是你主动告诉 TypeScript：这个值应该是什么类型。

```ts
const pageSize: number = 20
```

冒号后面的 `number` 就是类型标注。TypeScript 的标注写在“被标注的东西后面”，这和 Java、C# 那类 `int count = 1` 的写法不同。

常见标注位置：

- 变量：`const keyword: string = 'vue'`
- 函数参数：`function search(keyword: string) {}`
- 函数返回值：`function getTotal(): number {}`
- 对象结构：`type SearchParams = { keyword: string }`
- 接口边界：请求参数、响应数据、组件 Props、Store 状态

## 类型推断是什么

类型推断是 TypeScript 根据已有信息自动推导类型。

```ts
const pageSize = 20
```

这里没有写 `: number`，但 TypeScript 能根据初始化值推断出它是数字。

再比如：

```ts
const user = {
  id: 1,
  name: 'Alice'
}
```

TypeScript 会推断这个对象大致有两个属性：

```ts
{
  id: number
  name: string
}
```

推断也会来自上下文。例如数组的 `forEach` 回调中，参数类型常常能从数组元素类型推出来。后面学 Vue 3 时，`computed`、`watch`、组件事件回调也会大量依赖这种上下文推断。

## 一个实用规则：边界标注，内部推断

很多初学 TypeScript 的代码会走向两个极端：

- 每个变量都写类型，代码很吵。
- 基本不写类型，最后被 `any` 淹没。

更稳定的写法是：

```text
函数参数、返回值、接口数据、组件 Props、Store 状态：主动标注
函数内部临时变量、简单计算结果、明显字面量：交给推断
```

为什么这样分？因为边界是别人要调用、要维护、要复用的地方；局部变量只是当前函数内部实现细节。边界不清楚会影响协作，局部变量标太多会降低阅读效率。

## 示例：列表查询参数

示例目录：

```text
tracks/01-typescript-vue3/examples/L003-annotations-inference/
```

`src/index.ts`：

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

这个例子模拟后台列表页的查询参数。你现在在 Vue 2 项目里应该很熟悉这种数据：关键词、页码、每页条数。后面迁移到 Vue 3 时，它可能会同时出现在搜索表单、路由 query、请求函数和 Pinia Store 里。

## 关键代码解析

```ts
type SearchParams = {
  keyword: string
  page: number
  pageSize: number
}
```

`SearchParams` 是一个边界类型。它描述列表查询需要哪些字段。只要这个类型稳定，调用方就知道请求函数需要传什么。

```ts
type SearchResult = {
  query: string
  summary: string
}
```

`SearchResult` 是函数输出边界。返回值不是随手拼出来的临时对象，而是后续页面可能会使用的数据结构，所以给它命名。

```ts
function normalizeKeyword(keyword: string) {
  return keyword.trim().toLowerCase()
}
```

参数 `keyword` 是函数入口，应该标注。返回值没有写 `: string`，因为 TypeScript 能从 `trim().toLowerCase()` 推断它是字符串。

```ts
function createSearchQuery(params: SearchParams): string {
```

这里入口和出口都写了类型。`params` 是调用边界，`: string` 明确告诉维护者：这个函数返回 URL 查询字符串。

```ts
const keyword = normalizeKeyword(params.keyword)
const safePage = Math.max(params.page, 1)
const safePageSize = Math.min(Math.max(params.pageSize, 1), 100)
```

这三个局部变量都没有手写类型。`keyword` 可从 `normalizeKeyword` 推断，`safePage` 和 `safePageSize` 可从 `Math.max` / `Math.min` 推断。它们属于函数内部细节，写类型反而重复。

```ts
const defaultPageSize = 20
```

这里也不需要写 `: number`。右侧值已经足够明确。

```ts
const params: SearchParams = {
  keyword: ' Vue 3 ',
  page: 1,
  pageSize: defaultPageSize
}
```

这个对象会传入业务函数，所以主动标注为 `SearchParams`。如果漏掉 `pageSize`，或者把 `page` 写成字符串，类型检查会在运行前提醒你。

```ts
const result = buildSearchResult(params)
```

这里不用写 `: SearchResult`。因为 `buildSearchResult` 的返回值已经标注为 `SearchResult`，`result` 可以直接从函数调用推断出来。

```ts
if (false) {
  // @ts-expect-error: page 必须是 number，不能传 string。
  createSearchQuery({ keyword: 'vue', page: '1', pageSize: 20 })

  // @ts-expect-error: 缺少 pageSize，不能当作 SearchParams 使用。
  buildSearchResult({ keyword: 'vue', page: 1 })
}
```

这段代码保留了两个反例。它们不会在运行时执行，但会被 TypeScript 检查。`@ts-expect-error` 用来确认这些错误确实存在。

## 和 Vue 3 的联系

这节虽然还没写 Vue 代码，但规则会直接迁移到 Vue 3：

```ts
const loading = ref(false)
```

这种明显初始值可以让 Vue 和 TypeScript 推断。

```ts
type UserQuery = {
  keyword: string
  page: number
  pageSize: number
}

async function fetchUsers(query: UserQuery): Promise<UserListItem[]> {
  // ...
}
```

请求参数和返回值属于边界，应该主动写清楚。

```ts
const totalText = computed(() => `${total.value} 条`)
```

简单派生值通常可以先交给推断，除非你要限制公共 API 或避免返回值被改坏。

## 常见误区

不要给所有变量都写类型。`const count: number = 1` 在多数场景只是重复信息。

不要让函数参数变成隐式 `any`。在 `strict` 模式下，如果函数参数既没有标注，也无法从上下文推断，TypeScript 会报错。

不要用 `as` 断言代替正常建模。`as` 更像是“我知道得比编译器多”，滥用它会绕开类型检查。

不要忽略返回值类型的文档价值。公共函数、请求封装、状态转换函数可以显式写返回值类型，防止未来重构时不小心改变输出结构。

## 运行和已验证结果

进入示例目录：

```bash
cd tracks/01-typescript-vue3/examples/L003-annotations-inference
```

已真实执行：

```bash
tsc --noEmit
tsc
node dist/index.js
sed -n '1,160p' dist/index.js
```

`tsc --noEmit` 成功且无输出，说明类型检查通过，并且两个 `@ts-expect-error` 标记确实对应类型错误。

`tsc` 成功且无输出，并生成 `dist/index.js`。

`node dist/index.js` 的实际输出：

```text
keyword=vue 3&page=1&pageSize=20
搜索“ Vue 3 ”，第 1 页，每页 20 条
```

`sed -n '1,160p' dist/index.js` 已确认 `SearchParams`、`SearchResult`、函数参数类型和返回值类型都在编译后被擦除。

## 练习

在 `src/index.ts` 中新增：

```ts
function buildPageTitle(params: SearchParams): string {
  return `搜索结果：${params.keyword}`
}

console.log(buildPageTitle(params))
```

要求：

- `params` 参数主动标注为 `SearchParams`。
- 返回值主动标注为 `string`。
- 运行 `tsc --noEmit` 确认类型检查通过。
- 运行 `tsc` 和 `node dist/index.js`，确认多输出一行页面标题。

## 下一步

下一节进入基础类型、对象类型与函数类型。我们会系统整理 `string`、`number`、`boolean`、对象、数组和函数签名，并继续用接近 Vue 业务代码的例子练习。
