#!/usr/bin/env node
import { spawn } from "node:child_process";
import path from "node:path";

const args = process.argv.slice(2);
const projectRoot = process.cwd();
process.env.UNI_CLI_CONTEXT = projectRoot;
process.env.UNI_INPUT_DIR = path.join(projectRoot, "src");

const child = spawn("uni", args, {
  stdio: "inherit",
  shell: process.platform === "win32",
});

child.on("close", (code) => {
  process.exit(code ?? 0);
});




