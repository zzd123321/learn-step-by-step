import { createOrder, type Order } from '../domain/order'

export function payOrder(id: string, amount: number): Order {
  const order = createOrder(id, amount)

  return {
    ...order,
    status: 'paid'
  }
}
