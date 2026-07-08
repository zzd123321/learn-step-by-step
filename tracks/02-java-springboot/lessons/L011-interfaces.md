# L011：接口

抽象类适合表达“共享状态和共同流程”。接口（interface，接口）更适合表达“某个类具备一种能力”。

比如后端通知系统里，邮件、短信、站内信实现细节完全不同，但它们都有同一种能力：发送通知。业务层不需要知道具体渠道，只需要面向一个统一接口调用 `send(...)`。

本节用 `Notifier` 接口讲清楚接口的基础语法、`implements`、接口引用、多实现类，以及 `default` 方法。

## 示例：统一通知入口

示例文件：[NotifierInterfaceDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L011-interfaces/NotifierInterfaceDemo.java)

```java
public class NotifierInterfaceDemo {
    public static void main(String[] args) {
        Notifier[] notifiers = {
                new EmailNotifier("noreply@example.com"),
                new SmsNotifier("10690000"),
                new InboxNotifier()
        };

        NotificationMessage message = new NotificationMessage(
                "frontend-user",
                "Order paid",
                "Your order A1008 has been paid successfully."
        );

        for (int i = 0; i < notifiers.length; i++) {
            SendResult result = notifiers[i].send(message);
            System.out.println(result.summaryLine());
        }
    }
}
```

`notifiers` 的类型是 `Notifier[]`，但数组里放的是三个不同实现：

- `EmailNotifier`：邮件通知。
- `SmsNotifier`：短信通知。
- `InboxNotifier`：站内信通知。

调用方只看接口：

```java
notifiers[i].send(message)
```

至于具体怎么发送，由实现类自己决定。

## 定义接口

```java
interface Notifier {
    SendResult send(NotificationMessage message);

    default boolean supportsRetry() {
        return true;
    }
}
```

`interface` 定义接口。这里的 `Notifier` 表达一种能力：能发送通知。

`send(...)` 没有方法体，它定义了一个必须实现的动作：

```java
SendResult send(NotificationMessage message);
```

实现这个接口的类，必须提供 `send` 的具体代码。

接口里的方法默认是 `public abstract` 的，但初学阶段你可以先理解为：接口定义能力清单，实现类负责落地。

## 实现接口：`implements`

```java
class EmailNotifier implements Notifier {
    private final String fromAddress;

    EmailNotifier(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    @Override
    public SendResult send(NotificationMessage message) {
        return new SendResult(
                "EMAIL",
                message.receiver(),
                "from=" + fromAddress + ", title=" + message.title(),
                supportsRetry()
        );
    }
}
```

`implements Notifier` 表示 `EmailNotifier` 实现了 `Notifier` 接口。

这里 `send` 方法写成 `public`。这是一个容易踩的点：接口方法是公开契约，实现类不能降低访问级别。如果不写 `public`，编译器会报错。

`SmsNotifier` 和 `InboxNotifier` 也实现了同一个接口，所以它们都能放进 `Notifier[]`。

## 接口引用：调用方只依赖能力

```java
Notifier[] notifiers = {
        new EmailNotifier("noreply@example.com"),
        new SmsNotifier("10690000"),
        new InboxNotifier()
};
```

这段代码说明：变量类型可以是接口类型。只要对象实现了 `Notifier`，就能放进这个数组。

这种设计的好处是调用方稳定。以后新增 `PushNotifier`，主流程不需要改成一堆 `if` 判断，只要它也实现 `Notifier`，就能进入同一套发送流程。

## `default` 方法：接口里的默认实现

```java
default boolean supportsRetry() {
    return true;
}
```

接口可以提供 `default` 方法。它有方法体，实现类可以直接使用，也可以重写。

`EmailNotifier` 和 `SmsNotifier` 没有重写 `supportsRetry()`，所以默认支持重试。

`InboxNotifier` 重写了它：

```java
@Override
public boolean supportsRetry() {
    return false;
}
```

这表示站内信发送失败后不走这套重试机制。

## 接口和抽象类怎么选

抽象类适合“共享状态 + 共同流程”。例如上一节 `PaymentMethod` 有 `id`、`provider`，还有统一的 `pay` 流程。

接口适合“表达能力”。`Notifier` 不关心实现类有没有共同字段，也不关心它们是不是同一种父类。只要能 `send`，就是一个通知器。

粗略判断：

- “A 是一种 B”：可以考虑继承或抽象类。
- “A 具备某种能力”：优先考虑接口。

邮件通知、短信通知、站内信通知不一定应该共享同一个父类，但它们都具备“发送通知”的能力，所以接口很自然。

## JavaScript / TypeScript 对比

JavaScript 本身没有 Java 这种编译期接口。你通常通过“对象有这个方法就调用”来完成类似效果。

TypeScript 里的接口更接近 Java 接口：

```ts
interface Notifier {
  send(message: NotificationMessage): SendResult
}
```

区别是：TypeScript 主要做编译期结构检查，而 Java 的类需要显式写 `implements Notifier`，接口也会参与 Java 类型系统和编译检查。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l011 tracks/02-java-springboot/examples/L011-interfaces/NotifierInterfaceDemo.java
java -cp /tmp/learn-step-java-l011 NotifierInterfaceDemo
```

已真实执行，输出如下：

```text
EMAIL -> frontend-user | from=noreply@example.com, title=Order paid | retrySupported=true
SMS -> frontend-user | sender=10690000, content=Your order A1008 has been paid successfully. | retrySupported=true
INBOX -> frontend-user | title=Order paid | retrySupported=false
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

接口不能直接创建对象：

```java
new Notifier()
```

这会编译失败，因为接口只是能力契约，不是具体实现。

实现接口方法时不能降低访问级别。接口方法是公开契约，实现类方法要写 `public`。

一个类可以实现多个接口。这是接口和类继承的重要区别：Java 一个类只能 `extends` 一个父类，但可以 `implements` 多个接口。

不要把接口当成“空壳抽象类”。接口应该表达稳定能力，而不是随便把一堆不相关方法塞进去。

## 放到真实后端里看

Spring Boot 中接口非常常见：

- `UserRepository` 表达用户数据访问能力。
- `PaymentClient` 表达调用支付渠道的能力。
- `Notifier` 表达发送通知的能力。
- `PasswordEncoder` 表达密码加密与校验能力。

Service 层经常依赖接口，而不是直接依赖具体实现。这样测试时可以替换成假实现，线上也可以切换不同供应商。

比如：

```java
class OrderService {
    private final Notifier notifier;
}
```

`OrderService` 不关心 notifier 是邮件、短信还是站内信；它只关心“能发送通知”。这就是接口带来的解耦。

## 练习

1. 新增 `PushNotifier implements Notifier`，输出 `PUSH` 渠道。
2. 让 `SmsNotifier.supportsRetry()` 返回 `false`。
3. 删除 `EmailNotifier.send(...)`，观察编译错误。
4. 尝试写 `Notifier notifier = new Notifier();`，记录编译错误。

下一节会学习包装类型、`String` 常用方法与空值边界，为后续异常、集合和 DTO 字段处理做准备。
