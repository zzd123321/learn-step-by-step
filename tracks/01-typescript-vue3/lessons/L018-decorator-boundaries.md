# L018 装饰器的使用边界

装饰器（Decorator）是一种把函数挂到类或类成员上的语法。它看起来像注解：

```ts
class PaymentService {
  @loggedMethod
  pay() {}
}
```

但它不是单纯的类型标记。装饰器会在运行时执行，并且可以观察、替换或修改类和类成员。

本节只建立一个边界意识：

> 装饰器适合框架、库、横切逻辑和声明式配置；普通业务代码不要为了“看起来高级”而滥用装饰器。

官方参考：

- https://www.typescriptlang.org/docs/handbook/release-notes/typescript-5-0.html
- https://www.typescriptlang.org/docs/handbook/decorators.html

## 先区分新旧装饰器

TypeScript 曾长期支持一套旧的实验性装饰器，需要开启 `experimentalDecorators`。很多 Angular、NestJS、老教程都基于这套模型。

TypeScript 5.0 开始支持新的 ECMAScript Stage 3 装饰器语义。新的装饰器：

- 不需要 `experimentalDecorators` 才能作为语法使用。
- 类型和运行时调用方式与旧装饰器不同。
- 不兼容 `emitDecoratorMetadata` 那套元数据机制。
- 不支持装饰参数。

本节示例使用的是 TypeScript 5.0 之后的新装饰器模型。你以后阅读框架代码时，要先判断它使用的是新装饰器还是旧实验性装饰器。

## 为什么 Vue 2 前端开发者要学它

Vue 2 生态里，你可能见过 class 风格组件、装饰器式写法，或者在 Angular、NestJS 里见过：

```ts
@Controller('/users')
class UserController {}
```

装饰器很像“给代码贴标签”，但它其实是运行时函数调用。它经常用于：

- 路由注册。
- 依赖注入。
- 权限检查。
- 日志追踪。
- 参数校验。
- ORM 模型声明。

这些都属于横切逻辑或框架边界。普通业务函数如果大量依赖装饰器，调用链会变得不直观：你看见的是一个方法调用，实际运行前后可能被多个装饰器包裹。

## 方法装饰器的最小模型

一个新式方法装饰器接收两个参数：

```ts
function loggedMethod(originalMethod, context) {
  // ...
}
```

- `originalMethod`：原始方法。
- `context`：装饰目标的信息，例如方法名。

如果装饰器返回一个新函数，这个新函数会替换原方法。

这就是装饰器的关键力量，也是它的风险：它能改变运行时行为。

## 可运行示例：给支付方法加日志和金额校验

示例目录：

```text
tracks/01-typescript-vue3/examples/L018-decorator-boundaries/
```

`src/index.ts`：

```ts
type Order = {
  id: string
  amount: number
}

function loggedMethod<This, Args extends unknown[], Return>(
  originalMethod: (this: This, ...args: Args) => Return,
  context: ClassMethodDecoratorContext<This, (this: This, ...args: Args) => Return>
) {
  const methodName = String(context.name)

  function replacementMethod(this: This, ...args: Args): Return {
    console.log(`[log] enter ${methodName}`)
    const result = originalMethod.call(this, ...args)
    console.log(`[log] exit ${methodName}`)
    return result
  }

  return replacementMethod
}

function minAmount(limit: number) {
  return function <This>(
    originalMethod: (this: This, order: Order) => string,
    context: ClassMethodDecoratorContext<This, (this: This, order: Order) => string>
  ) {
    const methodName = String(context.name)

    function replacementMethod(this: This, order: Order): string {
      if (order.amount < limit) {
        throw new Error(`${methodName} 要求订单金额至少为 ${limit}`)
      }

      return originalMethod.call(this, order)
    }

    return replacementMethod
  }
}

class PaymentService {
  constructor(private readonly channel: string) {}

  @loggedMethod
  @minAmount(1)
  pay(order: Order): string {
    return `${this.channel}:${order.id}:${order.amount}`
  }
}

const service = new PaymentService('wechat')
const result = service.pay({ id: 'order-1001', amount: 99 })

console.log(`支付结果：${result}`)

try {
  service.pay({ id: 'order-1002', amount: 0 })
} catch (error) {
  console.log(`支付失败：${error instanceof Error ? error.message : '未知错误'}`)
}

if (false) {
  // @ts-expect-error: pay 的订单金额必须是 number。
  service.pay({ id: 'order-1003', amount: '99' })
}
```

## 代码解析

```ts
function loggedMethod<This, Args extends unknown[], Return>(
  originalMethod: (this: This, ...args: Args) => Return,
  context: ClassMethodDecoratorContext<This, (this: This, ...args: Args) => Return>
) {
```

这是一个带类型参数的方法装饰器。

`This` 表示方法运行时的 `this` 类型。`Args` 表示方法参数元组。`Return` 表示方法返回值。

这样写比 `any` 更啰嗦，但能保留原方法的参数和返回值类型。装饰器最容易把类型写松，所以真实项目里要尽量保留原始签名。

```ts
const methodName = String(context.name)
```

