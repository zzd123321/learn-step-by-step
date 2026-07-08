# L007：构造方法重载与 `this`

上一节我们用构造方法保护了对象创建时的基础数据。但真实后端里，对象创建来源可能不止一种：前端创建订单时只传必填字段，后台导入历史订单时可能带着状态，取消订单记录里还可能带着取消原因。

这时可以使用构造方法重载（constructor overloading，构造方法重载）：同一个类里写多个构造方法，它们名字相同，但参数列表不同。

本节重点是两件事：

- 多个构造方法如何表达不同创建场景。
- `this(...)` 如何把创建逻辑集中到一个主构造方法里，避免重复校验和默认值逻辑。

## 示例：不同来源创建订单

示例文件：[OrderConstructorDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L007-constructor-overloading-this/OrderConstructorDemo.java)

```java
public class OrderConstructorDemo {
    public static void main(String[] args) {
        Order draftOrder = new Order("A1004", 88.00);
        Order importedOrder = new Order("A1005", "PAID", 199.00);
        Order cancelledOrder = new Order("A1006", "CANCELLED", 20.00, "duplicate order");

        System.out.println(draftOrder.summaryLine());
        System.out.println(importedOrder.summaryLine());
        System.out.println(cancelledOrder.summaryLine());
    }
}

class Order {
    private final String id;
    private String status;
    private final double amount;
    private String cancelReason;

    Order(String id, double amount) {
        this(id, "CREATED", amount, "");
    }

    Order(String id, String status, double amount) {
        this(id, status, amount, "");
    }

    Order(String id, String status, double amount, String cancelReason) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Order id must not be blank");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Order amount must be greater than 0");
        }

        this.id = id;
        this.status = normalizeStatus(status);
        this.amount = amount;
        this.cancelReason = normalizeCancelReason(this.status, cancelReason);
    }

    String summaryLine() {
        String base = "Order " + id + " | status=" + status + " | amount=" + amount;

        if ("CANCELLED".equals(status)) {
            return base + " | cancelReason=" + cancelReason;
        }

        return base;
    }

    private String normalizeStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return "CREATED";
        }

        return rawStatus.trim().toUpperCase();
    }

    private String normalizeCancelReason(String status, String rawReason) {
        if (!"CANCELLED".equals(status)) {
            return "";
        }

        if (rawReason == null || rawReason.isBlank()) {
            return "no reason provided";
        }

        return rawReason.trim();
    }
}
```

`main` 里创建了三个订单，它们使用的都是 `new Order(...)`，但参数数量不同：

```java
new Order("A1004", 88.00)
new Order("A1005", "PAID", 199.00)
new Order("A1006", "CANCELLED", 20.00, "duplicate order")
```

Java 会根据参数列表自动选择匹配的构造方法。

## 什么是重载

重载（overloading，重载）指同一个类里有多个同名方法或构造方法，但参数列表不同。

这里有三个 `Order` 构造方法：

```java
Order(String id, double amount)
Order(String id, String status, double amount)
Order(String id, String status, double amount, String cancelReason)
```

它们名字都叫 `Order`，但参数数量和参数类型不同，所以 Java 能区分。

注意：返回类型不能用来区分重载。构造方法本来就没有返回类型，普通方法也是看方法名和参数列表，而不是看返回值。

## `this(...)`：调用同一个类里的其他构造方法

```java
Order(String id, double amount) {
    this(id, "CREATED", amount, "");
}
```

这里的 `this(...)` 不是“当前对象的字段”，而是调用当前类里的另一个构造方法。

这段代码的意思是：如果调用方只提供 `id` 和 `amount`，就默认状态为 `CREATED`，取消原因为空，然后交给四个参数的主构造方法处理。

第二个构造方法也是类似：

```java
Order(String id, String status, double amount) {
    this(id, status, amount, "");
}
```

它允许调用方提供状态，但不提供取消原因。

`this(...)` 必须是构造方法中的第一条语句。因为 Java 要先决定如何初始化对象，再执行其他逻辑。

## 主构造方法：集中校验与默认值

```java
Order(String id, String status, double amount, String cancelReason) {
    if (id == null || id.isBlank()) {
        throw new IllegalArgumentException("Order id must not be blank");
    }

    if (amount <= 0) {
        throw new IllegalArgumentException("Order amount must be greater than 0");
    }

    this.id = id;
    this.status = normalizeStatus(status);
    this.amount = amount;
    this.cancelReason = normalizeCancelReason(this.status, cancelReason);
}
```

