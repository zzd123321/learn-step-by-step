# L013 条件类型、映射类型与 infer 入门

前面三节讲了泛型函数、泛型约束、泛型接口和泛型类型别名。现在我们第一次接触 TypeScript 里更“类型编程”的部分：条件类型、映射类型和 `infer`。

这节不追求把高级类型一次讲完，只建立一个可靠心智模型：

- 条件类型：在类型层面写 `if...else`。
- 映射类型：按对象的 key 批量生成新对象类型。
- `infer`：在条件类型中把某个内部类型“拆出来”。

官方参考：

- https://www.typescriptlang.org/docs/handbook/2/conditional-types.html
- https://www.typescriptlang.org/docs/handbook/2/mapped-types.html

## 为什么 Vue 2 前端开发者要学它

Vue 2 项目里经常有这样的隐性规则：

```js
const form = {
  id: { value: 1, touched: false },
  name: { value: 'Alice', touched: false }
}
```

你知道 `id.value` 应该是数字，`name.value` 应该是字符串，但 JavaScript 不会帮你维护这个关系。项目变大后，字段从接口 DTO、表单模型、展示模型、提交参数之间来回转换，一旦类型关系靠人工记忆，就很容易出现“字段名还在，字段值类型已经错了”的问题。

条件类型、映射类型和 `infer` 的价值就是：把这类“字段之间的类型关系”写进类型系统。

在 Vue 3 里，这会频繁出现在：

- 根据接口 DTO 生成表单状态。
- 根据组件 Props 生成内部状态。
- 从请求响应类型中提取 `data`。
- 给 Composable 设计返回值类型。
- 实现通用表格、筛选器、表单组件。

## 条件类型：类型层面的 if...else

条件类型的基本形式是：

```ts
type Result<T> = T extends SomeType ? TrueType : FalseType
```

可以把它读成：

> 如果 `T` 可以赋值给 `SomeType`，那么结果是 `TrueType`，否则是 `FalseType`。

注意，这里的 `extends` 不是类继承，而是“是否满足某个类型约束”。

例如：

```ts
type NullableText<T> = T extends string ? T | null : T
```

它的意思是：

- 如果 `T` 是字符串类型，就允许它为 `null`。
- 如果 `T` 不是字符串类型，就保持原样。

这类类型常用于接口数据到展示数据的转换。例如后端返回空字符串，但前端展示层希望把空文本统一成 `null`。

## 映射类型：遍历对象类型的每个 key

映射类型的基本形式是：

```ts
type NewType<T> = {
  [Key in keyof T]: SomeType<T[Key]>
}
```

可以把它读成：

> 遍历 `T` 的所有属性名 `Key`，并为每个属性生成一个新类型。

例如：

```ts
type FieldState<T> = {
  value: T
  touched: boolean
  error?: string
}

type FormState<TModel> = {
  [Key in keyof TModel]: FieldState<TModel[Key]>
}
```

如果原始模型是：

```ts
type User = {
  id: number
  name: string
}
```

那么 `FormState<User>` 等价于：

```ts
type UserForm = {
  id: FieldState<number>
  name: FieldState<string>
}
```

这里最关键的是 `TModel[Key]`。它表示“取出当前属性对应的值类型”。当 `Key` 是 `id`，它就是 `number`；当 `Key` 是 `name`，它就是 `string`。

## infer：从已有类型里拆出内部类型

`infer` 只能出现在条件类型中，用来临时声明一个要推断的类型变量。

```ts
type FieldValue<TField> = TField extends FieldState<infer TValue> ? TValue : never
```

可以把它读成：

> 如果 `TField` 是 `FieldState<某个值类型>`，就把这个值类型命名为 `TValue` 并返回；否则返回 `never`。

例如：

```ts
type EmailField = FieldState<string>
type EmailValue = FieldValue<EmailField>
```

`EmailValue` 最终就是 `string`。

`infer` 的常见用途不是“凭空创造类型”，而是“从一个已经包装过的类型中拿回里面那层类型”。这和你在运行时代码里从对象中取值很像，只是它发生在编译期。

## 可运行示例：从用户 DTO 生成表单状态

示例目录：

```text
tracks/01-typescript-vue3/examples/L013-conditional-mapped-infer/
```

`src/index.ts`：

