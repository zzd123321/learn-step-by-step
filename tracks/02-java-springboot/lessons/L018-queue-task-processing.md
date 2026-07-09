# L018：`Queue`：先进先出的任务队列

`List` 关注顺序和可重复，`Set` 关注唯一性，`Map` 关注 key-value 查找。`Queue` 关注的是另一种业务语义：排队。

队列的典型规则是 FIFO（First In, First Out，先进先出）：先进入队列的元素先被取出。它很适合表达“任务按顺序等待处理”，比如发送通知、导出报表、处理订单事件。

## 示例：订单通知任务队列

示例文件：[QueueTaskProcessingDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L018-queue-task-processing/QueueTaskProcessingDemo.java)

```java
import java.util.ArrayDeque;
import java.util.Queue;

public class QueueTaskProcessingDemo {
    public static void main(String[] args) {
        Queue<NotificationTask> tasks = new ArrayDeque<>();

        tasks.offer(new NotificationTask("T1001", "A1001", "PAYMENT_SUCCESS"));
        tasks.offer(new NotificationTask("T1002", "A1002", "ORDER_CANCELLED"));
        tasks.offer(new NotificationTask("T1003", "A1003", "ORDER_SHIPPED"));

        System.out.println("Pending task count: " + tasks.size());
        System.out.println("Next task: " + tasks.peek().summaryLine());

        processAll(tasks);
    }
}
```

这段代码模拟一个后端任务队列：订单产生事件后，系统把通知任务放进队列，再由处理逻辑按顺序取出执行。

## `Queue` 是接口，`ArrayDeque` 是常用实现

```java
Queue<NotificationTask> tasks = new ArrayDeque<>();
```

这行代码和前面学过的集合写法一致：

- `Queue<NotificationTask>`：变量类型，表示“通知任务队列”。
- `new ArrayDeque<>()`：实际实现，适合做普通队列。

`ArrayDeque` 读作 array double-ended queue，中文常译为数组双端队列。它既能从队尾添加，也能从队头取出；本节只把它当普通 FIFO 队列使用。

普通业务代码里，如果你需要一个内存中的简单队列，`ArrayDeque` 往往比老的 `LinkedList` 更合适。后面进入并发模块时，还会学习线程安全队列和阻塞队列。

## `offer`：把任务放到队尾

```java
tasks.offer(new NotificationTask("T1001", "A1001", "PAYMENT_SUCCESS"));
```

`offer` 表示入队，把元素放到队尾。连续入队 3 个任务后，队列顺序是：

```text
T1001 -> T1002 -> T1003
```

按照 FIFO 规则，最先被处理的是 `T1001`。

你也可能见到 `add`：

```java
tasks.add(task);
```

对普通无容量限制的队列来说，`offer` 和 `add` 都能添加元素。但在有容量限制的队列里，`offer` 通常用返回值表示是否添加成功，而 `add` 失败时可能抛异常。写队列语义时，优先使用 `offer` 更清晰。

## `peek`：查看队首但不移除

```java
tasks.peek()
```

`peek` 返回队首元素，但不会把它从队列中删除。

这适合“先看看下一个任务是什么”的场景。比如监控系统想展示当前等待处理的第一个任务，但还没有真正执行它。

注意：如果队列为空，`peek` 返回 `null`。本节示例在已经入队 3 个任务后才调用 `peek`，所以可以直接读取 `summaryLine()`。真实业务中，如果队列可能为空，应先判断：

```java
NotificationTask next = tasks.peek();

if (next != null) {
    System.out.println(next.summaryLine());
}
```

## `poll`：取出队首并移除

处理所有任务的代码是：

```java
static void processAll(Queue<NotificationTask> tasks) {
    NotificationTask task = tasks.poll();

    while (task != null) {
        System.out.println("Processing: " + task.summaryLine());
        task = tasks.poll();
    }
}
```

`poll` 做两件事：

1. 返回当前队首元素。
2. 把这个元素从队列中移除。

