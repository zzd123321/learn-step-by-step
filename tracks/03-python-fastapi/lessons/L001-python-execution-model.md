# L001：Python 的执行模型与开发环境：它和 JavaScript 有哪些关键不同

## 1. 学习目标

本节只学习一个主知识点：Python 代码是如何被解释器执行的，以及它和你熟悉的 JavaScript 运行方式有什么关键不同。

学完后你应该能够：

- 知道 `python3 文件路径.py` 会发生什么。
- 理解 `__name__ == "__main__"` 为什么常出现在 Python 项目入口文件里。
- 区分“脚本入口文件”和“被导入的业务模块”。
- 初步知道后端项目为什么要拆分入口层和业务层。

## 2. 前置知识

你需要具备：

- 会在终端运行命令。
- 了解 JavaScript 中 `node index.js` 的基本执行方式。
- 知道前端项目里通常会把入口文件、组件、工具函数拆开维护。

本节不需要提前安装第三方包。已检查本机环境：

- Python：`3.13.4`
- pip：`25.1.1`
- `venv`：可用

本节示例只使用 Python 标准库，因此没有创建虚拟环境，也没有安装全局依赖。

## 3. JavaScript 对照表

| 主题 | JavaScript / Node.js | Python |
| --- | --- | --- |
| 执行命令 | `node index.js` | `python3 main.py` |
| 运行时 | Node.js 运行 JavaScript | Python 解释器运行 Python |
| 入口判断 | 常见于 `if (require.main === module)` 或 ESM 中比较入口 URL | `if __name__ == "__main__"` |
| 导入模块 | `import { fn } from "./service.js"` | `from user_service import fn` |
| 包管理 | npm / pnpm / yarn | pip / uv / poetry 等 |
| 隔离依赖 | `node_modules` 通常在项目内 | 虚拟环境常用 `.venv/` 隔离 |
| 类型系统 | TypeScript 是独立类型层 | Python 类型标注是运行时代码的一部分，但默认不强制校验 |

## 4. 概念解析

Python 是解释型语言。你执行：

```bash
python3 tracks/03-python-fastapi/examples/L001-python-execution-model/main.py Ada
```

解释器会从 `main.py` 第一行开始执行。遇到 `import` 时，它会加载被导入的模块；遇到函数定义时，它会创建函数对象，但不会自动运行函数体；只有真正调用函数时，函数体才会执行。

`__name__` 是 Python 给每个模块设置的特殊变量：

- 当前文件被直接运行时，`__name__` 的值是 `"__main__"`。
- 当前文件被其他文件导入时，`__name__` 通常是模块名。

所以这段代码的意思是：只有当前文件作为入口文件直接运行时，才调用 `main()`。

```python
if __name__ == "__main__":
    main()
```

这和后端工程很相关。FastAPI 项目里，入口文件负责启动应用，业务模块负责处理用户、订单、权限、AI 任务等具体逻辑。入口和业务拆开，测试和维护都会容易很多。

## 5. 心智模型

可以把 Python 程序想成一次“解释器沿着文件往下走”的过程：

1. 解释器先读取入口文件。
2. 顶层语句会立刻执行。
3. `import` 会加载其他模块。
4. `def` 只是登记函数，不等于执行函数。
5. `if __name__ == "__main__"` 是入口开关。

对比前端项目：`main.py` 有点像 `main.ts` 或 `index.js`，`user_service.py` 有点像 `services/user.ts`。入口文件组织流程，服务模块承载可复用业务逻辑。

## 6. 可运行代码

示例目录：

```text
tracks/03-python-fastapi/examples/L001-python-execution-model/
├── main.py
└── user_service.py
```

`main.py`：

```python
import platform
import sys

from user_service import build_welcome_message


def main() -> None:
    name = sys.argv[1] if len(sys.argv) > 1 else "前端同学"

    print(f"Python version: {platform.python_version()}")
    print(f"Executable: {sys.executable}")
    print(f"Module name: {__name__}")
    print(build_welcome_message(name))


if __name__ == "__main__":
    main()
```

`user_service.py`：

