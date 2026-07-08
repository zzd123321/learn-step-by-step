# L012：包装类型、`String` 常用方法与空值边界

前端调用接口时，很多参数先以字符串形式进入后端。比如分页查询：

```text
GET /orders?page=2&size=10&keyword=spring
```

到了 Java 后端，`page` 和 `size` 最终应该变成数字，`keyword` 要去掉多余空格，还要处理没传、传空字符串、传非法数字这些边界。

本节用一个小例子讲三个基础但非常高频的点：

- 基本类型 `int` 和包装类型 `Integer` 的区别。
- `String` 的 `trim()`、`isBlank()`、`isEmpty()`、`equalsIgnoreCase()`。
- 为什么接口参数处理时要先考虑 `null`。

## 示例：处理查询参数

示例文件：[RequestParameterDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L012-wrapper-string-null/RequestParameterDemo.java)

```java
public class RequestParameterDemo {
    public static void main(String[] args) {
        SearchRequest firstRequest = SearchRequest.fromRawParams(" 2 ", "10", "  spring boot ");
        SearchRequest secondRequest = SearchRequest.fromRawParams(null, "", " ALL ");
        SearchRequest thirdRequest = SearchRequest.fromRawParams("abc", "200", "   ");

        printRequest("first", firstRequest);
        printRequest("second", secondRequest);
        printRequest("third", thirdRequest);
    }

    static void printRequest(String label, SearchRequest request) {
        System.out.println(label + ": " + request.summaryLine());
    }
}
```

这里模拟三次请求：

- 第一次：参数都有值，但有多余空格。
- 第二次：`page` 没传，`size` 是空字符串，`keyword` 是特殊值 `ALL`。
- 第三次：`page` 是非法数字，`size` 超过上限，`keyword` 只有空白。

真实 Spring Boot 里，这些原始值可能来自 `@RequestParam`，本节先不用框架，只看 Java 语言层面的处理方式。

## `int` 和 `Integer`

```java
private final int page;
private final int size;
```

`int` 是基本类型，保存整数值，不能是 `null`。

```java
Integer parsedPage = parseInteger(rawPage);
```

`Integer` 是包装类型（wrapper type，包装类型），它是一个对象，可以保存整数，也可以是 `null`。

这就是两者最重要的第一层区别：

- `int`：一定有数字值，适合对象内部已经校验好的字段。
- `Integer`：可能有数字，也可能没有，适合表达“解析失败”或“参数缺失”。

本例中，原始字符串解析成功才返回 `Integer`；解析失败或没传就返回 `null`。等确定默认值以后，再放进最终的 `int page` 和 `int size`。

## 把字符串转成数字

```java
static Integer parseInteger(String rawValue) {
    if (rawValue == null || rawValue.isBlank()) {
        return null;
    }

    try {
        return Integer.valueOf(rawValue.trim());
    } catch (NumberFormatException exception) {
        return null;
    }
}
```

`Integer.valueOf(...)` 可以把字符串转成 `Integer`。例如 `"10"` 会变成数字 `10`。

但如果传入 `"abc"`，它会抛出 `NumberFormatException`。本节先用 `try/catch` 捕获异常，然后返回 `null`。异常体系后面会专门讲；当前先记住：外部传来的字符串不可信，转换数字必须考虑失败。

这里先判断：

```java
rawValue == null || rawValue.isBlank()
```

顺序不能随便换。如果 `rawValue` 是 `null`，先调用 `rawValue.isBlank()` 会直接空指针异常。`||` 有短路行为：左边已经为 `true` 时，右边不会执行。

## `String` 的几个常用方法

`trim()` 去掉字符串前后的空白：

```java
"  spring boot ".trim()
```

结果是 `"spring boot"`。它常用于处理用户输入或查询参数。

`isBlank()` 判断字符串是否为空白：

```java
"   ".isBlank()
```

结果是 `true`。空字符串 `""`、只有空格的字符串，也都会被认为是 blank。

`isEmpty()` 只判断长度是不是 `0`：

