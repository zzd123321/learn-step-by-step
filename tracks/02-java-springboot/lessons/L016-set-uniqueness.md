# L016：`Set`：用唯一性表达业务规则

上一节的 `List` 适合表示“有顺序、可重复”的列表，比如订单列表、评论列表、搜索结果。`Set` 则适合表示“不能重复”的一组值，比如标签、用户权限、黑名单用户 ID、已经处理过的订单号。

`Set` 的核心不是下标，也不是排序，而是唯一性。你往 `Set` 里添加重复元素时，集合不会增加新元素，`add` 方法会返回 `false`。

## 示例：标签、黑名单和权限去重

示例文件：[SetUniquenessDemo.java](/Users/zhuzhendong/Documents/前端转全栈/learn-step-by-step/tracks/02-java-springboot/examples/L016-set-uniqueness/SetUniquenessDemo.java)

```java
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class SetUniquenessDemo {
    public static void main(String[] args) {
        Set<String> tags = new LinkedHashSet<>();

        tags.add("java");
        tags.add("spring");
        boolean duplicateTagAdded = tags.add("java");
        tags.add("api");

        System.out.println("Tags: " + tags);
        System.out.println("Was duplicate tag added: " + duplicateTagAdded);
        System.out.println("Tag count: " + tags.size());
    }
}
```

这段代码里，第二次添加 `"java"` 时不会让集合变成 4 个元素。`duplicateTagAdded` 会是 `false`，表示这次添加没有改变集合。

## `Set` 和 `List` 的差异

`List` 关注列表语义：

```java
List<String> statuses = new ArrayList<>();
statuses.add("PAID");
statuses.add("PAID");
```

这表示两个位置上都出现了 `"PAID"`，重复是允许的。

`Set` 关注集合语义：

```java
Set<String> statuses = new HashSet<>();
statuses.add("PAID");
statuses.add("PAID");
```

这里最终只有一个 `"PAID"`。如果业务规则是“一个用户只能拥有一次同一个权限”，`Set` 比 `List` 更贴近问题本身。

## `HashSet` 与 `LinkedHashSet`

本节示例用了两种常见实现：

```java
Set<String> blockedUserIds = new HashSet<>();
Set<String> tags = new LinkedHashSet<>();
```

`HashSet` 基于哈希表实现，适合快速判断“是否存在”，例如：

```java
static boolean canComment(Set<String> blockedUserIds, String userId) {
    return !blockedUserIds.contains(userId);
}
```

这很像前端里用 `Set` 判断某个 ID 是否已经选中：

```js
const blockedUserIds = new Set(["u1001", "u1002"])
const canComment = !blockedUserIds.has("u1002")
```

`HashSet` 不保证遍历顺序。也就是说，你不能依赖它按添加顺序输出。

`LinkedHashSet` 也是 `Set`，但会保留插入顺序。示例里打印标签和权限时，为了让运行结果稳定可读，使用了 `LinkedHashSet`。

真实项目中可以这样选：

- 只关心唯一性和快速查找：优先考虑 `HashSet`。
- 既要唯一性，又希望按添加顺序展示：使用 `LinkedHashSet`。
- 需要排序：后面可以学习 `TreeSet`，但它涉及比较规则，本节先不展开。

## `add`、`contains` 和 `size`

`Set` 最常用的三个操作是：

```java
set.add(value);
set.contains(value);
set.size();
```

`add` 添加元素，并返回是否真的添加成功。这个返回值很有用：

```java
boolean added = tags.add("java");
```

如果 `added` 是 `false`，说明集合里已经有这个值。

`contains` 判断元素是否存在：

```java
blockedUserIds.contains("u1002")
```

在后端接口中，它常用于权限、黑名单、去重判断。比如“当前用户是否已经收藏过商品”“订单号是否已经处理过”“角色是否拥有某个权限”。

`size` 返回集合中唯一元素的数量。注意，它统计的是去重后的数量。

## 自定义对象：唯一性由 `equals` 和 `hashCode` 决定

字符串、整数这类标准类型已经定义好了相等规则，所以可以直接放进 `Set`。

但如果你把自己写的对象放进 `Set`，就要告诉 Java：什么样的两个对象算同一个业务对象。

