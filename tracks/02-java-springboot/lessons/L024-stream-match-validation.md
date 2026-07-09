# L024：Stream API：匹配判断 `anyMatch`、`allMatch`、`noneMatch`

很多后端接口在真正执行业务前，要先做一组校验：

- 是否存在不允许的状态？
- 是否所有数据都满足基本条件？
- 是否完全没有某类风险数据？

Stream API 提供了三个非常适合表达这类问题的方法：

- `anyMatch()`：是否至少有一个元素满足条件。
- `allMatch()`：是否所有元素都满足条件。
- `noneMatch()`：是否没有任何元素满足条件。

它们的返回值都是 `boolean`，非常适合接口参数校验、提交前检查、权限判断和业务规则判断。

## 示例：订单提交前校验

示例文件：[StreamMatchValidationDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L024-stream-match-validation/StreamMatchValidationDemo.java)

```java
boolean hasCancelledOrder = orders.stream()
        .anyMatch(order -> order.hasStatus("CANCELLED"));

boolean allAmountsValid = orders.stream()
        .allMatch(order -> order.amount() > 0.0);

boolean noPendingOrder = orders.stream()
        .noneMatch(order -> order.hasStatus("PENDING"));
```

这三行代码分别表达：

- 是否存在已取消订单。
- 是否所有订单金额都大于 `0`。
- 是否没有待确认订单。

它们比手写循环更接近业务语言：有任意一个、全部都、一个都没有。

## `anyMatch()`：至少一个满足条件

```java
orders.stream()
        .anyMatch(order -> order.hasStatus("CANCELLED"));
```

`anyMatch` 接收一个 `Predicate<T>`，只要有一个元素满足条件，就返回 `true`。

它适合表达“是否存在某种情况”：

- 是否存在取消订单？
- 是否存在无权限资源？
- 是否存在非法参数？
- 是否存在库存不足的商品？

JavaScript 中类似写法是：

```js
orders.some(order => order.status === "CANCELLED")
```

在后端接口里，`anyMatch` 很适合用来提前拦截风险数据。

## `allMatch()`：所有元素都满足条件

```java
orders.stream()
        .allMatch(order -> order.amount() > 0.0);
```

`allMatch` 表示每个元素都必须满足条件，结果才是 `true`。

它适合表达“整体是否合格”：

- 所有订单金额是否有效？
- 所有请求 ID 是否非空？
- 所有商品数量是否大于 `0`？
- 所有字段是否通过基础校验？

JavaScript 中类似写法是：

```js
orders.every(order => order.amount > 0)
```

注意，`allMatch` 对空 Stream 会返回 `true`。这是一个数学上的“空集上全称命题为真”的规则。真实接口里，如果空列表本身不允许，需要额外判断：

```java
if (orders.isEmpty()) {
    return false;
}
```

不要只靠 `allMatch` 判断“列表非空且全部有效”。

## `noneMatch()`：没有任何元素满足条件

```java
orders.stream()
        .noneMatch(order -> order.hasStatus("PENDING"));
```

`noneMatch` 表示没有元素满足条件时返回 `true`。

它适合表达“完全不允许出现某种情况”：

- 没有待确认订单。
- 没有黑名单用户。
- 没有重复提交标记。
- 没有禁用状态的数据。

你可以把它理解成：

```java
!orders.stream().anyMatch(...)
```

但 `noneMatch` 可读性更好，尤其是业务语言本身就是“不能有”“没有任何”时。

## 用 `allMatch` 检查重复 ID

示例里还检查了订单 ID 是否重复：

```java
static boolean hasNoDuplicateIds(List<OrderSummary> orders) {
    Set<String> seenIds = new HashSet<>();

    return orders.stream()
            .allMatch(order -> seenIds.add(order.id()));
}
```

`Set.add` 在添加新元素时返回 `true`，如果元素已经存在则返回 `false`。

所以这段代码的含义是：每个订单 ID 都必须能成功加入 `seenIds`，才说明没有重复 ID。

这段写法很短，但它在 Lambda 里修改了外部 `Set`。本节把它作为“去重校验”的例子，但真实项目中要注意：如果逻辑变复杂，普通循环可能更直观。Stream 不是为了消灭所有循环。

## 短路特性：找到结果就可以停止

`anyMatch`、`allMatch`、`noneMatch` 都有短路特性。

短路的意思是：一旦结果已经确定，就不需要继续遍历。

- `anyMatch` 找到第一个满足条件的元素，就可以返回 `true`。
- `allMatch` 找到第一个不满足条件的元素，就可以返回 `false`。
- `noneMatch` 找到第一个满足条件的元素，就可以返回 `false`。

这和 JavaScript 的 `some`、`every` 很像。对后端校验来说，短路通常也是符合直觉的：发现一个非法数据，就可以判断这批数据不合格。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l024 tracks/02-java-springboot/examples/L024-stream-match-validation/StreamMatchValidationDemo.java
java -cp /tmp/learn-step-java-l024 StreamMatchValidationDemo
```

已真实执行，输出如下：

```text
Valid orders:
Has cancelled order: false
All amounts valid: true
No pending order: true
No duplicate ids: true
Can submit: true
Invalid orders:
Has cancelled order: true
All amounts valid: false
No pending order: false
No duplicate ids: false
Can submit: false
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

不要用 `filter(...).toList().isEmpty()` 代替所有匹配判断。能用 `anyMatch`、`allMatch`、`noneMatch` 时，语义更直接，也有短路机会。

不要忘记空列表边界。`allMatch` 和 `noneMatch` 对空 Stream 都会返回 `true`，如果业务要求列表必须非空，要单独判断。

不要在复杂副作用里滥用 Stream。本节的重复 ID 校验修改了外部 `Set`，可以理解，但如果校验过程需要记录多种错误、累积详细上下文，普通循环往往更清楚。

不要把三个方法的方向记混：`any` 是至少一个，`all` 是全部，`none` 是一个都没有。

## 放到真实后端里看

在 Spring Boot 接口里，这些方法很适合做提交前校验：

```java
boolean hasInvalidItem = request.items().stream()
        .anyMatch(item -> item.quantity() <= 0);

boolean allIdsPresent = request.items().stream()
        .allMatch(item -> item.productId() != null);

boolean noDisabledProduct = products.stream()
        .noneMatch(product -> product.disabled());
```

这些判断通常会进入 Service 层或参数校验逻辑。如果校验失败，后端应该返回清晰的错误码和错误信息，方便前端联调时定位是哪类数据不合法。

## 练习

1. 新增一个空订单列表，分别观察 `anyMatch`、`allMatch`、`noneMatch` 的结果。
2. 新增 `hasHighValueOrder`，判断是否存在金额大于等于 `200.0` 的订单。
3. 新增 `allStatusKnown`，判断所有状态是否属于 `CREATED`、`PAID`、`SHIPPED`、`CANCELLED`。
4. 把重复 ID 校验改写成普通 `for` 循环，对比可读性。

下一节会学习 `Optional`：用更明确的方式表达“可能有值，也可能没有值”，减少随手返回 `null` 带来的空指针风险。