```java
"".isEmpty()
```

结果是 `true`，但 `"   ".isEmpty()` 是 `false`。所以处理输入时，`isBlank()` 往往比 `isEmpty()` 更贴近业务需要。

`equalsIgnoreCase()` 忽略大小写比较：

```java
"ALL".equalsIgnoreCase(keyword)
```

如果 `keyword` 是 `"all"`、`"All"`、`"ALL"`，都认为相等。本例把 `ALL` 视为“不筛选关键字”。

## 为什么常量字符串放左边

```java
if ("ALL".equalsIgnoreCase(keyword)) {
    return "";
}
```

这里把 `"ALL"` 放左边，是为了避免 `keyword` 可能为 `null` 时调用方法出错。

虽然本例中 `keyword` 已经来自 `rawKeyword.trim()`，不会是 `null`，但这个写法是后端代码里很常见的防御习惯。类似地，前面我们经常写：

```java
"PAID".equals(status)
```

而不是：

```java
status.equals("PAID")
```

## 默认值和边界值

```java
int safePage = parsedPage == null || parsedPage < 1 ? DEFAULT_PAGE : parsedPage;
int safeSize = parsedSize == null || parsedSize < 1 ? DEFAULT_SIZE : parsedSize;

if (safeSize > MAX_SIZE) {
    safeSize = MAX_SIZE;
}
```

这里的规则很像真实接口：

- `page` 没传、非法或小于 `1`，使用默认页码 `1`。
- `size` 没传、非法或小于 `1`，使用默认大小 `20`。
- `size` 太大时限制为 `100`，避免一次请求拉太多数据。

这就是后端接口保护自己的方式。前端可以传错，用户也可以手写 URL，后端不能假设参数永远合法。

## JavaScript 对比

JavaScript 里你可能会这样处理：

```js
const page = Number(query.page || 1)
```

这种写法很短，但要小心 `Number("abc")` 得到 `NaN`、空字符串和空格的行为也需要额外判断。

Java 的写法更啰嗦，但类型边界更明确：原始输入是 `String`，解析中间态是 `Integer`，最终业务字段是 `int`。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l012 tracks/02-java-springboot/examples/L012-wrapper-string-null/RequestParameterDemo.java
java -cp /tmp/learn-step-java-l012 RequestParameterDemo
```

已真实执行，输出如下：

```text
first: page=2, size=10, keyword=spring boot
second: page=1, size=20, keyword=<empty>
third: page=1, size=100, keyword=<empty>
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

不要把 `Integer` 当成一定有值。它可能是 `null`，直接拆箱成 `int` 时可能触发空指针异常。

不要在没判断 `null` 前调用字符串方法：

```java
rawValue.isBlank()
```

如果 `rawValue` 是 `null`，这会出错。

不要用 `==` 比较字符串内容。字符串内容比较用 `equals` 或 `equalsIgnoreCase`。

不要相信前端一定会传合法分页参数。接口要自己兜底。

## 放到真实后端里看

Spring Boot Controller 里常见这样的参数：

```java
@RequestParam(required = false) String page
@RequestParam(required = false) String keyword
```

真实项目通常会进一步用 DTO、参数校验注解和统一异常处理来管理这些边界。但底层思路和本节一样：外部输入先标准化，再进入业务逻辑。

以后你看到接口返回“分页大小过大”“参数格式错误”“关键字为空”这类行为时，可以回到本节的思路：字符串输入、类型转换、默认值、边界值、空值处理。

## 练习

1. 把 `MAX_SIZE` 改成 `50`，观察第三次请求输出。
2. 新增一个 `sort` 参数，只允许 `asc` 或 `desc`，非法时默认 `desc`。
3. 尝试让 `parseInteger` 直接返回 `int`，思考无法表达解析失败的问题。
4. 删除 `rawValue == null` 判断，传入 `null` 后观察错误。

下一节会学习异常体系入门：什么是异常、什么时候抛出、什么时候捕获，以及接口里如何把错误变成可理解的响应。
