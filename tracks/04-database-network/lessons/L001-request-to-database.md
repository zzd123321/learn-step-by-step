# L001：浏览器一次请求到服务端数据库之间到底发生了什么

## 1. 学习目标

本节课只解决一个问题：当你在浏览器里点击按钮、前端调用接口、后端把数据写入数据库时，整条链路上每一层大概做了什么。

学完后你应该能：

- 用后端视角描述一次 HTTP 请求从浏览器到服务端应用，再到数据库落库的过程。
- 区分前端看到的“接口请求”和后端看到的“网络连接、路由、业务逻辑、SQL、事务、响应”。
- 运行一个最小 HTTP 服务，把 JSON 请求写入 SQLite 数据库。
- 用 `curl` 验证请求、响应和数据库查询结果。

## 2. 前置知识

你需要知道：

- 浏览器可以通过 `fetch`、Axios 或表单发起 HTTP 请求。
- HTTP 有请求方法，例如 GET 和 POST。
- JSON 是前后端常用的数据交换格式。

本节会出现这些英文术语：

- Client：客户端，发起请求的一方，例如浏览器。
- Server：服务端，接收请求并返回响应的一方。
- HTTP：超文本传输协议，用来描述请求和响应如何表达。
- SQL：结构化查询语言，用来操作关系型数据库。
- SQLite：一个轻量级关系型数据库，适合学习和本地示例。

## 3. 概念解析

一次“前端调用接口并写入数据库”的链路可以拆成 9 步：

1. 用户在浏览器页面触发动作，例如点击“保存”。
2. 前端代码组装 HTTP 请求，包括 URL、方法、请求头和请求体。
3. 浏览器先处理网络细节，例如 DNS 查询、TCP 连接、HTTPS 场景下的 TLS 握手。
4. 请求到达服务端监听的端口。
5. 服务端 Web 框架或 HTTP 服务器解析请求路径、方法、头部和请求体。
6. 路由把请求分配给对应处理函数。
7. 业务代码校验参数，并决定要执行哪条 SQL。
8. 数据库执行 SQL，把数据写入表。
9. 服务端把处理结果包装成 HTTP 响应返回给浏览器。

文字版系统链路图：

```text
浏览器页面
  -> 前端 fetch/Axios
  -> DNS/TCP/TLS
  -> HTTP 请求
  -> 服务端端口
  -> 路由处理函数
  -> 参数校验
  -> SQL INSERT
  -> SQLite 数据库表
  -> HTTP JSON 响应
  -> 浏览器拿到结果并更新页面
```

本节先不展开 DNS、TCP、TLS、索引和事务，只建立全局地图。后续课程会逐段放大。

## 4. 心智模型

你可以把一次请求理解成“带回执的快递”：

- 前端是寄件人：准备地址、包裹内容和联系方式。
- HTTP 是快递单格式：规定方法、路径、头部和正文怎么写。
- 网络是运输过程：负责把包裹送到服务端机器。
- 服务端路由是收件分拣：决定哪个处理函数处理这个请求。
- 业务代码是仓库工作人员：检查包裹是否合法，决定是否入库。
- 数据库是仓库货架：按照表结构保存数据。
- HTTP 响应是回执：告诉前端是否成功、失败原因是什么、生成了什么 ID。

前端视角和后端视角的差异：

| 观察点 | 前端视角 | 后端视角 |
| --- | --- | --- |
| 主要关注 | 请求是否发出、页面是否更新 | 请求是否到达、参数是否可信、数据是否正确落库 |
| 常见工具 | DevTools Network、控制台日志 | 服务日志、SQL 日志、数据库查询、反向代理日志 |
| 错误感知 | 看到 400、500、CORS、超时 | 看到路由不匹配、JSON 解析失败、SQL 失败、锁等待 |
| 数据信任 | 用户输入来自页面状态 | 所有外部输入都不可信，必须校验 |

## 5. 可运行代码

本节示例在 `examples/L001-request-to-database/` 下，包含：

- `app.py`：最小 HTTP 服务，使用 Python 标准库和 SQLite。
- `schema.sql`：建表 SQL。
- `requests.http`：HTTP 请求样例，可供编辑器 REST Client 插件参考。

核心流程：

```text
POST /api/events
  -> 读取 JSON 请求体
  -> 校验 action 字段
  -> 执行 INSERT
  -> 返回 {"id": 1, "action": "..."}
```

## 6. 代码逐行说明

`app.py` 的关键结构如下：

```python
DB_PATH = os.environ.get("DB_PATH", "/tmp/learn_l001_requests.sqlite3")
```

这行读取数据库文件位置。默认放在 `/tmp`，避免把数据库数据文件提交到仓库。

```python
def get_connection():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn
```

这里创建 SQLite 连接。`row_factory` 让查询结果可以像字典一样按字段名读取。

```python
def init_db():
    with get_connection() as conn:
        conn.executescript(SCHEMA_PATH.read_text(encoding="utf-8"))
```

服务启动时执行 `schema.sql`。`CREATE TABLE IF NOT EXISTS` 保证重复启动不会重复建表报错。

```python
class Handler(BaseHTTPRequestHandler):
```

这是 HTTP 请求处理器。每次请求到来时，Python 会根据方法调用 `do_GET` 或 `do_POST`。

```python
def handle_create_event(self):
```

这是写入数据库的核心处理函数：读取 JSON、校验字段、执行 `INSERT`、返回 JSON。

