# L022：Stream API 入门：把集合处理写成流水线

前面我们已经学过 `List`、Lambda 表达式和标准函数式接口。Stream API 把这些能力组合起来，让集合处理变成一条清晰的流水线：

```java
orders.stream()
        .filter(order -> order.hasStatus("PAID"))
        .map(order -> new OrderResponse(...))
        .toList();
```

这和前端里的数组链式调用非常接近：

```js
orders
  .filter(order => order.status === "PAID")
  .map(order => toResponse(order))
```

本节先只学习最常用的一条路径：`stream()`、`filter()`、`map()`、`toList()`。

## 示例：把订单列表转换成响应列表

示例文件：[StreamApiBasicsDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L022-stream-api-basics/StreamApiBasicsDemo.java)

```java
List<OrderResponse> paidResponses = orders.stream()
        .filter(order -> order.hasStatus("PAID"))
        .map(order -> new OrderResponse(
                order.id(),
                order.status(),
                "$" + order.amount()
        ))
        .toList();
```

这段代码可以按顺序读：

1. 从 `orders` 创建一个 Stream 流水线。
2. 只保留状态为 `PAID` 的订单。
3. 把 `OrderSummary` 转成 `OrderResponse`。
4. 把结果收集成新的 `List`。

它表达的是后端接口中非常常见的流程：Repository 查出实体或摘要对象，Service 过滤业务数据，再转换成 Controller 返回给前端的响应对象。

## `stream()`：创建流水线，不是复制列表

```java
orders.stream()
```

`stream()` 从集合创建一个 Stream。Stream 不是新的 `List`，也不是数据库游标；在本节场景里，你可以把它理解成“准备对这个集合做一串处理”。

原始列表还在：

```java
System.out.println("Original order count: " + orders.size());
```

示例中筛选已支付订单后，原始订单数量仍然是 `4`。Stream 的过滤和转换会产生新结果，不会自动删除原列表中的元素。

## `filter()`：筛选元素

`filter` 接收一个 `Predicate<T>`，也就是“给一个元素，返回是否保留”：

```java
.filter(order -> order.hasStatus("PAID"))
```

这行可以读成：只保留 `PAID` 订单。

如果没有任何元素满足条件，结果就是空列表，而不是 `null`：

```java
List<OrderResponse> refundedResponses = orders.stream()
        .filter(order -> order.hasStatus("REFUNDED"))
        .map(order -> new OrderResponse(order.id(), order.status(), "$" + order.amount()))
        .toList();
```

真实接口里这很重要。列表查询没有数据时，通常返回 `[]` 比返回 `null` 更清晰。

## `map()`：把元素转换成另一种形状

`map` 接收一个 `Function<T, R>`，也就是“给一个输入，返回一个输出”：

```java
.map(order -> new OrderResponse(
        order.id(),
        order.status(),
        "$" + order.amount()
))
```

这里输入是 `OrderSummary`，输出是 `OrderResponse`。

这正好对应后端开发里的 DTO 转换：数据库对象、业务对象、接口响应对象往往不是同一个类。`map` 适合表达“把每个元素转换成另一种结构”。

也可以把订单转成字符串 ID：

```java
List<String> highValueOrderIds = orders.stream()
        .filter(order -> order.amount() >= 100.0)
        .map(order -> order.id())
        .toList();
```

这条流水线的结果类型是 `List<String>`，因为 `map` 的 Lambda 返回的是 `String`。

## `toList()`：结束流水线并得到结果

`filter` 和 `map` 只是中间步骤。真正得到列表的是：

```java
.toList()
```

它会执行前面的流水线，并返回一个新的 `List`。

Stream API 里有一个重要概念：中间操作和终止操作。

- `filter`、`map`：中间操作，描述要做什么。
- `toList`、`sum`：终止操作，真正产出结果。

如果只写：

```java
orders.stream()
        .filter(order -> order.hasStatus("PAID"));
```

这段代码既没有保存结果，也没有终止操作，对业务没有实际产出。实际项目里，Stream 通常会以 `toList()`、`count()`、`sum()`、`forEach()` 等操作结束。

## `mapToDouble().sum()`：数字汇总的第一眼认识

本节示例还计算了已支付金额合计：

```java
double paidAmountTotal = orders.stream()
        .filter(order -> order.hasStatus("PAID"))
        .mapToDouble(order -> order.amount())
        .sum();
```

`mapToDouble` 把每个订单映射成 `double`，然后 `sum` 求和。

这和前端里很常见的写法类似：

```js
orders
  .filter(order => order.status === "PAID")
  .reduce((sum, order) => sum + order.amount, 0)
```

Java 为数字流提供了 `sum()` 这类便捷方法。后面会再系统学习统计、分组和聚合，本节只先建立直觉。

## Stream 和普通循环怎么选

普通循环版本大概是：

```java
List<OrderResponse> result = new ArrayList<>();

for (OrderSummary order : orders) {
    if (order.hasStatus("PAID")) {
        result.add(new OrderResponse(order.id(), order.status(), "$" + order.amount()));
    }
}
```

Stream 版本更像声明式表达：

```java
orders.stream()
        .filter(order -> order.hasStatus("PAID"))
        .map(order -> new OrderResponse(order.id(), order.status(), "$" + order.amount()))
        .toList();
```

如果逻辑是简单的过滤、转换、收集，Stream 通常更紧凑。如果中间包含很多分支、异常处理、日志和副作用，普通循环可能更清楚。不要为了使用 Stream 而牺牲可读性。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l022 tracks/02-java-springboot/examples/L022-stream-api-basics/StreamApiBasicsDemo.java
java -cp /tmp/learn-step-java-l022 StreamApiBasicsDemo
```

已真实执行，输出如下：

```text
Original order count: 4
Paid response count: 2
A1002 | status=PAID | amount=$149.5
A1003 | status=PAID | amount=$20.0
High value order ids: [A1002, A1004]
Paid amount total: 169.5
Refunded response count: 0
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

不要以为 `stream()` 会修改原集合。Stream 流水线通常产出新结果，原集合还在。

不要忘记终止操作。只有 `filter` 和 `map` 不会给你一个最终业务结果。

不要在 `map` 里写大量副作用。`map` 的语义是转换，如果你要打印、写日志、发送消息，应该更谨慎地选择位置和方法。

不要把所有循环都改成 Stream。Stream 擅长简单流水线；复杂流程仍然可以使用普通循环。

## 放到真实后端里看

Spring Boot 接口里经常出现这样的代码形状：

```java
List<OrderResponse> responses = orders.stream()
        .filter(order -> order.visibleTo(currentUser))
        .map(order -> OrderResponse.from(order))
        .toList();
```

这段代码的业务含义很清楚：过滤当前用户可见的订单，再转换成响应对象。

前端联调时，你看到的是一个数组响应；后端内部常常就是通过这种集合流水线把数据库结果整理成接口 DTO。

## 练习

1. 新增一条 `SHIPPED` 订单，并筛选所有非 `CANCELLED` 订单。
2. 用 Stream 得到所有订单状态列表：`List<String>`。
3. 用 `count()` 统计金额大于等于 `100.0` 的订单数量。
4. 把某条 Stream 改写成普通 `for` 循环，对比可读性。

下一节会继续 Stream API：学习 `sorted()`、`distinct()`、`limit()`，处理排序、去重和截取这些接口列表常见需求。
