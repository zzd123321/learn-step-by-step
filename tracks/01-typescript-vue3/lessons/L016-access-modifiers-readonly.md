# L016 访问修饰符与只读字段

上一节我们学习了类、构造函数和实例类型。这一节继续看 class 里的成员可见性：`public`、`private`、`protected` 和 `readonly`。

这节的核心不是“怎么把字段藏起来”，而是理解它们在 TypeScript 中真正解决的问题：

> 访问修饰符用来表达类的使用边界；`readonly` 用来表达字段初始化后不应再被重新赋值。

官方参考：

- https://www.typescriptlang.org/docs/handbook/2/classes.html

## 为什么 Vue 2 前端开发者要学它

Vue 2 页面代码里，很多状态是直接暴露在组件实例上的：

```js
this.loading = true
this.userList = []
this.fetchUsers()
```

这种写法很直观，但当你开始封装 SDK、请求客户端、日志模块、Node.js 服务类时，所有字段都随便访问就会变得危险。

例如一个日志类：

- 外部应该能调用 `info()`、`warn()`。
- 外部不应该直接改内部日志数组。
- 子类可以复用格式化逻辑。
- 服务名创建后不应该被改成另一个服务。

这些边界就适合用 `public`、`private`、`protected` 和 `readonly` 表达。

## 四个关键词的心智模型

### `public`：公开 API

`public` 表示成员可以在任何地方访问。TypeScript class 成员默认就是 `public`，所以多数时候不用显式写。

```ts
class Logger {
  public info(message: string): void {}
}
```

你可以把 `public` 理解成“这个成员是类对外承诺的一部分”。

### `private`：只给当前类内部使用

`private` 表示成员只能在声明它的类内部访问。

```ts
class Logger {
  private entries: string[] = []

  public flush(): string[] {
    return this.entries
  }
}
```

外部不能访问 `logger.entries`，子类也不能访问。它适合放内部缓存、临时状态、封装细节。

### `protected`：给当前类和子类使用

`protected` 比 `private` 放宽一点：当前类内部和子类内部可以访问，外部不能访问。

```ts
class Logger {
  protected format(message: string): string {
    return `[log] ${message}`
  }
}

class HttpLogger extends Logger {
  public preview(message: string): string {
    return this.format(message)
  }
}
```

它适合“外部不应该调用，但子类扩展时需要复用”的能力。

### `readonly`：初始化后不能重新赋值

`readonly` 表示字段只能在声明处或构造函数中赋值，之后不能重新赋值。

```ts
class Logger {
  public readonly serviceName: string

  constructor(serviceName: string) {
    this.serviceName = serviceName
  }
}
```

`readonly` 很适合 ID、创建时间、服务名、配置名这类“创建后就不应该变”的字段。

注意：`readonly` 限制的是属性重新赋值，不等于对象深度不可变。如果 `readonly items: string[]`，你不能把 `items` 换成另一个数组，但仍可能对数组本身 `push`。如果需要深度不可变，要另行设计类型和运行时约束。

## 可运行示例：日志类的边界

示例目录：

```text
tracks/01-typescript-vue3/examples/L016-access-modifiers-readonly/
```

`src/index.ts`：

