type Product = {
  id: number
  name: string
  price: number
  inStock: boolean
  tags: string[]
}

type ProductCard = {
  title: string
  priceText: string
  stockText: string
  tagText: string
}

function formatPrice(price: number): string {
  return `¥${price.toFixed(2)}`
}

function formatStock(inStock: boolean): string {
  return inStock ? '有库存' : '缺货'
}

function buildProductCard(product: Product): ProductCard {
  const title = `${product.id}. ${product.name}`
  const priceText = formatPrice(product.price)
  const stockText = formatStock(product.inStock)
  const tagText = product.tags.length > 0 ? product.tags.join(' / ') : '暂无标签'

  return {
    title,
    priceText,
    stockText,
    tagText
  }
}

const products: Product[] = [
  {
    id: 1,
    name: 'Vue 3 实战课',
    price: 199,
    inStock: true,
    tags: ['frontend', 'vue']
  },
  {
    id: 2,
    name: 'TypeScript 入门手册',
    price: 69,
    inStock: false,
    tags: []
  }
]

const cards = products.map(buildProductCard)

for (const card of cards) {
  console.log(`${card.title} | ${card.priceText} | ${card.stockText} | ${card.tagText}`)
}

if (false) {
  // @ts-expect-error: price 必须是 number，不能传 string。
  formatPrice('199')

  // @ts-expect-error: Product 缺少 tags 字段。
  buildProductCard({
    id: 3,
    name: 'Node.js 小课',
    price: 99,
    inStock: true
  })
}
