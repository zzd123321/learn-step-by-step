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