```ts
type LogLevel = 'info' | 'warn' | 'error'

type LogEntry = {
  level: LogLevel
  message: string
  time: string
}

class AuditLogger {
  public readonly serviceName: string
  private readonly entries: LogEntry[] = []

  constructor(serviceName: string) {
    if (serviceName.trim() === '') {
      throw new Error('服务名不能为空')
    }

    this.serviceName = serviceName
  }

  public info(message: string): void {
    this.write('info', message)
  }

  public warn(message: string): void {
    this.write('warn', message)
  }

  protected format(entry: LogEntry): string {
    return `[${entry.time}] [${entry.level.toUpperCase()}] ${this.serviceName}: ${entry.message}`
  }

  private write(level: LogLevel, message: string): void {
    this.entries.push({
      level,
      message,
      time: new Date('2026-07-09T10:00:00.000Z').toISOString()
    })
  }

  public flush(): string[] {
    return this.entries.map((entry) => this.format(entry))
  }
}

class HttpAuditLogger extends AuditLogger {
  public request(method: string, url: string): void {
    this.info(`${method.toUpperCase()} ${url}`)
  }

  public preview(entry: LogEntry): string {
    return this.format(entry)
  }
}

const logger = new HttpAuditLogger('user-service')
logger.request('get', '/api/users')
logger.warn('响应时间超过阈值')

console.log(`服务名：${logger.serviceName}`)
console.log(logger.flush().join('\n'))
console.log(
  logger.preview({
    level: 'info',
    message: '预览日志',
    time: '2026-07-09T10:05:00.000Z'
  })
)

if (false) {
  // @ts-expect-error: readonly 字段不能在构造函数之外重新赋值。
  logger.serviceName = 'order-service'

  // @ts-expect-error: private 字段只能在 AuditLogger 类内部访问。
  console.log(logger.entries)

  // @ts-expect-error: protected 方法只能在类内部或子类内部访问。
  console.log(logger.format({ level: 'info', message: '外部访问', time: 'now' }))
}
```

## 代码解析

```ts
class AuditLogger {
  public readonly serviceName: string
  private readonly entries: LogEntry[] = []
```

`serviceName` 是公开只读字段。外部可以读取它，但不能重新赋值。服务名属于对象身份的一部分，创建后改变它会让日志来源变得不可信。

`entries` 是私有只读字段。外部不能读写它，子类也不能直接访问它。`readonly` 表示这个属性不能被替换成另一个数组，但类内部仍然可以对这个数组执行 `push`。

这正好展示了 `readonly` 的边界：它保护“属性引用不被重新赋值”，不是保护数组内容永远不变。

```ts
constructor(serviceName: string) {
  if (serviceName.trim() === '') {
    throw new Error('服务名不能为空')
  }

  this.serviceName = serviceName
}
```

`readonly serviceName` 可以在构造函数里赋值。TypeScript 允许这种初始化，因为构造函数就是对象建立身份的地方。

空字符串仍然是字符串，所以仍然需要运行时校验。这和上一节的订单 ID 一样：类型检查不能替代业务规则。

```ts
public info(message: string): void {
  this.write('info', message)
}
```

`info` 是公开方法，外部可以调用。它没有直接暴露 `entries`，而是通过私有方法 `write` 写入日志。

这就是封装的意义：外部使用稳定 API，不直接接触内部数据结构。

```ts
protected format(entry: LogEntry): string {
```

`format` 是受保护方法。外部不能调用，但子类可以复用。

这个设计表达了一个边界：格式化逻辑属于类体系内部的扩展点，不属于普通使用者的公开 API。

```ts
private write(level: LogLevel, message: string): void {
```

`write` 是私有方法，只有 `AuditLogger` 自己能调用。即使是 `HttpAuditLogger` 子类，也不能直接调用 `this.write(...)`。

如果以后内部日志结构从数组换成队列，外部代码不需要改，因为它从来没有依赖 `write` 和 `entries`。

```ts
public flush(): string[] {
  return this.entries.map((entry) => this.format(entry))
}
```

`flush` 是公开方法，返回格式化后的日志字符串。它把内部 `entries` 转换成外部可用结果，而不是把内部数组直接交出去。

```ts
class HttpAuditLogger extends AuditLogger {
```

`HttpAuditLogger` 继承 `AuditLogger`，扩展 HTTP 请求日志能力。

```ts
public request(method: string, url: string): void {
  this.info(`${method.toUpperCase()} ${url}`)
}
```

子类可以调用父类的公开方法 `info`。

```ts
public preview(entry: LogEntry): string {
  return this.format(entry)
}
```

子类也可以调用父类的 `protected format`。但是普通外部代码不能调用 `logger.format(...)`。

最后的 `if (false)` 不会执行，但 TypeScript 仍会检查里面的代码。三个 `@ts-expect-error` 分别验证：

