# L017：`Map`：用 key 快速找到 value

`List` 适合表示一组有顺序的数据，`Set` 适合表示一组不重复的数据。`Map` 则用来表示 key-value（键值对）关系：每个 key 对应一个 value。

在后端业务里，`Map` 很常见。比如按订单 ID 找订单、按用户 ID 找用户、按状态统计数量、把数据库查询结果整理成前端更容易使用的字典结构。

## 示例：按订单 ID 查找订单

示例文件：[MapKeyValueDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L017-map-key-value/MapKeyValueDemo.java)

```java
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapKeyValueDemo {
    public static void main(String[] args) {
        List<OrderSummary> orders = new ArrayList<>();
        orders.add(new OrderSummary("A1001", "PAID", 99.90));
        orders.add(new OrderSummary("A1002", "CREATED", 149.50));
        orders.add(new OrderSummary("A1003", "PAID", 20.00));

        Map<String, OrderSummary> ordersById = indexById(orders);

        System.out.println("Order ids: " + ordersById.keySet());
        printOrder(ordersById, "A1002");
        printOrder(ordersById, "A9999");
    }
}
```

这里的 `List<OrderSummary>` 模拟数据库查出来的订单列表，`Map<String, OrderSummary>` 则表示“用订单 ID 作为 key，订单摘要作为 value”。

## `Map<K, V>` 的两个类型参数

`Map` 有两个泛型参数：

```java
Map<String, OrderSummary> ordersById
```

可以读成：这是一个 `Map`，key 是 `String`，value 是 `OrderSummary`。

这和前端里常见的对象字典很接近：

```js
const ordersById = {
  A1001: { id: "A1001", status: "PAID" },
  A1002: { id: "A1002", status: "CREATED" }
}
```

Java 的 `Map<String, OrderSummary>` 比 JavaScript 普通对象更明确：key 类型和 value 类型都写在编译期，放错类型会直接编译失败。

## `put`：写入或替换 value

把列表转成按 ID 索引的 `Map`：

```java
static Map<String, OrderSummary> indexById(List<OrderSummary> orders) {
    Map<String, OrderSummary> result = new LinkedHashMap<>();

    for (OrderSummary order : orders) {
        result.put(order.id(), order);
    }

    return result;
}
```

`put(key, value)` 的规则是：

- 如果 key 不存在，就新增一组键值对。
- 如果 key 已存在，就用新的 value 替换旧的 value。
- `put` 会返回旧 value；如果之前没有这个 key，则返回 `null`。

示例里这段代码会替换 `A1002`：

```java
OrderSummary previous = ordersById.put(
        "A1002",
        new OrderSummary("A1002", "CANCELLED", 149.50)
);
```

`previous != null` 表示这次 `put` 不是新增，而是替换了已有订单。

这个行为非常重要。`Map` 的 key 是唯一的，同一个 key 不能同时对应两个 value。

## `get`：按 key 读取 value

```java
OrderSummary order = ordersById.get(id);
```

`get` 根据 key 查找 value。如果找到，返回对应对象；如果没找到，返回 `null`。

所以示例中的 `printOrder` 必须处理空值：

```java
static void printOrder(Map<String, OrderSummary> ordersById, String id) {
    OrderSummary order = ordersById.get(id);

    if (order == null) {
        System.out.println("Order " + id + ": <not found>");
        return;
    }

    System.out.println("Order " + id + ": " + order.summaryLine());
}
```

这和后端接口很像：前端请求 `/orders/A9999`，后端按 ID 查不到订单时，不能继续读取订单字段，而应该返回明确的 404 或业务错误。

## `getOrDefault`：给缺失 key 一个默认值

统计订单状态数量时，可以这样写：

```java
static Map<String, Integer> countByStatus(List<OrderSummary> orders) {
    Map<String, Integer> counts = new LinkedHashMap<>();

    for (OrderSummary order : orders) {
        int currentCount = counts.getOrDefault(order.status(), 0);
        counts.put(order.status(), currentCount + 1);
    }

    return counts;
}
```

`counts.getOrDefault(order.status(), 0)` 的意思是：

- 如果这个状态已经出现过，取已有数量。
- 如果这个状态还没出现过，就先当作 `0`。

这和前端统计很像：

```js
counts[status] = (counts[status] ?? 0) + 1
```

在 Java 里，`Map` 没有“读取不存在属性得到 `undefined`”这个概念；`get` 找不到 key 会返回 `null`，而 `getOrDefault` 可以让缺失 key 的处理更清楚。

