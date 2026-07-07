# L002：Java 基础语法、变量与类型

## 1. 学习目标

完成本节后，你应该能：

- 理解 Java 变量声明的基本格式。
- 区分 Java 的基本类型和引用类型。
- 理解 `final` 与 JavaScript `const` 的相似点和差异。
- 用一个最小 Java 程序表达“商品库存与价格”这类接口数据。

## 2. 前置知识

你需要已经理解：

- Java 程序需要先编译再运行。
- `public static void main(String[] args)` 是当前示例程序的入口。
- JavaScript 中 `let`、`const` 可以声明变量。

本节不展开对象、数组、集合和方法拆分，只专注“变量如何声明、值是什么类型”。

## 3. Java 与 JavaScript 的对应关系及差异

| 视角 | Java | JavaScript |
| --- | --- | --- |
| 变量声明 | `int stock = 12;` | `let stock = 12;` |
| 类型位置 | 类型写在变量名前 | 变量本身不写类型 |
| 类型检查 | 编译期检查，类型不匹配会编译失败 | 多数类型问题运行时才暴露 |
| 数字类型 | `int`、`long`、`double` 等多个数字类型 | 常用 `number`，另有 `bigint` |
| 字符串 | `String`，引用类型 | `string`，原始类型 |
| 常量 | `final double rate = 0.1;` | `const rate = 0.1;` |

JavaScript 中你可以写：

```js
let stock = 12;
stock = "sold out";
```

Java 中不能这样写：

```java
int stock = 12;
stock = "sold out";
```

因为 `stock` 已经被声明为 `int`，只能保存整数。这个错误会在 `javac` 编译阶段被发现。

## 4. 概念解析与心智模型

### 变量声明格式

Java 最常见的变量声明格式是：

```java
类型 变量名 = 初始值;
```

例如：

```java
int stock = 12;
double price = 99.90;
boolean available = true;
String productName = "Wireless Mouse";
```

你可以把 Java 变量理解为“带标签的盒子”：

- `int` 盒子只能放整数。
- `double` 盒子放小数。
- `boolean` 盒子只放 `true` 或 `false`。
- `String` 变量保存的是对字符串对象的引用。

### 基本类型与引用类型

本节先认识两类：

- 基本类型（primitive type，基本类型）：直接保存简单值，例如 `int`、`double`、`boolean`、`char`。
- 引用类型（reference type，引用类型）：变量里保存的是对象的引用，例如 `String`。

先不用纠结内存细节。当前阶段你只需要记住：Java 比 JavaScript 更早、更明确地要求你说明数据类型。

### `final` 与 `const`

`final` 表示这个变量赋值后不能再指向新值：

```java
final double discountRate = 0.10;
```

这和 JavaScript 的 `const` 很像，但不要简单等同：

- 对基本类型来说，`final int count = 1;` 之后不能改成 `2`。
- 对引用类型来说，`final` 限制的是“引用不能改指向另一个对象”，不代表对象内部一定不可变。

本节示例只使用基本类型的 `final`，先把“不可重新赋值”这个核心点吃透。

## 5. 心智模型

前端写接口数据时，经常先想“这个字段长什么样”：

```js
{
  productName: "Wireless Mouse",
  stock: 12,
  price: 99.90,
  available: true
}
```

Java 会逼你更早回答一个问题：每个字段到底是什么类型？

- `productName` 是文本，所以用 `String`。
- `stock` 是库存整数，所以用 `int`。
- `price` 是价格小数，所以用 `double`。
- `available` 是是否可售，所以用 `boolean`。

这就是后端开发里“数据契约”的雏形。类型越清楚，接口字段越不容易含糊。

## 6. 可运行代码

文件位置：`tracks/02-java-springboot/examples/L002-variables-and-types/ProductSummary.java`

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

## 7. 代码逐行说明

```java
public class ProductSummary {
```

定义一个公开类 `ProductSummary`。文件名必须是 `ProductSummary.java`。

```java
public static void main(String[] args) {
```

程序入口。JVM 从这里开始执行。

```java
String productName = "Wireless Mouse";
```

声明一个字符串变量，保存商品名称。`String` 是引用类型。

```java
int stock = 12;
```

声明一个整数变量，保存库存数量。`int` 适合常见整数。

```java
double price = 99.90;
```

声明一个小数变量，保存价格。`double` 是双精度浮点数。真实金融金额后续通常会用 `BigDecimal`，本节先用 `double` 学语法。

```java
boolean available = stock > 0;
```

声明布尔变量。`stock > 0` 的结果只能是 `true` 或 `false`。

