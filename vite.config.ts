import { defineConfig, loadEnv } from "vite";
import path from "node:path";
import uni from "@dcloudio/vite-plugin-uni";
import fs from "fs";

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  
  const productEnvPath = path.resolve(process.cwd(), '.env.product');
  let productEnv: Record<string, string> = {};
  if (fs.existsSync(productEnvPath)) {
    const productEnvContent = fs.readFileSync(productEnvPath, 'utf-8');
    productEnvContent.split('\n').forEach(line => {
      const trimmedLine = line.trim();
      if (trimmedLine && !trimmedLine.startsWith('#')) {
        const equalIndex = trimmedLine.indexOf('=');
        if (equalIndex > 0) {
          const key = trimmedLine.substring(0, equalIndex).trim();
          const value = trimmedLine.substring(equalIndex + 1).trim().replace(/^["']|["']$/g, '');
          if (key && value) {
            productEnv[key] = value;
          }
        }
      }
    });
  }
  
  return {
    plugins: [uni()],
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "src"),
      },
    },
    define: {
      'import.meta.env.VITE_API_BASE_URL_PROD': JSON.stringify(productEnv.VITE_API_BASE_URL || env.VITE_API_BASE_URL || ''),
      'import.meta.env.VITE_API_BASE_URL_DEV': JSON.stringify(env.VITE_API_BASE_URL || ''),
      'import.meta.env.VITE_API_BASE_URL_PY': JSON.stringify(env.VITE_API_BASE_URL_PY || ''),
    },
  };
});
