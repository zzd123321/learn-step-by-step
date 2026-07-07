# L005 数组、元组与字面量类型

上一节我们用 `Product[]` 表示商品列表，用对象类型描述列表项。本节继续往下走：数组、元组和字面量类型。

这三个类型经常一起出现：

- 数组描述“同类数据的列表”，例如订单列表、用户列表、标签列表。
- 元组描述“固定长度、固定位置含义的数据”，例如 `[状态值, 展示文案]`。
- 字面量类型描述“值只能是几个固定选项之一”，例如订单状态只能是 `'pending' | 'paid' | 'cancelled'`。

参考：

- https://www.typescriptlang.org/docs/handbook/2/everyday-types.html
- https://www.typescriptlang.org/docs/handbook/2/objects.html

## 数组：描述同类数据列表

TypeScript 中数组常见有两种写法：

```ts
type OrderList = Order[]
type OrderList2 = Array<Order>
```

这两种写法都表示“由 `Order` 组成的数组”。日常业务代码里更常见的是 `Order[]`，更短，也更接近 Vue 模板中遍历列表的直觉。

数组类型约束的是“每个元素应该是什么类型”。

```ts
type Order = {
  id: number
  customer: string
  status: OrderStatus
  total: number
}

const orders: Order[] = [
  {
    id: 1001,
    customer: 'Alice',
    status: 'paid',
    total: 199
  }
]
```

如果你往 `orders` 里放一个缺少 `status` 的对象，或者把 `total` 写成字符串，TypeScript 会在类型检查阶段提醒你。

## 字面量类型：把业务状态限制在固定范围

普通 `string` 太宽了。

```ts
type OrderStatus = string
```

如果这样写，`'paid'`、`'payed'`、`'finished'` 都是合法字符串，TypeScript 没法帮你发现状态拼错。

更适合业务状态的写法是字面量联合类型：

```ts
type OrderStatus = 'pending' | 'paid' | 'cancelled'
```

它的意思是：`OrderStatus` 只能是这三个字符串之一。这里的 `|` 是联合类型，中文可以理解为“或者”。

这个写法非常适合 Vue 项目里的状态字段：

- 请求状态：`'idle' | 'loading' | 'success' | 'error'`
- 用户角色：`'admin' | 'editor' | 'viewer'`
- 订单状态：`'pending' | 'paid' | 'cancelled'`
- 弹窗模式：`'create' | 'edit' | 'readonly'`

## 元组：固定位置有固定含义

数组只关心“元素类型一致”。元组更进一步：它关心长度和每个位置的含义。

```ts
type StatusOption = [OrderStatus, string]
```

这个类型表示一个固定两项的数组：

- 第 1 项是订单状态值，例如 `'paid'`。
- 第 2 项是展示文案，例如 `'已支付'`。

它适合描述轻量的配置对：

```ts
const statusOptions: StatusOption[] = [
  ['pending', '待支付'],
  ['paid', '已支付'],
  ['cancelled', '已取消']
]
```

不过要注意：元组依赖位置含义。如果字段变多、含义复杂，或者需要更强可读性，对象通常更好：

```ts
type StatusOptionObject = {
  value: OrderStatus
  label: string
}
```

简单固定结构可以用元组；复杂业务配置优先用对象。

## 示例：订单列表状态筛选

示例目录：

```text
tracks/01-typescript-vue3/examples/L005-arrays-tuples-literals/
```

`src/index.ts`：

```ts
type OrderStatus = 'pending' | 'paid' | 'cancelled'

type Order = {
  id: number
  customer: string
  status: OrderStatus
  total: number
}

type StatusOption = [OrderStatus, string]

const statusOptions: StatusOption[] = [
  ['pending', '待支付'],
  ['paid', '已支付'],
  ['cancelled', '已取消']
]

const orders: Order[] = [
  {
    id: 1001,
    customer: 'Alice',
    status: 'paid',
    total: 199
  },
  {
    id: 1002,
    customer: 'Bob',
    status: 'pending',
    total: 69
  },
  {
    id: 1003,
    customer: 'Cindy',
    status: 'cancelled',
    total: 299
  }
]

function getStatusLabel(status: OrderStatus): string {
  const option = statusOptions.find(([value]) => value === status)
  return option ? option[1] : '未知状态'
}

function filterOrdersByStatus(orderList: Order[], status: OrderStatus): Order[] {
  return orderList.filter((order) => order.status === status)
}

function formatOrderLines(orderList: Order[]): string[] {
  return orderList.map((order) => {
    const statusLabel = getStatusLabel(order.status)
    return `#${order.id} ${order.customer} ${statusLabel} ¥${order.total}`
  })
}

