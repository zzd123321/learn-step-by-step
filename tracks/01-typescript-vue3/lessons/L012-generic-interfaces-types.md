# L012 泛型接口与泛型类型别名

前两节我们学习了泛型函数和泛型约束。泛型不只用于函数，也经常用于长期存在的数据结构：接口响应、分页结果、表单状态、请求状态、组件 Props。

本节只讲一个核心问题：什么时候把泛型写在 `interface` 上，什么时候写在 `type` 上。

参考：

- https://www.typescriptlang.org/docs/handbook/2/generics.html
- https://www.typescriptlang.org/docs/handbook/2/everyday-types.html

## 泛型接口：稳定对象契约

如果你要描述一个稳定的对象结构，泛型接口很自然。

```ts
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
```

`ApiResponse<T>` 表示接口响应外壳固定，里面的 `data` 会随接口变化。

例如：

```ts
type UserResponse = ApiResponse<User>
type ProductResponse = ApiResponse<Product>
```

它很适合这些场景：

- 接口响应结构。
- 分页数据结构。
- 表单字段结构。
- 组件 Props 结构。
- Store 状态结构。

对象外形稳定，只是某些字段的类型变化，这就是泛型接口擅长的地方。

## 泛型类型别名：联合、组合和表达式

`type` 也能写泛型：

```ts
type AsyncState<T> =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'success'; data: T }
  | { status: 'error'; message: string }
```

这不是单一对象结构，而是一个联合类型。它表示请求状态可能是空闲、加载中、成功或失败。成功时才有 `data`。

这种“类型表达式”很适合用 `type`：

- 联合类型。
- 交叉类型。
- 元组。
- 函数类型。
- 条件类型和映射类型，后面会学。

## 示例：接口响应与请求状态

示例目录：

```text
tracks/01-typescript-vue3/examples/L012-generic-interfaces-types/
```

`src/index.ts`：

```ts
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

type AsyncState<T> =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'success'; data: T }
  | { status: 'error'; message: string }

type User = {
  id: number
  name: string
  role: 'admin' | 'viewer'
}

function unwrapResponse<T>(response: ApiResponse<T>): T {
  if (response.code !== 0) {
    throw new Error(response.message)
  }

  return response.data
}

function createSuccessState<T>(data: T): AsyncState<T> {
  return {
    status: 'success',
    data
  }
}

function formatUserPageState(state: AsyncState<PageResult<User>>): string {
  switch (state.status) {
    case 'idle':
      return '尚未请求'
    case 'loading':
      return '加载中'
    case 'error':
      return `加载失败：${state.message}`
    case 'success':
      return `共 ${state.data.total} 个用户：${state.data.list.map((user) => user.name).join('、')}`
  }
}

const response: ApiResponse<PageResult<User>> = {
  code: 0,
  message: 'ok',
  data: {
    list: [
      { id: 1, name: 'Alice', role: 'admin' },
      { id: 2, name: 'Bob', role: 'viewer' }
    ],
    total: 2,
    page: 1,
    pageSize: 10
  }
}

const page = unwrapResponse(response)
const state = createSuccessState(page)

console.log(formatUserPageState(state))

if (false) {
  const errorState: AsyncState<PageResult<User>> = {
    status: 'error',
    message: '网络错误'
  }

  // @ts-expect-error: error 状态没有 data 字段。
  console.log(errorState.data)

  const wrongResponse: ApiResponse<User> = {
    code: 0,
    message: 'ok',
    data: {
      // @ts-expect-error: ApiResponse<User> 的 data 必须是 User。
      title: '不是用户'
    }
  }

  console.log(wrongResponse)
}
```

## 关键代码解析

```ts
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
```

这是泛型接口。接口响应外层结构固定，`data` 的具体类型由 `T` 决定。

如果是用户详情接口，`T` 是 `User`；如果是用户分页接口，`T` 是 `PageResult<User>`。

```ts
interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}
```

分页结构也很适合泛型接口。`total`、`page`、`pageSize` 固定，只有 `list` 的元素类型变化。

```ts
type AsyncState<T> =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'success'; data: T }
  | { status: 'error'; message: string }
```

这是泛型类型别名。它不是一个固定对象，而是四种对象结构的联合。成功状态才有 `data`，失败状态才有 `message`。

这类联合类型更适合用 `type` 表达。

```ts
function unwrapResponse<T>(response: ApiResponse<T>): T {
```

这个函数接收 `ApiResponse<T>`，返回 `T`。它保留了响应中 `data` 的具体类型。

如果传入 `ApiResponse<PageResult<User>>`，返回值就是 `PageResult<User>`。

```ts
function createSuccessState<T>(data: T): AsyncState<T> {
```

这个函数把任意数据包装成成功状态。传入用户分页，返回 `AsyncState<PageResult<User>>`；传入商品列表，返回 `AsyncState<Product[]>`。

```ts
function formatUserPageState(state: AsyncState<PageResult<User>>): string {
```

这里把泛型类型组合起来：请求状态中的成功数据是用户分页。

```ts
case 'success':
  return `共 ${state.data.total} 个用户：${state.data.list.map((user) => user.name).join('、')}`
```

进入 `success` 分支后，TypeScript 知道 `state` 有 `data`，而且 `data` 是 `PageResult<User>`，所以可以访问 `total`、`list` 和 `user.name`。

## 和 Vue 3 业务代码的联系

请求层可以用泛型接口统一响应结构：

```ts
async function request<T>(url: string): Promise<ApiResponse<T>> {
  // ...
}
```

列表页状态可以用泛型类型别名表达：

```ts
const state = ref<AsyncState<PageResult<User>>>({ status: 'idle' })
```

组件逻辑里根据 `state.status` 分支，TypeScript 会自动收窄：

```ts
if (state.value.status === 'success') {
  console.log(state.value.data.list)
}
```

这比写一堆可选字段更清楚：

```ts
// 不推荐作为复杂请求状态的主要建模方式
type LooseState<T> = {
  loading?: boolean
  data?: T
  error?: string
}
```

可选字段太多时，状态之间的关系会变得模糊。判别联合能更准确地表达“成功才有数据，失败才有错误信息”。

## 常见误区

不要把所有泛型结构都写成 `interface`。联合类型、元组、函数类型更适合 `type`。

不要把请求状态写成一堆互相独立的可选字段。`loading: true` 和 `data` 同时存在时，到底该信哪个？判别联合能减少这类矛盾状态。

不要让泛型层数无限套娃。`AsyncState<ApiResponse<PageResult<User>>>` 不是不能写，但要确认每一层都真的有业务意义。

不要把 `T` 当成神秘语法。它只是类型参数，可以换成更语义化的名字，比如 `TData`、`TItem`。

## 运行和已验证结果

进入示例目录：

```bash
cd tracks/01-typescript-vue3/examples/L012-generic-interfaces-types
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
共 2 个用户：Alice、Bob
```

`sed -n '1,220p' dist/index.js` 已确认泛型接口和泛型类型别名都在编译后被擦除。

## 练习

新增一个商品类型：

```ts
type Product = {
  id: number
  title: string
  price: number
}
```

然后创建：

```ts
const productState: AsyncState<PageResult<Product>>
```

要求：

- 构造一个商品分页响应。
- 用 `unwrapResponse` 取出分页数据。
- 用 `createSuccessState` 包成成功状态。
- 写一个 `formatProductPageState` 输出商品标题。

运行：

```bash
tsc --noEmit
tsc
node dist/index.js
```

## 下一步

下一节学习条件类型、映射类型与 `infer` 的入门直觉。我们会非常克制地只讲它们解决什么问题，不会一上来写复杂类型体操。