- 不能重写 `readonly` 字段。
- 不能从外部访问 `private` 字段。
- 不能从外部访问 `protected` 方法。

## TypeScript private 和 JavaScript #private 的区别

这是本节最重要的边界。

TypeScript 的 `private` 和 `protected` 主要在类型检查阶段生效。编译成 JavaScript 后，如果你查看 `dist/index.js`，会看到普通属性名仍然存在。

这意味着它们适合表达“代码协作中的访问边界”，但不适合当作安全隔离手段。

JavaScript 还有一种 `#private` 私有字段：

```ts
class TokenStore {
  #token = ''
}
```

`#token` 是运行时私有字段，不能通过普通属性访问绕过去。它的语义更硬，但也更严格，写库或安全敏感代码时才更常见。

作为当前学习阶段，你先记住：

- `private` / `protected`：TypeScript 编译期边界。
- `#private`：JavaScript 运行时私有字段。

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
服务名：user-service
[2026-07-09T10:00:00.000Z] [INFO] user-service: GET /api/users
[2026-07-09T10:00:00.000Z] [WARN] user-service: 响应时间超过阈值
[2026-07-09T10:05:00.000Z] [INFO] user-service: 预览日志
```

## 常见误区

第一个误区：把 `public` 当作必须写的关键字。

TypeScript class 成员默认就是 `public`。显式写 `public` 通常只是为了教学、团队风格或突出 API 边界。

第二个误区：以为 `private` 是运行时安全机制。

TypeScript 官方文档明确提醒，`private` 和 `protected` 这类限制主要发生在类型检查阶段。编译后它们不会自动变成 JavaScript 的强私有字段。

第三个误区：以为 `readonly` 等于深度不可变。

```ts
private readonly entries: LogEntry[] = []
```

这表示 `entries` 这个属性不能被重新赋值，但数组内容仍可变化。要表达深度不可变，需要额外设计，例如使用 `ReadonlyArray<T>`，并避免暴露可变引用。

第四个误区：把所有方法都设成 `private`。

`private` 太多会让类难以扩展和测试。真正需要隐藏的是内部细节；稳定、明确的行为应该作为 `public` API 暴露出来。

## 一个小练习

在本节示例中新增一个 `PaymentClient` 类：

1. `public readonly merchantId: string`，构造函数初始化并校验不能为空。
2. `private readonly requests: string[] = []`，记录请求路径。
3. `public pay(orderId: string, amount: number): void`，调用私有方法记录请求。
4. `private record(path: string): void`，向 `requests` 追加路径。
5. `public getRequestCount(): number`，返回请求数量。
6. 用 `@ts-expect-error` 验证外部不能修改 `merchantId`，也不能访问 `requests`。

练习重点：不是写复杂支付逻辑，而是把公开 API、内部状态和只读身份字段分清楚。

## 真实业务场景

在 Node.js 后端里，你可能会封装一个用户服务：

- `public findUser(id)`：给路由层调用。
- `private validateUserId(id)`：内部校验规则。
- `protected formatUser(user)`：子类或测试服务可以复用的格式化逻辑。
- `readonly repositoryName`：服务创建后固定的依赖身份。

在 Vue 3 前端里，你也可能封装上传队列、日志客户端、WebSocket 客户端。访问修饰符能让“哪些东西给外部用、哪些只是内部细节”更清楚。

## 本节复盘

你可以用下面几个问题检查自己：

1. TypeScript class 成员默认是 `public` 还是 `private`？
2. `private` 和 `protected` 的区别是什么？
3. 为什么 `readonly entries: LogEntry[]` 仍然可以 `push`？
4. TypeScript 的 `private` 和 JavaScript 的 `#private` 有什么运行时差异？
5. 什么时候应该用 `protected` 而不是 `private`？

下一节建议学习 L017：抽象类与接口的取舍。我们会比较 `abstract class` 和 `interface`：什么时候需要共享实现，什么时候只需要描述契约。
