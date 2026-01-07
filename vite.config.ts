import { defineConfig, loadEnv } from "vite";
import path from "node:path";
import uni from "@dcloudio/vite-plugin-uni";
import fs from "fs";

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const isDev = mode === "development";

  const productEnvPath = path.resolve(process.cwd(), ".env.product");
  let productEnv: Record<string, string> = {};
  if (fs.existsSync(productEnvPath)) {
    const productEnvContent = fs.readFileSync(productEnvPath, "utf-8");
    productEnvContent.split("\n").forEach((line) => {
      const trimmedLine = line.trim();
      if (trimmedLine && !trimmedLine.startsWith("#")) {
        const equalIndex = trimmedLine.indexOf("=");
        if (equalIndex > 0) {
          const key = trimmedLine.substring(0, equalIndex).trim();
          const value = trimmedLine
            .substring(equalIndex + 1)
            .trim()
            .replace(/^["']|["']$/g, "");
          if (key && value) {
            productEnv[key] = value;
          }
        }
      }
    });
  }

  const proxyTarget = (productEnv.VITE_API_BASE_URL || "").replace(/\/+$/, "");
  const proxyTargetPy = (productEnv.VITE_API_BASE_URL_PY || "").replace(/\/+$/, "");

  return {
    plugins: [uni()],
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "src"),
      },
    },
    define: {
      // In H5 dev, use Vite proxy to avoid CORS; keep baseUrl empty so requests go to /api/*
      "import.meta.env.VITE_API_BASE_URL_PROD": JSON.stringify(
        isDev ? "" : productEnv.VITE_API_BASE_URL || env.VITE_API_BASE_URL || "",
      ),
      "import.meta.env.VITE_API_BASE_URL_DEV": JSON.stringify(isDev ? "" : env.VITE_API_BASE_URL || ""),
      "import.meta.env.VITE_API_BASE_URL_PY": JSON.stringify(isDev ? "" : env.VITE_API_BASE_URL_PY || ""),
      // Some code falls back to VITE_API_BASE_URL; override it too so dev doesn't hit apifoxmock.
      "import.meta.env.VITE_API_BASE_URL": JSON.stringify(isDev ? "" : env.VITE_API_BASE_URL || ""),
    },
    server:
      isDev && proxyTarget
        ? {
            proxy: {
              ...(proxyTargetPy
                ? {
                    "/api/ai": {
                      target: proxyTargetPy,
                      changeOrigin: true,
                    },
                  }
                : {}),
              "/api": {
                target: proxyTarget,
                changeOrigin: true,
              },
            },
          }
        : undefined,
  };
});

