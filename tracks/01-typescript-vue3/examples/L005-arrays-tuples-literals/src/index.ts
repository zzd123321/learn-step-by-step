type OrderStatus = 'pending' | 'paid' | 'cancelled'

type Order = {
  id: number
  customer: string
  status: OrderStatus
  total: number
}

type StatusOption = [OrderStatus, string]

const statusOptions: StatusOption[] = [
  ['pending', '待支付'],
  ['paid', '已支付'],
  ['cancelled', '已取消']
]

const orders: Order[] = [
  {
    id: 1001,
    customer: 'Alice',
    status: 'paid',
    total: 199
  },
  {
    id: 1002,
    customer: 'Bob',
    status: 'pending',
    total: 69
  },
  {
    id: 1003,
    customer: 'Cindy',
    status: 'cancelled',
    total: 299
  }
]

function getStatusLabel(status: OrderStatus): string {
  const option = statusOptions.find(([value]) => value === status)
  return option ? option[1] : '未知状态'
}

function filterOrdersByStatus(orderList: Order[], status: OrderStatus): Order[] {
  return orderList.filter((order) => order.status === status)
}

function formatOrderLines(orderList: Order[]): string[] {
  return orderList.map((order) => {
    const statusLabel = getStatusLabel(order.status)
    return `#${order.id} ${order.customer} ${statusLabel} ¥${order.total}`
  })
}

const paidOrders = filterOrdersByStatus(orders, 'paid')
const lines = formatOrderLines(paidOrders)

for (const line of lines) {
  console.log(line)
}

if (false) {
  // @ts-expect-error: finished 不是合法的订单状态。
  filterOrdersByStatus(orders, 'finished')

  // @ts-expect-error: StatusOption 必须是 [OrderStatus, string]。
  statusOptions.push(['paid', 1])
}
