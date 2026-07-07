# L001：Java 程序如何运行：从 `.java` 到 JVM

Java 后端学习的第一步，不是先背语法，而是先知道一段 Java 代码到底如何变成一个正在运行的后端服务。本节只抓住一条主线：

```text
.java 源码 -> javac 编译 -> .class 字节码 -> JVM 加载并执行
```

这条链路以后会不断出现。Spring Boot 的 Controller、Service、Repository 最终也都要先变成 JVM 能执行的字节码，后端接口才可能启动并响应前端请求。

## Java 与 JavaScript 最大的第一层差异

你熟悉的 JavaScript 通常是把 `.js` 文件交给浏览器或 Node.js 运行。现代 JavaScript 引擎内部会做解析、优化和即时编译，但作为开发者，日常感知最强的是“写完代码，运行环境直接执行”。

Java 的开发体验更强调显式编译：

```text
HelloJvm.java --javac--> HelloJvm.class --java/JVM--> 程序运行
```

`.java` 是你写的源码，`.class` 是 JVM（Java Virtual Machine，Java 虚拟机）能识别的字节码。你平时不会手写 `.class`，它由 `javac` 生成。

三个名词先这样理解：

- JDK（Java Development Kit，Java 开发工具包）：开发者安装的工具包，包含 `javac`、`java` 和标准类库。
- JRE（Java Runtime Environment，Java 运行环境）：运行 Java 程序所需的环境，包含 JVM 和运行类库。现代开发通常直接安装 JDK。
- JVM（Java Virtual Machine，Java 虚拟机）：真正加载并执行 `.class` 字节码的运行时。

## 最小可运行示例

示例文件：[HelloJvm.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L001-java-runtime/HelloJvm.java)

```java
public class HelloJvm {
    public static void main(String[] args) {
        String name = args.length > 0 ? args[0] : "frontend developer";

        System.out.println("Hello, " + name + "!");
        System.out.println("Java source file: HelloJvm.java");
        System.out.println("Compiled bytecode: HelloJvm.class");
        System.out.println("Runtime JVM: " + System.getProperty("java.vm.name"));
        System.out.println("Java version: " + System.getProperty("java.version"));
    }
}
```

这段代码只有一个目标：让你看见 Java 程序的入口、命令行参数、控制台输出，以及当前 JVM 信息。

## 关键语句怎么读

`public class HelloJvm` 定义了一个公开类。当前文件名是 `HelloJvm.java`，所以公开类名也必须是 `HelloJvm`。这是 Java 的重要规则：一个源码文件里如果有 `public` 类，文件名必须和这个 `public` 类名一致。

`public static void main(String[] args)` 是本节程序的入口。JVM 启动 `HelloJvm` 这个类时，会寻找这个固定形状的 `main` 方法。

- `public` 表示这个方法可以被 JVM 从外部访问。
- `static` 表示不需要先创建对象，JVM 可以直接调用它。
- `void` 表示没有返回值。
- `String[] args` 是命令行参数数组，和 Node.js 里从 `process.argv` 读取启动参数有相似之处。

`args.length > 0 ? args[0] : "frontend developer"` 是三元表达式。它和 JavaScript 的 `condition ? a : b` 非常接近：如果启动命令传了参数，就使用第一个参数；否则使用默认字符串。

`System.out.println(...)` 可以暂时类比为 Java 里的 `console.log(...)`。它会把内容打印到终端，并自动换行。

`System.getProperty("java.vm.name")` 和 `System.getProperty("java.version")` 会读取 JVM 名称和 Java 版本。它们能帮你确认：这段程序不是“文档里说跑了”，而是真的在本机 Java 环境中运行了。

## 编译和运行

为了不把 `.class` 编译产物放进仓库，本课程把输出写到临时目录：

```bash
javac -d /tmp/learn-step-java-l001 tracks/02-java-springboot/examples/L001-java-runtime/HelloJvm.java
java -cp /tmp/learn-step-java-l001 HelloJvm VueEngineer
```

这里有两个命令，要分清职责：

- `javac` 是编译器，把 `.java` 编译成 `.class`。
- `-d /tmp/learn-step-java-l001` 指定 `.class` 输出目录。
- `java` 启动 JVM 运行程序。
- `-cp /tmp/learn-step-java-l001` 告诉 JVM 去哪里找 `.class` 文件。
- `HelloJvm` 是要运行的类名，不要写成 `HelloJvm.class`。
- `VueEngineer` 会进入 `main(String[] args)`，成为 `args[0]`。

已真实执行，输出如下：

```text
Hello, VueEngineer!
Java source file: HelloJvm.java
Compiled bytecode: HelloJvm.class
Runtime JVM: Java HotSpot(TM) 64-Bit Server VM
Java version: 25.0.3
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

最常见的第一个错误是文件名和公开类名不一致。例如文件叫 `Hello.java`，里面却写 `public class HelloJvm`，编译器会直接报错。Java 在编译阶段就会检查这类结构问题。

第二个常见错误是把入口方法写错。`main` 需要写成：

```java
public static void main(String[] args)
```

少了 `static`，或者参数类型不对，JVM 都无法把它当作标准入口。

第三个错误是运行类名写错。编译后生成的是 `HelloJvm.class`，但运行时命令写的是类名：

```bash
java -cp /tmp/learn-step-java-l001 HelloJvm
```

不是：

```bash
java -cp /tmp/learn-step-java-l001 HelloJvm.class
```

还有一个仓库卫生问题也要从第一课养成习惯：`.class` 是构建产物，不是学习源码，不应该提交到 Git。后面 Maven 或 Gradle 生成的 `target/`、`build/` 目录也同理。

## 放到真实后端里看

前端联调 Spring Boot 接口时，你访问的是一个正在 JVM 中运行的后端进程。它背后的路径通常是：

```text
Java 源码 -> 编译 -> 打包 jar -> 启动 JVM -> Spring Boot 监听端口 -> 前端发送 HTTP 请求
```

所以当你遇到“接口没启动”“端口没监听”“jar 启动失败”“类找不到”这类问题时，不要只从接口 URL 看问题，也要能回头检查编译、打包、JVM 启动和运行参数。

## 练习

1. 把运行命令最后的 `VueEngineer` 改成你的名字，观察输出变化。
2. 不传命令行参数运行一次，确认默认值 `frontend developer` 是否生效。
3. 故意把 `public class HelloJvm` 改成 `public class HelloJava`，重新编译，记录编译器错误。

下一节会进入 Java 变量与类型：从你熟悉的 `let`、`const` 出发，对比 Java 的显式类型声明、基本类型和引用类型。
