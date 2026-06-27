#!/data/data/com.termux/files/usr/bin/sh
set -eu

base=$(sed -n '1p' "$HOME/.launcherctl/endpoint")
token=$(cat "$HOME/.launcherctl/token")
auth="Authorization: Bearer $token"

echo "OpenAI model discovery with client query"
curl --fail-with-body -sS -H "$auth" "$base/v1/models?client_version=smoke" |
  jq '{models: [.data[].id]}'

echo "Ollama discovery"
curl --fail-with-body -sS -H "$auth" "$base/api/version" | jq -e '.version'
curl --fail-with-body -sS -H "$auth" "$base/api/tags" | jq -e '.models | type == "array"'

model=$(curl --fail-with-body -sS -H "$auth" "$base/v1/models" |
  jq -r '[.data[] | select(._capabilities | index("text_chat"))][0].id // empty')
[ -n "$model" ] || { echo "No installed chat model; discovery checks passed."; exit 0; }

echo "OpenAI Responses"
curl --fail-with-body -sS -H "$auth" -H 'Content-Type: application/json' \
  --data "{\"model\":\"$model\",\"input\":\"Reply with READY\",\"max_output_tokens\":16}" \
  "$base/v1/responses" | jq -e '.object == "response" and (.output | type == "array")'

echo "Ollama chat"
curl --fail-with-body -sS -H "$auth" -H 'Content-Type: application/json' \
  --data "{\"model\":\"$model\",\"messages\":[{\"role\":\"user\",\"content\":\"Reply with READY\"}],\"stream\":false}" \
  "$base/api/chat" | jq -e '.done == true and .message.role == "assistant"'

echo "Client configuration generation"
for client in aichat opencode crush codex ollama; do
  launcherctl client-config "$client" >/dev/null
done

echo "Local AI compatibility smoke checks passed."
