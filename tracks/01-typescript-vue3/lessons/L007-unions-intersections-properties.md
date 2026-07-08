# L007 联合类型、交叉类型、可选属性与只读属性

上一节我们区分了 `interface` 和 `type`。本节继续学习对象建模中最常用的四个能力：联合类型、交叉类型、可选属性和只读属性。

它们解决的是非常具体的业务表达问题：

- 联合类型：一个值可能是几种情况之一。
- 交叉类型：一个对象同时拥有几组字段。
- 可选属性：字段可能不存在。
- 只读属性：字段创建后不应该被修改。

参考：

- https://www.typescriptlang.org/docs/handbook/2/everyday-types.html
- https://www.typescriptlang.org/docs/handbook/2/objects.html

## 联合类型：一个值可能属于多种情况

联合类型使用 `|`。

```ts
type SubmitStatus = 'success' | 'failed'
```

它表示 `SubmitStatus` 只能是 `'success'` 或 `'failed'`。这和前面学过的字面量类型是一脉相承的。

联合类型也可以用来描述接口结果：

```ts
type SubmitResult = SubmitSuccess | SubmitFailure
```

这表示提交结果要么是成功结构，要么是失败结构。真实项目中，接口返回、弹窗状态、请求状态、表单校验结果都很适合用联合类型表达。

## 交叉类型：同时拥有多组字段

交叉类型使用 `&`。

```ts
type OrderRecord = OrderDraft & AuditFields
```

它表示 `OrderRecord` 同时拥有 `OrderDraft` 和 `AuditFields` 的字段。

交叉类型常用于把一个业务实体和通用能力组合起来，例如：

- 订单字段 + 审计字段。
- 用户字段 + 权限字段。
- 接口数据 + 页面展示字段。
- 表单字段 + 校验状态。

如果只是稳定的对象继承关系，也可以用 `interface extends`。如果是在组合几个类型表达式，`type` + `&` 通常更自然。

## 可选属性：字段可能不存在

可选属性使用 `?`。

```ts
couponCode?: string
```

它表示 `couponCode` 可能是字符串，也可能不存在。读取可选属性时，TypeScript 会提醒你处理不存在的情况。

可选属性适合真实业务里的“不一定有”：

- 用户可能没有头像。
- 订单可能没有优惠码。
- 接口错误可能没有详细信息。
- 表单字段可能还没填写。

不要为了省事把所有字段都写成可选。字段是否可选应该来自业务事实，而不是为了绕过类型检查。

## 只读属性：创建后不应该修改

只读属性使用 `readonly`。

```ts
readonly id: number
```

它表示 `id` 创建后不能被重新赋值。这个限制发生在 TypeScript 类型检查阶段，编译后的 JavaScript 里不会自动冻结对象。

只读属性适合这些字段：

- 数据库 ID。
- 创建时间。
- 后端生成的流水号。
- 不应该在前端随意改写的接口字段。

## 示例：订单提交结果

示例目录：

```text
tracks/01-typescript-vue3/examples/L007-unions-intersections-properties/
```

`src/index.ts`：

```ts
type OrderStatus = 'draft' | 'submitted' | 'cancelled'

type OrderDraft = {
  readonly id: number
  customer: string
  total: number
  status: OrderStatus
  couponCode?: string
}

type AuditFields = {
  createdAt: string
  updatedAt: string
}

type OrderRecord = OrderDraft & AuditFields

type SubmitSuccess = {
  ok: true
  order: OrderRecord
}

type SubmitFailure = {
  ok: false
  message: string
  field?: 'customer' | 'total' | 'status'
}

type SubmitResult = SubmitSuccess | SubmitFailure

function formatCoupon(order: OrderDraft): string {
  return order.couponCode ? `优惠码：${order.couponCode}` : '无优惠码'
}

function submitOrder(order: OrderDraft): SubmitResult {
  if (order.customer.trim() === '') {
    return {
      ok: false,
      message: '客户名称不能为空',
      field: 'customer'
    }
  }

  if (order.total <= 0) {
    return {
      ok: false,
      message: '订单金额必须大于 0',
      field: 'total'
    }
  }

  const now = '2026-07-08T10:00:00.000Z'

  return {
    ok: true,
    order: {
      ...order,
      status: 'submitted',
      createdAt: now,
      updatedAt: now
    }
  }
}

function formatSubmitResult(result: SubmitResult): string {
  if (result.ok) {
    return `提交成功：#${result.order.id} ${result.order.customer} ${formatCoupon(result.order)}`
  }

  return `提交失败：${result.message}`
}

const draft: OrderDraft = {
  id: 1001,
  customer: 'Alice',
  total: 199,
  status: 'draft',
  couponCode: 'VUE3'
}

const result = submitOrder(draft)

console.log(formatSubmitResult(result))