这个四参数构造方法是主构造方法。所有创建路径最终都会走到这里，所以校验、默认值和标准化逻辑只写一份。

这很重要。如果三个构造方法各自写一遍校验，很容易出现某个入口漏掉校验。例如前端创建订单会检查金额，后台导入订单却忘了检查金额，坏数据就会混进系统。

## `this.field`：访问当前对象字段

```java
this.id = id;
this.status = normalizeStatus(status);
this.amount = amount;
```

这里的 `this.id` 表示当前对象的字段，右边的 `id` 是构造方法参数。

`this(...)` 和 `this.field` 都使用 `this`，但含义不同：

- `this(...)`：调用当前类的另一个构造方法，只能出现在构造方法第一行。
- `this.id`：访问当前对象的字段或方法。

可以把 `this` 理解成“当前正在创建或正在操作的这个对象”。

## 私有辅助方法：把标准化逻辑藏起来

```java
private String normalizeStatus(String rawStatus) {
    if (rawStatus == null || rawStatus.isBlank()) {
        return "CREATED";
    }

    return rawStatus.trim().toUpperCase();
}
```

`normalizeStatus` 只服务于 `Order` 内部，不需要暴露给外部，所以写成 `private`。它把状态空值处理、去空格、转大写集中在一个地方。

取消原因也类似：

```java
private String normalizeCancelReason(String status, String rawReason) {
    if (!"CANCELLED".equals(status)) {
        return "";
    }

    if (rawReason == null || rawReason.isBlank()) {
        return "no reason provided";
    }

    return rawReason.trim();
}
```

只有取消订单才保留取消原因；非取消订单的取消原因统一为空字符串。这样对象内部状态更一致。

## JavaScript 对比

JavaScript class 里通常只有一个 `constructor`：

```js
class Order {
  constructor(id, status = "CREATED", amount, cancelReason = "") {
    this.id = id;
    this.status = status;
    this.amount = amount;
    this.cancelReason = cancelReason;
  }
}
```

Java 不支持像 JavaScript 这样直接给参数写默认值。Java 常用多个重载构造方法表达不同参数组合。

也就是说，Java 里的：

```java
new Order("A1004", 88.00)
new Order("A1005", "PAID", 199.00)
```

更接近 JavaScript 里通过默认参数或对象参数来处理多种创建方式。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l007 tracks/02-java-springboot/examples/L007-constructor-overloading-this/OrderConstructorDemo.java
java -cp /tmp/learn-step-java-l007 OrderConstructorDemo
```

已真实执行，输出如下：

```text
Order A1004 | status=CREATED | amount=88.0
Order A1005 | status=PAID | amount=199.0
Order A1006 | status=CANCELLED | amount=20.0 | cancelReason=duplicate order
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

这说明三个不同构造入口都能创建合法订单，并且默认状态、状态标准化和取消原因处理都由主构造方法统一完成。

## 常见误区

`this(...)` 必须写在构造方法第一行。下面这样不行：

```java
Order(String id, double amount) {
    System.out.println("creating order");
    this(id, "CREATED", amount, "");
}
```

重载不是“随便写几个同名方法”。参数列表必须能让 Java 明确区分。如果两个构造方法参数类型和数量完全一样，编译会失败。

不要在每个构造方法里复制一整套校验逻辑。更好的方式是让简化构造方法调用主构造方法，把规则集中起来。

## 放到真实后端里看

后端对象经常有多种创建来源：

- 前端创建订单：只有必填字段。
- 后台导入订单：带着历史状态。
- 测试代码构造对象：希望用最少参数创建默认对象。
- 消息队列消费：可能携带额外元数据。

构造方法重载可以表达这些入口，但要小心别让入口太多。真实项目里，如果参数越来越复杂，后面还会学习静态工厂方法、Builder 模式，以及用 DTO 接收接口参数。

## 练习

1. 新增一个构造方法：`Order(String id)`，默认金额为 `1.0`。
2. 创建 `new Order("A1007", " paid ", 30.0)`，观察状态是否会变成 `PAID`。
3. 创建取消订单但不传取消原因，观察默认取消原因。
4. 故意把 `this(...)` 放到构造方法第二行，记录编译错误。

下一节会学习继承入门：为什么不是所有复用都应该靠继承，以及如何从一个小例子理解 `extends`。
