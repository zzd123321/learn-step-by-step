# L015：集合框架入门：从数组到 `List`

数组适合保存固定长度的一组数据，但真实后端接口里，列表长度经常是动态的：数据库查到几条订单就返回几条，过滤条件不同，结果数量也不同。

Java 集合框架（Collections Framework，集合框架）提供了更适合业务开发的数据结构。本节先只学习最常用的 `List`：有顺序、可重复、长度可动态变化的列表。

## 示例：订单摘要列表

示例文件：[ListCollectionDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L015-list-collection-basics/ListCollectionDemo.java)

```java
import java.util.ArrayList;
import java.util.List;

public class ListCollectionDemo {
    public static void main(String[] args) {
        List<OrderSummary> orders = new ArrayList<>();

        orders.add(new OrderSummary("A1001", "PAID", 99.90));
        orders.add(new OrderSummary("A1002", "CREATED", 149.50));
        orders.add(new OrderSummary("A1003", "PAID", 20.00));

        System.out.println("Total orders: " + orders.size());
        System.out.println("First order: " + orders.get(0).summaryLine());

        System.out.println("All orders:");
        printOrders(orders);

        List<OrderSummary> paidOrders = filterByStatus(orders, "PAID");
        System.out.println("Paid orders:");
        printOrders(paidOrders);

        List<OrderSummary> emptyResult = filterByStatus(orders, "CANCELLED");
        System.out.println("Cancelled order count: " + emptyResult.size());
    }
}
```

这段代码模拟一个订单列表接口的后端处理过程：先准备订单列表，再打印全部订单，最后过滤出已支付订单。

## `List` 是接口，`ArrayList` 是实现

```java
List<OrderSummary> orders = new ArrayList<>();
```

这行代码里有两个概念：

- `List<OrderSummary>`：变量类型，表示“订单摘要列表”。
- `new ArrayList<>()`：实际对象，表示使用 `ArrayList` 这种列表实现。

这和前面接口章节的思路一致：调用方尽量面向接口 `List` 编程，而不是把变量类型写死成 `ArrayList`。

`OrderSummary` 是泛型参数，表示这个列表里只能放 `OrderSummary`。如果你尝试放字符串，编译器会报错。

## 添加、长度和下标读取

添加元素：

```java
orders.add(new OrderSummary("A1001", "PAID", 99.90));
```

读取长度：

```java
orders.size()
```

注意，数组长度是 `array.length`，而 `List` 长度是方法调用 `list.size()`。

按下标读取：

```java
orders.get(0)
```

`List` 和数组一样，下标从 `0` 开始。`orders.get(0)` 是第一个元素。如果列表为空还去 `get(0)`，会抛出下标越界异常。

## 增强 `for`：更适合遍历集合

```java
for (OrderSummary order : orders) {
    System.out.println(order.summaryLine());
}
```

这叫增强 `for` 循环，也常叫 for-each。它适合“从头到尾遍历每个元素”的场景，不需要你手写下标。

和普通 `for` 相比：

```java
for (int i = 0; i < orders.size(); i++) {
    OrderSummary order = orders.get(i);
}
```

增强 `for` 更短，也更少下标错误。后端处理列表响应时非常常见。

## 空列表比 `null` 更好处理

```java
static void printOrders(List<OrderSummary> orders) {
    if (orders.isEmpty()) {
        System.out.println("<empty>");
        return;
    }

    for (OrderSummary order : orders) {
        System.out.println(order.summaryLine());
    }
}
```

`orders.isEmpty()` 判断列表是否为空。

真实接口里，如果没有查到订单，更推荐返回空列表：

```json
[]
```

而不是返回 `null`。空列表表示“查询成功，只是没有数据”；`null` 往往会让前端和后端都多一层空值判断。

## 过滤列表：返回新的 `List`

```java
static List<OrderSummary> filterByStatus(List<OrderSummary> orders, String status) {
    List<OrderSummary> result = new ArrayList<>();

    for (OrderSummary order : orders) {
        if (order.hasStatus(status)) {
            result.add(order);
        }
    }

    return result;
}
```

这个方法接收一个订单列表，返回一个新的列表。原列表不变，过滤结果放在 `result` 里。

这是一种很重要的后端习惯：尽量让方法的输入和输出清楚，避免在不必要的时候修改传入集合。

## JavaScript 对比

JavaScript 数组本身就很像动态列表：

```js
const orders = []
orders.push({ id: "A1001", status: "PAID" })
orders.length
orders[0]
```

Java 把“固定长度数组”和“动态集合”分得更清楚：

- `OrderSummary[]`：数组，长度固定。
- `List<OrderSummary>`：列表，长度可动态变化。

JavaScript 过滤常写：

```js
orders.filter(order => order.status === "PAID")
```

Java 后面学到 Stream API 后，也会有类似写法。本节先用普通循环打基础。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l015 tracks/02-java-springboot/examples/L015-list-collection-basics/ListCollectionDemo.java
java -cp /tmp/learn-step-java-l015 ListCollectionDemo
```

已真实执行，输出如下：

```text
Total orders: 3
First order: A1001 | status=PAID | amount=99.9
All orders:
A1001 | status=PAID | amount=99.9
A1002 | status=CREATED | amount=149.5
A1003 | status=PAID | amount=20.0
Paid orders:
A1001 | status=PAID | amount=99.9
A1003 | status=PAID | amount=20.0
Cancelled order count: 0
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

不要把数组和 `List` 的长度写法混淆：

```java
array.length
list.size()
```

不要在不确定列表是否有元素时直接 `get(0)`。先判断 `isEmpty()`。

不要使用原始类型：

```java
List orders = new ArrayList();
```

这样会丢掉元素类型信息。应该写：

```java
List<OrderSummary> orders = new ArrayList<>();
```

不要让“没有数据”返回 `null` 列表。空列表通常更清晰。

## 放到真实后端里看

Spring Boot 接口经常返回列表：

```java
ApiResponse<List<OrderSummaryResponse>>
```

这里同时用到了上一节泛型和本节 `List`：

- `ApiResponse<T>` 表示统一响应。
- `List<OrderSummaryResponse>` 表示响应数据是一个订单摘要列表。

真实项目里，Repository 从数据库查出多条记录，Service 做过滤或转换，Controller 返回列表响应。本节的 `filterByStatus` 就是这种流程的最小模型。

## 练习

1. 新增一个 `CANCELLED` 订单，观察取消订单数量。
2. 新增 `sumAmount(List<OrderSummary> orders)`，计算订单总金额。
3. 在空列表上调用 `get(0)`，记录异常。
4. 把 `List<OrderSummary>` 改成原始类型 `List`，观察编译警告。

下一节会继续集合框架：学习 `Set`，理解去重、唯一性和业务边界。
