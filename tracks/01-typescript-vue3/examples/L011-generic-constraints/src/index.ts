type User = {
  id: number
  name: string
  role: 'admin' | 'viewer'
}

type Product = {
  id: number
  title: string
  price: number
}

function indexById<T extends { id: number }>(items: T[]): Record<number, T> {
  const result: Record<number, T> = {}

  for (const item of items) {
    result[item.id] = item
  }

  return result
}

function pickField<T, K extends keyof T>(item: T, key: K): T[K] {
  return item[key]
}

function describeLength<T extends { length: number }>(value: T): string {
  return `长度是 ${value.length}`
}

const users: User[] = [
  { id: 1, name: 'Alice', role: 'admin' },
  { id: 2, name: 'Bob', role: 'viewer' }
]

const products: Product[] = [
  { id: 101, title: 'TypeScript 小册', price: 69 },
  { id: 102, title: 'Vue 3 实战课', price: 199 }
]

const usersById = indexById(users)
const productsById = indexById(products)

const firstUserName = pickField(users[0], 'name')
const firstProductPrice = pickField(products[0], 'price')

console.log(usersById[1].name)
console.log(productsById[102].title)
console.log(`${firstUserName} / ¥${firstProductPrice}`)
console.log(describeLength('TypeScript'))
console.log(describeLength(products))

if (false) {
  const tags = ['vue', 'typescript']

  // @ts-expect-error: string[] 没有 number 类型的 id 字段，不能用于 indexById。
  indexById(tags)

  // @ts-expect-error: User 没有 email 字段。
  pickField(users[0], 'email')

  // @ts-expect-error: number 没有 length 字段。
  describeLength(123)
}