```java
char currencySymbol = '$';
```

声明字符变量。`char` 使用单引号，表示单个字符。

```java
final double discountRate = 0.10;
```

声明不可重新赋值的折扣率。`final` 类似 JavaScript 的 `const`。

```java
double discountedPrice = price * (1 - discountRate);
```

计算折后价格。Java 的加减乘除符号和 JavaScript 很接近。

```java
System.out.println("Product: " + productName);
```

打印商品信息。字符串拼接也使用 `+`。

```java
stock = stock - 1;
```

模拟下单后库存减少。因为 `stock` 不是 `final`，所以可以重新赋值。

## 8. 运行方式

为了避免把 `.class` 编译产物提交到仓库，本节继续把输出放到临时目录：

```bash
javac -d /tmp/learn-step-java-l002 tracks/02-java-springboot/examples/L002-variables-and-types/ProductSummary.java
java -cp /tmp/learn-step-java-l002 ProductSummary
```

## 9. 预期结果

应该看到商品名、库存、可售状态、原价、折后价，以及下单后的库存。

## 10. 实际运行结果

已在本机真实执行编译和运行命令，结果如下：

```text
Product: Wireless Mouse
Stock: 12
Available: true
Original price: $99.9
Discounted price: $89.91000000000001
Stock after one order: 11
```

验证结论：本节代码已通过本机 Java `25.0.3 LTS` 编译和运行验证。

注意：`89.91000000000001` 是浮点数精度问题的体现。它不是 Java 独有，JavaScript 里也会遇到类似问题，例如 `0.1 + 0.2`。真实金额计算后续会学习 `BigDecimal`。

## 11. 常见错误与排查方式

### 错误 1：把字符串赋值给 `int`

```java
int stock = "12";
```

排查方式：确认等号右侧的值和左侧类型匹配。`"12"` 是字符串，不是整数。

### 错误 2：字符和字符串混用

```java
char currencySymbol = "$";
```

排查方式：`char` 用单引号，`String` 用双引号。应该写成 `char currencySymbol = '$';`。

### 错误 3：修改 `final` 变量

```java
final double discountRate = 0.10;
discountRate = 0.20;
```

排查方式：`final` 变量不能重新赋值。如果业务规则会变化，就不要把这个局部变量声明为 `final`。

### 错误 4：用 `double` 直接处理真实订单金额

本节用 `double` 是为了学习基础类型。真实订单金额直接用浮点数可能带来精度问题。

排查方式：后续处理金额时优先考虑 `BigDecimal`，并明确小数位和舍入规则。

## 12. 前端接口联调视角下的真实应用场景

假设后端返回商品摘要接口：

```json
{
  "productName": "Wireless Mouse",
  "stock": 12,
  "price": 99.9,
  "available": true
}
```

前端关心字段能不能正常展示，后端则必须更早决定字段类型：

- `stock` 是数字还是字符串？
- `price` 是否允许小数？
- `available` 是布尔值，还是用 `0` / `1`？
- 字段缺失时前端应该怎么兜底？

Java 的静态类型会让后端在编码阶段就明确这些约定。等后面学到 Spring Boot Controller 和 DTO 时，这些类型会直接变成接口契约的一部分。

## 13. 小练习

1. 把 `stock` 改成 `0`，观察 `available` 的输出。
2. 把 `discountRate` 改成 `0.20`，重新编译运行，观察折后价。
3. 尝试给 `stock` 赋值 `"12"`，记录编译错误。
4. 尝试修改 `final double discountRate`，记录编译错误。

## 14. 复盘问题

1. Java 变量声明为什么要写类型？
2. `int`、`double`、`boolean`、`char` 分别适合表达什么数据？
3. `String` 为什么不是基本类型？
4. `final` 和 JavaScript `const` 有什么相似点？
5. 为什么真实订单金额不建议长期用 `double` 表达？

## 15. 与真实全栈项目的联系

后端接口字段不是随手拼出来的文本，而是由类型、业务含义和序列化规则共同决定的。你现在看到的 `String productName`、`int stock`、`double price`、`boolean available`，以后会出现在 DTO、实体类、数据库字段映射和接口文档中。

前端联调时，如果你发现字段类型和文档不一致，例如 `stock` 有时是数字、有时是字符串，这往往说明后端数据模型或接口契约不稳定。学习 Java 类型系统，就是在为后续写稳定接口打地基。

## 16. 下一节预告

下一节学习 L003：控制流与方法。我们会用 `if`、`switch`、循环和方法封装一个简单的“订单状态判断”逻辑，并继续和 JavaScript 写法对比。
