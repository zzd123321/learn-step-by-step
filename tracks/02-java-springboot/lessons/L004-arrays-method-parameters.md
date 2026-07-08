# L004：数组与方法参数

数组用来保存一组同类型的数据，方法参数用来把数据交给一段可复用逻辑。把这两个概念放在一起看，会更接近真实后端工作：接口查出一批订单后，后端经常要遍历它们、计算汇总值，再组装成响应返回给前端。

本节先不用集合框架，只使用 Java 最基础的数组。数组的规则更“硬”，但也更适合打基础：长度固定、下标从 `0` 开始、元素类型一致。

## 示例：订单列表金额汇总

示例文件：[OrderListSummary.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L004-arrays-method-parameters/OrderListSummary.java)

```java
public class OrderListSummary {
    public static void main(String[] args) {
        double[] orderAmounts = {99.90, 149.50, 20.00, 300.00};

        printOrderAmounts(orderAmounts);

        double total = sum(orderAmounts);
        double average = average(orderAmounts);
        double max = max(orderAmounts);

        System.out.println("Total amount: " + total);
        System.out.println("Average amount: " + average);
        System.out.println("Max amount: " + max);

        applyDemoMutation(orderAmounts);
        System.out.println("First amount after method call: " + orderAmounts[0]);
    }

    static void printOrderAmounts(double[] amounts) {
        for (int i = 0; i < amounts.length; i++) {
            System.out.println("Order #" + (i + 1) + ": " + amounts[i]);
        }
    }

    static double sum(double[] amounts) {
        double total = 0.0;

        for (int i = 0; i < amounts.length; i++) {
            total = total + amounts[i];
        }

        return total;
    }

    static double average(double[] amounts) {
        if (amounts.length == 0) {
            return 0.0;
        }

        return sum(amounts) / amounts.length;
    }

    static double max(double[] amounts) {
        if (amounts.length == 0) {
            return 0.0;
        }

        double currentMax = amounts[0];

        for (int i = 1; i < amounts.length; i++) {
            if (amounts[i] > currentMax) {
                currentMax = amounts[i];
            }
        }

        return currentMax;
    }

    static void applyDemoMutation(double[] amounts) {
        if (amounts.length > 0) {
            amounts[0] = amounts[0] + 1.0;
        }
    }
}
```

这段代码模拟一个列表接口的后端汇总逻辑：订单金额数组进入多个方法，分别打印明细、计算总金额、平均金额和最大金额。

## 数组声明：类型后面加 `[]`

```java
double[] orderAmounts = {99.90, 149.50, 20.00, 300.00};
```

`double[]` 表示“double 类型数组”。它只能保存 `double` 值，不能混入字符串或布尔值。

和 JavaScript 数组相比：

```js
const orderAmounts = [99.90, 149.50, 20.00, 300.00];
```

JavaScript 数组更灵活，可以混放不同类型，也可以动态增删元素。Java 数组更严格：长度创建后固定，元素类型也固定。后面学习 `List` 时，会看到更适合业务开发的动态列表。

## 下标和长度

数组通过下标访问元素：

```java
amounts[i]
```

下标从 `0` 开始。`amounts[0]` 是第一个元素，`amounts[amounts.length - 1]` 是最后一个元素。

`amounts.length` 是数组长度，不是方法调用，所以没有括号。这里和 JavaScript 的 `array.length` 很像。

最常见的错误是越界访问：

```java
amounts[amounts.length]
```

这会访问“最后一个元素后面的位置”，运行时会抛出数组下标越界异常。正确的最后一个下标是 `amounts.length - 1`。

## 把数组传给方法

```java
static double sum(double[] amounts) {
    double total = 0.0;

    for (int i = 0; i < amounts.length; i++) {
        total = total + amounts[i];
    }

    return total;
}
```

`sum` 方法接收一个 `double[]` 参数，并返回一个 `double`。这让“求和”逻辑从 `main` 里独立出来，调用方不需要关心内部如何遍历，只需要知道传入金额数组，得到总金额。

方法参数的语法是：

```text
参数类型 参数名
```

所以 `double[] amounts` 的意思是：这个方法需要一个 double 数组，方法内部用 `amounts` 这个名字访问它。

## 空数组边界

```java
static double average(double[] amounts) {
    if (amounts.length == 0) {
        return 0.0;
    }

    return sum(amounts) / amounts.length;
}
```

平均值要除以数组长度。如果数组为空，长度是 `0`，直接相除没有业务意义，所以先用 `if` 兜底。

真实后端里，空列表非常常见：没有订单、没有搜索结果、没有消息通知。接口逻辑要先想清楚空列表返回什么，而不是等线上报错。

## 方法参数传递的关键点

```java
static void applyDemoMutation(double[] amounts) {
    if (amounts.length > 0) {
        amounts[0] = amounts[0] + 1.0;
    }
}
```

这段方法故意修改了数组第一个元素。运行后，`main` 里再次读取 `orderAmounts[0]`，会看到它也变了。

原因是：Java 方法参数是“值传递”（pass by value，值传递）。对于基本类型，传过去的是值本身的副本；对于数组这种引用类型，传过去的是“引用值”的副本。两个引用副本仍然指向同一个数组对象，所以方法内部能修改数组元素。

这和 JavaScript 里对象/数组作为参数时的表现很像：重新给参数变量赋值不会影响外部变量，但修改对象或数组内部内容会被外部看到。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l004 tracks/02-java-springboot/examples/L004-arrays-method-parameters/OrderListSummary.java
java -cp /tmp/learn-step-java-l004 OrderListSummary
```

已真实执行，输出如下：

```text
Order #1: 99.9
Order #2: 149.5
Order #3: 20.0
Order #4: 300.0
Total amount: 569.4
Average amount: 142.35
Max amount: 300.0
First amount after method call: 100.9
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

这里继续使用 `double` 是为了聚焦数组和方法参数。真实金额计算后续会单独学习更合适的 `BigDecimal`。

## 常见误区

数组下标从 `0` 开始，不是从 `1` 开始。循环条件通常写：

```java
i < amounts.length
```

不要写成：

```java
i <= amounts.length
```

后者会在最后一轮访问不存在的位置。

数组长度固定，不能像 JavaScript 一样直接 `push`：

```js
orderAmounts.push(88.0);
```

Java 数组没有这个能力。需要动态追加时，通常使用 `List`，后续集合章节会专门学习。

方法返回值要和声明匹配。`static double sum(...)` 必须返回 `double`，而 `static void printOrderAmounts(...)` 表示只执行打印，不返回结果。

## 放到真实接口里看

一个订单列表接口可能既返回列表，也返回汇总：

```json
{
  "orders": [99.9, 149.5, 20.0, 300.0],
  "summary": {
    "total": 569.4,
    "average": 142.35,
    "max": 300.0
  }
}
```

后端通常会把“遍历列表”“计算汇总”“格式化响应”拆成不同方法。这样 Controller 不会塞满细节，Service 里的业务逻辑也更容易测试。

## 练习

1. 新增一个 `min(double[] amounts)` 方法，返回最小金额。
2. 把订单金额数组改为空数组，观察 `average` 和 `max` 的返回。
3. 尝试把循环条件改成 `i <= amounts.length`，记录运行时错误。
4. 删除 `applyDemoMutation` 的调用，观察第一个金额是否还会变化。

下一节会学习面向对象入门：从“散落的方法和数组”过渡到 `Order` 类，把数据和行为组织在一起。
