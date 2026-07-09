export function formatTotal(items: number[]): string {
  const total = items.reduce((sum, item) => sum + item, 0)
  return `total=${total}`
}
