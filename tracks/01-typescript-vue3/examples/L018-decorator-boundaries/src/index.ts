type Order = {
  id: string
  amount: number
}

function loggedMethod<This, Args extends unknown[], Return>(
  originalMethod: (this: This, ...args: Args) => Return,
  context: ClassMethodDecoratorContext<This, (this: This, ...args: Args) => Return>
) {
  const methodName = String(context.name)

  function replacementMethod(this: This, ...args: Args): Return {
    console.log(`[log] enter ${methodName}`)
    const result = originalMethod.call(this, ...args)
    console.log(`[log] exit ${methodName}`)
    return result
  }

  return replacementMethod
}

function minAmount(limit: number) {
  return function <This>(
    originalMethod: (this: This, order: Order) => string,
    context: ClassMethodDecoratorContext<This, (this: This, order: Order) => string>
  ) {
    const methodName = String(context.name)

    function replacementMethod(this: This, order: Order): string {
      if (order.amount < limit) {
        throw new Error(`${methodName} 要求订单金额至少为 ${limit}`)
      }

      return originalMethod.call(this, order)
    }

    return replacementMethod
  }
}

class PaymentService {
  constructor(private readonly channel: string) {}

  @loggedMethod
  @minAmount(1)
  pay(order: Order): string {
    return `${this.channel}:${order.id}:${order.amount}`
  }
}

const service = new PaymentService('wechat')
const result = service.pay({ id: 'order-1001', amount: 99 })

console.log(`支付结果：${result}`)

try {
  service.pay({ id: 'order-1002', amount: 0 })
} catch (error) {
  console.log(`支付失败：${error instanceof Error ? error.message : '未知错误'}`)
}

if (false) {
  // @ts-expect-error: pay 的订单金额必须是 number。
  service.pay({ id: 'order-1003', amount: '99' })
}
