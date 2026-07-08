# L005：类与对象入门

前几节我们把数据放在变量和数组里，把逻辑拆成静态方法。这样能跑，但真实后端不会一直把订单写成几组散落的数组和方法。后端更常见的组织方式是：用一个类描述业务对象，让数据和与它相关的行为放在一起。

这一节只讲面向对象的第一步：类（class，类）和对象（object，对象）。先不展开继承、多态和接口。

## 从“散落数据”到 `Order` 对象

如果继续用数组表达订单，代码可能会变成这样：

```java
String[] ids = {"A1001", "A1002"};
String[] statuses = {"PAID", "CREATED"};
double[] amounts = {99.90, 149.50};
```

这会有一个问题：同一个订单的数据被拆在多个数组里，只能靠下标对应。`ids[0]`、`statuses[0]`、`amounts[0]` 必须永远代表同一个订单，一旦某个数组顺序错了，数据就乱了。

面向对象的想法是：把一个订单自己的数据放进同一个对象里。

```java
Order firstOrder = new Order("A1001", "PAID", 99.90);
```

`firstOrder` 不是三个孤立变量，而是一个订单对象。它知道自己的编号、状态和金额，也能回答“是否已付款”“能不能发货”“如何生成摘要文本”。

## 示例：订单对象

示例文件：[OrderObjectDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L005-classes-and-objects/OrderObjectDemo.java)

```java
public class OrderObjectDemo {
    public static void main(String[] args) {
        Order firstOrder = new Order("A1001", "PAID", 99.90);
        Order secondOrder = new Order("A1002", "CREATED", 149.50);

        System.out.println(firstOrder.summaryLine());
        System.out.println(secondOrder.summaryLine());

        if (firstOrder.canShip()) {
            firstOrder.markShipped();
        }

        Order[] orders = {firstOrder, secondOrder};

        System.out.println("After shipment:");
        printOrders(orders);
        System.out.println("Paid order count: " + countPaidOrders(orders));
    }

    static void printOrders(Order[] orders) {
        for (int i = 0; i < orders.length; i++) {
            System.out.println(orders[i].summaryLine());
        }
    }

    static int countPaidOrders(Order[] orders) {
        int count = 0;

        for (int i = 0; i < orders.length; i++) {
            if (orders[i].isPaid()) {
                count++;
            }
        }

        return count;
    }
}

class Order {
    private final String id;
    private String status;
    private final double amount;

    Order(String id, String status, double amount) {
        this.id = id;
        this.status = status;
        this.amount = amount;
    }

    boolean isPaid() {
        return "PAID".equals(status);
    }

    boolean canShip() {
        return isPaid();
    }

    void markShipped() {
        if (!canShip()) {
            return;
        }

        status = "SHIPPED";
    }

    String summaryLine() {
        return "Order " + id + " | status=" + status + " | amount=" + amount;
    }
}
```

这个文件里有两个类：

- `OrderObjectDemo` 是公开类，负责提供 `main` 入口。
- `Order` 是普通类，负责表达订单这个业务对象。

一个 `.java` 文件里只能有一个 `public` 顶层类，并且文件名必须和这个公开类一致。所以文件名是 `OrderObjectDemo.java`。

## 字段：对象保存的数据

```java
private final String id;
private String status;
private final double amount;
```

这些变量写在类里面、方法外面，叫字段（field，字段），也常被称为成员变量。每个 `Order` 对象都有自己的一份字段值。

- `id`：订单编号。
- `status`：订单状态，会从 `PAID` 变成 `SHIPPED`，所以没有写 `final`。
- `amount`：订单金额，本节仍用 `double` 聚焦面向对象，真实金额后续会学习 `BigDecimal`。

`private` 表示这些字段只能在 `Order` 类内部直接访问。外部代码不能随便写 `firstOrder.status = "xxx"`。这是封装（encapsulation，封装）的第一步：对象内部数据不随便暴露，外部通过方法表达业务动作。

## 构造方法：创建对象时填入初始数据

```java
Order(String id, String status, double amount) {
    this.id = id;
    this.status = status;
    this.amount = amount;
}
```

这个和类同名、没有返回类型的方法叫构造方法（constructor，构造方法）。执行：

```java
new Order("A1001", "PAID", 99.90)
```

时，Java 会创建一个新的 `Order` 对象，并调用构造方法把参数写入字段。

`this.id = id` 里的 `this` 表示“当前这个对象”。左边的 `this.id` 是字段，右边的 `id` 是构造方法参数。它们名字一样时，`this` 能帮你明确区分。

## 方法：对象能做什么

