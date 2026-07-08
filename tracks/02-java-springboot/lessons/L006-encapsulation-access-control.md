# L006：封装与访问控制

上一节我们已经能创建 `Order` 对象，并把订单编号、状态和金额放进对象里。本节往前走一步：对象里的数据不应该被外部随意修改。

封装（encapsulation，封装）的核心不是“把字段都写成 `private` 就完事”，而是让对象自己保护业务规则。外部代码只告诉对象“付款”“发货”“取消”，对象内部决定这些动作在当前状态下是否合法。

## 为什么不能直接暴露字段

如果订单状态可以被外部随便改，代码可能会变成这样：

```java
order.status = "SHIPPED";
```

这看起来方便，但问题很大：订单可能还没付款，却被直接改成已发货；状态也可能被写成 `"SHIPPPED"` 这种拼错的值。前端看到接口返回异常状态时，很难判断到底是前端映射问题，还是后端对象早就被写坏了。

更稳的做法是让字段私有化，然后提供表达业务意图的方法：

```java
order.pay();
order.ship();
order.cancel("customer changed address");
```

这样状态变化不再是“随便赋值”，而是经过对象自己的规则。

## 示例：受保护的订单状态

示例文件：[OrderEncapsulationDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L006-encapsulation-access-control/OrderEncapsulationDemo.java)

```java
public class OrderEncapsulationDemo {
    public static void main(String[] args) {
        Order order = new Order("A1003", 199.00);

        System.out.println(order.summaryLine());
        System.out.println("Can ship before payment: " + order.canShip());

        order.ship();
        System.out.println("After trying to ship before payment: " + order.summaryLine());

        order.pay();
        order.ship();
        System.out.println("After payment and shipment: " + order.summaryLine());

        order.cancel("customer changed address");
        System.out.println("After trying to cancel shipped order: " + order.summaryLine());
    }
}

class Order {
    private final String id;
    private String status;
    private final double amount;
    private String cancelReason;

    Order(String id, double amount) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Order id must not be blank");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Order amount must be greater than 0");
        }

        this.id = id;
        this.amount = amount;
        this.status = "CREATED";
        this.cancelReason = "";
    }

    String getId() {
        return id;
    }

    String getStatus() {
        return status;
    }

    double getAmount() {
        return amount;
    }

    String getCancelReason() {
        return cancelReason;
    }

    boolean canShip() {
        return "PAID".equals(status);
    }

    void pay() {
        if (!"CREATED".equals(status)) {
            return;
        }

        status = "PAID";
    }

    void ship() {
        if (!canShip()) {
            return;
        }

        status = "SHIPPED";
    }

    void cancel(String reason) {
        if ("SHIPPED".equals(status)) {
            return;
        }

        status = "CANCELLED";
        cancelReason = reason == null || reason.isBlank() ? "no reason provided" : reason;
    }

    String summaryLine() {
        String base = "Order " + id + " | status=" + status + " | amount=" + amount;

        if ("CANCELLED".equals(status)) {
            return base + " | cancelReason=" + cancelReason;
        }

        return base;
    }
}
```

这段代码不是为了模拟完整电商订单，而是为了看清封装的三个动作：

- 字段私有化：外部不能直接改 `status`。
- 构造方法校验：对象创建时先保证基础数据有效。
- 业务方法改状态：付款、发货、取消都经过对象自己的规则。

## `private`：把字段藏在对象内部

```java
private final String id;
private String status;
private final double amount;
private String cancelReason;
```

`private` 表示只能在当前类内部访问。`OrderEncapsulationDemo` 里的 `main` 不能直接写：

```java
order.status = "SHIPPED";
```

这就是访问控制（access control，访问控制）的作用：限制外部代码能碰到什么。

`final` 表示赋值后不能再重新赋值。`id` 和 `amount` 创建后不该变化，所以使用 `final`；`status` 和 `cancelReason` 会随着业务动作变化，所以没有使用 `final`。

## 构造方法里做基础校验

```java
Order(String id, double amount) {
    if (id == null || id.isBlank()) {
        throw new IllegalArgumentException("Order id must not be blank");
    }

    if (amount <= 0) {
        throw new IllegalArgumentException("Order amount must be greater than 0");
    }

    this.id = id;
    this.amount = amount;
    this.status = "CREATED";
    this.cancelReason = "";
}
```

