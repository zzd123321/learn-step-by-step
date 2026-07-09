# L023：Stream API：排序、去重和截取

上一节用 `filter()`、`map()`、`toList()` 完成了最基础的集合流水线。本节继续学习三个接口列表里非常常见的操作：

- `sorted()`：排序。
- `distinct()`：去重。
- `limit()`：截取前 N 个。

它们常出现在商品列表、订单列表、搜索结果和排行榜这类接口里。

## 示例：订单金额 Top 2、状态去重、取前两个已支付订单

示例文件：[StreamSortDistinctLimitDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L023-stream-sort-distinct-limit/StreamSortDistinctLimitDemo.java)

```java
List<OrderSummary> topTwoByAmount = orders.stream()
        .sorted((left, right) -> Double.compare(right.amount(), left.amount()))
        .limit(2)
        .toList();
```

这段代码可以读成：

1. 从订单列表创建 Stream。
2. 按金额从大到小排序。
3. 只取前 2 条。
4. 收集成新的 `List`。

这就是后端常见的“取金额最高的 N 条订单”。

## `sorted()`：排序 Stream，不直接改原列表

`sorted()` 可以不带参数，也可以带比较规则。对订单对象排序时，通常要告诉 Java 按哪个字段排：

```java
.sorted((left, right) -> Double.compare(right.amount(), left.amount()))
```

这里的 Lambda 有两个参数：

- `left`：参与比较的第一个订单。
- `right`：参与比较的第二个订单。

`Double.compare(right.amount(), left.amount())` 表示金额倒序。金额大的订单会排在前面。

这和 JavaScript 的数组排序比较像：

```js
orders.sort((left, right) => right.amount - left.amount)
```

但有一个重要差异：JavaScript 的 `Array.prototype.sort()` 会修改原数组；本节这种 Stream 写法不会直接修改原始 `orders` 列表，而是产生一个新的排序结果。

示例最后打印了原始订单列表，你会看到顺序仍然是加载时的顺序。

## `limit()`：只取前 N 个

```java
.limit(2)
```

`limit(2)` 表示只保留流水线里的前 2 个元素。

`limit` 对顺序非常敏感。下面两段含义不同：

```java
orders.stream()
        .sorted(...)
        .limit(2)
```

这是先排序，再取前 2 条，适合做 Top N。

```java
orders.stream()
        .limit(2)
        .sorted(...)
```

这是先取原列表前 2 条，再只对这 2 条排序。真实项目里，如果要做排行榜、最高金额、最新记录，通常应该先明确排序，再 `limit`。

## `distinct()`：按相等规则去重

示例里先把订单映射成状态字符串，再去重：

```java
List<String> distinctStatuses = orders.stream()
        .map(order -> order.status())
        .distinct()
        .toList();
```

结果是：

```text
[CREATED, PAID, CANCELLED]
```

因为 `PAID` 出现了多次，但 `distinct()` 只保留第一次出现的那个。

对 `String` 这种标准类型，Java 已经定义好了相等规则，所以可以直接去重。

如果你对自定义对象调用 `distinct()`，它会依赖对象的 `equals()` 和 `hashCode()`。这和前面 `Set` 的去重规则一致。也就是说，如果你希望两个 `OrderSummary` 按订单 ID 去重，就需要在类里正确定义相等规则，或者先 `map` 成订单 ID 这类标准值再去重。

## 组合顺序很重要

取前两个已支付订单的代码是：

```java
List<String> firstTwoPaidOrderIds = orders.stream()
        .filter(order -> order.hasStatus("PAID"))
        .map(order -> order.id())
        .limit(2)
        .toList();
```

这条流水线先筛选 `PAID`，再取前 2 个 ID。所以结果是“原列表顺序里前两个已支付订单”。

如果写成：

```java
orders.stream()
        .limit(2)
        .filter(order -> order.hasStatus("PAID"))
```

含义就变成“先取原列表前 2 条，再从这 2 条里筛选已支付订单”。结果可能完全不同。

Stream 的链式写法很短，但每一步顺序都是真实业务逻辑，不能随便调换。

## JavaScript 对比

前端里可能这样写：

```js
const topTwoByAmount = [...orders]
  .sort((left, right) => right.amount - left.amount)
  .slice(0, 2)

const distinctStatuses = [...new Set(orders.map(order => order.status))]
```

Java 对应写法是：

```java
List<OrderSummary> topTwoByAmount = orders.stream()
        .sorted((left, right) -> Double.compare(right.amount(), left.amount()))
        .limit(2)
        .toList();

List<String> distinctStatuses = orders.stream()
        .map(order -> order.status())
        .distinct()
        .toList();
```

你可以把 Stream 看成 Java 对集合链式处理的官方方式。它比手写循环更贴近“我要什么结果”，但需要你清楚每一步的输入和输出。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l023 tracks/02-java-springboot/examples/L023-stream-sort-distinct-limit/StreamSortDistinctLimitDemo.java
java -cp /tmp/learn-step-java-l023 StreamSortDistinctLimitDemo
```

已真实执行，输出如下：

```text
Top 2 orders by amount:
A1004 | status=CANCELLED | amount=300.0
A1002 | status=PAID | amount=149.5
Distinct statuses: [CREATED, PAID, CANCELLED]
First 2 paid order ids: [A1002, A1003]
Original order list:
A1001 | status=CREATED | amount=99.9
A1002 | status=PAID | amount=149.5
A1003 | status=PAID | amount=20.0
A1004 | status=CANCELLED | amount=300.0
A1005 | status=PAID | amount=88.0
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

不要以为 `sorted()` 会修改原列表。Stream 的排序结果在新的流水线结果里，原列表顺序仍然保留。

不要把 `limit()` 放错位置。先排序再取前 N，和先取前 N 再排序，不是同一件事。

不要对自定义对象随便 `distinct()`。如果对象没有合理的 `equals()` 和 `hashCode()`，去重结果可能不是你想要的业务去重。

不要在排序比较器里写含糊规则。真实接口如果有分页或缓存，排序规则最好稳定，例如金额相同再按创建时间或 ID 排序。稳定排序规则会让前端列表表现更可预测。

## 放到真实后端里看

真实接口里，这些操作很常见：

- 商品列表按价格、销量、创建时间排序。
- 搜索结果只取前 10 条推荐。
- 分类、品牌、订单状态去重后返回给前端做筛选项。
- 管理后台取金额最高的几笔订单做风控看板。

在 Spring Boot 项目中，这些操作有时会放在数据库 SQL 里完成，有时会在 Service 层对内存列表处理。一般来说，数据量大、需要分页时优先让数据库排序和截取；已经拿到的小集合、接口聚合后的列表，可以用 Stream 做二次整理。

## 练习

1. 把金额倒序改成金额升序。
2. 先筛选 `PAID` 订单，再按金额倒序取前 1 条。
3. 用 Stream 得到去重后的订单金额列表：`List<Double>`。
4. 新增一个同金额订单，尝试给排序增加第二规则：金额相同按订单 ID 升序。

下一节会继续 Stream API：学习 `anyMatch()`、`allMatch()`、`noneMatch()`，处理“是否存在”“是否全部满足”这类接口校验场景。
