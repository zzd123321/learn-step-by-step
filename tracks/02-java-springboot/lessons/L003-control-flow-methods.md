# L003：控制流与方法

控制流决定“代码按什么路径执行”，方法决定“把一段逻辑命名并复用”。这两个概念放在后端里非常常见：接口拿到一个状态字段后，要判断它属于哪种业务含义，再返回前端需要展示的文本、按钮或下一步动作。

本节用订单状态做例子。你可以把它想成前端从接口拿到：

```json
{
  "status": "PAID"
}
```

然后页面要展示“已付款，等待发货”。在真实项目中，这种状态映射既可能在前端做，也可能由后端整理好再返回。我们先用 Java 写一个最小版本。

## 示例：把订单状态映射成展示信息

示例文件：[OrderStatusFlow.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L003-control-flow-methods/OrderStatusFlow.java)

```java
import java.util.Locale;

public class OrderStatusFlow {
    public static void main(String[] args) {
        String[] statuses = args.length > 0
                ? args
                : new String[] {"created", "PAID", "shipped", "cancelled", "unknown"};

        for (int i = 0; i < statuses.length; i++) {
            String status = normalizeStatus(statuses[i]);
            String message = describeStatus(status);
            boolean supportVisible = shouldShowSupport(status);

            System.out.println(formatLine(i + 1, status, message, supportVisible));
        }
    }

    static String normalizeStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return "UNKNOWN";
        }

        return rawStatus.trim().toUpperCase(Locale.ROOT);
    }

    static String describeStatus(String status) {
        switch (status) {
            case "CREATED":
                return "Order created, waiting for payment";
            case "PAID":
                return "Payment received, waiting for shipment";
            case "SHIPPED":
                return "Package shipped, waiting for delivery";
            case "CANCELLED":
                return "Order cancelled";
            default:
                return "Unknown status, confirm backend mapping";
        }
    }

    static boolean shouldShowSupport(String status) {
        if ("CANCELLED".equals(status)) {
            return true;
        }

        switch (status) {
            case "CREATED":
            case "PAID":
            case "SHIPPED":
                return false;
            default:
                return true;
        }
    }

    static String formatLine(int index, String status, String message, boolean supportVisible) {
        return "#" + index
                + " [" + status + "] "
                + message
                + " | supportVisible=" + supportVisible;
    }
}
```

这段代码同时展示了四个基础能力：

- `for` 循环：逐个处理多个订单状态。
- `if` 判断：处理空值、未知状态和是否展示客服入口。
- `switch` 分支：把固定状态值映射成展示文案。
- 方法：把“标准化状态”“描述状态”“判断客服入口”“格式化输出”拆成清晰的小函数。

## `main`：程序入口也是流程入口

```java
String[] statuses = args.length > 0
        ? args
        : new String[] {"created", "PAID", "shipped", "cancelled", "unknown"};
```

这里先准备一组订单状态。如果命令行传了参数，就使用参数；否则使用默认数组。

`String[]` 表示字符串数组，类似 JavaScript 中的字符串数组：

```js
const statuses = ["created", "PAID", "shipped"];
```

但 Java 数组长度固定，且每个元素都必须是 `String`。后面学集合时，我们会再比较数组和 `List`。

## `for`：按下标遍历数组

```java
for (int i = 0; i < statuses.length; i++) {
    ...
}
```

这是 Java 最经典的计数循环。

- `int i = 0`：循环下标从 `0` 开始。
- `i < statuses.length`：只要下标还没超过数组长度，就继续执行。
- `i++`：每轮结束后下标加 `1`。

它和 JavaScript 的写法非常接近：

```js
for (let i = 0; i < statuses.length; i++) {
  // ...
}
```

后端里循环常用于批量处理数据，例如批量校验订单、组装响应列表、把数据库查询结果转换成接口 DTO。

## 方法：给一段逻辑起名字

```java
static String normalizeStatus(String rawStatus) {
    if (rawStatus == null || rawStatus.isBlank()) {
        return "UNKNOWN";
    }

    return rawStatus.trim().toUpperCase(Locale.ROOT);
}
```

这段方法做“状态标准化”：空值或空白字符串统一变成 `UNKNOWN`，其他状态先去掉前后空格，再转成大写。

方法签名从左到右可以这样读：

- `static`：当前还没有学习对象，所以先使用静态方法，可以直接在 `main` 中调用。
- `String`：这个方法会返回一个字符串。
- `normalizeStatus`：方法名，表达这段逻辑的意图。
- `String rawStatus`：参数，调用方传入原始状态。

`return` 会结束方法并返回结果。这里要特别注意：Java 方法声明了返回类型 `String`，就必须在所有可能路径上返回字符串。

## `if`：处理边界条件