对象创建时是保护数据的第一道门。订单编号不能为空，金额必须大于 `0`。如果参数不合法，就抛出 `IllegalArgumentException`。

`throw` 表示主动抛出异常。异常体系后面会专门学习；当前先理解为：与其创建一个坏对象，不如在创建时就明确失败。

## getter：读取可以，修改不一定开放

```java
String getStatus() {
    return status;
}
```

这种只返回字段值的方法常叫 getter。它允许外部读取状态，但没有允许外部直接修改状态。

很多初学者会机械生成一堆 getter 和 setter：

```java
void setStatus(String status) {
    this.status = status;
}
```

这会把 `private` 的保护力又打开一个洞。真实业务里，不是所有字段都应该提供 setter。状态变化更适合通过 `pay()`、`ship()`、`cancel()` 这类业务方法表达。

## 业务方法保护状态流转

```java
void ship() {
    if (!canShip()) {
        return;
    }

    status = "SHIPPED";
}
```

`ship()` 不是无条件把状态改成 `SHIPPED`。它先问 `canShip()`，只有已付款订单才能发货。

```java
boolean canShip() {
    return "PAID".equals(status);
}
```

这就是把业务规则放进对象内部。外部代码不需要重复判断“是不是已付款”，只需要调用 `order.ship()`。对象会自己决定能不能执行。

`cancel()` 也是类似：

```java
void cancel(String reason) {
    if ("SHIPPED".equals(status)) {
        return;
    }

    status = "CANCELLED";
    cancelReason = reason == null || reason.isBlank() ? "no reason provided" : reason;
}
```

已发货订单不能取消；取消原因为空时给一个默认值。外部只表达“我要取消”，对象内部保证取消后的数据仍然有意义。

## JavaScript 对比

在普通 JavaScript 对象里，你经常可以直接改属性：

```js
const order = { id: "A1003", status: "CREATED", amount: 199 };
order.status = "SHIPPED";
```

这种写法灵活，但大型业务里容易绕过规则。Java 的 `private` 字段和业务方法，会鼓励你把规则放在对象内部。

当然，现代 JavaScript 也可以用 class、私有字段、TypeScript 类型约束来做类似保护。差别在于 Java 从语言基础层面就非常强调访问控制。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l006 tracks/02-java-springboot/examples/L006-encapsulation-access-control/OrderEncapsulationDemo.java
java -cp /tmp/learn-step-java-l006 OrderEncapsulationDemo
```

已真实执行，输出如下：

```text
Order A1003 | status=CREATED | amount=199.0
Can ship before payment: false
After trying to ship before payment: Order A1003 | status=CREATED | amount=199.0
After payment and shipment: Order A1003 | status=SHIPPED | amount=199.0
After trying to cancel shipped order: Order A1003 | status=SHIPPED | amount=199.0
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

这段输出说明：未付款不能发货，付款后可以发货，已发货不能取消。状态不是被外部随便改的，而是被对象自己的方法保护着。

## 常见误区

不要以为 `private` 只是语法洁癖。它是在保护业务对象不被外部代码绕过规则。

不要机械给所有字段生成 setter。能不能修改字段，要看业务是否允许。订单编号通常不该改，订单状态也不该由一个通用 `setStatus` 随便改。

不要把所有逻辑都放在 `main` 或 Controller 里。对象自己的规则应该尽量靠近对象本身，否则业务会散落在各处。

## 放到真实后端里看

Spring Boot 项目里，你会经常看到实体、DTO、Service 这些类。封装思想会影响它们的写法：

- DTO 可以暴露给接口层，用来承载请求和响应。
- Entity 要保护数据库核心字段，避免随便写坏状态。
- Service 调用业务方法，而不是到处直接改字段。

比如订单支付接口不应该只是 `order.setStatus("PAID")`，更合理的是调用类似 `order.pay()` 的业务方法。这样支付前置条件、状态变化、默认值、异常处理都能集中在一个地方。

## 练习

1. 尝试在 `main` 中写 `order.status = "PAID";`，记录编译错误。
2. 新增 `boolean isCancelled()` 方法。
3. 创建一个金额为 `0` 的订单，观察构造方法抛出的异常。
4. 修改 `cancel()`：只有 `CREATED` 和 `PAID` 可以取消，其他状态直接返回。

下一节会学习构造方法重载与 `this` 的更多用法，继续把对象创建规则讲清楚。