```ts
type ApiUser = {
  id: number
  name: string
  email: string
  status: 'active' | 'disabled'
  tags: string[]
}

type FieldState<T> = {
  value: T
  touched: boolean
  error?: string
}

type FormState<TModel> = {
  [Key in keyof TModel]: FieldState<TModel[Key]>
}

type FieldValue<TField> = TField extends FieldState<infer TValue> ? TValue : never

type ValuesFromForm<TForm> = {
  [Key in keyof TForm]: TForm[Key] extends FieldState<infer TValue> ? TValue : never
}

type NullableTextModel<TModel> = {
  [Key in keyof TModel]: TModel[Key] extends string ? TModel[Key] | null : TModel[Key]
}

function createField<T>(value: T): FieldState<T> {
  return {
    value,
    touched: false
  }
}

function createUserForm(user: ApiUser): FormState<ApiUser> {
  return {
    id: createField(user.id),
    name: createField(user.name),
    email: createField(user.email),
    status: createField(user.status),
    tags: createField(user.tags)
  }
}

function extractFormValues<TModel>(form: FormState<TModel>): ValuesFromForm<FormState<TModel>> {
  const values = {} as ValuesFromForm<FormState<TModel>>

  for (const key of Object.keys(form) as Array<keyof TModel>) {
    values[key] = form[key].value as ValuesFromForm<FormState<TModel>>[typeof key]
  }

  return values
}

function toNullableTextUser(user: ApiUser): NullableTextModel<ApiUser> {
  return {
    ...user,
    email: user.email === '' ? null : user.email
  }
}

const user: ApiUser = {
  id: 1,
  name: 'Alice',
  email: 'alice@example.com',
  status: 'active',
  tags: ['vip', 'trial']
}

const form = createUserForm(user)
const restoredUser = extractFormValues(form)
const emailValue: FieldValue<typeof form.email> = form.email.value
const nullableTextUser = toNullableTextUser(user)

console.log(`表单字段：${Object.keys(form).join('、')}`)
console.log(`还原用户：${restoredUser.name} / ${restoredUser.email}`)
console.log(`邮箱字段值：${emailValue}`)
console.log(`可空文本状态：${nullableTextUser.status}`)

if (false) {
  const wrongForm: FormState<ApiUser> = {
    // @ts-expect-error: id 字段必须保存 number，不能保存 string。
    id: createField('1'),
    name: createField('Alice'),
    email: createField('alice@example.com'),
    status: createField('active'),
    tags: createField(['vip'])
  }

  // @ts-expect-error: form.email 的字段值类型是 string。
  const wrongEmailValue: FieldValue<typeof form.email> = 123

  console.log(wrongForm, wrongEmailValue)
}
```

## 代码解析

```ts
type ApiUser = {
  id: number
  name: string
  email: string
  status: 'active' | 'disabled'
  tags: string[]
}
```

`ApiUser` 模拟后端接口返回的用户 DTO。它不是表单状态，只是业务数据本身。

```ts
type FieldState<T> = {
  value: T
  touched: boolean
  error?: string
}
```

`FieldState<T>` 是单个表单字段的状态。`value` 保存真实字段值，`touched` 表示用户是否触碰过字段，`error` 表示校验错误。

这个类型本身不关心字段是数字、字符串还是数组，所以用泛型 `T`。

```ts
type FormState<TModel> = {
  [Key in keyof TModel]: FieldState<TModel[Key]>
}
```

这是映射类型。

`keyof TModel` 取出模型的所有属性名。对 `ApiUser` 来说，就是 `'id' | 'name' | 'email' | 'status' | 'tags'`。

`Key in keyof TModel` 遍历这些属性名。

`TModel[Key]` 取出当前字段的值类型：

- `TModel['id']` 是 `number`。
- `TModel['email']` 是 `string`。
- `TModel['tags']` 是 `string[]`。

因此 `FormState<ApiUser>` 会自动变成：

```ts
type UserForm = {
  id: FieldState<number>
  name: FieldState<string>
  email: FieldState<string>
  status: FieldState<'active' | 'disabled'>
  tags: FieldState<string[]>
}
```

这就是映射类型最常见的价值：字段名保持一致，但字段值被统一包了一层。

```ts
type FieldValue<TField> = TField extends FieldState<infer TValue> ? TValue : never
```

这是条件类型加 `infer`。

如果传入 `FieldState<string>`，`infer TValue` 会把 `string` 拆出来，所以结果是 `string`。

如果传入的不是 `FieldState<...>`，结果就是 `never`。`never` 在这里表示“这个类型没有合法结果”。

```ts
type ValuesFromForm<TForm> = {
  [Key in keyof TForm]: TForm[Key] extends FieldState<infer TValue> ? TValue : never
}
```

这里把映射类型和 `infer` 组合起来：遍历表单对象的所有字段，并从每个 `FieldState<T>` 里提取出 `T`。

如果输入是：

```ts
type UserForm = FormState<ApiUser>
```

那么 `ValuesFromForm<UserForm>` 会重新得到类似 `ApiUser` 的值对象类型。

```ts
type NullableTextModel<TModel> = {
  [Key in keyof TModel]: TModel[Key] extends string ? TModel[Key] | null : TModel[Key]
}
```

这是映射类型加条件类型：遍历所有字段，如果字段值是字符串，就允许它为 `null`；如果不是字符串，就保持原样。

真实项目中，这类类型常用于“后端 DTO 到前端展示模型”的轻量转换。

要注意一个边界：`'active' | 'disabled'` 这种字面量联合也属于字符串类型，所以 `status` 也会变成 `'active' | 'disabled' | null`。这不一定是你想要的结果，说明条件类型必须写得足够精确。

```ts
function createField<T>(value: T): FieldState<T> {
```

`createField` 是一个泛型函数。传入 `number`，返回 `FieldState<number>`；传入 `string[]`，返回 `FieldState<string[]>`。

```ts
function createUserForm(user: ApiUser): FormState<ApiUser> {
```

