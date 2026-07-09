# L017 抽象类与接口的取舍

上一节我们学习了 `public`、`private`、`protected` 和 `readonly`。这一节继续讨论 class 体系里一个很容易混用的问题：什么时候用 `interface`，什么时候用 `abstract class`。

先给结论：

> 只需要描述“对象应该具备什么能力”时，用接口。既要描述契约，又要共享字段、构造逻辑或部分实现时，才考虑抽象类。

官方参考：

- https://www.typescriptlang.org/docs/handbook/2/classes.html
- https://www.typescriptlang.org/docs/handbook/2/everyday-types.html

## 为什么 Vue 2 前端开发者要学它

Vue 2 项目里，我们通常更习惯对象、函数和组件配置。进入 TypeScript、Vue 3、Node.js 后，会更频繁遇到这些设计问题：

- 请求客户端都要有 `get`、`post`，但实现可能不同。
- 日志器都要有 `info`、`error`，但有的写控制台，有的写文件。
- 导出器都要能 `export`，但 CSV、JSON、PDF 的渲染不同。
- 服务类有相同的校验、格式化、错误处理逻辑，但具体业务方法不同。

这些场景看起来都像“抽象”，但不一定都需要抽象类。接口和抽象类的边界清楚，代码就不容易过度继承。

## 接口：只描述能力契约

接口适合描述“这个对象能做什么”，不关心它怎么做。

```ts
interface Notifier {
  send(message: string): void
}
```

这个接口只说一件事：任何 `Notifier` 都必须有 `send(message)` 方法。

它不提供字段，不提供构造函数，也没有运行时代码。编译后接口会被擦除。

不同实现可以完全没有继承关系：

```ts
class ConsoleNotifier implements Notifier {
  send(message: string): void {
    console.log(message)
  }
}

class MemoryNotifier implements Notifier {
  private readonly messages: string[] = []

  send(message: string): void {
    this.messages.push(message)
  }
}
```

这就是接口最舒服的地方：它强调能力一致，而不是来源一致。

## 抽象类：共享实现 + 留出必须实现的部分

抽象类用 `abstract class` 声明。它不能直接 `new`，只能被子类继承。

```ts
abstract class BaseReportExporter {
  constructor(public readonly fileExtension: string) {}

  public export(report: BusinessReport): string {
    const fileName = this.createFileName(report.title)
    const content = this.render(report)

    return `${fileName}\n${content}`
  }

  protected abstract render(report: BusinessReport): string
}
```

这里有两类成员：

- 已实现成员：`fileExtension`、`export()`、`createFileName()`。
- 抽象成员：`render()`，只声明，不实现。

抽象类适合表达：

> 子类都属于同一类东西，并且确实共享一部分实现，但其中某些步骤必须由子类完成。

这和“只要有 `send` 方法就行”的接口不是一个层级。

## 可运行示例：报表导出器与通知器

示例目录：

```text
tracks/01-typescript-vue3/examples/L017-abstract-class-vs-interface/
```

`src/index.ts`：

