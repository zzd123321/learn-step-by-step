# L009：方法重写与多态

上一节我们已经看过 `extends` 和 `@Override`。这一节把它们连起来：同一个父类类型的方法调用，为什么能在运行时表现出不同子类行为？

这就是多态（polymorphism，多态）：用统一的父类类型写代码，但实际执行哪个版本的方法，由运行时对象的真实类型决定。

在后端项目里，多态非常常见。比如支付接口只关心“支付方式”，但银行卡、钱包、银行转账的手续费计算规则各不相同。

## 示例：不同支付方式计算不同手续费

示例文件：[PaymentPolymorphismDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L009-overriding-polymorphism/PaymentPolymorphismDemo.java)

```java
public class PaymentPolymorphismDemo {
    public static void main(String[] args) {
        PaymentMethod[] methods = {
                new CardPayment("CARD-001", "Visa", "4242"),
                new WalletPayment("WALLET-001", "OpenPay", "frontend-user"),
                new BankTransferPayment("BANK-001", "MockBank")
        };

        double orderAmount = 200.00;

        for (int i = 0; i < methods.length; i++) {
            PaymentResult result = methods[i].pay(orderAmount);
            System.out.println(result.summaryLine());
        }
    }
}

class PaymentMethod {
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

    String displayName() {
        return "Payment method " + id;
    }

    double calculateFee(double amount) {
        return 0.0;
    }

    PaymentResult pay(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0");
        }

        double fee = calculateFee(amount);
        return new PaymentResult(displayName(), amount, fee);
    }
}

class CardPayment extends PaymentMethod {
    private final String lastFourDigits;

    CardPayment(String id, String provider, String lastFourDigits) {
        super(id, provider);
        this.lastFourDigits = lastFourDigits;
    }

    @Override
    String displayName() {
        return "Card " + getProvider() + " ending in " + lastFourDigits;
    }

    @Override
    double calculateFee(double amount) {
        return amount * 0.02;
    }
}

class WalletPayment extends PaymentMethod {
    private final String accountName;

    WalletPayment(String id, String provider, String accountName) {
        super(id, provider);
        this.accountName = accountName;
    }

    @Override
    String displayName() {
        return "Wallet " + getProvider() + " account " + accountName;
    }

    @Override
    double calculateFee(double amount) {
        return 1.00;
    }
}

class BankTransferPayment extends PaymentMethod {
    BankTransferPayment(String id, String provider) {
        super(id, provider);
    }

    @Override
    String displayName() {
        return "Bank transfer via " + getProvider();
    }
}

class PaymentResult {
    private final String paymentName;
    private final double amount;
    private final double fee;

    PaymentResult(String paymentName, double amount, double fee) {
        this.paymentName = paymentName;
        this.amount = amount;
        this.fee = fee;
    }

    double totalCharged() {
        return amount + fee;
    }

    String summaryLine() {
        return paymentName
                + " | amount=" + amount
                + " | fee=" + fee
                + " | total=" + totalCharged();
    }
}
```

`methods` 数组的类型是 `PaymentMethod[]`，但里面放了三个不同子类对象：

```java
PaymentMethod[] methods = {
        new CardPayment(...),
        new WalletPayment(...),
        new BankTransferPayment(...)
};
```

这正是多态的入口：统一用父类类型管理一组不同子类。

## 重写：子类替换父类默认行为

父类里有默认手续费规则：

```java
double calculateFee(double amount) {
    return 0.0;
}
```

银行卡支付重写成按比例收费：

```java
@Override
double calculateFee(double amount) {
    return amount * 0.02;
}
```

钱包支付重写成固定收费：

```java
@Override
double calculateFee(double amount) {
    return 1.00;
}
```

银行转账没有重写 `calculateFee`，所以沿用父类的 `0.0` 手续费。

这就是继承和重写的常见用法：父类给出通用结构和默认行为，子类按需覆盖差异点。

## 多态：同一个调用，不同行为

循环里只有一行核心代码：

```java
PaymentResult result = methods[i].pay(orderAmount);
```

`methods[i]` 的编译时类型是 `PaymentMethod`，所以编译器只要求 `PaymentMethod` 上存在 `pay` 方法。

