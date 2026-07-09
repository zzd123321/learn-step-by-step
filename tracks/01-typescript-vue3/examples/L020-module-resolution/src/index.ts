import { payOrder } from './services/order-service'

const order = payOrder('order-1001', 99)

console.log(`订单：${order.id} / ${order.status} / ${order.amount}`)

if (false) {
  // @ts-expect-error: payOrder 的 amount 必须是 number。
  payOrder('order-1002', '99')
}