if (false) {
  // @ts-expect-error: readonly id 不能被重新赋值。
  draft.id = 2001

  // @ts-expect-error: paid 不是合法的 OrderStatus。
  draft.status = 'paid'

  // @ts-expect-error: OrderRecord 必须同时包含订单字段和审计字段。
  const missingAuditFields: OrderRecord = {
    id: 1002,
    customer: 'Bob',
    total: 99,
    status: 'submitted'
  }

  console.log(missingAuditFields)
}
```

## 关键代码解析

```ts
type OrderStatus = 'draft' | 'submitted' | 'cancelled'
```

这是字面量联合类型。订单状态不是任意字符串，只能是草稿、已提交或已取消。

```ts
type OrderDraft = {
  readonly id: number
  customer: string
  total: number
  status: OrderStatus
  couponCode?: string
}
```

`readonly id` 表示订单 ID 不应该在前端被重新赋值。`couponCode?: string` 表示优惠码可能不存在，所以使用时必须考虑无优惠码的情况。

```ts
type AuditFields = {
  createdAt: string
  updatedAt: string
}

type OrderRecord = OrderDraft & AuditFields
```

`AuditFields` 是通用审计字段。`OrderRecord` 是交叉类型，表示这个对象既有订单草稿字段，也有审计字段。

```ts
type SubmitSuccess = {
  ok: true
  order: OrderRecord
}

type SubmitFailure = {
  ok: false
  message: string
  field?: 'customer' | 'total' | 'status'
}

type SubmitResult = SubmitSuccess | SubmitFailure
```

这里用 `ok: true` 和 `ok: false` 区分成功和失败。`SubmitResult` 是联合类型：提交结果要么成功，要么失败。

`field?: 'customer' | 'total' | 'status'` 表示错误可能关联某个字段，也可能只是整体错误。字段名也被限制在三个固定值里，避免写成不存在的字段。

```ts
function formatCoupon(order: OrderDraft): string {
  return order.couponCode ? `优惠码：${order.couponCode}` : '无优惠码'
}
```

因为 `couponCode` 是可选属性，所以这里先判断它是否存在。判断之后，TypeScript 知道它在真值分支里可以当作字符串使用。

```ts
function submitOrder(order: OrderDraft): SubmitResult {
```

函数返回 `SubmitResult`，调用方必须同时面对成功和失败两种情况，而不能只假设一定成功。

```ts
if (result.ok) {
  return `提交成功：#${result.order.id} ${result.order.customer} ${formatCoupon(result.order)}`
}

return `提交失败：${result.message}`
```

这段代码根据 `ok` 做分支。进入 `result.ok` 为真的分支后，TypeScript 能知道 `result` 是 `SubmitSuccess`，所以可以访问 `result.order`。否则就是 `SubmitFailure`，可以访问 `result.message`。

这种写法叫判别联合，后面学习类型收窄时会更系统地展开。本节先记住：联合类型配合固定字段，可以让分支逻辑更安全。

## 和 Vue 3 业务代码的联系

请求状态很适合用联合类型：

```ts
type RequestState =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'success'; data: OrderRecord[] }
  | { status: 'error'; message: string }
```

接口数据加页面字段很适合用交叉类型：

```ts
type OrderView = OrderRecord & {
  statusText: string
  canCancel: boolean
}
```

后端可能没有的字段适合可选属性：

```ts
avatarUrl?: string
remark?: string
```

不应该被页面随便改的字段适合只读属性：

```ts
readonly id: number
readonly createdAt: string
```

## 常见误区

不要把可能失败的结果写成单一成功对象。接口请求、表单提交和权限校验都可能失败，用联合类型能迫使调用方处理失败分支。

不要滥用可选属性。字段可选会把复杂度转移到所有读取它的地方，只有业务上真的可能不存在时才写 `?`。

不要误以为 `readonly` 会在运行时冻结对象。它是 TypeScript 的静态检查，不等于 `Object.freeze`。

不要为了复用字段强行使用交叉类型。交叉类型应该表达“同时拥有多组能力”，不是为了把毫无关系的字段拼在一起。

## 运行和已验证结果

进入示例目录：

```bash
cd tracks/01-typescript-vue3/examples/L007-unions-intersections-properties
```

已真实执行：

```bash
tsc --noEmit
tsc
node dist/index.js
sed -n '1,220p' dist/index.js
```

`tsc --noEmit` 成功且无输出，说明类型检查通过，并且三个 `@ts-expect-error` 标记确实对应类型错误。

`tsc` 成功且无输出，并生成 `dist/index.js`。

`node dist/index.js` 的实际输出：

```text
提交成功：#1001 Alice 优惠码：VUE3
```

`sed -n '1,220p' dist/index.js` 已确认联合类型、交叉类型、可选属性和只读属性都在编译后被擦除。

## 练习

新增一个失败示例：

```ts
const failedResult = submitOrder({
  id: 1002,
  customer: '',
  total: 88,
  status: 'draft'
})

console.log(formatSubmitResult(failedResult))
```

运行：

```bash
tsc --noEmit
tsc
node dist/index.js
```

观察输出是否多了一行：

```text
提交失败：客户名称不能为空
```

再尝试直接访问 `failedResult.order`，看看 TypeScript 为什么不允许你这么做。

## 下一步

下一节学习类型收窄和类型守卫。我们会更系统地看 `if`、`typeof`、`in`、判别字段如何一步步帮助 TypeScript 判断一个值到底是哪种类型。