`context.name` 是被装饰的方法名。这里转成字符串，方便日志输出。

```ts
function replacementMethod(this: This, ...args: Args): Return {
```

这是替换方法。它的 `this`、参数和返回值都保持和原方法一致。

```ts
const result = originalMethod.call(this, ...args)
```

这里调用原始方法。必须用 `.call(this, ...)`，否则原方法内部的 `this.channel` 可能丢失。

```ts
return replacementMethod
```

方法装饰器返回新函数时，新函数会替换原方法。因此 `@loggedMethod` 会让 `pay()` 在执行前后打印日志。

```ts
function minAmount(limit: number) {
  return function <This>(...) {
```

`minAmount` 是装饰器工厂。它先接收配置参数 `limit`，再返回真正的装饰器函数。

这和框架里的 `@Controller('/users')`、`@Get(':id')` 很像：外层函数接收配置，内层函数参与装饰。

```ts
if (order.amount < limit) {
  throw new Error(`${methodName} 要求订单金额至少为 ${limit}`)
}
```

这是运行时校验。装饰器可以把“所有支付方法都需要校验金额”的横切逻辑抽出来。

但也要注意：如果校验藏得太深，读业务方法时就不容易看出它会抛错。

```ts
@loggedMethod
@minAmount(1)
pay(order: Order): string {
```

这里给 `pay` 叠加了两个装饰器。

装饰器表达式从上到下求值，实际装饰时类似从下到上包裹。可以把它理解成：

```ts
pay = loggedMethod(minAmount(1)(pay))
```

所以金额校验先包住原方法，日志再包住校验后的方法。最终调用 `pay()` 时，会先进入日志，再执行金额校验和原方法。

```ts
try {
  service.pay({ id: 'order-1002', amount: 0 })
} catch (error) {
```

这里证明装饰器不是类型注解，而是真实参与运行时行为：金额小于 `1` 时，`minAmount` 装饰器会抛出错误。

最后的 `if (false)` 不会执行，但 TypeScript 会检查它。`amount` 必须是 `number`，不能是字符串。

## 运行与真实验证

在示例目录执行：

```bash
tsc --noEmit
tsc
node dist/index.js
sed -n '1,260p' dist/index.js
```

实际运行输出：

```text
[log] enter pay
[log] exit pay
支付结果：wechat:order-1001:99
[log] enter pay
支付失败：pay 要求订单金额至少为 1
```

你会注意到失败场景只打印了 `enter`，没有打印 `exit`。因为 `minAmount` 抛错后，`loggedMethod` 中 `originalMethod.call(...)` 后面的语句没有继续执行。这也说明装饰器会影响控制流，不能只把它当作漂亮语法。

## 常见误区

第一个误区：把装饰器当成类型系统功能。

装饰器会生成运行时代码，会执行函数，也可能替换方法。它不是 `interface`、`type` 那种编译期结构。

第二个误区：不知道新旧装饰器差异。

旧实验性装饰器通常配合 `experimentalDecorators`、`emitDecoratorMetadata` 和 `reflect-metadata`。TypeScript 5.0 新装饰器的类型和调用方式不同，不能盲目把旧教程代码搬过来。

第三个误区：在普通业务里层层叠装饰器。

```ts
@A
@B
@C
doSomething() {}
```

如果每个装饰器都有副作用，读者很难从方法体看出实际执行路径。装饰器应该优先用于稳定、统一、框架化的横切逻辑。

第四个误区：以为装饰器能替代清晰函数调用。

如果只有一个地方需要校验金额，直接在方法里写 `if` 可能更清楚。装饰器适合“很多方法都需要同一套机制”的场景。

## 一个小练习

在本节示例中新增一个装饰器工厂 `maxAmount(limit: number)`：

1. 它接收一个最大金额。
2. 当 `order.amount > limit` 时抛出错误。
3. 给 `pay` 同时加上 `@maxAmount(1000)`。
4. 运行一个 `amount: 2000` 的订单，观察错误输出。

练习重点：理解装饰器工厂和多个装饰器叠加后的执行顺序。

## 真实业务场景

装饰器适合这些地方：

- 后端控制器路由：`@Get('/users')`。
- 权限声明：`@RequireRole('admin')`。
- 事务边界：`@Transactional()`。
- 统一日志和追踪：`@Trace()`。
- ORM 字段声明：`@Column()`。

但在 Vue 3 前端业务代码中，大多数逻辑仍然更适合显式函数、Composable 和普通对象。装饰器可以读懂，可以在框架边界使用，但不应该成为日常业务逻辑的默认工具。

## 本节复盘

你可以用下面几个问题检查自己：

1. 装饰器是编译期类型，还是运行时函数调用？
2. 方法装饰器返回一个新函数时会发生什么？
3. 为什么装饰器里调用原方法要注意 `this`？
4. TypeScript 5.0 新装饰器和旧的 `experimentalDecorators` 有什么边界差异？
5. 为什么普通业务代码不建议大量堆叠装饰器？

下一节建议学习 L019：ESM 与 CommonJS 的差异。我们会从模块导入导出、运行时加载、Node.js 配置和 TypeScript 编译产物四个角度看模块系统。
