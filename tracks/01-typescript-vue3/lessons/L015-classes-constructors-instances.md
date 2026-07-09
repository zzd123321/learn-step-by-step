# L015 类、构造函数与实例类型

TypeScript 的 `class` 不是一套独立于 JavaScript 的新运行时机制。它建立在 JavaScript 的 `class` 之上，只是增加了类型标注、字段检查、构造函数参数检查、实例类型推断等能力。

本节只讲一个核心问题：

> 一个 class 同时出现在运行时和值空间，也会在 TypeScript 中产生实例类型。

这句话有点抽象，我们用订单示例拆开看。

官方参考：

- https://www.typescriptlang.org/docs/handbook/2/classes.html

## 为什么 Vue 2 前端开发者要学它

很多 Vue 2 项目主要使用对象字面量、函数和组件配置，不一定大量使用 class。到了 TypeScript、Vue 3 和 Node.js 场景，class 仍然不是默认答案，但你会在这些地方遇到它：

- 第三方 SDK：例如请求客户端、日志客户端、数据库客户端。
- Node.js 服务端：封装业务服务、仓储对象、任务执行器。
- 前端复杂业务：封装有状态的编辑器、播放器、流程控制器。
- 框架生态：某些依赖注入、ORM、验证库会用 class 表达模型。

你不需要把所有业务都改成 class，但要能读懂 class 的类型行为，尤其是“类本身”和“类实例”不是同一个东西。

## JavaScript class 与 TypeScript class 的关系

JavaScript 中的 class 是运行时代码：

```ts
class Order {}

console.log(typeof Order)
```

`Order` 在运行时是一个函数值，可以被 `new Order()` 调用。

TypeScript 在这个基础上增加类型检查：

```ts
class Order {
  id: string

  constructor(id: string) {
    this.id = id
  }
}
```

这里的 `id: string` 和 `constructor(id: string)` 都是编译期信息。编译到 JavaScript 后，类型标注会被擦除，但构造函数、字段赋值和方法会保留下来。

## 字段、构造函数和方法

一个 class 常见由三部分组成：

```ts
class Order {
  id: string
  status: 'draft' | 'submitted' = 'draft'

  constructor(id: string) {
    this.id = id
  }

  submit(): void {
    this.status = 'submitted'
  }
}
```

`id: string` 是字段声明。它告诉 TypeScript：每个订单实例都应该有一个字符串 `id`。

`status: 'draft' | 'submitted' = 'draft'` 是带初始值的字段。TypeScript 会检查它只能是 `'draft'` 或 `'submitted'`。

`constructor(id: string)` 是构造函数。调用 `new Order('order-1001')` 时会执行它。

`submit()` 是实例方法。只有通过实例才能调用：

```ts
const order = new Order('order-1001')
order.submit()
```

## 类名在类型位置表示实例类型

这是 class 最容易混淆的一点：

```ts
class Order {}

function formatOrder(order: Order) {
  // ...
}
```

在参数类型位置，`Order` 表示“Order 的实例类型”，不是构造函数本身。

换句话说，`formatOrder(order: Order)` 需要的是：

```ts
const order = new Order()
```

而不是：

```ts
Order
```

如果你想描述构造函数本身，要用 `typeof Order`。

```ts
type OrderConstructor = typeof Order
```

`Order` 和 `typeof Order` 的区别可以这样记：

- `Order`：实例类型，表示 `new Order()` 得到的对象。
- `typeof Order`：构造函数值的类型，表示 class 这个值本身。

## 可运行示例：订单类

示例目录：

```text
tracks/01-typescript-vue3/examples/L015-classes-constructors-instances/
```

`src/index.ts`：