```ts
type BusinessReport = {
  title: string
  rows: Array<Record<string, string | number>>
}

interface Notifier {
  send(message: string): void
}

class ConsoleNotifier implements Notifier {
  send(message: string): void {
    console.log(`通知：${message}`)
  }
}

class MemoryNotifier implements Notifier {
  private readonly messages: string[] = []

  send(message: string): void {
    this.messages.push(message)
  }

  all(): string[] {
    return [...this.messages]
  }
}

abstract class BaseReportExporter {
  constructor(public readonly fileExtension: string) {}

  public export(report: BusinessReport): string {
    const fileName = this.createFileName(report.title)
    const content = this.render(report)

    return `${fileName}\n${content}`
  }

  protected createFileName(title: string): string {
    const safeTitle = title.trim().toLowerCase().replaceAll(' ', '-')
    return `${safeTitle}.${this.fileExtension}`
  }

  protected abstract render(report: BusinessReport): string
}

class CsvReportExporter extends BaseReportExporter {
  constructor() {
    super('csv')
  }

  protected render(report: BusinessReport): string {
    const firstRow = report.rows[0]

    if (!firstRow) {
      return ''
    }

    const headers = Object.keys(firstRow)
    const lines = report.rows.map((row) => headers.map((header) => row[header]).join(','))

    return [headers.join(','), ...lines].join('\n')
  }
}

class TextReportExporter extends BaseReportExporter {
  constructor() {
    super('txt')
  }

  protected render(report: BusinessReport): string {
    return report.rows.map((row) => JSON.stringify(row)).join('\n')
  }
}

function publishReport(exporter: BaseReportExporter, notifier: Notifier, report: BusinessReport): string {
  const exported = exporter.export(report)
  notifier.send(`报表「${report.title}」已导出为 .${exporter.fileExtension}`)
  return exported
}

const report: BusinessReport = {
  title: 'User List',
  rows: [
    { id: 1, name: 'Alice', role: 'admin' },
    { id: 2, name: 'Bob', role: 'viewer' }
  ]
}

const memoryNotifier = new MemoryNotifier()
const csv = publishReport(new CsvReportExporter(), memoryNotifier, report)
const text = publishReport(new TextReportExporter(), new ConsoleNotifier(), report)

console.log(csv)
console.log(memoryNotifier.all().join('\n'))
console.log(text.split('\n')[0])

if (false) {
  // @ts-expect-error: 抽象类不能直接创建实例。
  const exporter = new BaseReportExporter('json')

  // @ts-expect-error: 子类必须实现抽象方法 render。
  class BrokenExporter extends BaseReportExporter {
    constructor() {
      super('broken')
    }
  }

  const badNotifier: Notifier = {
    // @ts-expect-error: Notifier 契约要求 send 方法，而不是 push 方法。
    push(message: string) {
      console.log(message)
    }
  }

  console.log(exporter, BrokenExporter, badNotifier)
}
```

## 代码解析

```ts
interface Notifier {
  send(message: string): void
}
```

`Notifier` 是一个接口。它只描述能力：能发送一条消息。

这个接口没有运行时代码，也不要求实现者必须继承某个父类。只要对象结构满足 `send(message: string): void`，就符合这个契约。

```ts
class ConsoleNotifier implements Notifier {
```

`implements Notifier` 表示这个类承诺实现 `Notifier` 接口。TypeScript 会检查 `ConsoleNotifier` 是否真的有 `send` 方法。

```ts
class MemoryNotifier implements Notifier {
  private readonly messages: string[] = []
```

`MemoryNotifier` 也实现同一个接口，但它把消息保存到内存数组里。它和 `ConsoleNotifier` 没有父子关系，只是拥有同一种能力。

这就是接口适合插件、适配器、策略对象的原因：不同实现可以来自完全不同的类。

```ts
abstract class BaseReportExporter {
```

`BaseReportExporter` 是抽象类，不能直接创建实例。它表示“报表导出器”这个共同基类。

```ts
constructor(public readonly fileExtension: string) {}
```

这里使用了参数属性写法。`public readonly fileExtension: string` 同时完成三件事：

1. 声明一个公开只读字段。
2. 声明构造函数参数。
3. 在构造函数中把参数赋给同名字段。

这是一种常见简写，适合简单依赖或身份字段。

```ts
public export(report: BusinessReport): string {
  const fileName = this.createFileName(report.title)
  const content = this.render(report)

  return `${fileName}\n${content}`
}
```

`export()` 是共享实现。无论 CSV 还是文本导出，都需要生成文件名，再渲染内容，再组合结果。

抽象类的价值就在这里：重复流程可以放在父类中。

```ts
protected createFileName(title: string): string {
```

`createFileName` 是受保护方法。它给父类和子类使用，但不作为外部公开 API。

```ts
protected abstract render(report: BusinessReport): string
```

`render` 是抽象方法，没有方法体。它强制子类提供自己的渲染实现。

`protected` 表示这个方法属于类体系内部；外部调用者不应该直接调用 `render`，而应该调用公开的 `export`。

```ts
class CsvReportExporter extends BaseReportExporter {
```

CSV 导出器继承抽象类，因此必须实现 `render`。

```ts
constructor() {
  super('csv')
}
```

子类构造函数必须先调用 `super(...)`，把文件扩展名交给父类初始化。

```ts
function publishReport(exporter: BaseReportExporter, notifier: Notifier, report: BusinessReport): string {
```

这个函数同时用到了抽象类和接口：

- `exporter: BaseReportExporter`：需要一个报表导出器实例，因为要复用父类定义的 `export()` 和 `fileExtension`。
- `notifier: Notifier`：只需要一个会 `send()` 的对象，不关心它是否来自某个父类。

