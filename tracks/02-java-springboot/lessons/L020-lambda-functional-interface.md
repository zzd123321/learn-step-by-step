# L020：Lambda 表达式和函数式接口：把行为传给方法

前面写过滤、统计、转换时，我们一直把逻辑写死在方法里。比如“筛选已支付订单”和“筛选高金额订单”很像，差别只在判断条件。

Lambda 表达式解决的就是这类问题：把一小段行为作为参数传给方法。它让 Java 代码开始接近你在 JavaScript 里常写的回调函数，但类型仍然由 Java 的接口系统约束。

## 示例：把订单筛选规则传给方法

示例文件：[LambdaFunctionalInterfaceDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L020-lambda-functional-interface/LambdaFunctionalInterfaceDemo.java)

```java
import java.util.ArrayList;
import java.util.List;

public class LambdaFunctionalInterfaceDemo {
    public static void main(String[] args) {
        List<OrderSummary> orders = loadOrders();

        List<OrderSummary> paidOrders = filterOrders(
                orders,
                order -> order.hasStatus("PAID")
        );

        List<OrderSummary> highValueOrders = filterOrders(
                orders,
                order -> order.amount() >= 100.0
        );
    }
}
```

`order -> order.hasStatus("PAID")` 就是 Lambda 表达式。它可以读成：给我一个 `order`，我返回这个订单是否为 `PAID`。

这和 JavaScript 的回调函数很像：

```js
orders.filter(order => order.status === "PAID")
```

不过 Java 不是把 Lambda 随便传给任何地方。Lambda 必须匹配一个函数式接口。

## 函数式接口：只有一个抽象方法的接口

示例里定义了一个订单过滤接口：

```java
@FunctionalInterface
interface OrderFilter {
    boolean matches(OrderSummary order);
}
```

函数式接口（functional interface）是只有一个抽象方法的接口。这里的唯一抽象方法是：

```java
boolean matches(OrderSummary order);
```

所以任何 `OrderFilter` 都必须表达一件事：接收一个 `OrderSummary`，返回一个 `boolean`。

`@FunctionalInterface` 不是必须的，但很建议写。它会让编译器帮你检查这个接口是否仍然满足“只有一个抽象方法”。如果以后你不小心加了第二个抽象方法，编译器会直接报错。

## Lambda 如何匹配接口

`filterOrders` 方法接收一个 `OrderFilter`：

```java
static List<OrderSummary> filterOrders(List<OrderSummary> orders, OrderFilter filter) {
    List<OrderSummary> result = new ArrayList<>();

    for (OrderSummary order : orders) {
        if (filter.matches(order)) {
            result.add(order);
        }
    }

    return result;
}
```

调用时传入：

```java
order -> order.hasStatus("PAID")
```

编译器会根据方法参数类型知道：这里需要的是 `OrderFilter`。于是它检查这个 Lambda 是否能匹配 `matches(OrderSummary order)`：

- 参数：一个 `OrderSummary`
- 返回值：`boolean`

`order.hasStatus("PAID")` 返回 `boolean`，所以匹配成功。

再看另一个：

```java
order -> order.amount() >= 100.0
```

它同样接收一个订单，返回布尔值，因此也能作为 `OrderFilter`。

## Lambda 不是“没有类型的函数”

这点和 JavaScript 很不一样。JavaScript 里函数值可以很自由：

```js
const filter = order => order.status === "PAID"
```

Java 的 Lambda 必须依附于一个目标类型，通常是函数式接口：

```java
OrderFilter filter = order -> order.hasStatus("PAID");
```

没有目标类型时，编译器不知道这个 Lambda 应该代表哪个接口，也就无法编译。

所以你可以把 Lambda 理解成“更短的接口实现写法”，而不是完全独立的函数对象。

## Lambda 让变化点变成参数

不用 Lambda 时，你可能会写两个方法：

```java
filterPaidOrders(orders);
filterHighValueOrders(orders);
```

