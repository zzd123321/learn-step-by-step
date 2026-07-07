# L002：变量、基本类型与引用类型

Java 的变量声明比 JavaScript 更“啰嗦”，但这份啰嗦换来的是更早的错误暴露和更清晰的接口契约。本节只围绕一个问题展开：一个后端接口字段，在 Java 里应该用什么类型表达？

先看一句 Java 变量声明：

```java
int stock = 12;
```

它的结构是：

```text
类型 变量名 = 初始值;
```

和 JavaScript 的 `let stock = 12` 相比，Java 要求你在变量名前写出类型。`stock` 一旦声明为 `int`，后续就不能再赋值成字符串。

## 从接口字段理解类型

假设商品摘要接口返回：

```json
{
  "productName": "Wireless Mouse",
  "stock": 12,
  "price": 99.9,
  "available": true
}
```

前端关心这些字段怎么展示，后端还要更早决定字段类型：

- `productName` 是文本，用 `String`。
- `stock` 是库存整数，用 `int`。
- `price` 是价格小数，本节先用 `double` 演示。
- `available` 是是否可售，用 `boolean`。

这就是 Java 静态类型的价值：接口字段不是临时拼出来的“差不多能用”的值，而是一组明确的数据契约。

## 最小示例：商品摘要

示例文件：[ProductSummary.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L002-variables-and-types/ProductSummary.java)

```java
public class ProductSummary {
    public static void main(String[] args) {
        String productName = "Wireless Mouse";
        int stock = 12;
        double price = 99.90;
        boolean available = stock > 0;
        char currencySymbol = '$';
        final double discountRate = 0.10;

        double discountedPrice = price * (1 - discountRate);

        System.out.println("Product: " + productName);
        System.out.println("Stock: " + stock);
        System.out.println("Available: " + available);
        System.out.println("Original price: " + currencySymbol + price);
        System.out.println("Discounted price: " + currencySymbol + discountedPrice);

        stock = stock - 1;
        System.out.println("Stock after one order: " + stock);
    }
}
```

这段代码模拟的是一个很小的后端数据整理过程：准备商品名、库存、价格和可售状态，然后计算折后价，最后模拟下单后库存减少。

## 一行一行看关键规则

`String productName = "Wireless Mouse";` 声明商品名称。`String` 是引用类型（reference type，引用类型），它表示变量中保存的是对字符串对象的引用。你可以先把它理解成 Java 里最常用的文本类型。

`int stock = 12;` 声明库存。`int` 是基本类型（primitive type，基本类型），适合常见整数。和 JavaScript 不同，Java 的整数、小数、布尔值、字符都有明确类型。

`double price = 99.90;` 声明价格。`double` 是双精度浮点数。它适合演示小数语法，但真实订单金额不建议长期直接用 `double`，因为浮点数存在精度问题。后面讲金额计算时会学习 `BigDecimal`。

`boolean available = stock > 0;` 声明可售状态。`stock > 0` 是一个条件表达式，结果只能是 `true` 或 `false`。Java 不会像 JavaScript 那样把很多值自动当作 truthy 或 falsy 使用。

`char currencySymbol = '$';` 声明单个字符。`char` 用单引号，`String` 用双引号，所以 `'$'` 和 `"$"` 不是同一种类型。

`final double discountRate = 0.10;` 声明折扣率，并且不允许重新赋值。`final` 可以类比 JavaScript 的 `const`：它限制变量不能再被赋成另一个值。要注意，`final` 修饰引用类型时，限制的是引用不能改指向，不等于对象内部一定不可变。

`double discountedPrice = price * (1 - discountRate);` 根据价格和折扣率计算折后价。Java 的算术运算符和 JavaScript 很接近，但参与运算的值必须符合类型规则。

`stock = stock - 1;` 模拟下单后库存减少。因为 `stock` 没有被 `final` 修饰，所以可以重新赋值。

## JavaScript 对比

JavaScript 允许变量保存不同类型的值：

```js
let stock = 12;
stock = "sold out";
```

Java 不允许：

```java
int stock = 12;
stock = "sold out";
```

这不是 Java “不灵活”，而是它选择在编译阶段提前挡住类型不一致的问题。后端接口一旦规模变大，这种约束会帮助团队减少很多“字段一会儿是数字、一会儿是字符串”的联调问题。

`final` 和 `const` 的相似点是不能重新赋值：

```java
final double discountRate = 0.10;
```

```js
const discountRate = 0.10;
```

但它们都不应该被误解成“深度不可变”。对于对象，是否能修改内部状态，要看对象本身和 API 设计。

## 编译和运行

继续把 `.class` 输出到临时目录，避免提交构建产物：

```bash
javac -d /tmp/learn-step-java-l002 tracks/02-java-springboot/examples/L002-variables-and-types/ProductSummary.java
java -cp /tmp/learn-step-java-l002 ProductSummary
```

已真实执行，输出如下：

```text
Product: Wireless Mouse
Stock: 12
Available: true
Original price: $99.9
Discounted price: $89.91000000000001
Stock after one order: 11
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

这里的 `89.91000000000001` 很值得注意。它来自浮点数精度问题，不是 Java 独有；JavaScript 中 `0.1 + 0.2` 也会出现类似现象。真实金额计算要使用更适合十进制精确计算的类型和规则。

## 常见误区

把字符串赋给整数变量会编译失败：

```java
int stock = "12";
```

`"12"` 是字符串，不是整数。如果你从 HTTP 请求里拿到的是字符串参数，后端需要显式转换并处理转换失败的情况。

字符和字符串也不能混用：

```java
char currencySymbol = "$";
```

这里 `"$"` 是 `String`，不是 `char`。单个字符要写成：

```java
char currencySymbol = '$';
```

`final` 变量不能重新赋值：

```java
final double discountRate = 0.10;
discountRate = 0.20;
```

如果折扣率来自配置或运营规则，就不要把“会变化的业务值”硬编码成一个局部 `final` 常量。

## 放到真实接口里看

后端返回给前端的 JSON 字段，最终往往来自 Java 对象。以后你会写类似 DTO（Data Transfer Object，数据传输对象）的类：

```java
class ProductSummaryResponse {
    String productName;
    int stock;
    double price;
    boolean available;
}
```

前端联调时，如果你发现接口文档写 `stock` 是数字，但实际返回 `"12"` 字符串，就说明后端类型约定或序列化过程不稳定。学习 Java 类型不是为了背关键字，而是为了写出更稳定的接口契约。

## 练习

1. 把 `stock` 改成 `0`，重新编译运行，观察 `available`。
2. 把 `discountRate` 改成 `0.20`，观察折后价。
3. 尝试给 `stock` 赋值 `"12"`，记录编译错误。
4. 尝试修改 `final double discountRate`，记录编译错误。

下一节会学习控制流与方法：用 `if`、`switch`、循环和方法封装一个简单的订单状态判断逻辑。
