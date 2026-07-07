# L001：AI 应用工程师在做什么：从调用大模型到构建可靠系统

## 1. 学习目标

学完本节后，你应该能说明：

- 为什么 AI 应用开发不等于“把用户问题转发给大模型”。
- 一个最小 AI 应用系统通常包含哪些工程环节。
- 为什么要在真实模型 API 外面包一层 Provider 抽象。
- 如何用本地 Fake Provider 验证请求链路，而不暴露 API Key 或产生调用成本。

## 2. 前置知识

- 会运行 Python 文件。
- 知道前端会通过 HTTP 调用后端接口。
- 知道后端通常负责鉴权、参数校验、业务规则、日志和错误处理。
- 不要求已经掌握真实模型 API，本节不会调用外部服务。

## 3. 概念解析

### 术语定义

- LLM：Large Language Model，大语言模型。它根据输入文本预测并生成后续文本，可用于问答、总结、改写、分类、代码生成等任务。
- Prompt：提示词。发送给模型的指令、上下文、约束和示例。
- System Prompt：系统提示词。用于设定模型角色、任务边界、安全要求和输出格式。
- Provider：模型服务提供方或模型调用适配层，例如把业务代码和具体模型 API 隔离开。
- Fake Provider：伪造 Provider。本地模拟模型返回结果，用于学习、测试和离线开发。
- Structured Output：结构化输出。让模型按 JSON 等机器可解析格式返回，便于后端校验和前端渲染。
- Evaluation：评测。用规则、数据集或人工检查判断 AI 输出是否满足要求。
- Observability：可观测性。通过日志、指标和链路追踪理解系统运行状态。

### 原理与系统结构

一个真实 AI 应用请求，通常不是这样：

```text
用户问题 -> 大模型 -> 答案
```

更接近这样：

```text
用户问题
  -> 输入校验
  -> 业务上下文读取
  -> Prompt 组装
  -> 模型 Provider 调用
  -> 结构化结果解析
  -> 安全与质量评测
  -> 日志、成本和链路记录
  -> 返回前端
```

本节用一个本地示例模拟这条链路。它不联网、不安装 SDK、不需要 API Key，但保留了真实项目中最重要的工程骨架。

## 4. 心智模型

把大模型想成一个“能力很强但不稳定的文本推理组件”，而不是传统意义上完全可预测的函数。

传统函数更像：

```text
固定输入 -> 固定逻辑 -> 固定输出
```

AI 应用更像：

```text
输入 + 上下文 + Prompt + 模型参数 + 供应商状态 -> 概率性输出
```

所以 AI 应用工程师的工作，是在概率性能力外面加上工程护栏：

- 输入能不能进来？
- Prompt 是否清晰、可版本化、可测试？
- 输出能不能被程序解析？
- 失败时如何重试、降级或提示用户？
- 是否可能泄露敏感信息？
- 每次调用花了多少成本？
- 线上出问题时能不能复盘？

## 5. 可运行代码

代码目录：

```text
tracks/05-ai-application/examples/L001-ai-application-engineer/
```

核心文件：

- `main.py`：最小 AI 应用请求链路。
- `.env.example`：真实 API 场景下的环境变量示例，本节不会读取真实密钥。

`main.py` 做了六件事：

1. 校验用户输入。
2. 构造 system/user 消息。
3. 调用本地 Fake Provider。
4. 解析 JSON 结构化输出。
5. 执行最小评测。
6. 输出脱敏后的结构化日志和最终回答。

## 6. 代码逐行说明

下面按主要代码块解释，而不是机械解释每个标点。

```python
@dataclass
class AppConfig:
    provider: str
    model: str
    max_input_chars: int
```

`AppConfig` 表示应用配置。真实项目里这些值通常来自环境变量、配置中心或部署平台。本节默认使用 `mock`，避免调用真实 API。

```python
def load_config() -> AppConfig:
    return AppConfig(
        provider=os.getenv("AI_PROVIDER", "mock"),
        model=os.getenv("AI_MODEL", "mock-stable-v1"),
        max_input_chars=int(os.getenv("AI_MAX_INPUT_CHARS", "200")),
    )
```

`load_config` 从环境变量读取配置。这样未来替换真实 Provider 时，不需要把密钥或模型名写死在代码里。

```python
def validate_user_input(question: str, max_chars: int) -> str:
    normalized = question.strip()
    if not normalized:
        raise ValueError("问题不能为空")
    if len(normalized) > max_chars:
        raise ValueError(f"问题过长，当前限制为 {max_chars} 个字符")
    return normalized
```

输入校验是 AI 应用的第一道边界。即使模型很强，也不应该把任意输入直接送给模型。

```python
def build_messages(question: str) -> list[dict[str, str]]:
    return [
        {"role": "system", "content": "..."},
        {"role": "user", "content": question},
    ]
```

`build_messages` 把业务规则写进系统提示词，并把用户问题作为独立消息。真实 API 中也常见这种消息列表结构，但具体字段和参数必须以对应官方文档为准。

```python
class FakeLLMProvider:
    def complete(self, messages: list[dict[str, str]], model: str) -> str:
        ...
        return json.dumps(payload, ensure_ascii=False)
```

`FakeLLMProvider` 模拟模型服务。它返回 JSON 字符串，让我们能练习结构化解析、评测和日志，而不消耗真实模型费用。

```python
def parse_ai_response(raw_text: str) -> dict:
    data = json.loads(raw_text)
    required_keys = {"answer", "confidence", "citations"}
    ...
```

