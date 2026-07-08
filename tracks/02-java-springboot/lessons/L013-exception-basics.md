# L013：异常体系入门

后端代码不会只处理“正常路径”。前端可能传空参数，用户可能查询不存在的订单，第三方服务也可能失败。Java 用异常（exception，异常）表达“当前流程无法按正常方式继续”。

本节不追求一次讲完整个异常体系，只先抓住接口开发最常见的链路：

```text
业务代码发现问题 -> throw 抛出异常 -> 上层 catch 捕获 -> 转成前端可理解的响应
```

## 示例：按订单 id 查询

示例文件：[ExceptionBasicsDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L013-exception-basics/ExceptionBasicsDemo.java)

```java
public class ExceptionBasicsDemo {
    public static void main(String[] args) {
        OrderService orderService = new OrderService();
        OrderController controller = new OrderController(orderService);

        String[] requestIds = {"A1001", "   ", "A404"};

        for (int i = 0; i < requestIds.length; i++) {
            ApiResponse response = controller.getOrder(requestIds[i]);
            System.out.println(response.summaryLine());
        }
    }
}
```

这里模拟三次接口请求：

- `A1001`：正常查到订单。
- 空白字符串：参数错误。
- `A404`：参数格式没问题，但订单不存在。

真实 Spring Boot 中，`OrderController` 会对应 Controller 层，`OrderService` 会对应 Service 层。现在先不用框架，只看异常如何流动。

## `throw`：发现问题时主动抛出

```java
private String normalizeOrderId(String rawOrderId) {
    if (rawOrderId == null || rawOrderId.isBlank()) {
        throw new InvalidRequestException("Order id must not be blank");
    }

    return rawOrderId.trim().toUpperCase();
}
```

`throw` 表示主动抛出异常。这里的规则是：订单 id 不能是 `null`，也不能只有空白。与其让后续逻辑拿着坏参数继续跑，不如在入口处明确失败。

订单不存在时也是类似：

```java
throw new OrderNotFoundException("Order " + orderId + " was not found");
```

这两种异常表达的是两类不同问题：

- `InvalidRequestException`：请求参数不合法。
- `OrderNotFoundException`：业务资源不存在。

区分异常类型，后面才能转换成不同错误码。

## 自定义异常

```java
class InvalidRequestException extends RuntimeException {
    InvalidRequestException(String message) {
        super(message);
    }
}
```

自定义异常也是类。这里让它继承 `RuntimeException`，表示它是运行时异常（runtime exception，运行时异常）。

`super(message)` 调用父类 `RuntimeException` 的构造方法，把错误信息交给父类保存。之后就可以用：

```java
exception.getMessage()
```

读取错误信息。

`OrderNotFoundException` 也是同样结构，只是表达另一种业务错误。

## `try/catch`：把异常转成响应

```java
ApiResponse getOrder(String rawOrderId) {
    try {
        Order order = orderService.findById(rawOrderId);
        return ApiResponse.success(order.summaryLine());
    } catch (InvalidRequestException exception) {
        return ApiResponse.fail("BAD_REQUEST", exception.getMessage());
    } catch (OrderNotFoundException exception) {
        return ApiResponse.fail("ORDER_NOT_FOUND", exception.getMessage());
    }
}
```

`try` 包住可能抛异常的代码。只要 `orderService.findById(...)` 正常返回，就走成功响应。

如果抛出 `InvalidRequestException`，第一个 `catch` 会捕获它，并返回：

```text
BAD_REQUEST
```

如果抛出 `OrderNotFoundException`，第二个 `catch` 会捕获它，并返回：

```text
ORDER_NOT_FOUND
```

这就是接口层常见职责：不要把 Java 异常栈直接丢给前端，而是转换成稳定的错误码和错误信息。

## 异常会中断当前正常流程

看 `findById`：

```java
Order findById(String rawOrderId) {
    String orderId = normalizeOrderId(rawOrderId);

    if ("A1001".equals(orderId)) {
        return new Order(orderId, "PAID", 99.90);
    }

    throw new OrderNotFoundException("Order " + orderId + " was not found");
}
```

如果 `normalizeOrderId` 抛出异常，后面的 `if` 不会继续执行，方法也不会正常返回 `Order`。异常会向上抛，直到被某一层 `catch` 捕获。

这和普通的 `return` 不同：`return` 是正常返回结果，`throw` 是告诉调用方“我无法正常完成”。

## 运行时异常和受检异常

Java 异常大致可以先分两类理解：

- 运行时异常：继承 `RuntimeException`，编译器不强制你捕获或声明。
- 受检异常（checked exception，受检异常）：编译器要求你处理，比如某些文件 I/O 异常。

本节自定义的两个异常都继承 `RuntimeException`。在业务参数校验、资源不存在、状态不允许这类场景中，运行时异常很常见。

受检异常后面讲文件 I/O 时会再展开。

## JavaScript 对比

JavaScript 也有 `throw` 和 `try/catch`：

```js
try {
  throw new Error("Order id must not be blank")
} catch (error) {
  return { code: "BAD_REQUEST", message: error.message }
}
```

Java 的不同在于：异常是类型系统的一部分。你可以定义 `InvalidRequestException`、`OrderNotFoundException` 这些具体类型，然后用不同 `catch` 分支处理。

这比只看错误字符串更可靠。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l013 tracks/02-java-springboot/examples/L013-exception-basics/ExceptionBasicsDemo.java
java -cp /tmp/learn-step-java-l013 ExceptionBasicsDemo
```

已真实执行，输出如下：

```text
success=true | code=OK | message=Order A1001 | status=PAID | amount=99.9
success=false | code=BAD_REQUEST | message=Order id must not be blank
success=false | code=ORDER_NOT_FOUND | message=Order A404 was not found
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

不要用返回 `null` 表达所有错误。`null` 只能说明“没有值”，但说不清是参数错、资源不存在，还是系统异常。

不要把异常吞掉：

```java
catch (Exception exception) {
}
```

这样调用方和用户都不知道发生了什么。

不要把所有异常都转成同一个错误码。参数错误、资源不存在、权限不足、系统错误应该区分。

不要把底层异常栈原样暴露给前端。前端需要稳定错误码和可理解信息，不需要看到服务器内部类名和堆栈。

## 放到真实后端里看

Spring Boot 中，Controller 通常不会在每个方法里写一堆 `try/catch`。更常见的是使用统一异常处理：

```java
@RestControllerAdvice
class GlobalExceptionHandler {
    // 把不同异常转换成统一响应
}
```

但统一异常处理的底层思想仍然是本节这条链路：

```text
业务层抛异常 -> Web 层捕获异常 -> 转成 HTTP 状态码和 JSON 响应
```

你以后联调接口时，如果看到响应：

```json
{
  "code": "ORDER_NOT_FOUND",
  "message": "Order A404 was not found"
}
```

背后很可能就是某个业务异常被统一转换后的结果。

## 练习

1. 新增 `PermissionDeniedException`，模拟无权限查询订单。
2. 在 `OrderController` 中新增对应 `catch`，返回 `FORBIDDEN`。
3. 删除 `OrderNotFoundException` 的 `catch`，观察程序运行时会发生什么。
4. 把空订单 id 的错误从异常改成返回 `null`，思考调用方会变复杂还是更简单。

下一节会学习泛型入门：为什么 `ApiResponse<T>` 可以让响应数据类型更清晰。
