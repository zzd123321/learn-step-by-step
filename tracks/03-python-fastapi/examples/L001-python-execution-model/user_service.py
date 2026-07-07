DEFAULT_ROLE = "API 学习者"


def build_welcome_message(name: str) -> str:
    return f"你好，{name}！当前角色：{DEFAULT_ROLE}。"
