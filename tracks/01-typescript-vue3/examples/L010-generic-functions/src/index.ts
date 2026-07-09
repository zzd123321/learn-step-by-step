type User = {
  id: number
  name: string
  role: 'admin' | 'editor' | 'viewer'
}

type Product = {
  id: number
  title: string
  price: number
}

type PageResult<T> = {
  list: T[]
  total: number
  page: number
  pageSize: number
}

function first<T>(items: T[]): T | undefined {
  return items[0]
}

function mapPage<T, U>(page: PageResult<T>, mapper: (item: T) => U): PageResult<U> {
  return {
    ...page,
    list: page.list.map(mapper)
  }
}

function getPageSummary<T>(page: PageResult<T>): string {
  return `第 ${page.page} 页，每页 ${page.pageSize} 条，共 ${page.total} 条`
}

const userPage: PageResult<User> = {
  list: [
    { id: 1, name: 'Alice', role: 'admin' },
    { id: 2, name: 'Bob', role: 'viewer' }
  ],
  total: 2,
  page: 1,
  pageSize: 10
}

const productPage: PageResult<Product> = {
  list: [
    { id: 101, title: 'TypeScript 小册', price: 69 },
    { id: 102, title: 'Vue 3 实战课', price: 199 }
  ],
  total: 2,
  page: 1,
  pageSize: 10
}

const firstUser = first(userPage.list)
const productTitles = mapPage(productPage, (product) => `${product.title}：¥${product.price}`)

console.log(firstUser ? `${firstUser.name} / ${firstUser.role}` : '没有用户')
console.log(getPageSummary(userPage))
console.log(productTitles.list.join('；'))

if (false) {
  const firstProduct = first(productPage.list)

  // @ts-expect-error: Product 没有 name 字段，泛型保留了具体元素类型。
  console.log(firstProduct?.name)

  const userNames = mapPage(userPage, (user) => user.name)

  // @ts-expect-error: 映射后 list 是 string[]，不是 User[]。
  const users: User[] = userNames.list

  console.log(users)
}
