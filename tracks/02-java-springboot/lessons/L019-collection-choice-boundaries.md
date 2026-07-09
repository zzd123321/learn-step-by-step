# L019：集合框架选择边界：`List`、`Set`、`Map`、`Queue` 怎么选

前面四节分别学了 `List`、`Set`、`Map`、`Queue`。这一节不引入新集合，而是把它们放进同一个真实接口场景里：前端传一组订单 ID，后端去重、查数据、按请求顺序组装响应，并把需要审计的订单排入任务队列。

这个场景的重点不是“会用更多 API”，而是建立一个工程判断：先问业务语义，再选集合类型。

## 示例：订单批量查询接口的集合选择

示例文件：[CollectionChoiceDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L019-collection-choice-boundaries/CollectionChoiceDemo.java)

```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class CollectionChoiceDemo {
    public static void main(String[] args) {
        List<String> requestedOrderIds = new ArrayList<>();
        requestedOrderIds.add("A1003");
        requestedOrderIds.add("A1001");
        requestedOrderIds.add("A1003");
        requestedOrderIds.add("A9999");
        requestedOrderIds.add("A1002");

        Set<String> uniqueOrderIds = uniqueIds(requestedOrderIds);
        List<OrderSummary> repositoryOrders = loadOrdersFromRepository();
        Map<String, OrderSummary> ordersById = indexById(repositoryOrders);

        List<OrderSummary> responseOrders = buildResponseInRequestOrder(uniqueOrderIds, ordersById);
        Queue<AuditTask> auditTasks = createAuditTasks(responseOrders);
        processAuditTasks(auditTasks);
    }
}
```

这段代码故意同时出现四种集合，但每一种都有清楚的职责：

- `List<String> requestedOrderIds`：保留前端原始请求顺序和重复项。
- `Set<String> uniqueOrderIds`：去掉重复订单 ID。
- `Map<String, OrderSummary> ordersById`：按订单 ID 快速查订单。
- `Queue<AuditTask> auditTasks`：把待审计任务按顺序排队处理。

## 选择集合时先问业务问题

不要先想“我最熟哪个集合”，而要先问这一组数据的业务语义。

如果问题是“我要返回一组有顺序的数据”，优先想到 `List`。

```java
List<OrderSummary> responseOrders = new ArrayList<>();
```

如果问题是“我要保证元素不重复”，优先想到 `Set`。

```java
Set<String> uniqueOrderIds = new LinkedHashSet<>(ids);
```

如果问题是“我要通过 key 快速找到 value”，优先想到 `Map`。

```java
Map<String, OrderSummary> ordersById = new LinkedHashMap<>();
```

如果问题是“我要按进入顺序逐个消费任务”，优先想到 `Queue`。

```java
Queue<AuditTask> tasks = new ArrayDeque<>();
```

这一层判断比记 API 更重要。API 可以查，语义选错了，后面代码会越来越别扭。

## `List`：保留顺序，也允许重复

前端传来的订单 ID 可能是这样的：

```java
List<String> requestedOrderIds = new ArrayList<>();
requestedOrderIds.add("A1003");
requestedOrderIds.add("A1001");
requestedOrderIds.add("A1003");
requestedOrderIds.add("A9999");
requestedOrderIds.add("A1002");
```

这里用 `List` 是合理的，因为它忠实保留了请求原貌：顺序存在，重复也存在。

真实接口里，Controller 收到的 JSON 数组天然接近 `List`：

```json
["A1003", "A1001", "A1003", "A9999", "A1002"]
```

这一步不要急着去重。先保留原始输入，后面需要去重时再明确转换。

## `Set`：表达唯一性，必要时保留插入顺序

去重代码很短：

```java
static Set<String> uniqueIds(List<String> ids) {
    return new LinkedHashSet<>(ids);
}
```

为什么用 `LinkedHashSet`，而不是 `HashSet`？

因为这个接口希望响应尽量贴近前端请求顺序。`LinkedHashSet` 去重后保留第一次出现的顺序：

```text
[A1003, A1001, A9999, A1002]
```

如果改成 `HashSet`，唯一性还在，但遍历顺序不应该被依赖。真实接口里，响应顺序一旦不稳定，前端列表、缓存、测试快照都可能变得难排查。

## `Map`：把列表变成按 ID 查找的索引

数据库查询通常返回列表：

```java
List<OrderSummary> repositoryOrders = loadOrdersFromRepository();
```

但如果你要按 ID 反复查找，直接在 `List` 里循环会让代码又慢又散。更好的方式是建索引：

```java
static Map<String, OrderSummary> indexById(List<OrderSummary> orders) {
    Map<String, OrderSummary> result = new LinkedHashMap<>();

    for (OrderSummary order : orders) {
        result.put(order.id(), order);
    }

    return result;
}
```

`Map` 的 key 是订单 ID，value 是订单摘要。之后查找就变成：

```java
OrderSummary order = ordersById.get(id);
```

这和前端数据归一化非常像：把数组转成 `{ [id]: entity }`，再按 id 读取。

## 组装响应：`Set` 控制请求顺序，`Map` 负责查找，`List` 承载输出

响应构造方法把三种集合一起用上：

