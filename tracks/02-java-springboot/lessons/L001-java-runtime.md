# L001：Java 程序如何运行：从 `.java` 到 JVM，与 JavaScript 执行模型的差异

## 1. 学习目标

完成本节后，你应该能：

- 说清楚 Java 源码从 `.java` 到 `.class` 再到 JVM 运行的基本流程。
- 理解 JDK、JRE、JVM 各自负责什么。
- 用 `javac` 编译一个 Java 文件，并用 `java` 运行它。
- 从前端开发视角对比 Java 和 JavaScript 的执行模型差异。

## 2. 前置知识

你只需要具备：

- 能在终端执行命令。
- 理解 JavaScript 文件通常由浏览器或 Node.js 运行。
- 知道前端接口联调时，前端代码最终会向后端服务发送 HTTP 请求。

本节暂不要求理解 Spring Boot、Maven、类加载器或 JVM 内存模型。

## 3. Java 与 JavaScript 的对应关系及差异

| 视角 | Java | JavaScript |
| --- | --- | --- |
| 源码文件 | `.java` | `.js` |
| 运行前步骤 | 通常先用 `javac` 编译为 `.class` 字节码 | 通常直接交给浏览器或 Node.js 执行 |
| 运行环境 | JVM（Java Virtual Machine，Java 虚拟机） | JavaScript 引擎，例如 V8 |
| 类型检查 | 编译阶段会检查大量类型错误 | 多数类型错误在运行时暴露 |
| 入口函数 | `public static void main(String[] args)` | 浏览器脚本从文件顶层执行，Node.js 也从模块顶层执行 |
| 部署形态 | 常见为 jar 包、容器镜像、后端服务进程 | 常见为静态资源、Node.js 服务或前端构建产物 |

可以先粗略理解为：

- JavaScript 更像“把源代码交给运行环境，边解析边执行”。
- Java 更像“先把源代码翻译成 JVM 能读的字节码，再交给 JVM 执行”。

现代 JavaScript 引擎内部也会做即时编译优化，但作为学习入口，你先抓住“Java 有显式编译步骤”这个差异即可。

## 4. 概念解析与心智模型

### JDK、JRE、JVM

- JDK（Java Development Kit，Java 开发工具包）：给开发者用，包含 `javac` 编译器、`java` 命令和标准库等工具。
- JRE（Java Runtime Environment，Java 运行环境）：给运行 Java 程序用，包含 JVM 和运行所需类库。
- JVM（Java Virtual Machine，Java 虚拟机）：真正执行 `.class` 字节码的虚拟机器。

现在很多现代 JDK 发行版不再强调单独安装 JRE。作为开发者，你通常安装 JDK 即可。

### 从 `.java` 到运行

本节最小流程是：

1. 写源码：`HelloJvm.java`
2. 编译源码：`javac HelloJvm.java`
3. 得到字节码：`HelloJvm.class`
4. 启动 JVM：`java HelloJvm`
5. JVM 执行 `main` 方法

### 心智模型

可以把 Java 程序想成一个前后分工更明确的流水线：

- `javac` 像前端构建工具中的“编译步骤”，提前发现一部分错误。
- `.class` 像给 JVM 准备的中间产物，不是给人直接阅读的业务源码。
- `java` 命令像“启动运行时”，它会加载字节码并让 JVM 执行入口方法。

这和前端构建有一点相似：你写的是源代码，但线上运行的可能是构建后的产物。不同的是，Java 从语言设计上就把编译和运行分得非常清楚。

## 5. 可运行代码

文件位置：`tracks/02-java-springboot/examples/L001-java-runtime/HelloJvm.java`

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

## 6. 代码解释

```java
public class HelloJvm {
```

定义一个公开类 `HelloJvm`。这个类名要和文件名 `HelloJvm.java` 保持一致，这是 Java 初学时最常见的规则之一。

```java
public static void main(String[] args) {
```

定义程序入口方法。JVM 启动这个类时，会寻找这个 `main` 方法。

- `public`：公开，JVM 可以访问。
- `static`：静态方法，不需要先创建对象就能调用。
- `void`：没有返回值。
- `String[] args`：命令行参数数组，类似 Node.js 里可以从 `process.argv` 读取启动参数。

```java
String name = args.length > 0 ? args[0] : "frontend developer";
```

如果运行命令里传了第一个参数，就使用它；否则使用默认值。这里的三元表达式和 JavaScript 的 `condition ? a : b` 很像。

```java
System.out.println("Hello, " + name + "!");
```

向控制台打印文本。你可以暂时把 `System.out.println` 理解为 Java 里的 `console.log`。

```java
System.getProperty("java.vm.name")
System.getProperty("java.version")
```

读取当前 JVM 名称和 Java 版本，帮助我们确认程序确实运行在本机 Java 环境中。

## 7. 编译/运行命令