这会让大量循环代码重复。用了 Lambda 后，循环逻辑放在 `filterOrders` 里，变化的判断规则由调用方传入：

```java
filterOrders(orders, order -> order.hasStatus("PAID"));
filterOrders(orders, order -> order.amount() >= 100.0);
```

这在后端 Service 里很常见：通用流程不变，规则经常变化。比如优惠规则、权限判断、字段转换、排序比较，都可以用类似思路表达。

## 另一个例子：优惠规则

示例里还定义了一个折扣规则：

```java
@FunctionalInterface
interface DiscountRule {
    double discountFor(OrderSummary order);
}
```

它接收订单，返回折扣金额。

计算方法不关心具体优惠规则：

```java
static double calculateDiscount(OrderSummary order, DiscountRule rule) {
    return rule.discountFor(order);
}
```

调用方可以传入不同规则：

```java
double vipDiscount = calculateDiscount(
        orders.get(1),
        order -> order.amount() * 0.15
);

double fixedDiscount = calculateDiscount(
        orders.get(2),
        order -> 10.0
);
```

第一段表示按订单金额打 15% 折扣，第二段表示固定优惠 10 元。方法没有变，变的是传入的行为。

## Lambda 写法规则

最常见写法是：

```java
parameter -> expression
```

例如：

```java
order -> order.amount() >= 100.0
```

如果有多个参数，需要加括号：

```java
(left, right) -> left.compareTo(right)
```

如果方法体有多行，需要用大括号，并显式 `return`：

```java
order -> {
    double discount = order.amount() * 0.15;
    return Math.min(discount, 30.0);
}
```

本节先掌握单参数和单表达式写法。后面学习排序、Stream API 时，多参数和多行 Lambda 会自然出现。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l020 tracks/02-java-springboot/examples/L020-lambda-functional-interface/LambdaFunctionalInterfaceDemo.java
java -cp /tmp/learn-step-java-l020 LambdaFunctionalInterfaceDemo
```

已真实执行，输出如下：

```text
Paid orders:
A1002 | status=PAID | amount=149.5
A1003 | status=PAID | amount=20.0
High value orders:
A1002 | status=PAID | amount=149.5
VIP discount for A1002: 22.425
Fixed discount for A1003: 10.0
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

不要把 Lambda 理解成完全没有类型的函数。Java 的 Lambda 必须匹配某个函数式接口。

不要给函数式接口添加多个抽象方法。加上 `@FunctionalInterface` 后，编译器会帮你守住这个边界。

不要在 Lambda 里写过多业务逻辑。逻辑一旦变长，优先抽成有名字的方法，否则可读性会变差。

不要急着把所有循环都改成 Lambda。本节只是为后续 Stream API 打基础；清晰的普通循环仍然完全可以用。

## 放到真实后端里看

在 Spring Boot 项目中，Lambda 常出现在这些场景：

- 根据不同条件过滤列表。
- 给通用方法传入一段校验、转换或计算规则。
- 配合集合排序、Stream API、Optional。
- 配合异步任务或回调接口。

比如一个订单查询接口可能允许不同筛选条件：只看已支付、只看大额、只看待发货。核心流程是“遍历订单并筛选”，变化点是“筛选规则”。Lambda 能让这个变化点更轻地传入方法。

## 练习

1. 新增一个筛选规则：筛选金额小于 `100.0` 的订单。
2. 新增一个 `OrderFilter createdOrderFilter = order -> order.hasStatus("CREATED");`，再传给 `filterOrders`。
3. 把 VIP 折扣改成多行 Lambda：折扣为 15%，但最多不超过 `20.0`。
4. 给 `OrderFilter` 增加第二个抽象方法，观察 `@FunctionalInterface` 的编译错误。

下一节会学习 Java 标准库里已经准备好的函数式接口，例如 `Predicate<T>`、`Function<T, R>`、`Consumer<T>`，减少自己定义接口的次数。