## `keySet`、`values` 和 `entrySet`

`Map` 不是 `List`，不能用 `get(0)` 按下标读取。它更常见的遍历方式有三种：

```java
ordersById.keySet();   // 所有 key
ordersById.values();   // 所有 value
ordersById.entrySet(); // 所有 key-value 条目
```

如果只关心所有订单 ID，用 `keySet()`。

如果只关心所有订单对象，用 `values()`。

如果同时需要 key 和 value，用 `entrySet()`：

```java
for (Map.Entry<String, OrderSummary> entry : ordersById.entrySet()) {
    String id = entry.getKey();
    OrderSummary order = entry.getValue();
}
```

真实项目里，`entrySet()` 常用于把一个字典转换成响应列表，或者对每个 key-value 做统一处理。

## `HashMap` 与 `LinkedHashMap`

最常见的 `Map` 实现是 `HashMap`：

```java
Map<String, OrderSummary> ordersById = new HashMap<>();
```

它适合快速按 key 查找，但不保证遍历顺序。

本节示例使用 `LinkedHashMap`：

```java
Map<String, OrderSummary> result = new LinkedHashMap<>();
```

原因是 `LinkedHashMap` 会保留插入顺序，打印结果更稳定，也更适合教学示例。真实接口如果需要稳定顺序，应该明确排序或选择能保证顺序的结构。

## JavaScript 对比

JavaScript 里可以用普通对象或 `Map` 表达 key-value：

```js
const ordersById = new Map()
ordersById.set("A1001", { id: "A1001", status: "PAID" })
ordersById.get("A1001")
ordersById.has("A9999")
```

Java 中对应的是：

```java
Map<String, OrderSummary> ordersById = new HashMap<>();
ordersById.put("A1001", order);
ordersById.get("A1001");
ordersById.containsKey("A9999");
```

对前端开发者来说，可以把 Java 的 `Map<String, OrderSummary>` 理解为更强类型的对象字典：key 的类型明确，value 的结构也明确。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l017 tracks/02-java-springboot/examples/L017-map-key-value/MapKeyValueDemo.java
java -cp /tmp/learn-step-java-l017 MapKeyValueDemo
```

已真实执行，输出如下：

```text
Order ids: [A1001, A1002, A1003]
Order A1002: A1002 | status=CREATED | amount=149.5
Order A9999: <not found>
Status counts: {PAID=2, CREATED=1}
Was A1002 replaced: true
A1002 after replace: A1002 | status=CANCELLED | amount=149.5
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

不要把 `Map` 当数组用。`Map` 没有“第 0 个元素”的核心语义，它关注的是 key。

不要忘记处理 `get` 返回 `null` 的情况。真实接口按 ID 查询不到数据时，要给出明确响应，而不是继续访问字段导致空指针异常。

不要以为 `put` 永远是新增。如果 key 已经存在，`put` 会替换旧 value。

不要依赖 `HashMap` 的遍历顺序。如果响应顺序重要，需要使用明确排序或保证顺序的实现。

## 放到真实后端里看

假设前端页面需要订单列表，同时还要把订单按 ID 快速定位。后端 Service 里可能先拿到：

```java
List<OrderSummary> orders
```

然后整理成：

```java
Map<String, OrderSummary> ordersById
```

这类结构在接口聚合时很常见。比如你先查订单列表，再批量查支付信息，最后用订单 ID 把支付信息挂回订单上。`Map` 可以避免每次都在列表里循环查找。

如果前端做过数据归一化，你会很熟悉这种结构：

```js
{
  ids: ["A1001", "A1002"],
  entities: {
    A1001: { id: "A1001" },
    A1002: { id: "A1002" }
  }
}
```

Java 后端里的 `Map` 就是这种“按 key 建索引”的基础工具。

## 练习

1. 新增一个 `findOrderStatus(Map<String, OrderSummary> ordersById, String id)` 方法，查不到时返回 `"UNKNOWN"`。
2. 把 `ordersById.put("A1002", ...)` 改成新增 `A2001`，观察 `previous` 是否为 `null`。
3. 新增一个重复 ID 的订单放进原始 `List`，观察 `indexById` 最终保留哪一个。
4. 使用 `entrySet()` 打印所有订单 ID 和订单状态。

下一节会继续集合框架：学习队列 `Queue`，理解先进先出、任务排队，以及它和后端异步任务的关系。
