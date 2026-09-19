#!/usr/bin/env node

import {closeSync, existsSync, openSync, readSync, readdirSync, statSync} from "node:fs";
import {homedir} from "node:os";
import {join} from "node:path";

const sessionsRoot = process.env.CODEX_SESSIONS_ROOT || join(homedir(), ".codex", "sessions");
const threadId = process.env.CODEX_THREAD_ID;
const minimumRemaining = Number(process.env.CODEX_MIN_REMAINING_PERCENT || "15");
const tailBytes = Number(process.env.CODEX_QUOTA_TAIL_BYTES || String(8 * 1024 * 1024));

function listJsonlFiles(root) {
  if (!existsSync(root)) return [];
  const files = [];
  const stack = [root];
  while (stack.length > 0) {
    const current = stack.pop();
    for (const entry of readdirSync(current)) {
      const path = join(current, entry);
      const stat = statSync(path);
      if (stat.isDirectory()) stack.push(path);
      else if (entry.endsWith(".jsonl")) files.push(path);
    }
  }
  return files;
}

function parseSamples(text) {
  const samples = [];
  for (const line of text.split("\n")) {
    if (!line.includes('"type":"token_count"') || !line.includes('"rate_limits"')) continue;
    try {
      const event = JSON.parse(line);
      const primary = event?.payload?.rate_limits?.primary;
      if (!primary || !Number.isFinite(primary.used_percent)) continue;
      samples.push({
        timestamp: event.timestamp,
        usedPercent: primary.used_percent,
        resetsAt: primary.resets_at,
        windowMinutes: primary.window_minutes
      });
    } catch {
      // 忽略并发写入产生的不完整行，等待下一条完整额度事件。
    }
  }
  return samples;
}

function readRecentSamples(file) {
  const stat = statSync(file);
  const bytesToRead = Math.min(stat.size, tailBytes);
  const start = stat.size - bytesToRead;
  const buffer = Buffer.alloc(bytesToRead);
  const fd = openSync(file, "r");
  try {
    readSync(fd, buffer, 0, bytesToRead, start);
  } finally {
    closeSync(fd);
  }
  let text = buffer.toString("utf8");
  if (start > 0) {
    const firstLineBreak = text.indexOf("\n");
    text = firstLineBreak === -1 ? "" : text.slice(firstLineBreak + 1);
  }
  return parseSamples(text);
}

if (!threadId || !/^[0-9a-f-]{36}$/.test(threadId)) {
  console.error("缺少有效的 CODEX_THREAD_ID，拒绝混合核算其他 Codex 会话。");
  process.exit(2);
}
if (!Number.isFinite(minimumRemaining) || minimumRemaining < 0 || minimumRemaining > 100) {
  console.error("CODEX_MIN_REMAINING_PERCENT 必须是 0 到 100 之间的数字。");
  process.exit(2);
}
if (!Number.isFinite(tailBytes) || tailBytes < 4096) {
  console.error("CODEX_QUOTA_TAIL_BYTES 必须是不小于 4096 的数字。");
  process.exit(2);
}

const threadFiles = listJsonlFiles(sessionsRoot).filter(file => file.includes(threadId));
const nested = threadFiles.map(readRecentSamples);
const samples = nested.flat().filter(sample => Number.isFinite(Date.parse(sample.timestamp)))
  .sort((left, right) => Date.parse(left.timestamp) - Date.parse(right.timestamp));

if (samples.length === 0) {
  console.error("未在当前线程会话文件尾部找到 Codex token_count 额度元数据，无法安全核算。");
  process.exit(2);
}

const latest = samples[samples.length - 1];
const remainingPercent = Math.max(0, 100 - latest.usedPercent);
const allowed = remainingPercent >= minimumRemaining;
console.log(JSON.stringify({
  latest: {
    timestamp: latest.timestamp,
    usedPercent: latest.usedPercent,
    remainingPercent,
    resetsAt: latest.resetsAt,
    windowMinutes: latest.windowMinutes
  },
  minimumRemainingPercent: minimumRemaining,
  allowed,
  policy: "不按自然日限额；总剩余额度不少于阈值时持续运行"
}, null, 2));
process.exit(allowed ? 0 : 10);
