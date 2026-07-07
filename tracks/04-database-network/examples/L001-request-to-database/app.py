from http.server import BaseHTTPRequestHandler, HTTPServer
import json
import os
from pathlib import Path
import sqlite3


BASE_DIR = Path(__file__).resolve().parent
SCHEMA_PATH = BASE_DIR / "schema.sql"
DB_PATH = os.environ.get("DB_PATH", "/tmp/learn_l001_requests.sqlite3")
HOST = os.environ.get("HOST", "127.0.0.1")
PORT = int(os.environ.get("PORT", "8004"))


def get_connection():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


def init_db():
    with get_connection() as conn:
        conn.executescript(SCHEMA_PATH.read_text(encoding="utf-8"))


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/health":
            self.send_json(200, {"status": "ok"})
            return

        if self.path == "/api/events":
            self.handle_list_events()
            return

        self.send_json(404, {"error": "未找到接口"})

    def do_POST(self):
        if self.path == "/api/events":
            self.handle_create_event()
            return

        self.send_json(404, {"error": "未找到接口"})

    def handle_create_event(self):
        payload = self.read_json_body()
        if payload is None:
            return

        action = payload.get("action")
        if not isinstance(action, str) or not action.strip():
            self.send_json(422, {"error": "action 必须是非空字符串"})
            return

        source = payload.get("source", "browser")
        if not isinstance(source, str) or not source.strip():
            self.send_json(422, {"error": "source 必须是非空字符串"})
            return

        event_payload = payload.get("payload", {})
        if not isinstance(event_payload, dict):
            self.send_json(422, {"error": "payload 必须是对象"})
            return

        with get_connection() as conn:
            cursor = conn.execute(
                "INSERT INTO request_events (action, source, payload) VALUES (?, ?, ?)",
                (action.strip(), source.strip(), json.dumps(event_payload, ensure_ascii=False)),
            )

        self.send_json(
            201,
            {
                "id": cursor.lastrowid,
                "action": action.strip(),
                "source": source.strip(),
            },
        )

    def handle_list_events(self):
        with get_connection() as conn:
            rows = conn.execute(
                """
                SELECT id, action, source, payload, created_at
                FROM request_events
                ORDER BY id ASC
                """
            ).fetchall()

        items = []
        for row in rows:
            items.append(
                {
                    "id": row["id"],
                    "action": row["action"],
                    "source": row["source"],
                    "payload": json.loads(row["payload"]),
                    "created_at": row["created_at"],
                }
            )

        self.send_json(200, {"items": items})

    def read_json_body(self):
        content_length = int(self.headers.get("Content-Length", "0"))
        raw_body = self.rfile.read(content_length)

        try:
            return json.loads(raw_body.decode("utf-8") or "{}")
        except json.JSONDecodeError:
            self.send_json(400, {"error": "请求体必须是合法 JSON"})
            return None

    def send_json(self, status, body):
        data = json.dumps(body, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def log_message(self, format, *args):
        print("%s - %s" % (self.address_string(), format % args))


def main():
    init_db()
    server = HTTPServer((HOST, PORT), Handler)
    print(f"Listening on http://{HOST}:{PORT}")
    print(f"SQLite database: {DB_PATH}")
    server.serve_forever()


if __name__ == "__main__":
    main()