```java
boolean isPaid() {
    return "PAID".equals(status);
}
```

`isPaid` 是订单对象自己的行为。调用时写：

```java
firstOrder.isPaid()
```

它会根据 `firstOrder` 这个对象内部的 `status` 字段返回结果。

```java
boolean canShip() {
    return isPaid();
}
```

`canShip` 把“能否发货”这条业务规则命名出来。目前规则很简单：已付款才能发货。以后规则变复杂时，比如还要检查库存、风控、地址，这个方法名依然能稳定表达业务含义。

```java
void markShipped() {
    if (!canShip()) {
        return;
    }

    status = "SHIPPED";
}
```

`markShipped` 是一个会改变对象状态的方法。如果订单不能发货，就直接 `return`，什么也不做；如果可以发货，就把 `status` 改成 `SHIPPED`。

这里的 `!canShip()` 表示“不能发货”。`!` 是逻辑非，和 JavaScript 里的 `!` 类似。

## 对象数组：数组里放的是引用

```java
Order[] orders = {firstOrder, secondOrder};
```

`Order[]` 表示订单对象数组。数组里保存的是对象引用，不是把整个对象复制一份。

所以当代码先执行：

```java
firstOrder.markShipped();
```

再把 `firstOrder` 放进数组或从数组里读取时，看到的是同一个对象的最新状态。

这和 JavaScript 里对象引用的行为很像：

```js
const firstOrder = { status: "PAID" };
const orders = [firstOrder];
firstOrder.status = "SHIPPED";
console.log(orders[0].status); // SHIPPED
```

## 静态方法和对象方法的区别

本节文件里同时出现了两种方法：

```java
static void printOrders(Order[] orders)
```

这是静态方法，属于类本身。调用时不依赖某个具体 `OrderObjectDemo` 对象。

```java
boolean isPaid()
```

这是对象方法，属于某个具体订单。调用时需要对象：

```java
firstOrder.isPaid()
```

初学阶段可以先这样判断：如果方法要使用某个订单自己的字段，通常应该放在 `Order` 里作为对象方法；如果方法只是处理一组外部传入的数据，暂时可以写成静态工具方法。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l005 tracks/02-java-springboot/examples/L005-classes-and-objects/OrderObjectDemo.java
java -cp /tmp/learn-step-java-l005 OrderObjectDemo
```

已真实执行，输出如下：

```text
Order A1001 | status=PAID | amount=99.9
Order A1002 | status=CREATED | amount=149.5
After shipment:
Order A1001 | status=SHIPPED | amount=99.9
Order A1002 | status=CREATED | amount=149.5
Paid order count: 0
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

注意最后 `Paid order count: 0`：因为第一个订单已经从 `PAID` 变成了 `SHIPPED`，所以此时没有状态仍为 `PAID` 的订单。这说明对象状态改变会影响后续业务判断。

## 常见误区

构造方法没有返回类型。下面这样不是构造方法，而是一个普通方法：

```java
void Order(String id, String status, double amount) {
    ...
}
```

字段如果声明为 `private`，外部就不能直接访问。这不是麻烦，而是为了避免外部代码绕过业务方法随便改状态。

字符串状态比较仍然要用 `equals`：

```java
"PAID".equals(status)
```

不要写：

```java
status == "PAID"
```

对象变量保存的是引用。把对象传给方法或放进数组，不代表复制了一个完全独立的新对象。

## 放到真实后端里看

后端项目里会大量出现这种写法：用类表达业务概念。

```java
class Order {
    private String id;
    private String status;
    private double amount;
}
```

以后进入 Spring Boot 后，你会看到更多类似结构：

- DTO（Data Transfer Object，数据传输对象）：表达接口入参和响应。
- Entity（实体）：表达数据库表中的一行数据。
- Service（服务）：表达业务流程。
- Repository（仓储）：表达数据访问。

类和对象是这些结构的基础。理解它们之后，Spring Boot 的分层才不会只是一堆陌生文件夹。

## 练习

1. 给 `Order` 新增 `boolean isShipped()` 方法。
2. 新增一个 `CREATED` 订单，尝试调用 `markShipped()`，观察它是否会变成 `SHIPPED`。
3. 新增 `String statusText()` 方法，把 `CREATED`、`PAID`、`SHIPPED` 转成中文文案。
4. 尝试在 `main` 里直接访问 `firstOrder.status`，记录编译错误。

下一节会继续面向对象：封装与访问控制。我们会把“为什么字段要 `private`、什么时候提供 getter、什么时候提供业务方法”讲得更清楚。
