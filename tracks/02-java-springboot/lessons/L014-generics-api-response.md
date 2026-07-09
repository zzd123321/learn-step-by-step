# L014：泛型入门与 `ApiResponse<T>`

后端接口的响应结构通常是固定的：

```json
{
  "success": true,
  "code": "OK",
  "message": "success",
  "data": {}
}
```

但 `data` 的具体内容会变化：订单详情接口返回订单对象，分页接口返回分页信息，失败响应可能没有数据。

泛型（generics，泛型）可以表达这种关系：外层结构固定，里面的数据类型由使用时决定。

## 示例：统一响应结构

示例文件：[GenericApiResponseDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L014-generics-api-response/GenericApiResponseDemo.java)

```java
public class GenericApiResponseDemo {
    public static void main(String[] args) {
        ApiResponse<OrderDetail> detailResponse = ApiResponse.success(
                new OrderDetail("A1001", "PAID", 99.90)
        );

        ApiResponse<SearchPage> pageResponse = ApiResponse.success(
                new SearchPage(1, 20, 58)
        );

        ApiResponse<Void> errorResponse = ApiResponse.fail(
                "ORDER_NOT_FOUND",
                "Order A404 was not found"
        );

        printOrderDetail(detailResponse);
        printSearchPage(pageResponse);
        printAnyResponse(errorResponse);
    }
}
```

这里有三个响应：

- `ApiResponse<OrderDetail>`：`data` 是订单详情。
- `ApiResponse<SearchPage>`：`data` 是分页信息。
- `ApiResponse<Void>`：失败响应没有数据。

同一个 `ApiResponse` 类，通过泛型参数承载不同数据类型。

## 定义泛型类

```java
class ApiResponse<T> {
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;
}
```

`T` 是类型参数。你可以先把它读成“将来再决定的类型”。

当写：

```java
ApiResponse<OrderDetail>
```

时，`T` 就是 `OrderDetail`，所以 `data` 的类型也是 `OrderDetail`。

当写：

```java
ApiResponse<SearchPage>
```

时，`T` 就是 `SearchPage`。

泛型的价值在于：编译器能知道 `data()` 返回什么类型，而不是一律返回 `Object` 让你自己猜。

## 泛型方法：`static <T>`

```java
static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, "OK", "success", data);
}
```

这里方法名前的 `<T>` 表示这是一个泛型方法。它会根据传入的 `data` 推断类型。

调用：

```java
ApiResponse.success(new OrderDetail("A1001", "PAID", 99.90))
```

时，Java 推断 `T` 是 `OrderDetail`，返回 `ApiResponse<OrderDetail>`。

`new ApiResponse<>(...)` 里的 `<>` 叫菱形语法（diamond operator，菱形操作符）。右边可以省略重复类型，让编译器从左边或上下文推断。

## `Void`：表示没有数据

```java
static ApiResponse<Void> fail(String code, String message) {
    return new ApiResponse<>(false, code, message, null);
}
```

失败响应没有 `data`，这里用 `Void` 表示“这个响应不携带数据”。它和 `void` 不一样：

- `void`：方法没有返回值。
- `Void`：一个类型，常用于泛型里表达“没有具体值”。

这里 `data` 实际上传的是 `null`。

## 类型安全：不同响应不能混用

```java
static void printOrderDetail(ApiResponse<OrderDetail> response) {
    OrderDetail detail = response.data();
    System.out.println(response.code() + " | order=" + detail.summaryLine());
}
```

这个方法明确要求 `ApiResponse<OrderDetail>`。因此传入 `ApiResponse<SearchPage>` 会编译失败。

这就是泛型提供的类型安全。你不需要在方法里强制转换，也不需要运行时才发现 `data` 不是订单详情。

## 通配符 `?`

```java
static void printAnyResponse(ApiResponse<?> response) {
    System.out.println(response.code() + " | message=" + response.message());
}
```

`?` 是通配符，表示“某种未知类型”。`ApiResponse<?>` 可以接收 `ApiResponse<OrderDetail>`、`ApiResponse<SearchPage>`、`ApiResponse<Void>`。

但因为它不知道 `data` 的具体类型，所以适合读取通用字段，比如 `code()`、`message()`，不适合直接处理具体业务数据。

## JavaScript / TypeScript 对比

JavaScript 没有编译期泛型，`data` 可以是任何东西：

```js
const response = { code: "OK", data: {} }
```

TypeScript 里的写法和 Java 很像：

```ts
type ApiResponse<T> = {
  success: boolean
  code: string
  message: string
  data: T
}
```

如果你熟悉 TypeScript 泛型，Java 泛型的心智模型会比较自然：都是“把类型作为参数传进去”，让容器或函数保留内部数据类型。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l014 tracks/02-java-springboot/examples/L014-generics-api-response/GenericApiResponseDemo.java
java -cp /tmp/learn-step-java-l014 GenericApiResponseDemo
```

已真实执行，输出如下：

```text
OK | order=A1001 status=PAID amount=99.9
OK | page=page=1, size=20, total=58
ORDER_NOT_FOUND | message=Order A404 was not found
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

不要为了省事使用原始类型：

```java
ApiResponse response = ApiResponse.success(...);
```

这样会丢失 `data` 的类型信息，编译器帮不上忙。

不要把 `ApiResponse<OrderDetail>` 当成 `ApiResponse<Object>`。Java 泛型默认不是这样协变的。后面集合章节会更细讲这个边界。

不要滥用 `ApiResponse<?>`。通配符适合处理通用字段，不适合处理具体 `data`。

## 放到真实后端里看

Spring Boot 项目里经常会看到统一响应类：

```java
class ApiResponse<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;
}
```

Controller 可以返回：

```java
ApiResponse<OrderDetailResponse>
ApiResponse<PageResult<OrderSummaryResponse>>
ApiResponse<Void>
```

这能让接口文档、前端类型、后端代码都更清楚：每个接口的 `data` 到底是什么。

## 练习

1. 新增 `UserProfile` 类，并创建 `ApiResponse<UserProfile>`。
2. 尝试把 `ApiResponse<SearchPage>` 传给 `printOrderDetail`，记录编译错误。
3. 新增 `static <T> ApiResponse<T> failWithData(String code, String message, T data)`。
4. 尝试声明原始类型 `ApiResponse response`，观察编译警告。

下一节会学习集合框架入门：从数组过渡到 `List`，理解动态列表和泛型如何一起工作。
