type OrderStatus = 'pending' | 'paid' | 'cancelled'

type Order = {
  id: number
  title: string
  status: OrderStatus
  total: number
}

function formatOrder(order: Order): string {
  const statusText = {
    pending: '待支付',
    paid: '已支付',
    cancelled: '已取消'
  }[order.status]

  return `订单 ${order.id}：${order.title}，${statusText}，金额 ${order.total}`
}

const order: Order = {
  id: 1001,
  title: 'Vue 3 进阶课',
  status: 'paid',
  total: 199
}

console.log(formatOrder(order))
console.log('运行时 typeof order.total =', typeof order.total)