```python
cursor = conn.execute(
    "INSERT INTO request_events (action, source, payload) VALUES (?, ?, ?)",
    (action, source, json.dumps(event_payload, ensure_ascii=False)),
)
```

这里使用参数占位符 `?`，而不是拼接 SQL 字符串。这样可以避免最基础的 SQL 注入风险。

```python
def send_json(self, status, body):
```

这个方法统一返回 JSON 响应，并设置 `Content-Type: application/json; charset=utf-8`。

## 7. 运行方式

在仓库根目录运行：

```bash
python3 tracks/04-database-network/examples/L001-request-to-database/app.py
```

服务默认监听：

```text
http://127.0.0.1:8004
```

另开一个终端验证健康检查：

```bash
curl -i http://127.0.0.1:8004/health
```

发送一次模拟前端埋点请求：

```bash
curl -i -X POST http://127.0.0.1:8004/api/events \
  -H 'Content-Type: application/json' \
  -d '{"action":"click_save_button","source":"vue-page","payload":{"page":"/orders/new","component":"OrderForm"}}'
```

查询已经写入数据库的事件：

```bash
curl -i http://127.0.0.1:8004/api/events
```

也可以直接用 SQLite 查询：

```bash
sqlite3 /tmp/learn_l001_requests.sqlite3 'SELECT id, action, source FROM request_events;'
```

## 8. 预期结果

健康检查预期返回：

```http
HTTP/1.0 200 OK
Content-Type: application/json; charset=utf-8

{"status": "ok"}
```

POST 请求预期返回：

```http
HTTP/1.0 201 Created
Content-Type: application/json; charset=utf-8

{"id": 1, "action": "click_save_button", "source": "vue-page"}
```

查询事件列表预期看到：

```json
{
  "items": [
    {
      "id": 1,
      "action": "click_save_button",
      "source": "vue-page",
      "payload": {
        "page": "/orders/new",
        "component": "OrderForm"
      }
    }
  ]
}
```

如果你运行了本节验证命令，并看到类似结果，说明这条链路已经打通：

```text
HTTP 请求 -> Python 服务 -> SQL INSERT -> SQLite 表 -> HTTP 响应
```

## 9. 常见错误

### 端口被占用

现象：

```text
OSError: [Errno 48] Address already in use
```

排查路径：

1. 换一个端口启动：`PORT=8014 python3 .../app.py`
2. 或查找占用 8004 的进程。

### JSON 格式错误

现象：

```http
HTTP/1.0 400 Bad Request
{"error": "请求体必须是合法 JSON"}
```

排查路径：

1. 检查请求体是否使用双引号。
2. 检查 `Content-Type` 是否是 `application/json`。
3. 在浏览器 DevTools Network 里查看 Request Payload。

### 缺少 action 字段

现象：

```http
HTTP/1.0 422 Unprocessable Entity
{"error": "action 必须是非空字符串"}
```

排查路径：

1. 检查前端传参字段名是否写成了 `action`。
2. 检查字段值是否为空字符串。
3. 后端不要相信前端一定传对，必须校验。

### 数据库文件找不到

现象：

```text
sqlite3: Error: unable to open database
```

排查路径：

1. 先启动服务并发送一次 POST 请求。
2. 确认 `DB_PATH` 环境变量和查询路径一致。
3. 默认数据库路径是 `/tmp/learn_l001_requests.sqlite3`。

## 10. 小练习

1. 把 POST 请求里的 `source` 改成 `admin-panel`，再查询事件列表。
2. 故意去掉 `action` 字段，观察 HTTP 状态码和错误响应。
3. 增加一个新的 payload 字段，例如 `userId`，观察数据库里保存的 JSON。
4. 用 `PORT=8014` 启动服务，验证同一份代码可以通过环境变量改变配置。

## 11. 复盘问题

1. 为什么服务端不能相信前端传来的字段一定合法？
2. `POST /api/events` 和 `GET /api/events` 的职责分别是什么？
3. 为什么示例里用 SQL 参数占位符，而不是字符串拼接？
4. 如果浏览器显示请求超时，你会从哪几层开始排查？
5. 如果 POST 返回 201，但页面没有更新，问题更可能在前端还是后端？

## 12. 与真实全栈项目的联系

真实项目里，这条链路会更长，但结构类似：

```text
Vue 页面
  -> Axios 请求
  -> Nginx 反向代理
  -> Java Spring Boot / Python FastAPI
  -> 参数校验
  -> 业务 Service
  -> 数据库事务
  -> MySQL / PostgreSQL
  -> Redis 缓存更新或失效
  -> JSON 响应
```

与 Java 的关联：

- Java Spring Boot 中通常由 Controller 接收请求，Service 处理业务，Repository 或 Mapper 执行 SQL。
- 数据库连接通常来自连接池，例如 HikariCP。

与 Python 的关联：

- Python FastAPI 或 Django 中也有路由、请求体解析、参数校验、数据库访问这几层。
- 本节用标准库手写，是为了看清框架背后的最小链路。

与 AI 应用的关联：

- AI 应用也需要保存用户请求、对话记录、任务状态、模型响应和计费记录。
- 如果一个 AI 接口请求很慢，需要同时排查网络、服务端日志、数据库写入和外部模型 API 调用。

## 13. 下一步建议

下一节建议学习关系型数据库最基础的结构：表、行、列、主键、外键和约束。

你会从“请求能写入数据库”继续推进到“为什么数据库要这样设计表”，为后续 SQL、索引和事务打基础。