这个函数把接口用户转换成表单状态。返回类型 `FormState<ApiUser>` 会强制每个字段都使用正确的 `FieldState<T>`。

如果把 `id` 写成 `createField('1')`，TypeScript 会报错，因为 `id` 对应的字段值必须是 `number`。

```ts
function extractFormValues<TModel>(form: FormState<TModel>): ValuesFromForm<FormState<TModel>> {
```

这个函数做反向转换：从表单状态中提取原始值。

运行时代码只是遍历对象并读取 `.value`；类型层面则用 `ValuesFromForm<FormState<TModel>>` 表示“把每个字段里的值类型拆出来”。

```ts
const values = {} as ValuesFromForm<FormState<TModel>>
```

这里使用了类型断言。原因是 TypeScript 无法从一个空对象自动推导出“它最终会被循环填满所有 key”。这是运行时逐步构造对象时常见的限制。

类型断言不是让错误消失的万能胶。这里能接受它，是因为后面的循环确实来自同一个 `form` 对象，字段来源可控。

```ts
for (const key of Object.keys(form) as Array<keyof TModel>) {
```

`Object.keys` 在 JavaScript 中返回 `string[]`。但我们知道这些字符串来自 `form` 的 key，所以把它收窄为 `Array<keyof TModel>`。

这是 TypeScript 和 JavaScript 的一个重要差异：运行时 API 很通用，类型系统有时需要你把更具体的业务信息补回去。

```ts
const emailValue: FieldValue<typeof form.email> = form.email.value
```

`typeof form.email` 取到的是 `FieldState<string>`，`FieldValue<...>` 再把里面的 `string` 拆出来。因此 `emailValue` 的类型就是 `string`。

最后的 `if (false)` 不会在运行时执行，但 TypeScript 仍会检查里面的类型。`@ts-expect-error` 表示“下一行应该报错”。如果下一行没有报错，编译也会失败。这让反例也能参与验证。

## 运行与真实验证

在示例目录执行：

```bash
tsc --noEmit
tsc
node dist/index.js
sed -n '1,240p' dist/index.js
```

实际运行输出：

```text
表单字段：id、name、email、status、tags
还原用户：Alice / alice@example.com
邮箱字段值：alice@example.com
可空文本状态：active
```

## 常见误区

第一个误区：把条件类型理解成运行时判断。

```ts
type A = string extends string ? true : false
```

这段代码不会生成任何 JavaScript 判断。它只在编译期产生类型结果。

第二个误区：以为 `infer` 可以在任何地方使用。

```ts
type Wrong<T> = infer U
```

这是非法的。`infer` 必须放在条件类型的 `extends` 分支里。

第三个误区：映射类型会自动处理运行时数据。

```ts
type UserForm = FormState<ApiUser>
```

这只创建类型，不会创建真实对象。真实对象仍然要靠 `createUserForm(user)` 这样的运行时代码构造。

第四个误区：条件类型写得越宽越好。

`T extends string` 会匹配普通字符串，也会匹配字符串字面量联合。比如 `status: 'active' | 'disabled'` 也会被当成字符串处理。真实业务中，你可能需要更精确地区分“普通文本字段”和“枚举字段”。

## 一个小练习

在本节示例中新增一个 `ApiProduct`：

```ts
type ApiProduct = {
  id: number
  title: string
  price: number
  visible: boolean
}
```

然后完成三件事：

1. 用 `FormState<ApiProduct>` 创建商品表单。
2. 用 `extractFormValues` 还原商品值对象。
3. 故意把 `price` 写成字符串，并用 `@ts-expect-error` 验证 TypeScript 能发现错误。

如果你能完成这个练习，就说明你已经理解了“对象模型和表单状态之间的类型同步”。

## 真实业务场景

假设你在 Vue 3 后台系统里写一个用户编辑页：

- 后端接口返回 `ApiUser`。
- 页面里需要每个字段都有 `value`、`touched`、`error`。
- 提交前又要从表单状态还原成接口需要的 DTO。

不用映射类型时，你通常会手写一份 `UserForm`，再手写一份 `UserSubmitPayload`。字段一多，这两份类型很容易和 `ApiUser` 不同步。

使用这节的思路后：

```ts
type UserForm = FormState<ApiUser>
type UserSubmitPayload = ValuesFromForm<UserForm>
```

字段关系由 TypeScript 自动维护。以后 `ApiUser` 增加字段，表单类型会跟着变化；如果运行时代码忘了处理新增字段，编译器更容易提醒你。

## 本节复盘

你可以用下面几个问题检查自己：

1. 条件类型里的 `extends` 和类继承有什么不同？
2. `keyof T` 和 `T[Key]` 分别表示什么？
3. 为什么 `FormState<ApiUser>` 能保持字段名不变，但改变字段值类型？
4. `infer` 解决的是“包装类型”还是“原始对象”问题？
5. 为什么 `type UserForm = FormState<ApiUser>` 不会创建任何运行时对象？

下一节建议学习 L014：常用 Utility Types。`Partial`、`Pick`、`Omit`、`Record`、`Readonly` 这些工具类型本质上就是条件类型和映射类型的常见封装。
