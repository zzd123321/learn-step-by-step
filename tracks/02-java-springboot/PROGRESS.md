# Java / Spring Boot 学习进度

## 当前 lesson 编号

- L020

## 已讲授内容

- L001：Java 程序如何运行：从 `.java` 到 JVM，与 JavaScript 执行模型的差异
- L002：Java 基础语法、变量与类型
- L003：控制流与方法
- L004：数组与方法参数
- L005：类与对象入门
- L006：封装与访问控制
- L007：构造方法重载与 `this`
- L008：继承入门与 `extends`
- L009：方法重写与多态
- L010：抽象类
- L011：接口
- L012：包装类型、`String` 常用方法与空值边界
- L013：异常体系入门
- L014：泛型入门与 `ApiResponse<T>`
- L015：集合框架入门：从数组到 `List`
- L016：`Set`：用唯一性表达业务规则
- L017：`Map`：用 key 快速找到 value
- L018：`Queue`：先进先出的任务队列
- L019：集合框架选择边界：`List`、`Set`、`Map`、`Queue` 怎么选
- L020：Lambda 表达式和函数式接口：把行为传给方法

## 练习已完成

- 暂无

## 待练习内容

- L001：修改命令行参数，观察 `args` 如何影响输出。
- L001：尝试故意写错类名或文件名，记录编译错误信息。
- L002：把 `stock` 改成 `0`，观察 `available` 的输出。
- L002：尝试给 `stock` 赋值 `"12"`，记录编译错误。
- L002：尝试修改 `final double discountRate`，记录编译错误。
- L003：新增 `DELIVERED` 状态并输出交付文案。
- L003：修改 `shouldShowSupport`，让 `SHIPPED` 状态也显示客服入口。
- L003：传入自定义状态参数，观察大小写和空格如何被标准化。
- L004：新增 `min(double[] amounts)` 方法，返回最小金额。
- L004：把订单金额数组改为空数组，观察边界返回。
- L004：尝试制造数组下标越界错误，记录异常信息。
- L005：给 `Order` 新增 `isShipped()` 方法。
- L005：尝试让 `CREATED` 订单发货，观察状态是否变化。
- L005：尝试直接访问 `private` 字段，记录编译错误。
- L006：尝试在 `main` 中直接修改 `private` 字段，记录编译错误。
- L006：新增 `isCancelled()` 方法。
- L006：创建金额为 `0` 的订单，观察构造方法抛出的异常。
- L007：新增 `Order(String id)` 构造方法，默认金额为 `1.0`。
- L007：创建带空格和小写状态的订单，观察状态标准化。
- L007：故意把 `this(...)` 放到构造方法第二行，记录编译错误。
- L008：新增 `BankTransferPayment` 子类并重写展示名。
- L008：尝试在子类中直接访问父类 `private` 字段，记录编译错误。
- L008：故意写错重写方法名并保留 `@Override`，记录编译错误。
- L009：新增 `CouponPayment` 子类并重写手续费规则。
- L009：修改 `WalletPayment.calculateFee`，观察多态输出变化。
- L009：故意写错重写方法名并保留 `@Override`，记录编译错误。
- L010：新增 `BankTransferPayment` 子类，手续费为 `0`。
- L010：尝试直接创建抽象类，记录编译错误。
- L010：删除子类抽象方法实现，观察编译错误。
- L011：新增 `PushNotifier implements Notifier`。
- L011：让 `SmsNotifier.supportsRetry()` 返回 `false`。
- L011：尝试直接创建接口对象，记录编译错误。
- L012：修改 `MAX_SIZE`，观察分页大小限制。
- L012：新增 `sort` 参数并处理非法值。
- L012：删除 `null` 判断，观察空指针错误。
- L013：新增 `PermissionDeniedException` 并映射为 `FORBIDDEN`。
- L013：删除某个 `catch` 分支，观察未捕获异常。
- L013：比较“抛异常”和“返回 `null`”对调用方复杂度的影响。
- L014：新增 `UserProfile` 并创建 `ApiResponse<UserProfile>`。
- L014：尝试把 `ApiResponse<SearchPage>` 传给订单详情方法，记录编译错误。
- L014：尝试声明原始类型 `ApiResponse response`，观察编译警告。
- L015：新增一个 `CANCELLED` 订单，观察过滤结果。
- L015：新增 `sumAmount(List<OrderSummary> orders)` 方法。
- L015：在空列表上调用 `get(0)`，记录异常。
- L016：把 `tags` 从 `LinkedHashSet` 改成 `HashSet`，观察输出顺序。
- L016：注释掉 `RolePermission` 的 `equals` 和 `hashCode`，观察权限数量变化。
- L016：给 `RolePermission` 新增 `resourceId` 字段并参与唯一性判断。
- L016：新增 `hasPermission(Set<RolePermission> permissions, String module, String action)` 方法。
- L017：新增 `findOrderStatus(Map<String, OrderSummary> ordersById, String id)` 方法。
- L017：把替换 `A1002` 改成新增 `A2001`，观察 `previous` 是否为 `null`。
- L017：新增一个重复 ID 的订单，观察 `indexById` 最终保留哪一个。
- L017：使用 `entrySet()` 打印所有订单 ID 和订单状态。
- L018：在 `processAll` 前调用两次 `peek`，观察队列长度是否变化。
- L018：把 `poll` 改成 `remove`，在空队列时再调用一次，记录异常。
- L018：新增 `processOne(Queue<NotificationTask> tasks)`，每次只处理一个任务。
- L018：给 `NotificationTask` 增加 `retryCount` 字段，模拟失败后重新入队。
- L019：把 `LinkedHashSet` 改成 `HashSet`，观察响应顺序是否仍值得依赖。
- L019：新增一个重复订单到 `loadOrdersFromRepository()`，观察 `indexById` 最终保留哪一个。
- L019：把缺失 ID 收集到 `List<String> missingIds`，而不是直接跳过。
- L019：修改 `needsAudit()` 规则，观察任务队列数量变化。
- L020：新增一个筛选规则，筛选金额小于 `100.0` 的订单。
- L020：新增 `OrderFilter createdOrderFilter = order -> order.hasStatus("CREATED");` 并传给 `filterOrders`。
- L020：把 VIP 折扣改成多行 Lambda，并限制最高折扣 `20.0`。
- L020：给 `OrderFilter` 增加第二个抽象方法，观察 `@FunctionalInterface` 的编译错误。

## 当前状态

- 已完成 L001、L002、L003、L004、L005、L006、L007、L008、L009、L010、L011、L012、L013、L014、L015、L016、L017、L018、L019、L020 课程正文与最小可运行示例。
- L001 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L002 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L003 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证；默认状态和自定义参数均已执行。
- L004 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L005 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L006 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L007 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L008 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L009 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L010 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L011 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L012 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L013 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L014 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L015 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L016 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L017 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L018 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L019 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- L020 示例已使用本机 Java `25.0.3 LTS` 编译并运行验证。
- 已按新版课程输出规则重写 L001、L002 文档，去除冗长固定模板，改为围绕知识点展开的学习文档写法。
- 重写后已重新编译并运行 L001、L002 示例，验证结果与文档记录一致。