但 `pay` 方法内部调用：

```java
double fee = calculateFee(amount);
return new PaymentResult(displayName(), amount, fee);
```

这里的 `calculateFee` 和 `displayName` 会根据真实对象类型动态决定：

- 真实对象是 `CardPayment`，就执行银行卡的手续费和展示名。
- 真实对象是 `WalletPayment`，就执行钱包的手续费和展示名。
- 真实对象是 `BankTransferPayment`，展示名用转账版本，手续费沿用父类默认值。

这就是“同一个调用，不同行为”。

## 父类方法也会触发子类重写

这个示例里有一个细节很重要：`pay` 方法没有在子类里重写，它定义在父类 `PaymentMethod` 中。

但父类 `pay` 内部调用 `calculateFee()` 和 `displayName()` 时，仍然会触发子类重写后的版本。

这说明 Java 的实例方法调用是动态分派（dynamic dispatch，动态分派）：运行时会看对象真实类型，而不是只看变量声明类型。

## `@Override` 的保护作用

如果你把：

```java
double calculateFee(double amount)
```

不小心写成：

```java
double calculateFees(double amount)
```

那就不是重写，而是新增了一个方法。`pay` 仍然会调用父类的 `calculateFee`，你的手续费规则不会生效。

加上 `@Override` 后，编译器会帮你发现这种错误。所以重写父类方法时，建议总是写 `@Override`。

## JavaScript 对比

JavaScript 也有类似的运行时行为：

```js
class PaymentMethod {
  calculateFee(amount) {
    return 0
  }
}

class CardPayment extends PaymentMethod {
  calculateFee(amount) {
    return amount * 0.02
  }
}
```

调用 `method.calculateFee(200)` 时，也会根据真实对象决定执行哪个方法。

Java 的不同在于：父类类型、子类类型、重写签名都在编译阶段检查。你可以更早发现“这个对象不是支付方式”“这个方法签名没重写成功”之类的问题。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l009 tracks/02-java-springboot/examples/L009-overriding-polymorphism/PaymentPolymorphismDemo.java
java -cp /tmp/learn-step-java-l009 PaymentPolymorphismDemo
```

已真实执行，输出如下：

```text
Card Visa ending in 4242 | amount=200.0 | fee=4.0 | total=204.0
Wallet OpenPay account frontend-user | amount=200.0 | fee=1.0 | total=201.0
Bank transfer via MockBank | amount=200.0 | fee=0.0 | total=200.0
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

父类引用只能直接访问父类声明过的方法。比如变量类型是 `PaymentMethod`，就不能直接调用某个只存在于 `CardPayment` 的特殊方法。编译器看的是变量声明类型。

重写的方法名、参数列表要一致。返回值也要兼容。写错时让 `@Override` 帮你发现。

不要把所有差异都塞进父类的 `if` / `switch`：

```java
if ("CARD".equals(type)) { ... }
else if ("WALLET".equals(type)) { ... }
```

当差异行为属于不同子类时，多态通常更清晰。后续学习接口后，这种设计会更常见。

## 放到真实后端里看

支付、通知、文件存储、消息发送这类功能，经常会出现“统一入口，不同实现”：

- 支付：银行卡、钱包、银行转账。
- 通知：短信、邮件、站内信。
- 文件存储：本地、对象存储、云厂商。
- 登录：密码、短信验证码、OAuth。

后端业务层可以面向一个统一类型调用：

```java
paymentMethod.pay(amount);
```

至于具体怎么算手续费、怎么展示、怎么调用外部渠道，由不同实现自己负责。这能让调用方更稳定，也让新增实现更可控。

## 练习

1. 新增 `CouponPayment extends PaymentMethod`，让手续费为 `0`，展示名包含 `Coupon`。
2. 把 `WalletPayment.calculateFee` 改成 `amount * 0.01`，重新运行观察输出。
3. 故意把 `calculateFee` 写成 `calculateFees`，保留 `@Override`，记录编译错误。
4. 给 `CardPayment` 新增一个只属于它的方法，然后尝试通过 `PaymentMethod card = new CardPayment(...)` 调用，观察编译错误。

下一节会学习抽象类：当父类不应该被直接创建、只想提供共同结构时，该怎么表达。
