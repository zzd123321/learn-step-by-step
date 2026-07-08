# L010：抽象类

上一节的 `PaymentMethod` 父类可以被直接创建，但这在业务上有点奇怪：一个“支付方式”通常只是一个共同概念，真实可用的应该是银行卡、钱包、银行转账这类具体支付方式。

抽象类（abstract class，抽象类）用来表达这种关系：父类提供共同字段和共同流程，但它本身不应该被直接创建；某些关键行为必须由子类实现。

本节继续支付场景，把 `PaymentMethod` 改成抽象类。

## 示例：抽象支付方式

示例文件：[AbstractPaymentDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L010-abstract-classes/AbstractPaymentDemo.java)

```java
public class AbstractPaymentDemo {
    public static void main(String[] args) {
        PaymentMethod[] methods = {
                new CardPayment("CARD-001", "Visa", "4242"),
                new WalletPayment("WALLET-001", "OpenPay", "frontend-user")
        };

        double orderAmount = 300.00;

        for (int i = 0; i < methods.length; i++) {
            PaymentResult result = methods[i].pay(orderAmount);
            System.out.println(result.summaryLine());
        }
    }
}

abstract class PaymentMethod {
    private final String id;
    private final String provider;

    PaymentMethod(String id, String provider) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Payment method id must not be blank");
        }

        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("Payment provider must not be blank");
        }

        this.id = id;
        this.provider = provider;
    }

    String getId() {
        return id;
    }

    String getProvider() {
        return provider;
    }

    PaymentResult pay(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0");
        }

        double fee = calculateFee(amount);
        return new PaymentResult(displayName(), amount, fee);
    }

    abstract String displayName();

    abstract double calculateFee(double amount);
}
```

示例里省略了部分子类代码展示，完整代码在示例文件中。核心变化是：

```java
abstract class PaymentMethod
```

以及两个抽象方法：

```java
abstract String displayName();
abstract double calculateFee(double amount);
```

## 抽象类不能直接创建对象

`PaymentMethod` 现在是抽象类，所以不能写：

```java
PaymentMethod method = new PaymentMethod("PAY-001", "Unknown");
```

因为它的 `displayName()` 和 `calculateFee(...)` 没有具体实现。Java 会要求你创建某个具体子类，比如：

```java
PaymentMethod method = new CardPayment("CARD-001", "Visa", "4242");
```

这和真实业务更一致：系统里不会出现一个模糊的“支付方式对象”，只会出现具体支付渠道对象。

## 抽象方法：父类定义规则，子类给出实现

```java
abstract double calculateFee(double amount);
```

抽象方法没有方法体，结尾是分号。它的意思是：所有非抽象子类都必须实现这个方法。

`CardPayment` 给出银行卡手续费规则：

```java
@Override
double calculateFee(double amount) {
    return amount * 0.02;
}
```

`WalletPayment` 给出钱包手续费规则：

```java
@Override
double calculateFee(double amount) {
    return 1.00;
}
```

如果某个子类没有实现所有抽象方法，它自己也必须声明为 `abstract`，否则编译失败。

## 抽象类可以有普通方法

抽象类不是只能放抽象方法。这里的 `pay` 是一个完整的普通方法：

```java
PaymentResult pay(double amount) {
    if (amount <= 0) {
        throw new IllegalArgumentException("Payment amount must be greater than 0");
    }

    double fee = calculateFee(amount);
    return new PaymentResult(displayName(), amount, fee);
}
```

这正是抽象类的价值：把共同流程放在父类，差异步骤留给子类。

这个模式在后端很常见：父类规定“先校验、再执行差异逻辑、最后组装结果”，子类只负责不同渠道的细节。

## `abstract` 和普通继承的区别

普通父类可以被直接创建，抽象类不行。

普通方法必须有方法体，抽象方法没有方法体。

抽象类里既可以有字段、构造方法、普通方法，也可以有抽象方法。构造方法虽然不能被 `new PaymentMethod(...)` 直接调用，但子类创建时仍然会通过 `super(...)` 调用它，用来初始化父类字段。

## JavaScript 对比

JavaScript 本身没有完全等价的抽象类语法。你可以通过约定或在父类方法里抛错来模拟：

```js
class PaymentMethod {
  calculateFee() {
    throw new Error("subclass must implement calculateFee")
  }
}
```

Java 的抽象类更严格：子类没实现抽象方法时，编译阶段就会失败。它把“必须实现”从运行时约定提前到了编译期规则。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l010 tracks/02-java-springboot/examples/L010-abstract-classes/AbstractPaymentDemo.java
java -cp /tmp/learn-step-java-l010 AbstractPaymentDemo
```

已真实执行，输出如下：

```text
Card Visa ending in 4242 | amount=300.0 | fee=6.0 | total=306.0
Wallet OpenPay account frontend-user | amount=300.0 | fee=1.0 | total=301.0
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

抽象类不能直接创建对象，但它可以有构造方法。这个构造方法是给子类通过 `super(...)` 调用的。

有抽象方法的类必须声明为 `abstract`。否则编译器会报错。

非抽象子类必须实现父类所有抽象方法。漏掉 `calculateFee` 或 `displayName` 都会编译失败。

抽象类不是接口。抽象类适合放共同状态和共同流程；接口更适合表达“具备某种能力”。下一阶段学接口时会再细分。

## 放到真实后端里看

抽象类适合这种场景：多个实现有相同生命周期或共同模板，但某些步骤不同。

比如支付处理可以统一为：

1. 校验金额。
2. 计算手续费。
3. 调用具体渠道。
4. 返回支付结果。

父类可以固定共同流程，子类实现渠道差异。这样业务层调用 `pay(amount)` 时，不需要关心具体渠道细节。

## 练习

1. 新增 `BankTransferPayment` 子类，手续费为 `0`。
2. 尝试写 `new PaymentMethod(...)`，记录编译错误。
3. 删除 `WalletPayment.calculateFee`，观察编译错误。
4. 给抽象类新增普通方法 `providerLine()`，在两个子类对象上调用它。

下一节会学习接口：当你只想表达“某个类具备一种能力”，而不是共享父类状态时，应该如何建模。