```java
if (rawStatus == null || rawStatus.isBlank()) {
    return "UNKNOWN";
}
```

`if` 用来表达条件分支。这里先处理边界情况：状态可能是 `null`，也可能是空白字符串。

`||` 表示“或”。只要左边或右边有一个为 `true`，整个条件就是 `true`。

条件顺序很重要：先判断 `rawStatus == null`，再调用 `rawStatus.isBlank()`。如果顺序反过来，当 `rawStatus` 是 `null` 时就会触发空指针问题。Java 的 `||` 有短路行为：左边已经为 `true` 时，右边不会继续执行。

## `switch`：处理固定枚举值

```java
switch (status) {
    case "CREATED":
        return "Order created, waiting for payment";
    case "PAID":
        return "Payment received, waiting for shipment";
    case "SHIPPED":
        return "Package shipped, waiting for delivery";
    case "CANCELLED":
        return "Order cancelled";
    default:
        return "Unknown status, confirm backend mapping";
}
```

`switch` 适合处理“一个值对应多个固定分支”的场景，比如订单状态、支付状态、用户角色、消息类型。

这里每个 `case` 都直接 `return`，所以不需要再写 `break`。如果 `case` 里不是 `return`，传统 `switch` 往往需要 `break` 防止继续落到后面的分支。这个点以后写业务代码时很容易踩。

`default` 是兜底分支。真实接口里，后端新增状态但前端或旧逻辑没更新时，就会出现未知状态。保留兜底比让程序直接崩掉更适合面向用户的接口。

## 字符串比较：为什么写 `"CANCELLED".equals(status)`

```java
if ("CANCELLED".equals(status)) {
    return true;
}
```

Java 中比较字符串内容，要使用 `equals`，不要使用 `==`。`==` 比较的是两个引用是否指向同一个对象，不是文本内容是否相等。

这里把常量字符串写在前面，是一个常见防御写法。即使 `status` 是 `null`，`"CANCELLED".equals(status)` 也只会返回 `false`，不会因为调用 `status.equals(...)` 而出错。

## 编译和运行

继续把 `.class` 输出到临时目录，避免把构建产物提交到仓库：

```bash
javac -d /tmp/learn-step-java-l003 tracks/02-java-springboot/examples/L003-control-flow-methods/OrderStatusFlow.java
java -cp /tmp/learn-step-java-l003 OrderStatusFlow
```

已真实执行，输出如下：

```text
#1 [CREATED] Order created, waiting for payment | supportVisible=false
#2 [PAID] Payment received, waiting for shipment | supportVisible=false
#3 [SHIPPED] Package shipped, waiting for delivery | supportVisible=false
#4 [CANCELLED] Order cancelled | supportVisible=true
#5 [UNKNOWN] Unknown status, confirm backend mapping | supportVisible=true
```

也可以传入自定义状态测试：

```bash
java -cp /tmp/learn-step-java-l003 OrderStatusFlow paid refunded "  shipped  "
```

真实项目里，这些参数可以类比为接口请求、数据库字段或第三方回调里的状态值。

## 常见误区

`if` 条件必须是布尔表达式。Java 不会像 JavaScript 一样把字符串、数字或对象自动转成真假值，所以不能写：

```java
if (status) {
    ...
}
```

字符串内容比较不要写：

```java
if (status == "PAID") {
    ...
}
```

应该写：

```java
if ("PAID".equals(status)) {
    ...
}
```

方法返回类型要和 `return` 的值匹配。如果方法声明 `static boolean shouldShowSupport(...)`，就必须返回 `true` 或 `false`，不能返回 `"true"` 字符串。

`shouldShowSupport` 里也用了一个小的兜底分支：已知正常状态返回 `false`，取消或未识别状态返回 `true`。这样后端遇到新状态时，不会让前端页面完全失去处理入口。

## 放到真实接口里看

在 Spring Boot 中，后端经常会把数据库里的状态码转换成前端更容易消费的响应字段。例如：

```json
{
  "status": "PAID",
  "statusText": "Payment received, waiting for shipment",
  "supportVisible": false
}
```

如果后端不做任何兜底，前端遇到新状态时可能显示空白、按钮错误或逻辑异常。本节的 `default`、`UNKNOWN` 和 `supportVisible` 判断，就是后端处理边界状态的雏形。

## 练习

1. 新增一个状态 `DELIVERED`，让它输出 `Package delivered`。
2. 修改 `shouldShowSupport`，让 `SHIPPED` 状态也显示客服入口。
3. 运行自定义参数：`paid refunded "  shipped  "`，观察大小写和空格如何被处理。
4. 尝试把 `"PAID".equals(status)` 改成 `status == "PAID"`，思考为什么这不是可靠写法。

下一节会学习数组与方法参数的进一步使用，为后面集合框架和接口响应列表做准备。