为了不把 `.class` 构建产物留在仓库里，本次验证把编译输出放到了临时目录：

```bash
javac -d /tmp/learn-step-java-l001 tracks/02-java-springboot/examples/L001-java-runtime/HelloJvm.java
java -cp /tmp/learn-step-java-l001 HelloJvm VueEngineer
```

命令含义：

- `javac`：调用 Java 编译器。
- `-d /tmp/learn-step-java-l001`：把编译出的 `.class` 文件放到临时目录。
- `java`：启动 JVM 运行程序。
- `-cp /tmp/learn-step-java-l001`：告诉 JVM 去哪里找 `.class` 文件。
- `HelloJvm`：要运行的类名。
- `VueEngineer`：传给 `main(String[] args)` 的第一个命令行参数。

## 8. 预期结果

运行后应该看到：

- 程序问候传入的名字。
- 程序说明源码文件名和编译后的字节码文件名。
- 程序打印当前 JVM 名称和 Java 版本。

## 9. 实际运行结果

已在本机真实执行编译和运行命令，结果如下：

```text
Hello, VueEngineer!
Java source file: HelloJvm.java
Compiled bytecode: HelloJvm.class
Runtime JVM: Java HotSpot(TM) 64-Bit Server VM
Java version: 25.0.3
```

验证结论：本节代码已通过本机 Java `25.0.3 LTS` 编译和运行验证。

## 10. 常见错误与排查方式

### 错误 1：文件名和 public 类名不一致

如果文件叫 `Hello.java`，但里面写 `public class HelloJvm`，编译会报错。Java 要求一个公开类的类名与文件名一致。

排查方式：

- 确认文件名是 `HelloJvm.java`。
- 确认代码里是 `public class HelloJvm`。

### 错误 2：找不到 main 方法

如果把 `main` 写成了 `public void main(String[] args)`，JVM 无法把它识别为标准入口。

排查方式：

- 确认入口完整写法是 `public static void main(String[] args)`。

### 错误 3：运行时找不到类

如果编译到了 `/tmp/learn-step-java-l001`，但运行时没有加 `-cp /tmp/learn-step-java-l001`，可能会出现找不到类的问题。

排查方式：

- 确认 `java -cp` 后面的目录就是 `.class` 所在目录。
- 运行类名写 `HelloJvm`，不要写 `HelloJvm.class`。

### 错误 4：把编译产物提交到 Git

`.class` 是编译产物，不是学习源码。后续真实项目中，`target/`、`build/` 等目录通常也不提交。

排查方式：

- 编译输出尽量放到临时目录或构建目录。
- 提交前检查 `git status`，不要把 `.class` 暂存进去。

## 11. 前端接口联调视角下的真实应用场景

当前端请求一个 Spring Boot 接口时，后端并不是“直接运行 Java 源码文件”。真实流程更接近：

1. 后端开发者写 Java 源码。
2. 构建工具把源码编译成 `.class`，再打包成 jar。
3. 服务器启动 JVM，运行这个 jar。
4. Spring Boot 在 JVM 中启动 Web 服务，监听端口。
5. 前端通过 HTTP 请求访问后端接口。

所以当前端看到“接口服务没启动”“端口没监听”“jar 启动失败”时，背后经常和 Java 编译、JVM 启动、配置加载有关。理解今天这条链路，会让你后面排查联调问题更有底气。

## 12. 小练习

1. 把运行命令最后的 `VueEngineer` 改成你的英文名或昵称，观察输出变化。
2. 不传命令行参数运行一次，观察默认值 `frontend developer` 是否生效。
3. 故意把文件里的 `HelloJvm` 改成 `HelloJava`，再编译，记录错误信息。

## 13. 复盘问题

1. JDK、JRE、JVM 分别负责什么？
2. 为什么 Java 程序通常需要先编译再运行？
3. `javac` 和 `java` 两个命令分别做了什么？
4. Java 的 `main(String[] args)` 和 Node.js 读取命令行参数有什么相似点？
5. 为什么不应该把 `.class` 文件提交到 Git？

## 14. 与真实全栈项目的联系

在真实后端项目中，`.java` 文件通常位于 `src/main/java` 下，构建工具会统一编译、测试、打包。你后面写的 Controller、Service、Repository，本质上也都会经历“源码编译成字节码，再由 JVM 运行”的过程。

从前端协作角度看，你不需要一开始掌握 JVM 的全部细节，但需要知道：后端接口服务是一个运行在 JVM 里的进程。接口返回慢、启动失败、配置不生效、依赖冲突，很多都要沿着“源码、编译、打包、JVM 启动、HTTP 服务”这条链路排查。

## 15. 下一节预告

下一节学习 L002：Java 基础语法、变量与类型。我们会从 JavaScript 的 `let`、`const`、动态类型出发，对比 Java 的基本类型、引用类型和显式类型声明。