const paidOrders = filterOrdersByStatus(orders, 'paid')
const lines = formatOrderLines(paidOrders)

for (const line of lines) {
  console.log(line)
}

if (false) {
  // @ts-expect-error: finished 不是合法的订单状态。
  filterOrdersByStatus(orders, 'finished')

  // @ts-expect-error: StatusOption 必须是 [OrderStatus, string]。
  statusOptions.push(['paid', 1])
}
```

## 关键代码解析

```ts
type OrderStatus = 'pending' | 'paid' | 'cancelled'
```

这行定义了订单状态的合法范围。只要函数参数、对象字段或筛选条件使用 `OrderStatus`，TypeScript 就会阻止非法状态传入。

```ts
type StatusOption = [OrderStatus, string]
```

这是一个元组类型。它不是“任意长度的数组”，而是固定两项：第 1 项是状态值，第 2 项是文案。

```ts
const statusOptions: StatusOption[] = [
  ['pending', '待支付'],
  ['paid', '已支付'],
  ['cancelled', '已取消']
]
```

`StatusOption[]` 表示“由状态选项元组组成的数组”。这里如果写成 `['paid', 1]`，第二项不是字符串，会被类型检查拦住。

```ts
function getStatusLabel(status: OrderStatus): string {
  const option = statusOptions.find(([value]) => value === status)
  return option ? option[1] : '未知状态'
}
```

`status` 是函数入口边界，标注为 `OrderStatus`。`find` 的回调里使用了元组解构：`[value]` 取出元组第一项。因为 `statusOptions` 是 `StatusOption[]`，所以 TypeScript 知道 `value` 是 `OrderStatus`。

`find` 的返回值可能是 `undefined`，因为数组里不一定能找到匹配项。虽然当前 `OrderStatus` 和 `statusOptions` 是对应的，但 TypeScript 不会自动证明配置一定完整，所以这里用三元表达式处理找不到的情况。

```ts
function filterOrdersByStatus(orderList: Order[], status: OrderStatus): Order[] {
  return orderList.filter((order) => order.status === status)
}
```

`orderList` 是订单数组，`status` 是合法状态，返回值仍然是订单数组。`filter` 不会改变元素类型，所以返回 `Order[]`。

```ts
function formatOrderLines(orderList: Order[]): string[] {
```

这里返回 `string[]`，表示格式化后的每一行都是字符串。这类函数很像 Vue 列表渲染前的数据转换：后端给对象数组，页面需要展示字符串或视图对象数组。

```ts
const paidOrders = filterOrdersByStatus(orders, 'paid')
```

`'paid'` 是合法的 `OrderStatus` 字面量，所以可以传入。如果写 `'finished'`，类型检查会失败。

## 和 Vue 3 业务代码的联系

数组对应 `v-for` 渲染的数据源：

```ts
const orders = ref<Order[]>([])
```

字面量类型对应页面状态或业务状态：

```ts
type RequestStatus = 'idle' | 'loading' | 'success' | 'error'
```

元组适合轻量固定配置：

```ts
type TabOption = ['all' | 'paid' | 'pending', string]
```

但如果配置要带图标、权限、颜色、排序等字段，就应该换成对象类型。不要为了“看起来短”牺牲可读性。

## 常见误区

不要把业务状态写成普通 `string`。状态通常是有限集合，字面量联合类型能防止拼写错误。

不要把元组当成万能配置。元组依赖位置记忆，字段超过两三项后可读性会快速下降。

不要忽略 `find` 可能返回 `undefined`。即使你认为配置完整，TypeScript 仍然会提醒你处理找不到的情况，这是好事。

不要在数组类型里混入不一致的数据。`Order[]` 表示每一项都是 `Order`，不是“差不多像 Order 就行”。

## 运行和已验证结果

进入示例目录：

```bash
cd tracks/01-typescript-vue3/examples/L005-arrays-tuples-literals
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
#1001 Alice 已支付 ¥199
```

`sed -n '1,180p' dist/index.js` 已确认 `OrderStatus`、`Order`、`StatusOption` 和函数类型标注都在编译后被擦除。

## 练习

给订单状态增加一个新值：

```ts
'refunded'
```

然后完成三件事：

- 把 `OrderStatus` 改成包含 `'refunded'`。
- 在 `statusOptions` 中增加 `['refunded', '已退款']`。
- 在 `orders` 中增加一条已退款订单，并筛选输出已退款订单。

运行：

```bash
tsc --noEmit
tsc
node dist/index.js
```

观察：如果只改 `orders`，但忘记改 `OrderStatus`，TypeScript 会如何提醒你？

## 下一步

下一节学习 `interface` 与 `type`。我们会比较它们都能描述对象结构，但在扩展、组合、命名语义和团队约定上有什么差异。