当队列已经空了，`poll` 返回 `null`。所以这个循环会一直处理到没有任务为止。

这和 `List` 的遍历不同。遍历 `List` 通常不会改变原列表；而 `poll` 会消费队列里的元素。处理完成后，队列长度会变成 `0`。

## `poll` 和 `remove` 的差异

你也可能见到 `remove()`：

```java
tasks.remove();
```

它也会取出并移除队首元素，但空队列时会抛出异常。`poll()` 在空队列时返回 `null`。

对初学者和普通业务代码来说，`poll` 更容易写出可控的边界处理：

```java
NotificationTask task = tasks.poll();

if (task == null) {
    System.out.println("No task to process");
}
```

同理，`peek` 对应的“空队列抛异常版本”是 `element()`。本路线现阶段优先使用 `offer`、`peek`、`poll` 这组更温和的 API。

## JavaScript 对比

JavaScript 没有内置专门的 `Queue` 类型，常见写法是用数组模拟：

```js
const tasks = []
tasks.push(task)  // 入队
const next = tasks.shift() // 出队
```

`push` 把元素放到数组末尾，`shift` 从数组开头取出，整体上也是 FIFO。

Java 的 `Queue` 把这个意图表达得更明确：

```java
tasks.offer(task); // 入队
tasks.poll();      // 出队
tasks.peek();      // 查看队首
```

从代码语义上看，`Queue` 一眼就说明“这里不是随便访问数组，而是在排队处理任务”。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l018 tracks/02-java-springboot/examples/L018-queue-task-processing/QueueTaskProcessingDemo.java
java -cp /tmp/learn-step-java-l018 QueueTaskProcessingDemo
```

已真实执行，输出如下：

```text
Pending task count: 3
Next task: T1001 | order=A1001 | event=PAYMENT_SUCCESS
Processing: T1001 | order=A1001 | event=PAYMENT_SUCCESS
Processing: T1002 | order=A1002 | event=ORDER_CANCELLED
Processing: T1003 | order=A1003 | event=ORDER_SHIPPED
Pending task count after processing: 0
Polling empty queue: null
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

不要用 `Queue` 做随机访问。它没有 `get(0)` 这种核心用法，重点是入队、查看队首、出队。

不要忘记 `poll` 会移除元素。如果你只是想看下一个任务，不想消费它，用 `peek`。

不要在可能为空的队列上直接调用 `peek().summaryLine()`。空队列的 `peek` 返回 `null`，继续调用方法会触发空指针异常。

不要把本节的内存队列误解成完整消息队列。`ArrayDeque` 只存在于当前 JVM 内存里，程序退出就没有了；Redis、RabbitMQ、Kafka 这类外部消息队列还涉及持久化、确认、重试、消费者等机制，后面会单独学习。

## 放到真实后端里看

真实 Spring Boot 项目里，队列思想经常出现：

- 接口收到请求后，先把耗时任务排队，避免前端一直等待。
- 订单支付成功后，把短信、邮件、站内信通知拆成任务。
- 批量导入时，把每一行数据作为待处理任务。
- 系统内部需要按顺序处理某类事件。

本节示例还只是单线程内存队列。真正的生产系统通常不会只靠一个 `ArrayDeque` 扛异步任务，因为服务重启会丢数据，多实例部署也无法共享这个内存队列。但作为理解“任务排队”和“消费队列”的第一步，它足够小，也足够贴近后端心智模型。

## 练习

1. 在 `processAll` 前调用两次 `peek`，观察队列长度是否变化。
2. 把 `poll` 改成 `remove`，在空队列时再调用一次，记录异常。
3. 新增 `processOne(Queue<NotificationTask> tasks)`，每次只处理一个任务。
4. 给 `NotificationTask` 增加 `retryCount` 字段，模拟处理失败后重新 `offer` 回队尾。

下一节会对集合框架做一次小结：梳理 `List`、`Set`、`Map`、`Queue` 的选择边界，避免在真实接口代码里选错数据结构。
