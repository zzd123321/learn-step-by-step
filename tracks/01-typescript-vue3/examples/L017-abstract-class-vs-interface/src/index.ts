type BusinessReport = {
  title: string
  rows: Array<Record<string, string | number>>
}

interface Notifier {
  send(message: string): void
}

class ConsoleNotifier implements Notifier {
  send(message: string): void {
    console.log(`通知：${message}`)
  }
}

class MemoryNotifier implements Notifier {
  private readonly messages: string[] = []

  send(message: string): void {
    this.messages.push(message)
  }

  all(): string[] {
    return [...this.messages]
  }
}

abstract class BaseReportExporter {
  constructor(public readonly fileExtension: string) {}

  public export(report: BusinessReport): string {
    const fileName = this.createFileName(report.title)
    const content = this.render(report)

    return `${fileName}\n${content}`
  }

  protected createFileName(title: string): string {
    const safeTitle = title.trim().toLowerCase().replaceAll(' ', '-')
    return `${safeTitle}.${this.fileExtension}`
  }

  protected abstract render(report: BusinessReport): string
}

class CsvReportExporter extends BaseReportExporter {
  constructor() {
    super('csv')
  }

  protected render(report: BusinessReport): string {
    const firstRow = report.rows[0]

    if (!firstRow) {
      return ''
    }

    const headers = Object.keys(firstRow)
    const lines = report.rows.map((row) => headers.map((header) => row[header]).join(','))

    return [headers.join(','), ...lines].join('\n')
  }
}

class TextReportExporter extends BaseReportExporter {
  constructor() {
    super('txt')
  }

  protected render(report: BusinessReport): string {
    return report.rows.map((row) => JSON.stringify(row)).join('\n')
  }
}

function publishReport(exporter: BaseReportExporter, notifier: Notifier, report: BusinessReport): string {
  const exported = exporter.export(report)
  notifier.send(`报表「${report.title}」已导出为 .${exporter.fileExtension}`)
  return exported
}

const report: BusinessReport = {
  title: 'User List',
  rows: [
    { id: 1, name: 'Alice', role: 'admin' },
    { id: 2, name: 'Bob', role: 'viewer' }
  ]
}

const memoryNotifier = new MemoryNotifier()
const csv = publishReport(new CsvReportExporter(), memoryNotifier, report)
const text = publishReport(new TextReportExporter(), new ConsoleNotifier(), report)

console.log(csv)
console.log(memoryNotifier.all().join('\n'))
console.log(text.split('\n')[0])

if (false) {
  // @ts-expect-error: 抽象类不能直接创建实例。
  const exporter = new BaseReportExporter('json')

  // @ts-expect-error: 子类必须实现抽象方法 render。
  class BrokenExporter extends BaseReportExporter {
    constructor() {
      super('broken')
    }
  }

  const badNotifier: Notifier = {
    // @ts-expect-error: Notifier 契约要求 send 方法，而不是 push 方法。
    push(message: string) {
      console.log(message)
    }
  }

  console.log(exporter, BrokenExporter, badNotifier)
}
