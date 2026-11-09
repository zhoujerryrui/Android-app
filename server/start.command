#!/bin/zsh
cd "${0:A:h}"
if [[ -z "$DEEPSEEK_API_KEY" ]]; then
  read -rs 'DEEPSEEK_API_KEY?请输入 DeepSeek API Key（隐藏输入，不保存）：'
  print
  export DEEPSEEK_API_KEY
fi
if [[ -z "$DEEPSEEK_API_KEY" ]]; then
  print '未输入密钥，已退出。'
  exit 1
fi
export DEEPSEEK_MODEL="${DEEPSEEK_MODEL:-deepseek-chat}"
"$HOME/Library/Android/sdk/platform-tools/adb" reverse tcp:8765 tcp:8765
exec python3 server.py