```ts
type OrderStatus = 'draft' | 'submitted'

type OrderItem = {
  sku: string
  price: number
  quantity: number
}

class Order {
  id: string
  status: OrderStatus = 'draft'
  items: OrderItem[]

  constructor(id: string, items: OrderItem[] = []) {
    if (id.trim() === '') {
      throw new Error('订单 id 不能为空')
    }

    this.id = id
    this.items = items
  }

  addItem(item: OrderItem): void {
    if (item.quantity <= 0) {
      throw new Error('商品数量必须大于 0')
    }

    this.items.push(item)
  }

  getTotal(): number {
    return this.items.reduce((sum, item) => sum + item.price * item.quantity, 0)
  }

  submit(): void {
    if (this.items.length === 0) {
      throw new Error('空订单不能提交')
    }

    this.status = 'submitted'
  }
}

type OrderInstance = InstanceType<typeof Order>
type OrderConstructorArgs = ConstructorParameters<typeof Order>

function createEntity<TInstance, TArgs extends unknown[]>(
  Entity: new (...args: TArgs) => TInstance,
  ...args: TArgs
): TInstance {
  return new Entity(...args)
}

function formatOrder(order: OrderInstance): string {
  return `${order.id} / ${order.status} / ${order.items.length} 件 / ${order.getTotal()} 元`
}

const orderArgs: OrderConstructorArgs = [
  'order-1001',
  [{ sku: 'keyboard', price: 399, quantity: 1 }]
]

const order = createEntity(Order, ...orderArgs)
order.addItem({ sku: 'mouse', price: 129, quantity: 2 })
order.submit()

console.log(`Order 在运行时是：${typeof Order}`)
console.log(formatOrder(order))
console.log(`order instanceof Order：${order instanceof Order}`)

if (false) {
  // @ts-expect-error: 构造函数第一个参数必须是 string。
  const wrongOrder = new Order(1001)

  // @ts-expect-error: Order 实例必须拥有类定义中的方法。
  const fakeOrder: Order = {
    id: 'order-1002',
    status: 'draft',
    items: []
  }

  console.log(wrongOrder, fakeOrder)
}
```

## 代码解析

```ts
type OrderStatus = 'draft' | 'submitted'
```

订单状态用字面量联合表示，只允许草稿和已提交。这样 `status` 不会被随手写成 `'done'`、`'finish'` 这类业务外状态。

```ts
type OrderItem = {
  sku: string
  price: number
  quantity: number
}
```

`OrderItem` 只是普通对象类型。不是所有东西都需要 class。商品行没有行为，只是数据结构，用 `type` 足够。

```ts
class Order {
  id: string
  status: OrderStatus = 'draft'
  items: OrderItem[]
```

`Order` 是真正的运行时 class。它有字段，也有后面的行为方法。

`status` 有初始值，所以创建实例时不用在构造函数里单独赋值。

`id` 和 `items` 没有在字段声明处初始化，所以必须在构造函数里赋值。由于示例项目开启了 `strict`，TypeScript 会检查这些字段是否被正确初始化。

```ts
constructor(id: string, items: OrderItem[] = []) {
```

构造函数的参数可以写类型，也可以有默认值。第二个参数默认是空数组，所以这两种写法都合法：

```ts
new Order('order-1001')
new Order('order-1001', [{ sku: 'keyboard', price: 399, quantity: 1 }])
```

```ts
if (id.trim() === '') {
  throw new Error('订单 id 不能为空')
}
```

这是运行时校验。TypeScript 能保证 `id` 是字符串，但不能保证字符串不是空值。类型检查不能替代业务校验。

```ts
addItem(item: OrderItem): void {
```

`addItem` 是实例方法。参数必须符合 `OrderItem`，返回值是 `void`，表示这个方法的主要作用是修改当前订单实例。

```ts
getTotal(): number {
  return this.items.reduce((sum, item) => sum + item.price * item.quantity, 0)
}
```

方法内部通过 `this` 访问当前实例。返回值标注为 `number`，如果误返回字符串，TypeScript 会报错。

```ts
submit(): void {
```

`submit` 修改订单状态。这里仍然保留运行时校验：空订单不能提交。

```ts
type OrderInstance = InstanceType<typeof Order>
```

`InstanceType` 是上一节学过的 Utility Type。它从构造函数类型中提取实例类型。

`typeof Order` 是构造函数类型，`InstanceType<typeof Order>` 得到的就是 `new Order(...)` 之后的实例类型。这个结果和直接写 `Order` 基本一致，但它能帮助你理解 class 的两面：

- `Order` 在类型位置常表示实例。
- `typeof Order` 表示 class 这个构造函数值。

```ts
type OrderConstructorArgs = ConstructorParameters<typeof Order>
```

`ConstructorParameters` 会提取构造函数参数元组。这里得到的类型类似：

```ts
type OrderConstructorArgs = [id: string, items?: OrderItem[]]
```

所以 `orderArgs` 必须按构造函数签名提供参数。

```ts
function createEntity<TInstance, TArgs extends unknown[]>(
  Entity: new (...args: TArgs) => TInstance,
  ...args: TArgs
): TInstance {
  return new Entity(...args)
}
```