这正好体现二者取舍：需要共享实现时用抽象类，只需要能力契约时用接口。

```ts
const memoryNotifier = new MemoryNotifier()
const csv = publishReport(new CsvReportExporter(), memoryNotifier, report)
```

`MemoryNotifier` 可以传给 `publishReport`，因为它实现了 `Notifier`。

```ts
const text = publishReport(new TextReportExporter(), new ConsoleNotifier(), report)
```

`TextReportExporter` 可以传给 `publishReport`，因为它继承了 `BaseReportExporter`。

最后的 `if (false)` 中有三个反例：

```ts
const exporter = new BaseReportExporter('json')
```

抽象类不能直接实例化。

```ts
class BrokenExporter extends BaseReportExporter {}
```

子类如果没有实现抽象方法 `render`，就不能作为具体类使用。

```ts
const badNotifier: Notifier = { push() {} }
```

对象没有 `send` 方法，不满足 `Notifier` 契约。

## 接口和抽象类的运行时差异

接口会在编译后完全消失。它只服务于类型检查。

抽象类会编译成 JavaScript class。`abstract` 限制发生在 TypeScript 编译期；编译产物里不会保留 `abstract` 关键字。

所以：

- 接口是纯类型契约。
- 抽象类既参与类型检查，也有运行时代码。

这也是为什么不能把抽象类当成“更高级的接口”随便使用。只要它变成 class，就会带来继承关系和运行时结构。

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
通知：报表「User List」已导出为 .txt
user-list.csv
id,name,role
1,Alice,admin
2,Bob,viewer
报表「User List」已导出为 .csv
user-list.txt
```

## 常见误区

第一个误区：能用接口就写抽象类。

如果你只想说“这个对象有 `send` 方法”，接口更轻。抽象类会引入继承关系和运行时代码，不应该为了“看起来更面向对象”而使用。

第二个误区：抽象类可以完全替代接口。

接口支持描述很多结构化能力，并且不要求实现者来自同一继承体系。比如第三方对象只要有 `send` 方法，也可以作为 `Notifier` 使用。

第三个误区：抽象方法可以有方法体。

`abstract render(...)` 只声明签名，不能写实现。如果父类想提供默认实现，就不要把它声明为 `abstract`。

第四个误区：抽象类在运行时仍然不能被实例化。

TypeScript 会阻止你在源码里 `new BaseReportExporter()`。但编译后的 JavaScript 中，抽象类只是普通 class。不要把 `abstract` 当作运行时安全边界。

## 一个小练习

在本节示例中新增一个通知渠道：

1. 定义 `class SilentNotifier implements Notifier`。
2. `send(message: string): void` 中什么都不做。
3. 用 `publishReport(new CsvReportExporter(), new SilentNotifier(), report)` 验证它能替换其他通知器。

再新增一个导出器：

1. 定义 `class JsonReportExporter extends BaseReportExporter`。
2. 构造函数中调用 `super('json')`。
3. 实现 `render(report: BusinessReport): string`，返回 `JSON.stringify(report.rows)`。

练习重点：通知器只要实现接口即可；导出器必须继承抽象类并实现抽象方法。

## 真实业务场景

在 Node.js 后端里，你可能会设计这样的结构：

```ts
interface CacheStore {
  get(key: string): Promise<string | undefined>
  set(key: string, value: string): Promise<void>
}
```

内存缓存、Redis 缓存、测试假缓存都可以实现这个接口。它们不需要共享父类。

另一个场景是服务基类：

```ts
abstract class BaseService {
  protected log(message: string): void {
    // 统一日志
  }

  protected abstract validate(): void
}
```

如果所有服务确实共享日志、错误格式化、事务包装等实现，抽象类才有意义。

## 本节复盘

你可以用下面几个问题检查自己：

1. 接口编译后会不会出现在 JavaScript 里？
2. 抽象类为什么不能直接实例化？
3. `implements Notifier` 和 `extends BaseReportExporter` 的语义有什么不同？
4. 什么时候接口比抽象类更合适？
5. 为什么不要把 `abstract` 当成运行时安全边界？

下一节建议学习 L018：装饰器的使用边界。我们会讲清楚装饰器是什么、为什么它经常出现在框架里，以及为什么普通业务代码不应该一上来就用装饰器。