模型输出不能直接相信。即使你要求模型返回 JSON，也要在后端解析和校验字段。

```python
def evaluate_response(data: dict) -> dict:
    checks = {
        "has_answer": bool(data.get("answer")),
        "has_citations": bool(data.get("citations")),
        "confidence_in_range": 0 <= data.get("confidence", -1) <= 1,
    }
```

这是最小评测。真实项目会有更复杂的测试集、人工标注、自动评分、引用检查和线上监控。

```python
def redact_for_log(value: str) -> str:
    digest = hashlib.sha256(value.encode("utf-8")).hexdigest()[:12]
    return f"<redacted:{digest}>"
```

日志里不直接记录完整用户输入，而是记录哈希摘要。真实系统要根据业务场景决定哪些字段可以记录、保存多久、谁能访问。

## 7. 运行方式

在仓库根目录执行：

```bash
python3 tracks/05-ai-application/examples/L001-ai-application-engineer/main.py
```

也可以传入自己的问题：

```bash
python3 tracks/05-ai-application/examples/L001-ai-application-engineer/main.py "AI 应用为什么需要评测？"
```

本节示例不需要安装依赖，不需要 `.env`，不需要真实 API Key。

真实 API 场景中，只能创建本地 `.env`，不要提交到 Git：

```bash
cp tracks/05-ai-application/examples/L001-ai-application-engineer/.env.example tracks/05-ai-application/examples/L001-ai-application-engineer/.env
```

`.env.example` 只能放占位符，不能放真实密钥。

## 8. 预期结果

运行后会看到三段输出：

1. `STRUCTURED_LOG`：脱敏日志，展示 provider、model、输入摘要和评测结果。
2. `ANSWER`：模拟 AI 回答。
3. `EVALUATION`：最小评测结果。

本节已在本机 `Python 3.13.4` 下真实执行：

```text
STRUCTURED_LOG {"provider": "mock", "model": "mock-stable-v1", "question": "<redacted:...>", "evaluation": {"has_answer": true, "has_citations": true, "confidence_in_range": true, "passed": true}}
ANSWER AI 应用工程师不只是转发问题给大模型，而是把模型能力放进一个可验证、可观测、可控成本、可保护数据的系统里。
EVALUATION {"has_answer": true, "has_citations": true, "confidence_in_range": true, "passed": true}
```

注意：上面的哈希摘要会随输入内容变化。

## 9. 常见错误

- 只在前端直接调用模型 API：容易泄露 API Key，也难以统一鉴权、限流、日志和成本控制。
- 把 Prompt 当成临时字符串：后续很难测试、复盘和版本管理。
- 相信模型一定返回合法 JSON：模型可能返回多余文本、缺字段或格式错误。
- 没有评测就上线：无法判断改 Prompt、换模型或调参数后是否退化。
- 日志记录完整敏感输入：可能造成隐私和合规风险。

## 10. 小练习

1. 在 `validate_user_input` 中新增规则：如果输入包含 `sk-`，拒绝处理并提示“疑似密钥，禁止提交”。
2. 在 `evaluate_response` 中新增一项检查：回答中必须包含“系统”或“工程”。
3. 修改默认问题，观察结构化日志里的哈希摘要是否变化。

## 11. 复盘问题

- 为什么本节不用真实模型 API，也能学习 AI 应用工程？
- 为什么模型输出需要后端解析和校验？
- Provider 抽象解决了什么问题？
- 如果这个示例要接入 Vue 前端，哪些逻辑应该放后端，哪些逻辑可以放前端？
- 你认为一个 AI 应用上线前至少要做哪些验证？

## 12. 与真实全栈项目的联系

- 前端：负责输入框、流式展示、错误提示、引用展示和用户反馈收集，但不应保存真实 API Key。
- Python/FastAPI：适合作为 AI 服务层，封装模型 Provider、RAG、工具调用、评测和日志。
- Java 后端：适合承载账户、权限、订单、企业业务数据等稳定业务能力，并通过内部接口提供给 AI 服务。
- 数据库：保存会话、文档、向量索引元数据、评测集、调用记录和成本统计。
- 运维与安全：负责密钥管理、限流、审计、监控、告警和数据保留策略。

## 13. 下一步建议

下一节学习 LLM、Token、上下文窗口和消息结构。你会理解为什么同样的问题在不同上下文下会得到不同回答，以及为什么 Token 是成本、性能和体验的共同边界。

## 真实 API 时的环境变量说明与安全边界

本节不调用真实 API。如果未来把 `FakeLLMProvider` 替换为真实 Provider，需要至少遵守：

- API Key 只放在本地 `.env`、系统环境变量或密钥管理服务中。
- 仓库只提交 `.env.example`，不提交 `.env`。
- 后端调用模型 API，前端不直接持有密钥。
- 日志默认脱敏，不记录完整密钥、身份证、手机号、隐私文档等敏感内容。
- 为每次请求设置超时、重试上限、Token 预算和错误降级策略。

## 评测或验证方式

本节使用三类验证：

- 运行验证：实际执行 Python 示例，确认代码能运行。
- 结构验证：确认 Fake Provider 返回的 JSON 包含 `answer`、`confidence`、`citations`。
- 质量验证：确认回答非空、有引用、置信度在 `0` 到 `1` 之间。

这些验证还很简单，但它们已经体现了 AI 应用工程和普通 API Demo 的区别：输出不只要“看起来像答案”，还要能被系统检查。