这是一个通用工厂函数。`Entity: new (...args: TArgs) => TInstance` 表示传入的必须是“可以被 `new` 调用的东西”。

这不是在描述实例，而是在描述构造函数。真实项目里，依赖注入容器、模型工厂、测试辅助函数经常会需要这种类型。

```ts
const order = createEntity(Order, ...orderArgs)
```

这里传入的是 class 本身，也就是构造函数值。`createEntity` 内部执行 `new Entity(...args)`，返回订单实例。

```ts
function formatOrder(order: OrderInstance): string {
```

`formatOrder` 接收的是订单实例，所以可以访问 `order.id`、`order.status`、`order.getTotal()`。

```ts
console.log(`Order 在运行时是：${typeof Order}`)
console.log(`order instanceof Order：${order instanceof Order}`)
```

这两行证明 class 不是纯类型。`Order` 在运行时真实存在，`order instanceof Order` 也是真实的 JavaScript 判断。

最后的 `if (false)` 不会执行，但 TypeScript 仍会检查里面的代码：

```ts
const wrongOrder = new Order(1001)
```

构造函数第一个参数必须是字符串，所以这里应该报错。

```ts
const fakeOrder: Order = {
  id: 'order-1002',
  status: 'draft',
  items: []
}
```

这个对象缺少 `addItem`、`getTotal`、`submit` 方法，因此不能当作完整的 `Order` 实例。

## 运行与真实验证

在示例目录执行：

```bash
tsc --noEmit
tsc
node dist/index.js
sed -n '1,220p' dist/index.js
```

实际运行输出：

```text
Order 在运行时是：function
order-1001 / submitted / 2 件 / 657 元
order instanceof Order：true
```

## 常见误区

第一个误区：以为 class 只是类型。

```ts
class Order {}
console.log(Order)
```

`Order` 会出现在编译后的 JavaScript 里，它是运行时值。

第二个误区：以为 TypeScript 能替代所有构造函数校验。

```ts
new Order('')
```

空字符串仍然是字符串，TypeScript 不会自动阻止它。像“不能为空”“数量必须大于 0”这类规则仍然要写运行时校验。

第三个误区：把实例类型和构造函数类型混在一起。

```ts
function useOrder(order: Order) {}
```

这个函数要的是实例。

```ts
function useOrderClass(OrderClass: typeof Order) {}
```

这个函数要的是 class 本身。

第四个误区：为了 TypeScript 而滥用 class。

如果只是普通接口数据，比如接口返回的用户、商品、列表项，用 `type` 或 `interface` 就很好。class 更适合“数据和行为必须放在一起”的场景。

## 一个小练习

在本节示例中新增一个 `Cart` 类：

1. 字段包含 `id: string` 和 `items: OrderItem[]`。
2. 构造函数接收 `id`，并校验不能为空。
3. 实现 `addItem(item: OrderItem): void`。
4. 实现 `getTotal(): number`。
5. 用 `InstanceType<typeof Cart>` 定义 `CartInstance`，并写一个 `formatCart(cart: CartInstance): string`。

练习重点不是多写一个类，而是确认你能区分 `Cart`、`typeof Cart` 和 `InstanceType<typeof Cart>`。

## 真实业务场景

在 Vue 3 前端里，大部分页面状态可以继续用普通对象和组合式函数。但当你有一个复杂、有状态、带行为的对象时，class 会更自然。

例如一个上传队列：

- 字段：任务列表、当前状态、失败原因。
- 方法：添加任务、开始上传、暂停、重试、计算进度。
- 运行时：需要 `new UploadQueue()` 创建独立实例。

这时 class 能把数据和行为组织在一起。TypeScript 则负责检查构造参数、字段初始化、方法参数和实例类型。

## 本节复盘

你可以用下面几个问题检查自己：

1. `Order` 在类型位置和运行时分别表示什么？
2. `typeof Order` 表示实例类型还是构造函数类型？
3. 为什么 `new Order('')` 仍然需要运行时校验？
4. `InstanceType<typeof Order>` 能提取什么？
5. 普通接口数据为什么不一定需要写成 class？

下一节建议学习 L016：访问修饰符与只读字段。我们会看 `public`、`private`、`protected`、`readonly` 的边界，以及它们和 JavaScript 运行时行为之间的差异。
