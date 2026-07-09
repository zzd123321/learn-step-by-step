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