```java
static List<OrderSummary> buildResponseInRequestOrder(
        Set<String> uniqueOrderIds,
        Map<String, OrderSummary> ordersById
) {
    List<OrderSummary> result = new ArrayList<>();

    for (String id : uniqueOrderIds) {
        OrderSummary order = ordersById.get(id);

        if (order == null) {
            System.out.println("Missing order id: " + id);
            continue;
        }

        result.add(order);
    }

    return result;
}
```

这里的分工很清楚：

- `uniqueOrderIds` 决定遍历哪些 ID，以及顺序是什么。
- `ordersById` 负责按 ID 找订单。
- `result` 是最终要返回给前端的有序列表。

查不到 `A9999` 时，示例打印缺失信息并跳过。真实接口里可以根据产品规则决定：整体返回错误、部分成功、或在响应中标记缺失 ID。

## `Queue`：把后续任务排队，而不是塞进响应逻辑

有些订单需要审计：

```java
boolean needsAudit() {
    return "CANCELLED".equals(status) || amount >= 100.0;
}
```

创建审计任务时使用 `Queue`：

```java
static Queue<AuditTask> createAuditTasks(List<OrderSummary> orders) {
    Queue<AuditTask> tasks = new ArrayDeque<>();

    for (OrderSummary order : orders) {
        if (order.needsAudit()) {
            tasks.offer(new AuditTask("AUDIT-" + order.id(), order.auditReason()));
        }
    }

    return tasks;
}
```

为什么这里不是 `List<AuditTask>`？因为后续逻辑不是“展示任务列表”，而是“一个个消费任务”：

```java
AuditTask task = tasks.poll();
```

`Queue` 让代码读起来更接近业务动作：任务入队，任务出队，处理到队列为空。

## JavaScript 对比

前端里类似流程可能这样写：

```js
const requestedOrderIds = ["A1003", "A1001", "A1003", "A9999", "A1002"]
const uniqueOrderIds = [...new Set(requestedOrderIds)]
const ordersById = Object.fromEntries(orders.map(order => [order.id, order]))
const responseOrders = uniqueOrderIds
  .map(id => ordersById[id])
  .filter(Boolean)
```

Java 的集合选择和这个思路高度对应：

- JS 数组：Java `List`
- JS `Set`：Java `Set`
- JS 对象字典或 `Map`：Java `Map`
- JS `push` + `shift` 模拟队列：Java `Queue`

不同点在于，Java 会把元素类型写进泛型里。比如 `Map<String, OrderSummary>` 会明确告诉编译器：key 是字符串，value 是订单摘要。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l019 tracks/02-java-springboot/examples/L019-collection-choice-boundaries/CollectionChoiceDemo.java
java -cp /tmp/learn-step-java-l019 CollectionChoiceDemo
```

已真实执行，输出如下：

```text
Raw request ids: [A1003, A1001, A1003, A9999, A1002]
Unique request ids: [A1003, A1001, A9999, A1002]
Missing order id: A9999
Response order list:
A1003 | status=CANCELLED | amount=20.0
A1001 | status=CREATED | amount=99.9
A1002 | status=PAID | amount=149.5
Audit queue size: 2
Processing audit: AUDIT-A1003 | reason=CANCELLED_ORDER
Processing audit: AUDIT-A1002 | reason=HIGH_AMOUNT
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

不要用 `List` 硬做所有事情。它能做很多事，但表达不了唯一性、按 key 查找、任务消费这些更明确的语义。

不要为了去重随手用 `HashSet`，然后又期待顺序稳定。如果顺序重要，使用 `LinkedHashSet` 或在输出前明确排序。

不要在 `List` 里反复循环查找 ID。数据量小时看不出问题，但接口聚合多了以后，`Map` 索引会让代码更清楚。

不要把 `Queue` 当成完整的异步系统。本节的 `ArrayDeque` 只是内存结构，不负责持久化、重试、分布式消费。

## 放到真实后端里看

一个 Spring Boot 批量查询接口可能分成这些步骤：

1. Controller 接收 `List<String> orderIds`。
2. Service 用 `Set` 去掉重复 ID。
3. Repository 根据 ID 集合查询数据库，得到 `List<OrderSummary>`。
4. Service 建 `Map<String, OrderSummary>`，方便按 ID 匹配。
5. Service 组装 `List<OrderSummaryResponse>` 返回给前端。
6. 对需要后续处理的数据，生成任务并放入 `Queue` 或真正的消息队列。

你会发现，集合类型不是“语法题”，而是后端分层和数据流设计的一部分。

## 练习

1. 把 `LinkedHashSet` 改成 `HashSet`，观察响应顺序是否仍值得依赖。
2. 新增一个重复订单到 `loadOrdersFromRepository()`，观察 `indexById` 最终保留哪一个。
3. 修改缺失 ID 策略：不要跳过，而是把缺失 ID 收集到 `List<String> missingIds`。
4. 把 `needsAudit()` 的规则改成只有 `amount >= 100.0` 才审计，观察任务队列数量。

下一节会进入 Lambda 表达式和函数式接口：把“行为”作为参数传给方法，为后续 Stream API 打基础。
