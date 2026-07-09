# L021：标准函数式接口：`Predicate`、`Function`、`Consumer`

上一节我们自己定义了 `OrderFilter` 和 `DiscountRule`，用它们接收 Lambda。真实 Java 项目里，很多常见形状的函数式接口已经由标准库提供，放在 `java.util.function` 包里。

本节先学习三个最常用的：

- `Predicate<T>`：接收一个 `T`，返回 `boolean`，适合判断和过滤。
- `Function<T, R>`：接收一个 `T`，返回一个 `R`，适合转换。
- `Consumer<T>`：接收一个 `T`，不返回结果，适合消费和执行动作。

这三个接口会在后续 Stream API 中高频出现。

## 示例：过滤、转换和打印订单

示例文件：[StandardFunctionalInterfacesDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L021-standard-functional-interfaces/StandardFunctionalInterfacesDemo.java)

```java
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class StandardFunctionalInterfacesDemo {
    public static void main(String[] args) {
        List<OrderSummary> orders = loadOrders();

        Predicate<OrderSummary> paidOrder = order -> order.hasStatus("PAID");
        Function<OrderSummary, OrderResponse> toResponse = order -> new OrderResponse(
                order.id(),
                order.status(),
                "$" + order.amount()
        );
        Consumer<OrderResponse> printResponse = response -> System.out.println(response.line());
    }
}
```

这里没有再定义自有接口，而是直接使用标准库接口。它们的命名非常直白：判断用 `Predicate`，转换用 `Function`，消费用 `Consumer`。

## `Predicate<T>`：判断一个对象是否符合条件

`Predicate<T>` 的核心方法是：

```java
boolean test(T value);
```

示例中的判断规则是：

```java
Predicate<OrderSummary> paidOrder = order -> order.hasStatus("PAID");
Predicate<OrderSummary> highValueOrder = order -> order.amount() >= 100.0;
```

可以读成：

- `paidOrder`：给我一个订单，我判断它是否已支付。
- `highValueOrder`：给我一个订单，我判断它金额是否不小于 `100.0`。

过滤方法接收 `Predicate<OrderSummary>`：

```java
static List<OrderSummary> filterOrders(
        List<OrderSummary> orders,
        Predicate<OrderSummary> predicate
) {
    List<OrderSummary> result = new ArrayList<>();

    for (OrderSummary order : orders) {
        if (predicate.test(order)) {
            result.add(order);
        }
    }

    return result;
}
```

注意调用方法名是 `test`，不是上一节自定义接口里的 `matches`。

`Predicate` 还提供了组合方法：

```java
paidOrder.and(highValueOrder)
```

这表示“既是已支付订单，又是高金额订单”。它和 JavaScript 里这样写类似：

```js
order => order.status === "PAID" && order.amount >= 100
```

标准接口的好处是，很多常用组合能力已经写好了。

## `Function<T, R>`：把一种对象转换成另一种对象

`Function<T, R>` 的核心方法是：

```java
R apply(T value);
```

`T` 是输入类型，`R` 是返回类型。示例里：

```java
Function<OrderSummary, OrderResponse> toResponse = order -> new OrderResponse(
        order.id(),
        order.status(),
        "$" + order.amount()
);
```

可以读成：接收一个 `OrderSummary`，返回一个 `OrderResponse`。

转换方法写成了泛型方法：

```java
static <R> List<R> mapOrders(
        List<OrderSummary> orders,
        Function<OrderSummary, R> mapper
) {
    List<R> result = new ArrayList<>();

    for (OrderSummary order : orders) {
        result.add(mapper.apply(order));
    }

    return result;
}
```

`<R>` 表示返回列表的元素类型由调用方决定。传入 `Function<OrderSummary, OrderResponse>`，返回 `List<OrderResponse>`；传入 `Function<OrderSummary, String>`，返回 `List<String>`。

这和前端的 `map` 很像：

```js
orders.map(order => ({
  id: order.id,
  status: order.status
}))
```

Java 里的区别是，输入类型和输出类型都写在 `Function<T, R>` 里。

