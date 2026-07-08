type OrderStatus = 'draft' | 'submitted' | 'cancelled'

type OrderDraft = {
  readonly id: number
  customer: string
  total: number
  status: OrderStatus
  couponCode?: string
}

type AuditFields = {
  createdAt: string
  updatedAt: string
}

type OrderRecord = OrderDraft & AuditFields

type SubmitSuccess = {
  ok: true
  order: OrderRecord
}

type SubmitFailure = {
  ok: false
  message: string
  field?: 'customer' | 'total' | 'status'
}

type SubmitResult = SubmitSuccess | SubmitFailure

function formatCoupon(order: OrderDraft): string {
  return order.couponCode ? `优惠码：${order.couponCode}` : '无优惠码'
}

function submitOrder(order: OrderDraft): SubmitResult {
  if (order.customer.trim() === '') {
    return {
      ok: false,
      message: '客户名称不能为空',
      field: 'customer'
    }
  }

  if (order.total <= 0) {
    return {
      ok: false,
      message: '订单金额必须大于 0',
      field: 'total'
    }
  }

  const now = '2026-07-08T10:00:00.000Z'

  return {
    ok: true,
    order: {
      ...order,
      status: 'submitted',
      createdAt: now,
      updatedAt: now
    }
  }
}

function formatSubmitResult(result: SubmitResult): string {
  if (result.ok) {
    return `提交成功：#${result.order.id} ${result.order.customer} ${formatCoupon(result.order)}`
  }

  return `提交失败：${result.message}`
}

const draft: OrderDraft = {
  id: 1001,
  customer: 'Alice',
  total: 199,
  status: 'draft',
  couponCode: 'VUE3'
}

const result = submitOrder(draft)

console.log(formatSubmitResult(result))

if (false) {
  // @ts-expect-error: readonly id 不能被重新赋值。
  draft.id = 2001

  // @ts-expect-error: paid 不是合法的 OrderStatus。
  draft.status = 'paid'

  // @ts-expect-error: OrderRecord 必须同时包含订单字段和审计字段。
  const missingAuditFields: OrderRecord = {
    id: 1002,
    customer: 'Bob',
    total: 99,
    status: 'submitted'
  }

  console.log(missingAuditFields)
}
