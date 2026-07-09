type LogLevel = 'info' | 'warn' | 'error'

type LogEntry = {
  level: LogLevel
  message: string
  time: string
}

class AuditLogger {
  public readonly serviceName: string
  private readonly entries: LogEntry[] = []

  constructor(serviceName: string) {
    if (serviceName.trim() === '') {
      throw new Error('服务名不能为空')
    }

    this.serviceName = serviceName
  }

  public info(message: string): void {
    this.write('info', message)
  }

  public warn(message: string): void {
    this.write('warn', message)
  }

  protected format(entry: LogEntry): string {
    return `[${entry.time}] [${entry.level.toUpperCase()}] ${this.serviceName}: ${entry.message}`
  }

  private write(level: LogLevel, message: string): void {
    this.entries.push({
      level,
      message,
      time: new Date('2026-07-09T10:00:00.000Z').toISOString()
    })
  }

  public flush(): string[] {
    return this.entries.map((entry) => this.format(entry))
  }
}

class HttpAuditLogger extends AuditLogger {
  public request(method: string, url: string): void {
    this.info(`${method.toUpperCase()} ${url}`)
  }

  public preview(entry: LogEntry): string {
    return this.format(entry)
  }
}

const logger = new HttpAuditLogger('user-service')
logger.request('get', '/api/users')
logger.warn('响应时间超过阈值')

console.log(`服务名：${logger.serviceName}`)
console.log(logger.flush().join('\n'))
console.log(
  logger.preview({
    level: 'info',
    message: '预览日志',
    time: '2026-07-09T10:05:00.000Z'
  })
)

if (false) {
  // @ts-expect-error: readonly 字段不能在构造函数之外重新赋值。
  logger.serviceName = 'order-service'

  // @ts-expect-error: private 字段只能在 AuditLogger 类内部访问。
  console.log(logger.entries)

  // @ts-expect-error: protected 方法只能在类内部或子类内部访问。
  console.log(logger.format({ level: 'info', message: '外部访问', time: 'now' }))
}
