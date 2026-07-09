type OrderStatus = 'draft' | 'submitted'

type OrderItem = {
  sku: string
  price: number
  quantity: number
}

class Order {
  id: string
  status: OrderStatus = 'draft'
  items: OrderItem[]

  constructor(id: string, items: OrderItem[] = []) {
    if (id.trim() === '') {
      throw new Error('订单 id 不能为空')
    }

    this.id = id
    this.items = items
  }

  addItem(item: OrderItem): void {
    if (item.quantity <= 0) {
      throw new Error('商品数量必须大于 0')
    }

    this.items.push(item)
  }

  getTotal(): number {
    return this.items.reduce((sum, item) => sum + item.price * item.quantity, 0)
  }

  submit(): void {
    if (this.items.length === 0) {
      throw new Error('空订单不能提交')
    }

    this.status = 'submitted'
  }
}

type OrderInstance = InstanceType<typeof Order>
type OrderConstructorArgs = ConstructorParameters<typeof Order>

function createEntity<TInstance, TArgs extends unknown[]>(
  Entity: new (...args: TArgs) => TInstance,
  ...args: TArgs
): TInstance {
  return new Entity(...args)
}

function formatOrder(order: OrderInstance): string {
  return `${order.id} / ${order.status} / ${order.items.length} 件 / ${order.getTotal()} 元`
}

const orderArgs: OrderConstructorArgs = [
  'order-1001',
  [{ sku: 'keyboard', price: 399, quantity: 1 }]
]

const order = createEntity(Order, ...orderArgs)
order.addItem({ sku: 'mouse', price: 129, quantity: 2 })
order.submit()

console.log(`Order 在运行时是：${typeof Order}`)
console.log(formatOrder(order))
console.log(`order instanceof Order：${order instanceof Order}`)

if (false) {
  // @ts-expect-error: 构造函数第一个参数必须是 string。
  const wrongOrder = new Order(1001)

  // @ts-expect-error: Order 实例必须拥有类定义中的方法。
  const fakeOrder: Order = {
    id: 'order-1002',
    status: 'draft',
    items: []
  }

  console.log(wrongOrder, fakeOrder)
}
