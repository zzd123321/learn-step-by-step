type SearchParams = {
  keyword: string
  page: number
  pageSize: number
}

type SearchResult = {
  query: string
  summary: string
}

function normalizeKeyword(keyword: string) {
  return keyword.trim().toLowerCase()
}

function createSearchQuery(params: SearchParams): string {
  const keyword = normalizeKeyword(params.keyword)
  const safePage = Math.max(params.page, 1)
  const safePageSize = Math.min(Math.max(params.pageSize, 1), 100)

  return `keyword=${keyword}&page=${safePage}&pageSize=${safePageSize}`
}

function buildSearchResult(params: SearchParams): SearchResult {
  const query = createSearchQuery(params)
  const summary = `搜索“${params.keyword}”，第 ${params.page} 页，每页 ${params.pageSize} 条`

  return {
    query,
    summary
  }
}

const defaultPageSize = 20
const params: SearchParams = {
  keyword: ' Vue 3 ',
  page: 1,
  pageSize: defaultPageSize
}

const result = buildSearchResult(params)

console.log(result.query)
console.log(result.summary)

if (false) {
  // @ts-expect-error: page 必须是 number，不能传 string。
  createSearchQuery({ keyword: 'vue', page: '1', pageSize: 20 })

  // @ts-expect-error: 缺少 pageSize，不能当作 SearchParams 使用。
  buildSearchResult({ keyword: 'vue', page: 1 })
}
