export type OrderStatus = 'draft' | 'paid'

export type Order = {
  id: string
  amount: number
  status: OrderStatus
}

export function createOrder(id: string, amount: number): Order {
  if (id.trim() === '') {
    throw new Error('订单 id 不能为空')
  }

  if (amount <= 0) {
    throw new Error('订单金额必须大于 0')
  }

  return {
    id,
    amount,
    status: 'draft'
  }
}
