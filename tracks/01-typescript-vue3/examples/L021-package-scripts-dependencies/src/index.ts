type ScriptName = 'check' | 'build' | 'start' | 'verify'

type ProjectScript = {
  name: ScriptName
  command: string
  purpose: string
}

const scripts: ProjectScript[] = [
  {
    name: 'check',
    command: 'tsc --noEmit',
    purpose: '只做类型检查，不输出文件'
  },
  {
    name: 'build',
    command: 'tsc',
    purpose: '编译 TypeScript 到 dist'
  },
  {
    name: 'start',
    command: 'node dist/index.js',
    purpose: '运行编译后的 JavaScript'
  },
  {
    name: 'verify',
    command: 'npm run check && npm run build && npm run start',
    purpose: '串联本项目的基础验证流程'
  }
]

function formatScript(script: ProjectScript): string {
  return `${script.name}: ${script.command} -> ${script.purpose}`
}

console.log('脚本清单')
console.log(scripts.map(formatScript).join('\n'))

if (false) {
  const wrongScript: ProjectScript = {
    // @ts-expect-error: ScriptName 只允许项目已定义的脚本名。
    name: 'deploy',
    command: 'node deploy.js',
    purpose: '部署'
  }

  console.log(wrongScript)
}
