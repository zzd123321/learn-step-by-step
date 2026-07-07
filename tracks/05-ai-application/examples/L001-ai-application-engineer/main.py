from __future__ import annotations

import hashlib
import json
import os
import sys
from dataclasses import dataclass


DEFAULT_QUESTION = "AI 应用工程师和只调用聊天接口有什么区别？"


@dataclass(frozen=True)
class AppConfig:
    provider: str
    model: str
    max_input_chars: int


def load_config() -> AppConfig:
    return AppConfig(
        provider=os.getenv("AI_PROVIDER", "mock"),
        model=os.getenv("AI_MODEL", "mock-stable-v1"),
        max_input_chars=int(os.getenv("AI_MAX_INPUT_CHARS", "200")),
    )


def validate_user_input(question: str, max_chars: int) -> str:
    normalized = question.strip()
    if not normalized:
        raise ValueError("问题不能为空")
    if len(normalized) > max_chars:
        raise ValueError(f"问题过长，当前限制为 {max_chars} 个字符")
    return normalized


def build_messages(question: str) -> list[dict[str, str]]:
    return [
        {
            "role": "system",
            "content": (
                "你是一个 AI 应用开发教练。回答必须简洁，必须强调："
                "可靠系统、验证、观测、安全和成本边界。"
                "请返回 JSON，字段为 answer、confidence、citations。"
            ),
        },
        {"role": "user", "content": question},
    ]


class FakeLLMProvider:
    def complete(self, messages: list[dict[str, str]], model: str) -> str:
        user_message = messages[-1]["content"]
        if "忽略系统提示" in user_message:
            payload = {
                "answer": "我不能忽略系统边界。可靠 AI 应用需要遵守上层规则。",
                "confidence": 0.72,
                "citations": ["system_prompt"],
            }
        else:
            payload = {
                "answer": (
                    "AI 应用工程师不只是转发问题给大模型，而是把模型能力放进"
                    "一个可验证、可观测、可控成本、可保护数据的系统里。"
                ),
                "confidence": 0.86,
                "citations": ["local_fake_provider", model],
            }
        return json.dumps(payload, ensure_ascii=False)


def parse_ai_response(raw_text: str) -> dict:
    data = json.loads(raw_text)
    required_keys = {"answer", "confidence", "citations"}
    missing_keys = required_keys - set(data)
    if missing_keys:
        raise ValueError(f"模型输出缺少字段：{sorted(missing_keys)}")
    if not isinstance(data["citations"], list):
        raise ValueError("citations 必须是列表")
    return data


def evaluate_response(data: dict) -> dict:
    checks = {
        "has_answer": bool(data.get("answer")),
        "has_citations": bool(data.get("citations")),
        "confidence_in_range": 0 <= data.get("confidence", -1) <= 1,
    }
    return {**checks, "passed": all(checks.values())}


def redact_for_log(value: str) -> str:
    digest = hashlib.sha256(value.encode("utf-8")).hexdigest()[:12]
    return f"<redacted:{digest}>"


def run(question: str) -> dict:
    config = load_config()
    safe_question = validate_user_input(question, config.max_input_chars)
    messages = build_messages(safe_question)

    if config.provider != "mock":
        raise ValueError("本节只支持 AI_PROVIDER=mock，避免误调用真实 API")

    provider = FakeLLMProvider()
    raw_response = provider.complete(messages, config.model)
    parsed_response = parse_ai_response(raw_response)
    evaluation = evaluate_response(parsed_response)

    log_event = {
        "provider": config.provider,
        "model": config.model,
        "question": redact_for_log(safe_question),
        "evaluation": evaluation,
    }

    return {
        "log": log_event,
        "answer": parsed_response["answer"],
        "evaluation": evaluation,
    }


def main() -> None:
    question = " ".join(sys.argv[1:]) if len(sys.argv) > 1 else DEFAULT_QUESTION
    result = run(question)
    print("STRUCTURED_LOG", json.dumps(result["log"], ensure_ascii=False))
    print("ANSWER", result["answer"])
    print("EVALUATION", json.dumps(result["evaluation"], ensure_ascii=False))


if __name__ == "__main__":
    main()
