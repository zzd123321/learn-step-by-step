# L008：继承入门与 `extends`

继承（inheritance，继承）让一个类基于另一个类扩展。子类可以复用父类已有字段和方法，也可以重写某些行为。

不过，继承不是“代码复用的万能按钮”。真实后端里，如果两个概念只是碰巧有一些字段一样，不一定应该继承。继承更适合表达稳定的“is-a”（是一个）关系：银行卡支付是一种支付方式，钱包支付也是一种支付方式。

本节只讲继承的入门语法和边界感：`extends`、`super(...)`、`@Override`、父类引用指向子类对象。

## 示例：多种支付方式

示例文件：[PaymentInheritanceDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L008-inheritance-extends/PaymentInheritanceDemo.java)

```java
public class PaymentInheritanceDemo {
    public static void main(String[] args) {
        PaymentMethod card = new CardPayment("CARD-001", "Visa", "4242");
        PaymentMethod wallet = new WalletPayment("WALLET-001", "OpenPay", "frontend-user");

        PaymentMethod[] methods = {card, wallet};

        for (int i = 0; i < methods.length; i++) {
            printPaymentMethod(methods[i]);
        }
    }

    static void printPaymentMethod(PaymentMethod method) {
        System.out.println(method.displayName());
        System.out.println("Provider: " + method.getProvider());
        System.out.println("Online: " + method.canPayOnline());
        System.out.println("---");
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

    boolean canPayOnline() {
        return true;
    }

    String displayName() {
        return "Payment method " + id;
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
}
```

这段代码里，`PaymentMethod` 是父类，`CardPayment` 和 `WalletPayment` 是子类。两个子类都有支付方式的共同属性：`id` 和 `provider`；也各自有自己的字段：银行卡有尾号，钱包有账户名。

## `extends`：声明“子类继承父类”

```java
class CardPayment extends PaymentMethod {
    ...
}
```

`extends` 表示 `CardPayment` 继承 `PaymentMethod`。继承后，`CardPayment` 就是一种 `PaymentMethod`。

因此下面的写法成立：

```java
PaymentMethod card = new CardPayment("CARD-001", "Visa", "4242");
```

左边变量类型是父类，右边实际对象是子类。这是后面学习多态（polymorphism，多态）的基础。

Java 里一个类只能直接继承一个父类。也就是说，`class A extends B, C` 这种写法不存在。后面会用接口解决“一个类具备多种能力”的问题。

## `super(...)`：先初始化父类部分

```java
CardPayment(String id, String provider, String lastFourDigits) {
    super(id, provider);
    this.lastFourDigits = lastFourDigits;
}
```

子类对象里包含父类那部分状态。创建 `CardPayment` 时，要先把 `PaymentMethod` 需要的 `id` 和 `provider` 初始化好。

`super(id, provider)` 调用父类构造方法。它必须出现在子类构造方法的第一行，和上一节讲过的 `this(...)` 规则很像。

区别是：

- `this(...)` 调用同一个类里的其他构造方法。
- `super(...)` 调用父类构造方法。

## `private` 字段不会被子类直接访问

父类里有：

```java
private final String provider;
```

子类不能直接写：

```java
provider
```

因为 `private` 只允许在 `PaymentMethod` 类内部访问。子类想读取供应商名称，需要通过父类提供的方法：

```java
getProvider()
```

这点很关键：继承不等于子类可以随便碰父类内部所有东西。封装仍然存在。

## `@Override`：重写父类方法

父类提供默认展示名：

```java
String displayName() {
    return "Payment method " + id;
}
```

银行卡支付想展示成“Card Visa ending in 4242”，所以子类重写它：

```java
@Override
String displayName() {
    return "Card " + getProvider() + " ending in " + lastFourDigits;
}
```

`@Override` 是注解（annotation，注解）。它告诉编译器：我本来就想重写父类方法。如果方法名或参数写错，编译器会报错。

建议你以后重写方法时都写 `@Override`。它能避免很多“以为重写了，其实只是写了一个新方法”的低级 bug。

## 父类引用，执行子类行为

```java
static void printPaymentMethod(PaymentMethod method) {
    System.out.println(method.displayName());
    System.out.println("Provider: " + method.getProvider());
    System.out.println("Online: " + method.canPayOnline());
    System.out.println("---");
}
```

这个方法只声明自己需要一个 `PaymentMethod`。它不关心传进来的是银行卡还是钱包。

但当它调用：

```java
method.displayName()
```

如果实际对象是 `CardPayment`，就执行 `CardPayment` 的 `displayName()`；如果实际对象是 `WalletPayment`，就执行 `WalletPayment` 的 `displayName()`。

这就是继承配合方法重写带来的效果：调用方面向父类写代码，实际行为由子类决定。

## JavaScript 对比

JavaScript 也有 `extends` 和 `super`：

```js
class CardPayment extends PaymentMethod {
  constructor(id, provider, lastFourDigits) {
    super(id, provider);
    this.lastFourDigits = lastFourDigits;
  }
}
```

Java 和 JavaScript 在语法上有相似处，但 Java 的类型检查更严格。Java 会在编译阶段确认父类方法是否存在、重写签名是否匹配、构造方法是否正确调用。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l008 tracks/02-java-springboot/examples/L008-inheritance-extends/PaymentInheritanceDemo.java
java -cp /tmp/learn-step-java-l008 PaymentInheritanceDemo
```

已真实执行，输出如下：

```text
Card Visa ending in 4242
Provider: Visa
Online: true
---
Wallet OpenPay account frontend-user
Provider: OpenPay
Online: true
---
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

不要为了复用两个字段就急着继承。继承应该表达稳定的业务关系。比如“银行卡支付是一种支付方式”比较自然；但“订单和用户都有 id，所以订单继承用户”就明显不合理。

子类不能直接访问父类的 `private` 字段。需要父类提供方法，或者把字段设计成更合适的访问级别。初学阶段先坚持 `private` 字段 + 方法访问。

重写方法时尽量写 `@Override`。如果把 `displayName` 拼成 `displayname`，没有 `@Override` 时它会变成一个新方法；有 `@Override` 时编译器会提醒你写错了。

`super(...)` 必须放在子类构造方法第一行。如果父类没有无参构造方法，子类必须显式调用某个父类构造方法。

## 放到真实后端里看

继承在后端里常见，但要克制使用。比较适合继承的例子包括：

- 通用异常基类与具体业务异常。
- 通用响应类型与更具体的响应对象。
- 抽象支付方式与不同支付渠道。

但很多时候，组合（composition，组合）比继承更稳。例如订单包含一个支付方式，而不是订单继承支付方式：

```java
class Order {
    private PaymentMethod paymentMethod;
}
```

这就是“has-a”（有一个）关系。真实项目里，优先判断关系是“is-a”还是“has-a”，再决定是否继承。

## 练习

1. 新增 `BankTransferPayment extends PaymentMethod`，重写 `displayName()`。
2. 在 `WalletPayment` 中重写 `canPayOnline()`，让它返回 `true`，观察输出是否变化。
3. 尝试在 `CardPayment` 中直接访问父类 `provider` 字段，记录编译错误。
4. 故意把 `displayName` 写成 `displayname` 并保留 `@Override`，记录编译错误。

下一节会学习方法重写与多态：继续用父类引用和子类对象，把“同一个调用，不同行为”讲清楚。