示例中的权限对象是这样定义唯一性的：

```java
class RolePermission {
    private final String module;
    private final String action;

    RolePermission(String module, String action) {
        this.module = normalize(module);
        this.action = normalize(action);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof RolePermission)) {
            return false;
        }

        RolePermission permission = (RolePermission) other;
        return module.equals(permission.module) && action.equals(permission.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, action);
    }
}
```

这里的业务规则是：`module` 和 `action` 都相同，就算同一个权限。

构造方法里还做了标准化：

```java
this.module = normalize(module);
this.action = normalize(action);
```

所以 `"order" + "read"` 和 `"ORDER" + "READ"` 会被视为同一个权限。

`equals` 用于判断两个对象是否相等，`hashCode` 用于支持哈希集合快速定位。只重写 `equals` 而不重写 `hashCode`，会让 `HashSet`、`HashMap` 这类结构出现很难排查的行为问题。实际开发中，这两个方法通常成对出现。

## JavaScript 对比

JavaScript 的 `Set` 对基础值很直观：

```js
const tags = new Set()
tags.add("java")
tags.add("java")
console.log(tags.size) // 1
```

但对象去重时，JavaScript 默认按引用判断：

```js
new Set([{ id: 1 }, { id: 1 }]).size // 2
```

Java 也类似：如果自定义对象不重写 `equals` 和 `hashCode`，默认更接近“引用是否同一个对象”。这就是为什么本节的 `RolePermission` 必须明确写出相等规则。

从前端经验看，可以把 `equals` 理解为“对象去重时使用哪些字段作为 key”。比如你在 JS 里会用 `id` 做去重 key，在 Java 里就要把这个业务 key 写进 `equals` / `hashCode`。

## 编译和运行

```bash
javac -d /tmp/learn-step-java-l016 tracks/02-java-springboot/examples/L016-set-uniqueness/SetUniquenessDemo.java
java -cp /tmp/learn-step-java-l016 SetUniquenessDemo
```

已真实执行，输出如下：

```text
Tags: [java, spring, api]
Was duplicate tag added: false
Tag count: 3
Can u1002 comment: false
Can u2001 comment: true
Permission count: 2
Duplicate permission added: false
Permissions:
ORDER:READ
ORDER:WRITE
```

验证环境：本机 Java `25.0.3 LTS`，`javac 25.0.3`。

## 常见误区

不要用 `Set` 保存需要重复次数的数据。比如购物车里同一个商品买了 3 件，应该用数量字段表达，而不是把 3 个相同商品对象直接丢进 `Set`。

不要依赖 `HashSet` 的输出顺序。如果接口响应必须稳定排序，应该先明确排序规则，或使用能保证顺序的集合。

不要忘记自定义对象的 `equals` 和 `hashCode`。如果你希望两个权限对象按 `module + action` 去重，就必须把这条业务规则写出来。

不要把 `Set` 当成数组使用。`Set` 没有 `get(0)` 这种按下标读取的方法，因为它的核心不是位置，而是唯一性和存在性。

## 放到真实后端里看

在 Spring Boot 后端里，`Set` 经常出现在这些地方：

- 接收前端传来的标签 ID，先去重再查询数据库。
- 判断用户是否拥有某个权限码。
- 防止同一个订单号在一次批处理中被重复处理。
- 聚合接口返回数据时，去掉重复的分类、品牌或地区。

例如，一个角色权限接口可以在 Service 层使用：

```java
Set<RolePermission> permissions = new HashSet<>();
```

这样业务代码表达得很直接：一个角色拥有的是“一组不重复的权限”。

## 练习

1. 把 `tags` 从 `LinkedHashSet` 改成 `HashSet`，多运行几次，观察输出顺序是否仍然值得依赖。
2. 注释掉 `RolePermission` 的 `equals` 和 `hashCode`，再次运行，观察权限数量变化。
3. 给 `RolePermission` 新增 `resourceId` 字段，尝试让唯一性变成 `module + action + resourceId`。
4. 新增一个方法 `hasPermission(Set<RolePermission> permissions, String module, String action)`，判断角色是否拥有某个权限。

下一节会继续集合框架：学习 `Map`，理解键值对、按 key 查找，以及它和前端对象字典、接口数据归一化的关系。