```python
DEFAULT_ROLE = "API 学习者"


def build_welcome_message(name: str) -> str:
    return f"你好，{name}！当前角色：{DEFAULT_ROLE}。"
```

## 7. 代码逐行说明

`main.py`：

- `import platform`：导入标准库 `platform`，用于读取 Python 版本信息。
- `import sys`：导入标准库 `sys`，用于读取命令行参数和解释器路径。
- `from user_service import build_welcome_message`：从业务模块导入函数。
- `def main() -> None:`：定义入口函数，`-> None` 表示这个函数不返回业务值。
- `sys.argv[1] if len(sys.argv) > 1 else "前端同学"`：如果命令行传了名字就使用传入值，否则使用默认值。
- `print(...)`：输出当前 Python 版本、解释器路径、模块名和欢迎语。
- `if __name__ == "__main__":`：判断当前文件是否被直接运行。
- `main()`：真正执行入口函数。

`user_service.py`：

- `DEFAULT_ROLE = "API 学习者"`：定义模块级常量，用于表示默认角色。
- `def build_welcome_message(name: str) -> str:`：定义一个可复用业务函数，参数 `name` 是字符串，返回值也是字符串。
- `return ...`：返回拼接好的欢迎语。

## 8. 运行方式

在仓库根目录执行：

```bash
python3 tracks/03-python-fastapi/examples/L001-python-execution-model/main.py Ada
```

也可以不传参数：

```bash
python3 tracks/03-python-fastapi/examples/L001-python-execution-model/main.py
```

## 9. 预期结果

传入 `Ada` 时，会看到类似输出：

```text
Python version: 3.13.4
Executable: /Library/Frameworks/Python.framework/Versions/3.13/bin/python3
Module name: __main__
你好，Ada！当前角色：API 学习者。
```

其中 `Executable` 的具体路径可能因你的机器安装方式不同而变化。

## 10. 实际验证结果

已在本机仓库根目录真实执行：

```bash
python3 tracks/03-python-fastapi/examples/L001-python-execution-model/main.py Ada
```

实际输出：

```text
Python version: 3.13.4
Executable: /Library/Frameworks/Python.framework/Versions/3.13/bin/python3
Module name: __main__
你好，Ada！当前角色：API 学习者。
```

验证结论：示例运行成功，退出码为 `0`。

## 11. 常见错误

### `python3: command not found`

说明系统没有找到 Python 解释器。先执行：

```bash
python --version
```

如果也不可用，需要安装 Python，但不要在项目中盲目安装全局依赖。

### `ModuleNotFoundError: No module named 'user_service'`

通常是运行位置或文件路径不对。请在仓库根目录执行完整命令，或者进入示例目录后执行：

```bash
python3 main.py Ada
```

### 中文输出乱码

现代 macOS 和大多数 Linux 终端默认支持 UTF-8。如果乱码，优先检查终端编码设置。

## 12. 小练习

请修改 `user_service.py`，新增一个函数：

```python
def build_api_path(resource: str) -> str:
    return f"/api/v1/{resource}"
```

然后在 `main.py` 里打印：

```text
API path: /api/v1/users
```

练习目标：体会“入口文件负责组织流程，服务模块负责业务函数”的拆分方式。

## 13. 复盘问题

- 为什么 Python 里常见 `if __name__ == "__main__"`？
- `def main()` 定义函数时，函数体会不会立刻执行？
- `main.py` 和 `user_service.py` 分别承担什么职责？
- 这个结构和你熟悉的 Vue / JavaScript 项目入口有什么相似点？

## 14. 与真实全栈项目的联系

在真实后端或 AI 应用中，我们经常会这样拆分：

- `main.py`：应用入口，负责启动服务。
- `services/`：业务逻辑，比如用户服务、订单服务、AI 任务服务。
- `schemas/`：请求和响应数据模型。
- `tests/`：测试入口和业务函数。

今天的示例很小，但它已经体现了后端工程的一个基本原则：入口文件要薄，业务逻辑要可复用。

## 15. 下一步建议

下一节建议学习：Python 变量与基础类型。我们会从字符串、数字、布尔值和 `None` 开始，并重点对比 JavaScript 的 `undefined`、`null`、动态类型和隐式转换。