## `Consumer<T>`：消费一个对象，不返回结果

`Consumer<T>` 的核心方法是：

```java
void accept(T value);
```

示例中的消费动作是打印响应行：

```java
Consumer<OrderResponse> printResponse = response -> System.out.println(response.line());
```

它接收一个 `OrderResponse`，执行打印，没有返回值。

消费方法写成了通用泛型：

```java
static <T> void consumeAll(List<T> items, Consumer<T> consumer) {
    for (T item : items) {
        consumer.accept(item);
    }
}
```

这和 JavaScript 的 `forEach` 很像：

```js
responses.forEach(response => console.log(response.line))
```

`Consumer` 适合表达“对每个元素做一件事”，比如打印日志、发送通知、写审计记录。它不适合表达转换，因为它没有返回值。

## 为什么标准接口更常用

上一节自定义 `OrderFilter` 是为了理解 Lambda 如何匹配函数式接口。但真实项目里，如果接口形状很通用，优先使用标准接口。

例如：

- 判断条件：`Predicate<T>`
- 对象转换：`Function<T, R>`
- 消费动作：`Consumer<T>`
- 没有参数但返回值：`Supplier<T>`，后面会遇到

只有当标准接口无法清楚表达业务含义，或者你需要在接口上放领域语义时，才考虑自定义函数式接口。

例如 `DiscountRule` 这种名字在业务上很明确，保留自定义接口也可以。但普通的“过滤订单”，通常用 `Predicate<OrderSummary>` 就够了。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l021 tracks/02-java-springboot/examples/L021-standard-functional-interfaces/StandardFunctionalInterfacesDemo.java
java -cp /tmp/learn-step-java-l021 StandardFunctionalInterfacesDemo
```

已真实执行，输出如下：

```text
Paid order count: 2
Paid high value order count: 1
Response lines:
A1002 | status=PAID | amount=$149.5
A1003 | status=PAID | amount=$20.0
Display text:
A1001 => CREATED
A1002 => PAID
A1003 => PAID
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

不要把 `Predicate<T>` 和 `Function<T, Boolean>` 混用。虽然二者都能表达“返回布尔值”，但判断语义优先用 `Predicate<T>`，它还有 `and`、`or`、`negate` 等组合方法。

不要用 `Consumer<T>` 做转换。`Consumer` 没有返回值，如果你需要把订单转成响应对象，应该用 `Function<T, R>`。

不要因为标准接口存在，就完全不自定义接口。标准接口适合通用形状；当业务名字本身能让代码更清楚时，自定义接口仍然有价值。

不要在 `Consumer` 里隐藏复杂副作用。打印日志可以，真正涉及数据库写入、远程调用、事务边界时，要让方法名和代码结构足够清楚。

## 放到真实后端里看

在 Spring Boot 项目中，你会经常看到类似模式：

```java
Predicate<OrderSummary> visibleOrder = order -> order.hasStatus("PAID");
Function<OrderSummary, OrderResponse> toResponse = order -> new OrderResponse(...);
Consumer<OrderResponse> logResponse = response -> log.info(...);
```

后续学习 Stream API 时，它们会直接出现在链式调用里：

```java
orders.stream()
        .filter(visibleOrder)
        .map(toResponse)
        .forEach(logResponse);
```

本节先用普通循环实现 `filter`、`map` 和 `forEach`，是为了让你看清楚：Stream 不是魔法，只是把这些模式封装成更顺滑的 API。

## 练习

1. 新增 `Predicate<OrderSummary> createdOrder`，筛选 `CREATED` 订单。
2. 使用 `paidOrder.negate()`，筛选非已支付订单。
3. 新增 `Function<OrderSummary, String>`，输出 `"A1002:PAID"` 这种格式。
4. 新增 `Consumer<OrderSummary>`，打印所有原始订单的 `id` 和 `amount`。

下一节会正式学习 Stream API 的入门写法：`stream()`、`filter()`、`map()`、`toList()`，把本节这些函数式接口放进集合流水线里。
